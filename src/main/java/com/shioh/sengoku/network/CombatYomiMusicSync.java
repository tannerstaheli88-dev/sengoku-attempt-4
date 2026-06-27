package com.shioh.sengoku.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatYomiMusicSync {
    // Tagged mobs that should trigger Yomi (Nether) combat music
    private static final TagKey<EntityType<?>> COMBAT_TAG = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("sengoku", "initiate_yomi_music"));

    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();
    private static int tickCounter = 0;

    public static void serverTick(ServerLevel level) {
        // Only run in the Nether
        if (level.dimension() != Level.NETHER) return;

        if ((tickCounter = (tickCounter + 1) % 20) != 0) return; // once per second
        try {
            for (ServerPlayer player : level.players()) {
                int aggroCount = 0;
                try {
                    for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(96.0D))) {
                        if (aggroCount >= 3) break;
                        if (mob == null || mob.isRemoved()) continue;
                        EntityType<?> et = mob.getType();
                        if (!et.is(COMBAT_TAG)) continue;

                        // Count mobs that are targeting or recently hurt the player (and have LOS)
                        try {
                            if (mob.getTarget() != null && mob.getTarget().getUUID().equals(player.getUUID())) {
                                try { if (!mob.getSensing().hasLineOfSight(player)) continue; } catch (Throwable ignored) {}
                                aggroCount++;
                                continue;
                            }
                        } catch (Throwable ignored) {}

                        try {
                            if (mob.getLastHurtByMob() != null && mob.getLastHurtByMob().getUUID().equals(player.getUUID())) {
                                try { if (!mob.getSensing().hasLineOfSight(player)) continue; } catch (Throwable ignored) {}
                                aggroCount++;
                                continue;
                            }
                        } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {}

                // Determine new active state using sticky behaviour:
                // - Once activated (>=3 aggro), remain active until aggro reaches 0
                // - Only deactivate when there are no mobs aggroing the player
                Boolean last = LAST_SENT.get(player.getUUID());
                boolean previous = last != null && last.booleanValue();
                boolean active = previous;

                if (aggroCount >= 3) {
                    // start music when 3 or more mobs are aggro
                    active = true;
                } else if (aggroCount == 0) {
                    // only stop music when no mobs are aggroing the player
                    active = false;
                }

                if (last == null || last.booleanValue() != active) {
                    LAST_SENT.put(player.getUUID(), active);
                    ServerPlayNetworking.send(player, new CombatYomiMusicPayload(active));
                }
            }
        } catch (Throwable ignored) {}
    }
}
