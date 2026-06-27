package com.shioh.sengoku.fabric;

import com.shioh.sengoku.system.PostureHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TickHandler {
    private static final Map<UUID, Long> LAST_SENT_POISE = new HashMap<>();

    public static void register() {
        // Runs every server tick, after game logic
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                PostureHandler.tick(player);
                syncPoiseHud(player);
            }
        });
    }

    private static void syncPoiseHud(ServerPlayer player) {
        if (player == null || player.connection == null) return;

        float current = PostureHandler.getCurrentPosture(player);
        float max = PostureHandler.getMaxPostureValue(player);

        int packedCurrent = Math.round(current * 100.0F);
        int packedMax = Math.round(max * 100.0F);
        long packedState = (((long) packedCurrent) << 32) | (packedMax & 0xFFFFFFFFL);

        Long lastPacked = LAST_SENT_POISE.get(player.getUUID());
        if (lastPacked != null && lastPacked == packedState) {
            return;
        }

        LAST_SENT_POISE.put(player.getUUID(), packedState);
        ServerPlayNetworking.send(player, new com.shioh.sengoku.network.PlayerPoisePayload(current, max));
    }
}
