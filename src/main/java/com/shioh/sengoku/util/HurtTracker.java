package com.shioh.sengoku.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HurtTracker {
    private HurtTracker() {}

    private static final Map<UUID, UUID> LAST_HURT_BY_PLAYER = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> LAST_HURT_BY_PLAYER_TIME = new ConcurrentHashMap<>();

    public static void record(UUID mobId, UUID playerId, long gameTime) {
        LAST_HURT_BY_PLAYER.put(mobId, playerId);
        LAST_HURT_BY_PLAYER_TIME.put(mobId, gameTime);
    }

    public static UUID getLastPlayer(UUID mobId) {
        return LAST_HURT_BY_PLAYER.get(mobId);
    }

    public static Long getLastTime(UUID mobId) {
        return LAST_HURT_BY_PLAYER_TIME.get(mobId);
    }

    public static void clear(UUID mobId) {
        LAST_HURT_BY_PLAYER.remove(mobId);
        LAST_HURT_BY_PLAYER_TIME.remove(mobId);
    }
}
