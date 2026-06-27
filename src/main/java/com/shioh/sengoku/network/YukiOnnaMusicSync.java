package com.shioh.sengoku.network;

import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.entity.YukiOnnaEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side sync: informs players when a nearby Yuki Onna is in stalk/aggro state.
 */
public class YukiOnnaMusicSync {
    public static final net.minecraft.resources.ResourceLocation CHANNEL = sengokuFabric.asId("yuki_onna_music");
    private static final Map<UUID, Boolean> LAST_SENT = new HashMap<>();
    private static int tickCounter = 0;

    public static void serverTick(ServerLevel level) {
        // Run once per second
        if ((tickCounter = (tickCounter + 1) % 20) != 0) return;

        try {
            for (ServerPlayer player : level.players()) {
                boolean active = false;
                try {
                    List<YukiOnnaEntity> nearby = level.getEntitiesOfClass(YukiOnnaEntity.class, player.getBoundingBox().inflate(96.0), e -> !e.isRemoved());
                    for (YukiOnnaEntity e : nearby) {
                        // consider aggro (has target) or stalking this player
                        if (e.getTarget() != null) {
                            active = true;
                            break;
                        }
                        if (e.getStalkingTargetUuid().filter(uuid -> uuid.equals(player.getUUID())).isPresent()) {
                            active = true;
                            break;
                        }
                    }
                } catch (Throwable ignored) {}

                Boolean last = LAST_SENT.get(player.getUUID());
                if (last == null || last.booleanValue() != active) {
                    LAST_SENT.put(player.getUUID(), active);
                    ServerPlayNetworking.send(player, new YukiOnnaMusicPayload(active));
                }
            }
        } catch (Throwable t) {
            // Non-critical
        }
    }
}
