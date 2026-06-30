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

public class CastleMusicSync {
    private static final TagKey<Structure> CASTLE_TAG = TagKey.create(Registries.STRUCTURE, ResourceLocation.parse("minecraft:castle_music"));
    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();
    private static final Map<UUID, Boolean> CASTLE_STATE = new HashMap<>();
    private static final int EXIT_PADDING = 32;
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
                boolean isDay = (level.getDayTime() % 24000L) < 12000L;
                boolean currentlyInCastle = CASTLE_STATE.getOrDefault(player.getUUID(), false);
                boolean insideCastle = false;

                if (isDay) {
                    try {
                        for (Holder<Structure> holder : structureRegistry.getTagOrEmpty(CASTLE_TAG)) {
                            Structure structure = holder.value();

                            if (currentlyInCastle) {
                                if (isInsideInflatedStart(structureManager, structure, player.blockPosition(), EXIT_PADDING)) {
                                    insideCastle = true;
                                    break;
                                }
                            } else {
                                StructureStart start = structureManager.getStructureAt(player.blockPosition(), structure);
                                if (start != null && start.isValid() && start.getBoundingBox().isInside(player.blockPosition())) {
                                    insideCastle = true;
                                    break;
                                }
                            }
                        }
                    } catch (Throwable ignored) {}
                }

                CASTLE_STATE.put(player.getUUID(), insideCastle);

                Boolean last = LAST_SENT.get(player.getUUID());
                if (last == null || last.booleanValue() != insideCastle) {
                    LAST_SENT.put(player.getUUID(), insideCastle);
                    ServerPlayNetworking.send(player, new CastleMusicPayload(insideCastle));
                }
            }
        } catch (Throwable t) {
            // non-critical
        }
    }
}