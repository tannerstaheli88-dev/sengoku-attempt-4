package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Makes Endermen (Kuchisake-onna) stalk players - following them slowly to build tension
 * Only one enderman can stalk each player at a time
 */
@Mixin(EnderMan.class)
public class EndermanStalkingMixin {
    
    // Track which enderman is currently stalking each player
    private static final Map<UUID, UUID> PLAYER_STALKERS = new HashMap<>();
    
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void sengoku$addStalkingBehavior(CallbackInfo ci) {
        EnderMan enderman = (EnderMan) (Object) this;
        // Only add stalking behavior in the Overworld
        if (enderman.level().dimension() == Level.OVERWORLD) {
            // Add stalking behavior at priority 3 (before normal wander/look goals)
            ((MobAccessor)enderman).getGoalSelector().addGoal(3, new StalkPlayerGoal(enderman));
        }
    }
    
    /**
     * Make enderman ALWAYS look at nearest player every tick
     * This creates the unsettling feeling of always being watched
     */
    @Inject(method = "aiStep", at = @At("TAIL"))
    private void sengoku$alwaysLookAtPlayer(CallbackInfo ci) {
        EnderMan enderman = (EnderMan) (Object) this;

        // Only act in the Overworld
        if (enderman.level().dimension() != Level.OVERWORLD) return;

        Player nearestPlayer = enderman.level().getNearestPlayer(
            enderman.getX(),
            enderman.getY(),
            enderman.getZ(),
            48.0,
            false
        );

        if (nearestPlayer == null || nearestPlayer.isSpectator()) return;

        // If player is extremely close (within 1 block) — set as target so vanilla anger triggers
        double closeDistSq = enderman.distanceToSqr(nearestPlayer);
        if (closeDistSq <= 1.0 && enderman.getTarget() == null) {
            enderman.setTarget(nearestPlayer);
        }

        // Use look control for natural head movement
        try {
            enderman.getLookControl().setLookAt(
                nearestPlayer,
                30.0F,
                (float) enderman.getMaxHeadXRot()
            );
        } catch (Throwable ignored) {}

        // Also force the body's yaw to face the player to ensure consistent facing
        double dx = nearestPlayer.getX() - enderman.getX();
        double dz = nearestPlayer.getZ() - enderman.getZ();
        if (Math.abs(dx) > 1e-6 || Math.abs(dz) > 1e-6) {
            float desiredYaw = (float)(Math.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
            enderman.setYRot(desiredYaw);
            enderman.yHeadRot = enderman.getYRot();
            enderman.yBodyRot = enderman.yHeadRot;
        }
    }
    
    /**
     * Stalking goal - follows players at a distance to create tension
     * Like Kuchisake-onna following victims in the streets
     * Stalks from far away and tries to stay out of direct view
     * Only one enderman can stalk each player at a time
     */
    private static class StalkPlayerGoal extends Goal {
        private final EnderMan enderman;
        private Player targetPlayer;
        private int teleportCooldown = 0;
        
        public StalkPlayerGoal(EnderMan enderman) {
            this.enderman = enderman;
        }
        
        @Override
        public boolean canUse() {
            // Only stalk in the Overworld
            if (this.enderman.level().dimension() != Level.OVERWORLD) {
                return false;
            }
            // Don't stalk if taking sun damage (burning during daytime)
            if (this.enderman.isOnFire()) {
                return false;
            }
            
            // Only stalk if not already targeting someone
            LivingEntity currentTarget = this.enderman.getTarget();
            if (currentTarget != null) {
                return false;
            }
            
            // UNDERGROUND SPAWN LIMITING: If underground (below Y=50), check for nearby endermen
            // This prevents 3-4 endermen from all stalking at once in caves
            if (this.enderman.getY() < 50.0) {
                // Count nearby endermen within 32 blocks
                long nearbyEndermen = this.enderman.level().getEntitiesOfClass(
                    EnderMan.class,
                    this.enderman.getBoundingBox().inflate(32.0),
                    (other) -> other != this.enderman && !other.isRemoved()
                ).size();
                
                // If there are already 2+ endermen nearby underground, don't stalk
                // This ensures only 1-2 endermen stalk at once in caves
                if (nearbyEndermen >= 2) {
                    return false;
                }
            }
            
            // Find nearest player within 48 blocks (extended range)
            this.targetPlayer = this.enderman.level().getNearestPlayer(
                this.enderman.getX(), 
                this.enderman.getY(), 
                this.enderman.getZ(), 
                48.0, 
                false
            );
            
            if (this.targetPlayer == null || this.targetPlayer.isSpectator()) {
                return false;
            }
            
            // Check if another enderman is already stalking this player
            UUID playerUUID = this.targetPlayer.getUUID();
            UUID stalkerUUID = PLAYER_STALKERS.get(playerUUID);
            
            if (stalkerUUID != null && !stalkerUUID.equals(this.enderman.getUUID())) {
                // Another enderman is registered as stalking this player
                // Check if that enderman still exists and is alive
                boolean stalkerStillExists = this.enderman.level().getEntitiesOfClass(
                    EnderMan.class,
                    this.enderman.getBoundingBox().inflate(64.0),
                    (other) -> other.getUUID().equals(stalkerUUID) && !other.isRemoved() && other.isAlive()
                ).size() > 0;
                
                if (stalkerStillExists) {
                    return false; // Another enderman is still actively stalking
                } else {
                    // Stalker is gone, we can take over
                    PLAYER_STALKERS.remove(playerUUID);
                }
            }
            
            // Claim this player
            PLAYER_STALKERS.put(playerUUID, this.enderman.getUUID());
            return true;
        }
        
        @Override
        public boolean canContinueToUse() {
            // Only continue stalking in the Overworld
            if (this.enderman.level().dimension() != Level.OVERWORLD) {
                return false;
            }
            // Stop stalking if taking sun damage (burning during daytime)
            if (this.enderman.isOnFire()) {
                return false;
            }
            
            // Stop if enderman has a target (got angry)
            if (this.enderman.getTarget() != null) {
                return false;
            }
            
            if (this.targetPlayer == null || !this.targetPlayer.isAlive()) {
                return false;
            }
            
            // Keep stalking even if player gets far - we'll teleport to stay close
            return true;
        }
        
        @Override
        public void start() {
            this.teleportCooldown = 0;
        }
        
        @Override
        public void tick() {
            if (this.targetPlayer == null) return;
            
            // If enderman has a target (got angry), stop stalking
            if (this.enderman.getTarget() != null) {
                return;
            }
            
            // Always look at the player menacingly
            this.enderman.getLookControl().setLookAt(
                this.targetPlayer, 
                10.0F, 
                (float) this.enderman.getMaxHeadXRot()
            );
            
            double distanceSq = this.enderman.distanceToSqr(this.targetPlayer);
            
            // Reduce teleport cooldown
            if (teleportCooldown > 0) {
                teleportCooldown--;
            }
            
            // If player is too far (more than 40 blocks), teleport closer
            if (distanceSq > 1600.0 && teleportCooldown == 0) {
                // Try to teleport behind or to the side of the player (out of view)
                teleportBehindPlayer();
            } else if (distanceSq > 900.0) { // More than 30 blocks away
                // Move closer slowly
                this.enderman.getNavigation().moveTo(this.targetPlayer, 0.25);
            } else if (distanceSq < 400.0) { // Less than 20 blocks away
                // Back off to maintain far stalking distance
                double dx = this.enderman.getX() - this.targetPlayer.getX();
                double dz = this.enderman.getZ() - this.targetPlayer.getZ();
                double length = Math.sqrt(dx * dx + dz * dz);
                
                if (length > 0) {
                    double targetX = this.enderman.getX() + (dx / length) * 5.0;
                    double targetZ = this.enderman.getZ() + (dz / length) * 5.0;
                    this.enderman.getNavigation().moveTo(targetX, this.enderman.getY(), targetZ, 0.25);
                }
            } else {
                // At perfect stalking distance (20-30 blocks) - stop moving, just watch
                this.enderman.getNavigation().stop();
            }
        }
        
        /**
         * Teleport behind or to the sides of the player, avoiding their direct view
         */
        private void teleportBehindPlayer() {
            // Get player's look direction
            double playerYaw = Math.toRadians(this.targetPlayer.getYRot() + 180.0); // Behind player
            
            for (int i = 0; i < 16; i++) {
                // Prefer positions behind the player (±90 degrees from behind)
                double angleOffset = (this.enderman.getRandom().nextDouble() - 0.5) * Math.PI; // ±90 degrees
                double angle = playerYaw + angleOffset;
                
                // Teleport to 24-32 blocks away (far stalking distance)
                double distance = 24.0 + this.enderman.getRandom().nextDouble() * 8.0;
                
                double newX = this.targetPlayer.getX() + Math.cos(angle) * distance;
                double newY = this.targetPlayer.getY();
                double newZ = this.targetPlayer.getZ() + Math.sin(angle) * distance;
                
                // Try to teleport silently
                if (this.enderman.randomTeleport(newX, newY, newZ, false)) {
                    teleportCooldown = 100; // 5 second cooldown between teleports
                    break;
                }
            }
        }
        
        @Override
        public void stop() {
            // Release claim on this player
            if (this.targetPlayer != null) {
                UUID playerUUID = this.targetPlayer.getUUID();
                UUID currentStalker = PLAYER_STALKERS.get(playerUUID);
                // Only remove if we're still the registered stalker
                if (currentStalker != null && currentStalker.equals(this.enderman.getUUID())) {
                    PLAYER_STALKERS.remove(playerUUID);
                }
            }
            
            this.targetPlayer = null;
            this.enderman.getNavigation().stop();
        }
    }
}
