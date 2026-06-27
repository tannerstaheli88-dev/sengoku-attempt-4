package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VillageMusicSync {
    public static final ResourceLocation CHANNEL = sengokuFabric.asId("village_music");
    private static final TagKey<Structure> VILLAGE_TAG = TagKey.create(Registries.STRUCTURE, ResourceLocation.parse("minecraft:village_music"));

    // Cache last sent value per player to avoid spamming packets
    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();
    private static int tickCounter = 0;

    public static void serverTick(ServerLevel level) {
        // Run once per second per level
        if ((tickCounter = (tickCounter + 1) % 20) != 0) return;

        try {
            Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            StructureManager structureManager = level.structureManager();

            for (ServerPlayer player : level.players()) {
                boolean isDay = (level.getDayTime() % 24000L) < 13000L;
                boolean insideVillage = false;
                if (isDay) {
                    try {
                        // 1) Check via user tag
                        for (Holder<Structure> holder : structureRegistry.getTagOrEmpty(VILLAGE_TAG)) {
                            try {
                                // Skip desert village specifically (we don't want music there)
                                var optKey = holder.unwrapKey();
                                if (optKey.isPresent()) {
                                    var key = optKey.get().location();
                                    if ("minecraft:village_desert".equals(key.toString())) continue;
                                }
                            } catch (Throwable ignored) {}

                            Structure structure = holder.value();
                            StructureStart start = structureManager.getStructureAt(player.blockPosition(), structure);
                            if (start != null && start.isValid() && start.getBoundingBox().isInside(player.blockPosition())) {
                                insideVillage = true;
                                break;
                            }
                        }

                        // 2) Fallback to hardcoded vanilla village structures (covers cases where tag is missing/misnamed)
                        if (!insideVillage) {
                            String[] vanillaVillages = new String[] {
                                "minecraft:village_plains",
                                // desert intentionally omitted
                                "minecraft:village_savanna",
                                "minecraft:village_snowy",
                                "minecraft:village_taiga",
                                // Some packs add jungle/bamboo variants; try both ids just in case
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
                }

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
