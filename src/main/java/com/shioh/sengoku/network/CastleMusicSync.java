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

public class CastleMusicSync {
    private static final TagKey<Structure> CASTLE_TAG = TagKey.create(Registries.STRUCTURE, ResourceLocation.parse("minecraft:castle_music"));

    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();
    private static int tickCounter = 0;

    public static void serverTick(ServerLevel level) {
        if ((tickCounter = (tickCounter + 1) % 20) != 0) return; // once per second
        try {
            Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            StructureManager structureManager = level.structureManager();

            for (ServerPlayer player : level.players()) {
                boolean insideCastle = false;
                boolean isDay = (level.getDayTime() % 24000L) < 12000L; // mirror village logic (daytime focus)
                try {
                    if (isDay) {
                        for (Holder<Structure> holder : structureRegistry.getTagOrEmpty(CASTLE_TAG)) {
                            Structure structure = holder.value();
                            StructureStart start = structureManager.getStructureAt(player.blockPosition(), structure);
                            if (start != null && start.isValid() && start.getBoundingBox().isInside(player.blockPosition())) {
                                insideCastle = true;
                                break;
                            }
                        }
                    }
                } catch (Throwable ignored) {}

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
