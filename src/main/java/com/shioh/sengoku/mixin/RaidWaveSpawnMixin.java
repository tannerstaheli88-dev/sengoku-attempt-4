package com.shioh.sengoku.mixin;

import com.shioh.sengoku.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Mixin to add custom Bandit and Ronin entities to village raids.
 * Injects into the Raid.tick() method to spawn our custom raiders alongside vanilla ones.
 */
@Mixin(Raid.class)
public abstract class RaidWaveSpawnMixin {
    
    @Shadow
    private int groupsSpawned;
    
    @Shadow
    private boolean started;
    
    @Shadow
    private long ticksActive;
    
    @Shadow
    private ServerLevel level;
    
    @Shadow
    private BlockPos center;
    
    @Shadow
    private boolean active;
    
    // Track which waves we've already spawned for to avoid duplicates
    private static final Set<String> spawnedWaves = new HashSet<>();
    
    // Track the last groupsSpawned value to detect when a new wave starts
    private int lastGroupsSpawned = 0;
    
    /**
     * Inject right after a wave spawns to add our custom raiders.
     * This runs during Raid.tick() after the vanilla spawning logic.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void spawnCustomRaiders(CallbackInfo ci) {
        Raid raid = (Raid) (Object) this;
        
        // Only spawn if raid is active and a wave has spawned
        if (!active || !started || groupsSpawned <= 0) {
            return;
        }
        
        // Only spawn when groupsSpawned changes (new wave started)
        if (groupsSpawned == lastGroupsSpawned) {
            return; // No new wave
        }
        
        // Update last wave counter
        lastGroupsSpawned = groupsSpawned;
        
        // Create unique key for this raid and wave
        String waveKey = System.identityHashCode(raid) + "_" + groupsSpawned;
        
        // Skip if we've already spawned for this wave (extra safety)
        if (spawnedWaves.contains(waveKey)) {
            return;
        }
        
        // Mark this wave as spawned
        spawnedWaves.add(waveKey);
        
        // Clean up old entries (keep last 100)
        if (spawnedWaves.size() > 100) {
            spawnedWaves.clear();
        }
        
        int wave = groupsSpawned;
        
        // Wave counts (1-indexed for groupsSpawned)
        // Wave 1 = index 0, Wave 2 = index 1, etc.
        int[] banditCounts = {1, 1, 2, 1, 2, 2, 3, 3};  // Waves 1-8
        int[] roninCounts = {1, 1, 1, 2, 2, 2, 3, 3};   // Waves 1-8
        
        int waveIndex = wave - 1;
        if (waveIndex < 0 || waveIndex >= banditCounts.length) {
            return;
        }
        
        // Spawn bandits
        for (int i = 0; i < banditCounts[waveIndex]; i++) {
            spawnCustomRaider(ModEntities.BANDIT, raid, level, center, wave);
        }
        
        // Spawn ronin
        for (int i = 0; i < roninCounts[waveIndex]; i++) {
            spawnCustomRaider(ModEntities.RONIN, raid, level, center, wave);
        }
    }
    
    /**
     * Spawn a custom raider entity and properly add it to the raid.
     * Spawns them far from the village center (50-80 blocks away).
     */
    private void spawnCustomRaider(EntityType<? extends Raider> entityType, Raid raid, ServerLevel level, BlockPos center, int wave) {
        // Create the raider
        Raider raider = entityType.create(level);
        if (raider == null) return;
        
        // Find a random spawn position far from the raid center (50-80 blocks away)
        int distance = 50 + level.random.nextInt(31); // Random distance between 50-80 blocks
        double angle = level.random.nextDouble() * Math.PI * 2; // Random angle
        
        int offsetX = (int) (Math.cos(angle) * distance);
        int offsetZ = (int) (Math.sin(angle) * distance);
        
        BlockPos spawnPos = center.offset(offsetX, 0, offsetZ);
        
        // Find ground level at this position
        BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos);
        
        // Set the raider's position
        raider.moveTo(groundPos.getX() + 0.5, groundPos.getY(), groundPos.getZ() + 0.5, 
                     level.random.nextFloat() * 360F, 0.0F);
        
        // Ensure they spawn with weapons by calling finalizeSpawn
        raider.finalizeSpawn(level, level.getCurrentDifficultyAt(groundPos), 
                           MobSpawnType.EVENT, null);
        
        // Add to the world and raid
        level.addFreshEntityWithPassengers(raider);
        raid.joinRaid(wave, raider, groundPos, true);
    }
}
