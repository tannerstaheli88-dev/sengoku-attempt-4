package com.shioh.sengoku.system;

import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Server-side system for tracking mist weather after rain.
 * Mist occurs naturally after rain/thunderstorms end.
 * Creates ambient fog effect (no player effects needed).
 */
public class MistWeatherSystem {

    private static final class MistState {
        int mistDuration = 0;
        boolean isMisty = false;
        boolean wasRaining = false;
    }

    private static final Map<String, MistState> STATES = new HashMap<>();

    private static String levelKey(ServerLevel level) {
        // Include server identity so one world's state never affects another world loaded later.
        return Integer.toHexString(System.identityHashCode(level.getServer()))
            + "|"
            + level.dimension().location();
    }

    private static MistState getOrCreateState(ServerLevel level) {
        return STATES.computeIfAbsent(levelKey(level), key -> new MistState());
    }
    
    /**
     * Server tick - called every tick on server side
     */
    public static void serverTick(ServerLevel level) {
        MistState state = getOrCreateState(level);
        boolean isRaining = level.isRaining();
        
        // Detect rain ending
        if (state.wasRaining && !isRaining && !state.isMisty) {
            // Rain just stopped - start mist immediately 40% of the time
            RandomSource random = level.getRandom();
            if (random.nextFloat() < 0.4f) {
                state.isMisty = true;
                state.mistDuration = 6000 + random.nextInt(6000); // 5-10 minutes
            }
        }
        
        // Update mist duration
        if (state.isMisty && state.mistDuration > 0) {
            state.mistDuration--;
            
            if (state.mistDuration == 0) {
                // End mist
                state.isMisty = false;
            }
        }
        
        // Track rain state for next tick
        state.wasRaining = isRaining;
    }

    /**
     * Returns mist status for the provided level.
     */
    public static boolean isMisty(Level level) {
        if (level == null) {
            return false;
        }

        if (level instanceof ServerLevel serverLevel) {
            MistState state = STATES.get(levelKey(serverLevel));
            return state != null && state.isMisty;
        }

        // Client-side fallback for integrated-server rendering paths.
        return isMisty();
    }
    
    /**
     * Check if mist weather is currently active
     */
    public static boolean isMisty() {
        // Backward-compatible fallback: true if any tracked world is currently misty.
        for (MistState state : STATES.values()) {
            if (state.isMisty) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns mist duration for the provided level.
     */
    public static int getMistDuration(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            MistState state = STATES.get(levelKey(serverLevel));
            return state != null ? state.mistDuration : 0;
        }
        return getMistDuration();
    }
    
    /**
     * Get remaining mist duration in ticks
     */
    public static int getMistDuration() {
        int max = 0;
        for (MistState state : STATES.values()) {
            if (state.mistDuration > max) {
                max = state.mistDuration;
            }
        }
        return max;
    }

    /**
     * Force start mist weather for a specific level.
     */
    public static void startMist(ServerLevel level, int duration) {
        MistState state = getOrCreateState(level);
        state.isMisty = true;
        state.mistDuration = Math.max(1, duration);
    }
    
    /**
     * Force start mist weather (for testing/commands)
     */
    public static void startMist(int duration) {
        // Legacy behavior: apply to all tracked worlds.
        if (STATES.isEmpty()) {
            return;
        }
        int clamped = Math.max(1, duration);
        for (MistState state : STATES.values()) {
            state.isMisty = true;
            state.mistDuration = clamped;
        }
    }

    /**
     * Force stop mist weather for a specific level.
     */
    public static void stopMist(ServerLevel level) {
        MistState state = STATES.get(levelKey(level));
        if (state == null) {
            return;
        }
        state.isMisty = false;
        state.mistDuration = 0;
    }
    
    /**
     * Force stop mist weather
     */
    public static void stopMist() {
        for (MistState state : STATES.values()) {
            state.isMisty = false;
            state.mistDuration = 0;
        }
    }

    /**
     * Clear all tracked mist state; call on server/world transitions.
     */
    public static void clearAll() {
        STATES.clear();
    }
}
