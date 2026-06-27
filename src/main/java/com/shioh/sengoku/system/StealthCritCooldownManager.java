package com.shioh.sengoku.system;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks stealth-crit cooldowns per attacker-target pair.
 * Uses game ticks (from attacker level) to measure cooldown.
 */
public class StealthCritCooldownManager {
    private static final StealthCritCooldownManager INSTANCE = new StealthCritCooldownManager();

    // Map key: attackerUUID + '|' + targetUUID -> lastUsedTick
    private final Map<String, Long> lastUsed = new HashMap<>();

    private StealthCritCooldownManager() {}

    public static StealthCritCooldownManager getInstance() {
        return INSTANCE;
    }

    private String key(Player attacker, LivingEntity target) {
        return attacker.getUUID().toString() + "|" + target.getUUID().toString();
    }

    /**
     * Try to consume a stealth-crit for the attacker against the target.
     * @param attacker the player performing the attack
     * @param target the target entity
     * @param cooldownTicks cooldown in ticks (20 ticks = 1 second)
     * @return true if the stealth-crit is allowed (and recorded), false if still on cooldown
     */
    public synchronized boolean tryConsume(Player attacker, LivingEntity target, long cooldownTicks) {
        if (attacker == null || target == null) return false;
        long now = attacker.level().getGameTime();
        String k = key(attacker, target);
        Long prev = lastUsed.get(k);
        if (prev != null && (now - prev) < cooldownTicks) {
            return false;
        }
        lastUsed.put(k, now);
        return true;
    }

    /**
     * Clear entries older than threshold ticks to avoid memory leak; can be called periodically.
     */
    public synchronized void cleanup(long currentTick, long maxAgeTicks) {
        lastUsed.entrySet().removeIf(e -> (currentTick - e.getValue()) > maxAgeTicks);
    }
}
