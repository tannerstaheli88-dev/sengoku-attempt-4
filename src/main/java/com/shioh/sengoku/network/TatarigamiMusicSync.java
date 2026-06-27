package com.shioh.sengoku.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.wither.WitherBoss;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side scanner that detects active Wither fights near players
 * and notifies clients to toggle Tatarigami combat music.
 */
public class TatarigamiMusicSync {
    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();
    private static int tickCounter = 0;
    private static final int INTERVAL = 20; // once per second
    private static final double RADIUS = 128.0D; // a bit larger for boss fights

    public static void serverTick(ServerLevel level) {
        if ((tickCounter = (tickCounter + 1) % INTERVAL) != 0) return;
        try {
            for (ServerPlayer player : level.players()) {
                boolean active = false;
                try {
                    for (WitherBoss wither : level.getEntitiesOfClass(WitherBoss.class, player.getBoundingBox().inflate(RADIUS))) {
                        if (!wither.isRemoved()) {
                            try {
                                // Active when a Wither is present and alive nearby (presence triggers boss music)
                                if (wither.isAlive()) {
                                    active = true;
                                    break;
                                }
                            } catch (Throwable ignored) {}
                        }
                    }
                } catch (Throwable ignored) {}

                Boolean last = LAST_SENT.get(player.getUUID());
                if (last == null || last.booleanValue() != active) {
                    LAST_SENT.put(player.getUUID(), active);
                    try { sengokuFabric.LOGGER.info("[Music] Sending Tatarigami flag: {}", active); } catch (Throwable ignored) {}
                    ServerPlayNetworking.send(player, new TatarigamiMusicPayload(active));
                }
            }
        } catch (Throwable ignored) {}
    }
}