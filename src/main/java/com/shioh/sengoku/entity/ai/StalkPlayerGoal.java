package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.entity.YukiOnnaEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * AI Goal that makes Yuki Onna stalk players from a distance
 * Follows at 20-30 blocks away, always watching menacingly
 */
public class StalkPlayerGoal extends Goal {
    private final YukiOnnaEntity yukiOnna;
    private Player targetPlayer;
    private int teleportCooldown;
    
    public StalkPlayerGoal(YukiOnnaEntity yukiOnna, double speedModifier) {
        this.yukiOnna = yukiOnna;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }
    
    @Override
    public boolean canUse() {
        // Only stalk if not actively attacking someone
        LivingEntity currentTarget = this.yukiOnna.getTarget();
        if (currentTarget != null) {
            return false;
        }
        
        // Find nearest player within 48 blocks (exclude creative and spectator)
        this.targetPlayer = this.yukiOnna.level().getNearestPlayer(
            this.yukiOnna.getX(),
            this.yukiOnna.getY(),
            this.yukiOnna.getZ(),
            48.0,
            entity -> {
                if (entity instanceof Player player) {
                    return !player.isSpectator() && !player.isCreative();
                }
                return false;
            }
        );
        
        if (this.targetPlayer == null) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean canContinueToUse() {
        // Stop if entity has a target (got angry/hit)
        if (this.yukiOnna.getTarget() != null) {
            return false;
        }
        
        if (this.targetPlayer == null || !this.targetPlayer.isAlive()) {
            return false;
        }
        
        // Stop stalking if player is in creative or spectator
        if (this.targetPlayer.isCreative() || this.targetPlayer.isSpectator()) {
            return false;
        }
        
        // Keep stalking even if player gets far - we'll teleport to stay close
        return true;
    }
    
    @Override
    public void start() {
        this.teleportCooldown = 0;
        this.yukiOnna.setCreepy(true);
        this.yukiOnna.setStalkingTarget(this.targetPlayer);
        triggerStorm();
    }
    
    @Override
    public void stop() {
        this.targetPlayer = null;
        this.yukiOnna.getNavigation().stop();
        this.yukiOnna.setCreepy(false);
        this.yukiOnna.clearStalkingTarget();
    }
    
    @Override
    public void tick() {
        if (this.targetPlayer == null) {
            return;
        }
        
        // If entity has a target (gets mad lol), stop stalking
        if (this.yukiOnna.getTarget() != null) {
            return;
        }
        
        double distanceSq = this.yukiOnna.distanceToSqr(this.targetPlayer);
        
        // ALWAYS look at the player menacingly (but only update look, never move)
        this.yukiOnna.getLookControl().setLookAt(
            this.targetPlayer, 
            30.0F, // High turn speed - snap to player quickly
            (float) this.yukiOnna.getMaxHeadXRot()
        );
        
        // Stop all movement - she stands completely still while stalking
        this.yukiOnna.getNavigation().stop();
        // Set velocity to zero to prevent any movement
        this.yukiOnna.setDeltaMovement(0, this.yukiOnna.getDeltaMovement().y, 0);
        
        // Reduce teleport cooldown
        if (teleportCooldown > 0) {
            teleportCooldown--;
        }
        
        // If player is too far (more than 40 blocks), teleport closer
        if (distanceSq > 1600.0 && teleportCooldown == 0) {
            // Try to teleport behind or to the side of the player
            teleportBehindPlayer();
        } else if (distanceSq > 900.0 && teleportCooldown == 0) { // More than 30 blocks away
            // Teleport closer instead of walking
            teleportBehindPlayer();
        } else if (distanceSq < 400.0 && teleportCooldown == 0) { // Less than 20 blocks away - too close
            // Teleport farther away to maintain stalking distance
            teleportBehindPlayer();
        }
        // Otherwise, at perfect stalking distance (20-30 blocks) - stand completely still and watch
    }
    
    /**
     * Teleport behind or to the sides of the player, avoiding their direct view
     */
    private void teleportBehindPlayer() {
        // Get player's look direction
        double playerYaw = Math.toRadians(this.targetPlayer.getYRot() + 180.0); // Behind player
        
        for (int i = 0; i < 16; i++) {
            // Prefer positions behind the player (±90 degrees from behind)
            double angleOffset = (this.yukiOnna.getRandom().nextDouble() - 0.5) * Math.PI; // ±90 degrees
            double angle = playerYaw + angleOffset;
            
            // Teleport to 24-32 blocks away (far stalking distance)
            double distance = 24.0 + this.yukiOnna.getRandom().nextDouble() * 8.0;
            
            double newX = this.targetPlayer.getX() + Math.cos(angle) * distance;
            double newY = this.targetPlayer.getY();
            double newZ = this.targetPlayer.getZ() + Math.sin(angle) * distance;
            
            // Try to teleport silently
            if (this.yukiOnna.randomTeleport(newX, newY, newZ, false)) {
                teleportCooldown = 100; // 5 second cooldown between teleports
                break;
            }
        }
    }

    private void triggerStorm() {
        if (this.targetPlayer == null || this.yukiOnna.level().isClientSide()) {
            return;
        }
        if (!(this.yukiOnna.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        MinecraftServer server = serverLevel.getServer();
        if (server == null) {
            return;
        }
        if (server.isSingleplayer() || server.getPlayerList().getPlayerCount() <= 1) {
            serverLevel.setWeatherParameters(0, 6000, true, true);
        }
    }
}
