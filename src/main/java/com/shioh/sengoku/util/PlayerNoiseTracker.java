package com.shioh.sengoku.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.shioh.sengoku.item.ShinobiArmorItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks noise levels for players based on their actions (sprinting, jumping, taking damage and farting lol).
 * High noise levels alert nearby mobs even if the player is behind them.
 */
public class PlayerNoiseTracker {
    private static final PlayerNoiseTracker INSTANCE = new PlayerNoiseTracker();
    
    // Noise levels for different actions
    public static final float SPRINT_NOISE = 1.0F;
    // Jump noise deprecated (kept for backward compatibility, unused)
    public static final float JUMP_NOISE = 0.0F;
    public static final float DAMAGE_NOISE = 2.0F;
    // Noise generated when the player initiates an attack on an entity
    public static final float ATTACK_NOISE = 2.5F;
    // Noise generated when firing a crossbow (gun-like loudness)
    public static final float HORN_NOISE = 3.0F; // goat horn blast — maximum noise
    public static final float CROSSBOW_NOISE = 2.8F;
    public static final float BLOCK_BREAK_NOISE = 1.2F;
    public static final float EAT_NOISE = 0.8F;
    
    // Noise decay rate per tick (20 ticks = 1 second)
    private static final float NOISE_DECAY = 0.05F; // Decays over ~20 ticks (1 second)
    
    // Minimum noise level to be considered "making noise"
    private static final float NOISE_THRESHOLD = 0.1F;
    
    // Map of player UUID to their current noise level
    private final Map<UUID, NoiseData> noiseLevels = new HashMap<>();
    // Map of player UUID to the last game tick they were in tall concealment
    private final Map<UUID, Long> lastTallConcealTick = new ConcurrentHashMap<>();
    
    private PlayerNoiseTracker() {}
    
    public static PlayerNoiseTracker getInstance() {
        return INSTANCE;
    }
    
    /**
     * Add noise from a player action
     */
    public void addNoise(Player player, float amount) {
        UUID uuid = player.getUUID();
        NoiseData data = noiseLevels.computeIfAbsent(uuid, k -> new NoiseData());
        data.noiseLevel = Math.min(data.noiseLevel + amount, 3.0F); // Cap at 3.0
        data.lastUpdate = player.level().getGameTime();
    }

    /**
     * Mark that the player is currently in a tall concealment block (e.g., tall grass).
     * Stores the current game tick so other systems can allow a short linger after leaving.
     */
    public void markTallConcealment(Player player) {
        try {
            long tick = player.level().getGameTime();
            lastTallConcealTick.put(player.getUUID(), tick);
        } catch (Throwable ignored) {}
    }

    /**
     * Returns true if the player was in tall concealment within the last {@code lingerTicks} ticks.
     */
    public boolean wasRecentlyInTallConcealment(Player player, long lingerTicks) {
        Long last = lastTallConcealTick.get(player.getUUID());
        if (last == null) return false;
        try {
            long now = player.level().getGameTime();
            return now - last <= lingerTicks;
        } catch (Throwable ignored) {
            return false;
        }
    }
    
    /**
     * Get current noise level for a player (decays over time)
     */
    public float getNoiseLevel(Player player) {
        UUID uuid = player.getUUID();
        NoiseData data = noiseLevels.get(uuid);
        if (data == null) return 0.0F;
        
        // Calculate decay
        long currentTime = player.level().getGameTime();
        long ticksSinceUpdate = currentTime - data.lastUpdate;
        float decayedNoise = Math.max(0.0F, data.noiseLevel - (ticksSinceUpdate * NOISE_DECAY));
        
        // Clean up if noise is negligible
        if (decayedNoise < NOISE_THRESHOLD) {
            noiseLevels.remove(uuid);
            return 0.0F;
        }
        
        // Update stored value
        data.noiseLevel = decayedNoise;
        data.lastUpdate = currentTime;
        
        return decayedNoise;
    }
    
    /**
     * Check if player is currently making significant noise
     */
    public boolean isNoisy(Player player) {
        return getNoiseLevel(player) >= NOISE_THRESHOLD;
    }
    
    /**
     * Get detection radius multiplier based on noise level
     * 0.0 noise = 1.0x (normal)
     * 1.0 noise = 2.0x (sprinting)
     * 2.0 noise = 3.0x (damage)
     * 3.0 noise = 4.0x (max)
     */
    public float getDetectionMultiplier(Player player) {
        float noise = getNoiseLevel(player);
        float multiplier = 1.0F + noise;

        // If player is wearing the Shinobi chest armor, reduce detection multiplier.
        try {
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            if (chest != null && chest.getItem() instanceof ShinobiArmorItem) {
                multiplier *= 0.5F; // 50% detection effectiveness while worn
                // Keep a sane minimum so detection isn't completely disabled
                multiplier = Math.max(0.2F, multiplier);
            }
        } catch (Throwable ignored) {}

        return multiplier;
    }
    
    /**
     * Clear noise for a player (called when they disconnect)
     */
    public void clearNoise(Player player) {
        noiseLevels.remove(player.getUUID());
    }
    
    /**
     * Clean up old entries (call periodically)
     */
    public void cleanup(long currentTime) {
        noiseLevels.entrySet().removeIf(entry -> {
            long ticksSinceUpdate = currentTime - entry.getValue().lastUpdate;
            return ticksSinceUpdate > 100; // Remove if no update in 5 seconds
        });
        // Also clean up old concealment entries (keep a small window)
        lastTallConcealTick.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > 200L);
    }
    
    private static class NoiseData {
        float noiseLevel = 0.0F;
        long lastUpdate = 0L;
    }
}
