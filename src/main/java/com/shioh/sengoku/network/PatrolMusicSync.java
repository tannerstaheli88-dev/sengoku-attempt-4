package com.shioh.sengoku.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PatrolMusicSync {
    private static final TagKey<EntityType<?>> RONIN_TAG =
            TagKey.create(Registries.ENTITY_TYPE,
                    ResourceLocation.parse("minecraft:initiate_ronin_music"));
    private static final TagKey<EntityType<?>> RONIN_ELITE_TAG =
            TagKey.create(Registries.ENTITY_TYPE,
                    ResourceLocation.parse("minecraft:initiate_ronin_music_elite"));
    private static final TagKey<Structure> CASTLE_TAG =
            TagKey.create(Registries.STRUCTURE,
                    ResourceLocation.parse("minecraft:castle_music"));

    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();
    private static final Map<UUID, Integer> AGGRO_HOLD_TICKS = new HashMap<>();
    private static final int AGGRO_HOLD_DURATION = 60;
    private static int tickCounter = 0;

    public static void serverTick(ServerLevel level) {
        if ((tickCounter = (tickCounter + 1) % 20) != 0) return;

        for (ServerPlayer player : level.players()) {
            int aggroCount = 0;
            boolean eliteTriggered = false;

            for (Mob mob : level.getEntitiesOfClass(
                    Mob.class,
                    player.getBoundingBox().inflate(96.0D))) {
                if (mob.isRemoved()) continue;
                boolean isNormal = mob.getType().is(RONIN_TAG);
                boolean isElite = mob.getType().is(RONIN_ELITE_TAG);
                if (!isNormal && !isElite) continue;

                boolean isAggro = false;

                // Mob is actively targeting the player (LOS required)
                try {
                    if (mob.getTarget() != null &&
                            mob.getTarget().getUUID().equals(player.getUUID())) {
                        try {
                            if (mob.getSensing().hasLineOfSight(player)) {
                                isAggro = true;
                            }
                        } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {}

                // Mob was hurt by the player — counts even if now infighting
                try {
                    if (mob.getLastHurtByMob() != null &&
                            mob.getLastHurtByMob().getUUID().equals(player.getUUID())) {
                        isAggro = true;
                    }
                } catch (Throwable ignored) {}

                // Player was hurt by this mob — counts even if mob is now infighting
                try {
                    if (player.getLastHurtByMob() != null &&
                            player.getLastHurtByMob().getUUID().equals(mob.getUUID())) {
                        isAggro = true;
                    }
                } catch (Throwable ignored) {}

                if (!isAggro) continue;

                if (isElite) {
                    eliteTriggered = true;
                    break;
                }
                if (isNormal) {
                    aggroCount++;
                }
            }

            Boolean last = LAST_SENT.get(player.getUUID());
            boolean previous = last != null && last;
            boolean active = previous;

            if (eliteTriggered) {
                active = true;
            } else if (aggroCount >= 3) {
                active = true;
            } else if (aggroCount == 0) {
                active = false;
            }

            // Apply hold ticks on top of sticky logic
            int holdTicks = AGGRO_HOLD_TICKS.getOrDefault(player.getUUID(), 0);
            if (active) {
                holdTicks = AGGRO_HOLD_DURATION;
            } else if (holdTicks > 0) {
                holdTicks -= 20;
            }
            AGGRO_HOLD_TICKS.put(player.getUUID(), Math.max(0, holdTicks));

            active = active || holdTicks > 0;

            // Block patrol music if inside a castle
            boolean insideCastle =
                    level.structureManager()
                         .getStructureWithPieceAt(player.blockPosition(), CASTLE_TAG)
                         .isValid();
            if (insideCastle) {
                active = false;
            }

            if (last == null || last != active) {
                LAST_SENT.put(player.getUUID(), active);
                ServerPlayNetworking.send(player, new PatrolMusicPayload(active));
            }
        }

        AGGRO_HOLD_TICKS.keySet().removeIf(uuid ->
                level.getServer().getPlayerList().getPlayer(uuid) == null);
        LAST_SENT.keySet().removeIf(uuid ->
                level.getServer().getPlayerList().getPlayer(uuid) == null);
    }
}