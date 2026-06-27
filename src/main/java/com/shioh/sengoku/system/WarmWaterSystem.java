package com.shioh.sengoku.system;

import com.shioh.sengoku.registry.ParticleRegistry;
import com.shioh.sengoku.config.SengokuConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * System to create "warm water" effects when players are in water near magma blocks.
 * This simulates hot springs without modifying vanilla water blocks.
 */
public class WarmWaterSystem {

    private static final net.minecraft.resources.ResourceLocation ONSEN_SATURATION_ADVANCEMENT =
        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "onsen_saturation");
    private static final String ONSEN_SATURATION_CRITERION = "gain_onsen_saturation";
    
    // Warm water feature toggle is controlled by config (`warmWaterEnabled`)
    
    private static final int MAGMA_DETECTION_RADIUS = 3;
    private static final int TICKS_FOR_SATURATION = 200; // 10 seconds (20 ticks per second)
    private static final int MELT_INTERVAL_TICKS = 20; // run ice melt once per second
    private static final int MELT_SAMPLES_PER_PLAYER = 6; // light sampling budget per run
    private static final int MELT_SAMPLE_RADIUS = 8;
    private static final int MELT_VERTICAL_RANGE = 4;
    private static final int MELT_PER_PASS_BUDGET = 8; // prevent large spikes when many players are nearby
    private static int meltTickCounter = 0;
    private static final int SMOKE_PARTICLE_BUDGET_PER_TICK = 24; // global cap for cosy smoke per tick per level
    private static long smokeParticleTick = -1;
    private static int smokeParticlesEmittedThisTick = 0;
    // (sampling parameters are declared locally in the method below)

    // Track time each player has spent in warm water (ticks)
    private static final Map<UUID, Integer> warmWaterTimeTracker = new HashMap<>();
    
    /**
     * Check if a player is in warm water (water near magma blocks) and apply effects
     */
    public static void handlePlayerInWater(ServerPlayer player) {
        if (!SengokuConfig.getInstance().warmWaterEnabled) return;
        UUID playerId = player.getUUID();
        
        if (!player.isInWater()) {
            // Player left water, reset their timer
            warmWaterTimeTracker.remove(playerId);
            return;
        }
        
        ServerLevel level = player.serverLevel();
        if (level.dimension() == Level.NETHER) {
            warmWaterTimeTracker.remove(playerId);
            return;
        }

        BlockPos playerPos = player.blockPosition();
        
        // Check if there are magma blocks nearby
        if (isNearMagmaBlock(level, playerPos)) {
            // Increment time in warm water using compute to avoid double-map lookups
            int timeInWarmWater = warmWaterTimeTracker.compute(playerId, (k, v) -> v == null ? 1 : v + 1);
            // Apply warm water effects to player
            applyWarmWaterEffects(player, level, playerPos, timeInWarmWater);
        } else {
            // In water but not near magma, reset timer
            warmWaterTimeTracker.remove(playerId);
        }
    }
    
    /**
     * Actively melt ice blocks near magma blocks
     * Call this periodically from server tick
     */
    public static void meltIceNearMagma(ServerLevel level) {
        if (!SengokuConfig.getInstance().warmWaterEnabled) return;
        if (level.dimension() == Level.NETHER) return;
        // Skip entirely if no players are present in this level
        if (level.players().isEmpty()) return;

        // Throttle to once per second to avoid per-tick scanning cost
        if ((meltTickCounter = (meltTickCounter + 1) % MELT_INTERVAL_TICKS) != 0) return;

        // Iterate through all players and perform a limited number of random samples around each player
        // This reduces worst-case CPU cost compared to scanning a large volume each tick.
        // More efficient approach: sample a small number of random positions near each player
        // instead of scanning a large cube. Only perform the relatively-expensive
        // `isNearMagmaBlock` proximity test when we actually find an ice block.
        int meltedThisPass = 0;

        for (var player : level.players()) {
            BlockPos playerPos = player.blockPosition();
            for (int i = 0; i < MELT_SAMPLES_PER_PLAYER; i++) {
                int dx = level.random.nextInt(MELT_SAMPLE_RADIUS * 2 + 1) - MELT_SAMPLE_RADIUS;
                int dy = level.random.nextInt(MELT_VERTICAL_RANGE * 2 + 1) - MELT_VERTICAL_RANGE;
                int dz = level.random.nextInt(MELT_SAMPLE_RADIUS * 2 + 1) - MELT_SAMPLE_RADIUS;

                BlockPos checkPos = playerPos.offset(dx, dy, dz);
                if (!level.isLoaded(checkPos)) continue;

                var state = level.getBlockState(checkPos);
                // Only proceed if the block is regular or frosted ice. Packed ice and blue
                // ice are intentionally left unchanged so onsen pools built with them do not
                // melt in cold biomes.
                if (!(state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE))) continue;

                // Now check if this ice is near magma (small radius check inside)
                if (!isNearMagmaBlock(level, checkPos)) continue;

                // Melt the ice into water.
                try {
                    level.setBlockAndUpdate(checkPos, Blocks.WATER.defaultBlockState());
                    meltedThisPass++;
                    if (meltedThisPass >= MELT_PER_PASS_BUDGET) return; // cap per-pass work
                } catch (Throwable ignored) {
                }
            }
        }
    }
    
    /**
     * Handle ambient warm water effects for all warm water areas
     * This creates campfire smoke at all warm water locations regardless of player presence
     */
    /**
     * Handle ambient warm water effects for all warm water areas
     * Server-side method that is no longer used - particles moved to client-side
     */
    public static void handleAmbientWarmWaterEffects(ServerLevel level) {
        // This method is deprecated - particles are now handled client-side
        // Kept for compatibility
    }
    
    /**
     * Check if there are magma blocks within the detection radius
     * Public so it can be used by mixins and client code
     */
    public static boolean isNearMagmaBlock(net.minecraft.world.level.Level level, BlockPos centerPos) {
        if (!SengokuConfig.getInstance().warmWaterEnabled) return false;
        if (level.dimension() == Level.NETHER) return false;
        for (int x = -MAGMA_DETECTION_RADIUS; x <= MAGMA_DETECTION_RADIUS; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -MAGMA_DETECTION_RADIUS; z <= MAGMA_DETECTION_RADIUS; z++) {
                    BlockPos checkPos = centerPos.offset(x, y, z);
                    if (level.getBlockState(checkPos).is(Blocks.MAGMA_BLOCK)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Apply warm water effects to player
     * Grants saturation effect after 10 seconds of immersion, but only if player is not underwater
     */
    private static void applyWarmWaterEffects(ServerPlayer player, ServerLevel level, BlockPos playerPos, int timeInWarmWater) {
        // After 10 seconds (200 ticks), grant saturation effect - but only if player is NOT underwater
        if (timeInWarmWater >= TICKS_FOR_SATURATION && !player.isUnderWater()) {
            // Grant Saturation effect (amplifier 0 = level 1, duration 10 seconds)
            MobEffectInstance saturation = new MobEffectInstance(
                MobEffects.SATURATION, 
                200,  // 10 seconds duration
                0,    // Level 1 (amplifier 0)
                false, // not ambient
                false, // no visible particles
                true   // show icon
            );
            player.addEffect(saturation);
            awardAdvancement(player, ONSEN_SATURATION_ADVANCEMENT, ONSEN_SATURATION_CRITERION);
            
            // Reset timer after granting effect so it refreshes every 10 seconds
            warmWaterTimeTracker.put(player.getUUID(), 0);
        }
        
        // Play gentle bubbling sound occasionally
        if (player.getRandom().nextInt(100) == 0) {
            level.playSound(null, playerPos, SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, 
                SoundSource.AMBIENT, 0.3f, 1.5f);
        }
    }

    private static void awardAdvancement(ServerPlayer player, net.minecraft.resources.ResourceLocation advancementId, String criterion) {
        net.minecraft.advancements.AdvancementHolder holder = player.server.getAdvancements().get(advancementId);
        if (holder != null) {
            player.getAdvancements().award(holder, criterion);
        }
    }
    
private static void createCampfireSmokeAtSurface(ServerLevel level, BlockPos waterPos) {
    long nowTick = level.getGameTime();
    if (nowTick != smokeParticleTick) {
        smokeParticleTick = nowTick;
        smokeParticlesEmittedThisTick = 0;
    }
    if (smokeParticlesEmittedThisTick >= SMOKE_PARTICLE_BUDGET_PER_TICK) return;

    // Only search a few blocks up — particles should stay near the spring itself,
    // not float up to the top of a deep water column
    BlockPos surfacePos = waterPos;
    for (int y = 0; y < 4; y++) {
        BlockPos checkPos = waterPos.offset(0, y, 0);
        if (level.getFluidState(checkPos).getType() != Fluids.WATER) {
            surfacePos = checkPos.below();
            break;
        }
    }

    double x = surfacePos.getX() + 0.3 + level.random.nextDouble() * 0.4;
    double y = surfacePos.getY() + 0.8;
    double z = surfacePos.getZ() + 0.3 + level.random.nextDouble() * 0.4;

    for (int i = 0; i < 3; i++) {
        if (smokeParticlesEmittedThisTick >= SMOKE_PARTICLE_BUDGET_PER_TICK) break;
        double yOffset = y + (i * 1.2);
        double xOffset = x + (level.random.nextDouble() - 0.5) * 0.3;
        double zOffset = z + (level.random.nextDouble() - 0.5) * 0.3;
        level.sendParticles(
            ParticleTypes.CAMPFIRE_COSY_SMOKE,
            xOffset, yOffset, zOffset,
            1,
            0.2, 0.0, 0.2,
            0.002
        );
        smokeParticlesEmittedThisTick++;
    }
}

/**
 * Create misty fog particles that hover at the water surface.
 * This creates an atmospheric onsen/hot spring effect.
 */
private static void createMistyFogAtSurface(ServerLevel level, BlockPos waterPos) {
    // Spawn fog particles hovering just above the water surface
    double x = waterPos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;
    double y = waterPos.getY() + 0.9; // hover just above surface
    double z = waterPos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8;

    // Spawn fog particle - reduced to 1 since we're calling this frequently
    level.sendParticles(
        ParticleRegistry.FOG,
        x, y, z,
        1,
        0.05, 0.01, 0.05,
        0.0
    );
}
}