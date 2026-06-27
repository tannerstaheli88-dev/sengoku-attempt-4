package com.shioh.sengoku.network;

import com.shioh.sengoku.entity.WarlordEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-side scanner that detects the current nearby Warlord music phase.
 * Phase two wins over phase one when multiple Warlords are in range.
 */
public class WarlordMusicSync {
    private static final Map<UUID, Integer> LAST_SENT = new HashMap<>();
    private static int tickCounter = 0;
    private static final int INTERVAL = 20; // once per second
    private static final double RADIUS = 96.0D;

    public static void serverTick(ServerLevel level) {
        if ((tickCounter = (tickCounter + 1) % INTERVAL) != 0) return;
        try {
            for (ServerPlayer player : level.players()) {
                int phase = 0;
                try {
                    for (WarlordEntity w : level.getEntitiesOfClass(WarlordEntity.class, player.getBoundingBox().inflate(RADIUS))) {
                        if (!w.isRemoved()) {
                            try {
                                int warlordPhase = w.getMusicPhase();
                                if (warlordPhase > phase) {
                                    phase = warlordPhase;
                                }
                                if (phase >= 2) {
                                    break;
                                }
                            } catch (Throwable ignored) {}
                        }
                    }
                } catch (Throwable ignored) {}

                Integer last = LAST_SENT.get(player.getUUID());
                if (last == null || last.intValue() != phase) {
                    LAST_SENT.put(player.getUUID(), phase);
                    ServerPlayNetworking.send(player, new WarlordMusicPayload(phase));
                }
            }
        } catch (Throwable ignored) {}
    }
}
