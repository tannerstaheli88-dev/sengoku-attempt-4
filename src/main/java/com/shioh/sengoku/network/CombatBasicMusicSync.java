package com.shioh.sengoku.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatBasicMusicSync {

    private static final TagKey<EntityType<?>> COMBAT_TAG =
            TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("minecraft:initiate_combat_music"));

    private static final TagKey<EntityType<?>> ELITE_COMBAT_TAG =
            TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("minecraft:initiate_combat_music_elite"));

    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();

    private static int tickCounter = 0;

    public static void serverTick(ServerLevel level) {

        if ((tickCounter = (tickCounter + 1) % 20) != 0) return;

        for (ServerPlayer player : level.players()) {

            int aggroCount = 0;
            boolean eliteTriggered = false;

            for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(96.0D))) {

                if (mob == null || mob.isRemoved()) continue;

                boolean isNormal = mob.getType().is(COMBAT_TAG);
                boolean isElite = mob.getType().is(ELITE_COMBAT_TAG);

                if (!isNormal && !isElite) continue;

                boolean isAggro = false;

                try {
                    if (mob.getTarget() != null &&
                        mob.getTarget().getUUID().equals(player.getUUID())) {

                        try {
                            if (!mob.getSensing().hasLineOfSight(player)) continue;
                        } catch (Throwable ignored) {}

                        isAggro = true;
                    }
                } catch (Throwable ignored) {}

                try {
                    if (mob.getLastHurtByMob() != null &&
                        mob.getLastHurtByMob().getUUID().equals(player.getUUID())) {

                        try {
                            if (!mob.getSensing().hasLineOfSight(player)) continue;
                        } catch (Throwable ignored) {}

                        isAggro = true;
                    }
                } catch (Throwable ignored) {}

                if (!isAggro) continue;

                if (isElite) {
                    eliteTriggered = true;
                    break; // elite overrides everything
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

            if (last == null || last != active) {
                LAST_SENT.put(player.getUUID(), active);
                ServerPlayNetworking.send(player, new CombatBasicMusicPayload(active));
            }
        }

        LAST_SENT.keySet().removeIf(uuid ->
                level.getServer().getPlayerList().getPlayer(uuid) == null);
    }
}