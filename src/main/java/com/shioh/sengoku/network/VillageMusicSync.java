package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillageMusicSync {
    public static final ResourceLocation CHANNEL = sengokuFabric.asId("village_music");
    private static final TagKey<Structure> VILLAGE_TAG = TagKey.create(Registries.STRUCTURE, ResourceLocation.parse("minecraft:village_music"));

    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();
    private static final Map<UUID, Boolean> VILLAGE_STATE = new HashMap<>();
    private static final int EXIT_PADDING = 64;
    private static int tickCounter = 0;

    private static boolean isInsideInflatedStart(StructureManager structureManager, Structure structure, net.minecraft.core.BlockPos pos, int padding) {
        SectionPos center = SectionPos.of(pos);
        int chunkRadius = (padding >> 4) + 2;
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                for (StructureStart start : structureManager.startsForStructure(
                        SectionPos.of(center.x() + dx, 0, center.z() + dz), structure)) {
                    if (start.isValid() && start.getBoundingBox().inflatedBy(padding).isInside(pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void serverTick(ServerLevel level) {
        if ((tickCounter = (tickCounter + 1) % 20) != 0) return;

        try {
            Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            StructureManager structureManager = level.structureManager();

            for (ServerPlayer player : level.players()) {
                boolean isDay = (level.getDayTime() % 24000L) < 13000L;
                boolean currentlyInVillage = VILLAGE_STATE.getOrDefault(player.getUUID(), false);
                boolean insideVillage = false;

                if (isDay) {
                    try {
                        // 1) Check via user tag
                        for (Holder<Structure> holder : structureRegistry.getTagOrEmpty(VILLAGE_TAG)) {
                            try {
                                var optKey = holder.unwrapKey();
                                if (optKey.isPresent()) {
                                    var key = optKey.get().location();
                                    if ("minecraft:village_desert".equals(key.toString())) continue;
                                }
                            } catch (Throwable ignored) {}

                            Structure structure = holder.value();

                            if (currentlyInVillage) {
                                if (isInsideInflatedStart(structureManager, structure, player.blockPosition(), EXIT_PADDING)) {
                                    insideVillage = true;
                                    break;
                                }
                            } else {
                                StructureStart start = structureManager.getStructureAt(player.blockPosition(), structure);
                                if (start != null && start.isValid() && start.getBoundingBox().isInside(player.blockPosition())) {
                                    insideVillage = true;
                                    break;
                                }
                            }
                        }

                        // 2) Fallback to hardcoded vanilla village structures
                        if (!insideVillage) {
                            String[] vanillaVillages = new String[] {
                                "minecraft:village_plains",
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

                                if (currentlyInVillage) {
                                    if (isInsideInflatedStart(structureManager, structure, player.blockPosition(), EXIT_PADDING)) {
                                        insideVillage = true;
                                        break;
                                    }
                                } else {
                                    StructureStart start = structureManager.getStructureAt(player.blockPosition(), structure);
                                    if (start != null && start.isValid() && start.getBoundingBox().isInside(player.blockPosition())) {
                                        insideVillage = true;
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (Throwable ignored) {}
                }

                VILLAGE_STATE.put(player.getUUID(), insideVillage);

                Boolean last = LAST_SENT.get(player.getUUID());
                if (last == null || last.booleanValue() != insideVillage) {
                    LAST_SENT.put(player.getUUID(), insideVillage);
                    ServerPlayNetworking.send(player, new VillageMusicPayload(insideVillage));
                }
            }
        } catch (Throwable t) {
            // Silent fail; music system is non-critical
        }
    }
}