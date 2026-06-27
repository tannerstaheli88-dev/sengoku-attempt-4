package com.shioh.sengoku.ai;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Custom AI goal that makes parrots flee from players unless they're slowly approaching with seeds.
 * Similar to cat behavior - cautious and skittish until properly tempted.
 */
public class ParrotCautiousTemptGoal extends Goal {
    private final Parrot parrot;
    private Player targetPlayer;
    private final double fleeSpeed;
    private final double approachSpeed;
    private final double fleeDistance;
    private final double temptDistance;
    private int delayCounter;
    
    // Speed threshold - player must be moving slower than this to tempt
    private static final double SLOW_APPROACH_THRESHOLD = 0.08;
    private static final double SPRINT_THRESHOLD = 0.15;
    
    public ParrotCautiousTemptGoal(Parrot parrot, double fleeSpeed, double approachSpeed) {
        this.parrot = parrot;
        this.fleeSpeed = fleeSpeed;
        this.approachSpeed = approachSpeed;
        this.fleeDistance = 7.0; // Flee if player within 7 blocks (parrots are more skittish and aware)
        this.temptDistance = 12.0; // Can be tempted from further away - birds have good vision!
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }
    
    @Override
    public boolean canUse() {
        // Only if parrot is not tamed
        if (this.parrot.isTame()) {
            return false;
        }
        
        // Find nearest player - activate whenever a player is nearby
        this.targetPlayer = this.parrot.level().getNearestPlayer(
            this.parrot, this.temptDistance
        );
        
        // Don't be afraid of creative mode players
        if (this.targetPlayer != null && this.targetPlayer.isCreative()) {
            return false;
        }
        
        // Activate whenever a player is nearby, regardless of food
        return this.targetPlayer != null;
    }
    
    @Override
    public boolean canContinueToUse() {
        // Stop immediately if tamed
        if (this.parrot.isTame()) {
            return false;
        }
        
        if (this.targetPlayer == null || !this.targetPlayer.isAlive()) {
            return false;
        }
        
        // Don't be afraid of creative mode players
        if (this.targetPlayer.isCreative()) {
            return false;
        }
        
        // If player is very close with food and moving slowly, stop the goal to let vanilla taming work
        double distance = this.parrot.distanceTo(this.targetPlayer);
        boolean hasFood = isTemptingItem(this.targetPlayer.getMainHandItem()) || 
                         isTemptingItem(this.targetPlayer.getOffhandItem());
        Vec3 playerVelocity = this.targetPlayer.getDeltaMovement();
        double playerSpeed = Math.sqrt(
            playerVelocity.x * playerVelocity.x + 
            playerVelocity.z * playerVelocity.z
        );
        boolean playerMovingSlow = playerSpeed < SLOW_APPROACH_THRESHOLD || this.targetPlayer.isCrouching();
        
        // If player is close enough with food and being gentle, stop this goal to let taming happen
        if (distance < 2.0 && hasFood && playerMovingSlow) {
            return false;
        }
        
        // Continue as long as player is within range
        return this.parrot.distanceToSqr(this.targetPlayer) <= this.temptDistance * this.temptDistance;
    }
    
    @Override
    public void start() {
        this.delayCounter = 0;
    }
    
    @Override
    public void stop() {
        this.targetPlayer = null;
        this.parrot.getNavigation().stop();
    }
    
    @Override
    public void tick() {
        this.parrot.getLookControl().setLookAt(
            this.targetPlayer,
            (float)(this.parrot.getMaxHeadYRot() + 20),
            (float)this.parrot.getMaxHeadXRot()
        );
        
        double distanceSq = this.parrot.distanceToSqr(this.targetPlayer);
        double distance = Math.sqrt(distanceSq);
        
        // Check if player has tempting food
        boolean hasFood = isTemptingItem(this.targetPlayer.getMainHandItem()) || 
                         isTemptingItem(this.targetPlayer.getOffhandItem());
        
        // Calculate player's movement speed
        Vec3 playerVelocity = this.targetPlayer.getDeltaMovement();
        double playerSpeed = Math.sqrt(
            playerVelocity.x * playerVelocity.x + 
            playerVelocity.z * playerVelocity.z
        );
        
        // Check if player is being calm and patient
        boolean playerMovingSlow = playerSpeed < SLOW_APPROACH_THRESHOLD || this.targetPlayer.isCrouching();
        boolean playerBeingGentle = hasFood && playerMovingSlow;
        
        // If player is sprinting or moving too fast, always flee!
        boolean playerMovingTooFast = playerSpeed > SPRINT_THRESHOLD || this.targetPlayer.isSprinting();
        
        if (distance < this.fleeDistance && playerMovingTooFast) {
            fleeFast();
            return;
        }
        
        // If player has food and is being gentle, allow them to get close
        if (playerBeingGentle) {
            // Player is doing everything right - don't flee, maybe approach
            if (distance > 3.0) {
                // Still a bit far, slowly approach
                if (this.delayCounter++ > 20) {
                    this.delayCounter = 0;
                    this.parrot.getNavigation().moveTo(this.targetPlayer, this.approachSpeed * 0.5);
                }
            } else if (distance > 1.5) {
                // Medium distance - slow down to let player feed
                if (this.delayCounter++ > 40) {
                    this.delayCounter = 0;
                    this.parrot.getNavigation().stop();
                }
            } else {
                // Very close - stop completely to allow taming/feeding interaction
                this.parrot.getNavigation().stop();
            }
        } else if (distance < this.fleeDistance) {
            // Player doesn't have food or is moving too fast - flee
            flee();
        } else {
            // Player is far enough, just watch
            this.parrot.getNavigation().stop();
        }
    }
    
    private void flee() {
        // Flee away from player at normal speed
        Vec3 fleeDirection = this.parrot.position().subtract(this.targetPlayer.position()).normalize();
        Vec3 fleeTarget = this.parrot.position().add(fleeDirection.scale(6.0));
        
        this.parrot.getNavigation().moveTo(
            fleeTarget.x, 
            this.parrot.getY(), 
            fleeTarget.z, 
            this.fleeSpeed
        );
    }
    
    private void fleeFast() {
        // Flee away from player at MUCH higher speed when spooked - birds are FAST!
        Vec3 fleeDirection = this.parrot.position().subtract(this.targetPlayer.position()).normalize();
        Vec3 fleeTarget = this.parrot.position().add(fleeDirection.scale(12.0));
        
        this.parrot.getNavigation().moveTo(
            fleeTarget.x, 
            this.parrot.getY(), 
            fleeTarget.z, 
            this.fleeSpeed * 2.0  // Up to 6.0x speed when spooked - BLAZING fast!
        );
    }
    
    /**
     * Check if an item is tempting to parrots.
     * Parrots eat seeds.
     */
    private boolean isTemptingItem(ItemStack stack) {
        return stack.is(Items.WHEAT_SEEDS) || 
               stack.is(Items.MELON_SEEDS) || 
               stack.is(Items.PUMPKIN_SEEDS) || 
               stack.is(Items.BEETROOT_SEEDS) ||
               stack.is(Items.TORCHFLOWER_SEEDS) ||
               stack.is(Items.PITCHER_POD);
    }
}
