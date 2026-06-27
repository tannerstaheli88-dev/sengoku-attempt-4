package com.shioh.sengoku.network;

import com.shioh.sengoku.config.SengokuConfig;
import com.shioh.sengoku.sengokuFabric;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WarmWaterConfigSync {
    public static final net.minecraft.resources.ResourceLocation CHANNEL = sengokuFabric.asId("warm_water_config");
    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();
    private static int tickCounter = 0;

    // Send server config to players once per second (throttled) and only when value differs
    public static void serverTick(ServerLevel level) {
        if ((tickCounter = (tickCounter + 1) % 20) != 0) return;
        boolean enabled = SengokuConfig.getInstance().warmWaterEnabled;
        try {
            for (ServerPlayer player : level.players()) {
                UUID id = player.getUUID();
                Boolean last = LAST_SENT.get(id);
                if (last == null || last.booleanValue() != enabled) {
                    LAST_SENT.put(id, enabled);
                    ServerPlayNetworking.send(player, new WarmWaterConfigPayload(enabled));
                }
            }
        } catch (Throwable ignored) {}
    }
}
