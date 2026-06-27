package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes Endermen teleport away when players get too close (within 10 blocks)
 * Unless the enderman is aggressive (has a target)
 * Adds to the Kuchisake-onna ghost behavior - disappears when approached
 * Maintains proper stalking distance to keep tension without being redundant
 */
@Mixin(EnderMan.class)
public abstract class EndermanTeleportAwayMixin extends PathfinderMob {
    
    private static final double TELEPORT_DISTANCE_SQ = 400.0; // 20 blocks (squared) - maintains stalking tension
    private int sengoku$teleportCooldown = 0;
    
    protected EndermanTeleportAwayMixin(Level level) {
        super(null, level);
    }
    
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void sengoku$teleportAwayFromPlayer(CallbackInfo ci) {
        EnderMan enderman = (EnderMan) (Object) this;
        
        // Only teleport away in the Overworld - disabled in other dimensions
        if (enderman.level().dimension() != Level.OVERWORLD) {
            return;
        }
        
        // Reduce cooldown
        if (sengoku$teleportCooldown > 0) {
            sengoku$teleportCooldown--;
            return;
        }
        
        // Don't teleport if aggressive (has a target)
        LivingEntity target = enderman.getTarget();
        if (target != null) {
            return;
        }
        
        // Don't teleport if in water (already has vanilla teleport behavior)
        if (enderman.isInWaterOrRain()) {
            return;
        }
        
        // Check for nearby players
        Player nearestPlayer = enderman.level().getNearestPlayer(
            enderman.getX(),
            enderman.getY(),
            enderman.getZ(),
            20.5, // Check slightly beyond 20 blocks
            false
        );
        
        if (nearestPlayer != null && !nearestPlayer.isSpectator()) {
            // Don't teleport away from creative mode players
            if (nearestPlayer.isCreative()) {
                return;
            }
            
            double distanceSq = enderman.distanceToSqr(nearestPlayer);
            
            // If player is within 10 blocks, teleport away to maintain stalking distance
            if (distanceSq < TELEPORT_DISTANCE_SQ) {
                // Try to teleport to a safe location
                for (int i = 0; i < 16; i++) {
                    double newX = enderman.getX() + (enderman.getRandom().nextDouble() - 0.5) * 32.0;
                    double newY = enderman.getY() + (enderman.getRandom().nextInt(16) - 8);
                    double newZ = enderman.getZ() + (enderman.getRandom().nextDouble() - 0.5) * 32.0;
                    
                    BlockPos targetPos = BlockPos.containing(newX, newY, newZ);
                    
                    // Check if location is safe
                    if (enderman.level().getBlockState(targetPos).isAir() && 
                        enderman.level().getBlockState(targetPos.above()).isAir() &&
                        !enderman.level().getBlockState(targetPos.below()).isAir()) {
                        
                        // Teleport to position (silent - no sound effects)
                        if (enderman.randomTeleport(newX, newY, newZ, false)) {
                            // Teleport successful, set cooldown
                            sengoku$teleportCooldown = 20; // 1 second cooldown
                            break;
                        }
                    }
                }
            }
        }
    }
}
