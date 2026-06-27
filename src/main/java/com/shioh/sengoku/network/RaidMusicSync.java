package com.shioh.sengoku.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.raid.Raid;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RaidMusicSync {
    // Cache last sent value per player to avoid spamming packets
    private static final Map<UUID, Integer> LAST_SENT = new HashMap<>();
    // Track last raid ID for which each player already received a VICTORY notification
    private static final Map<UUID, Integer> LAST_VICTORY_RAID = new HashMap<>();
    private static int tickCounter = 0;

    public static void serverTick(ServerLevel level) {
        // Run once per second per level
        if ((tickCounter = (tickCounter + 1) % 20) != 0) return;

        try {
            for (ServerPlayer player : level.players()) {
                int state = 0; // 0 = NONE, 1 = ACTIVE, 2 = VICTORY
                try {
                    Raid raid = level.getRaidAt(player.blockPosition());
                    if (raid != null) {
                        // Prioritize victory detection: when a raid reaches the victory state,
                        // `isVictory()` may be true at the same time as `isActive()` in some versions,
                        // so check victory first. However, victory music should play only for
                        // players who are actually inside the village ("at the bell").
                        if (raid.isVictory()) {
                            boolean insideVillage = false;
                            try {
                                Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
                                StructureManager structureManager = level.structureManager();
                                TagKey<Structure> VILLAGE_TAG = TagKey.create(Registries.STRUCTURE, ResourceLocation.parse("minecraft:village_music"));

                                // 1) Check via user tag
                                for (Holder<Structure> holder : structureRegistry.getTagOrEmpty(VILLAGE_TAG)) {
                                    Structure structure = holder.value();
                                    StructureStart start = structureManager.getStructureAt(player.blockPosition(), structure);
                                    if (start != null && start.isValid() && start.getBoundingBox().isInside(player.blockPosition())) {
                                        insideVillage = true;
                                        break;
                                    }
                                }

                                // 2) Fallback to hardcoded vanilla village structures
                                if (!insideVillage) {
                                    String[] vanillaVillages = new String[]{
                                            "minecraft:village_plains",
                                            "minecraft:village_desert",
                                            "minecraft:village_savanna",
                                            "minecraft:village_snowy",
                                            "minecraft:village_taiga",
                                            "minecraft:village_jungle",
                                            "minecraft:village_bamboo"
                                    };
                                    for (String id : vanillaVillages) {
                                        ResourceLocation rl = ResourceLocation.parse(id);
                                        Structure structure = structureRegistry.get(rl);
                                        if (structure == null) continue;
                                        StructureStart start = structureManager.getStructureAt(player.blockPosition(), structure);
                                        if (start != null && start.isValid() && start.getBoundingBox().isInside(player.blockPosition())) {
                                            insideVillage = true;
                                            break;
                                        }
                                    }
                                }
                            } catch (Throwable ignored) {}

                            if (insideVillage) {
                                int raidId = raid.getId();
                                Integer lastRaid = LAST_VICTORY_RAID.get(player.getUUID());
                                if (lastRaid == null || lastRaid.intValue() != raidId) {
                                    // First time this player is eligible for this raid victory
                                    state = 2;
                                    LAST_VICTORY_RAID.put(player.getUUID(), raidId);
                                } else {
                                    // Already notified once for this raid; do not resend VICTORY
                                    state = 0;
                                }
                            } else if (raid.isActive()) {
                                state = 1;
                            }
                        } else if (raid.isActive()) {
                            state = 1;
                        }
                    }

                    if (state == 0) {
                        // Fallback: detect nearby active raiders
                        AABB box = player.getBoundingBox().inflate(96.0);
                        for (Raider raider : level.getEntitiesOfClass(Raider.class, box)) {
                            if (raider != null && raider.hasActiveRaid()) {
                                state = 1;
                                break;
                            }
                        }
                    }
                } catch (Throwable ignored) {}

                Integer last = LAST_SENT.get(player.getUUID());
                if (last == null || last.intValue() != state) {
                    LAST_SENT.put(player.getUUID(), state);
                    ServerPlayNetworking.send(player, new RaidMusicPayload(state));
                }
            }
        } catch (Throwable t) {
            // Non-critical system; ignore errors
        }
    }
}
