package com.shioh.sengoku.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

/**
 * Custom AI goal that makes llamas (deer) flee from players unless they're slowly approaching with food.
 * Players must walk slowly (sneaking or walking, not sprinting) to be able to tempt llamas.
 */
public class LlamaCautiousTemptGoal extends Goal {
    private final Llama llama;
    private Player targetPlayer;
    private final double fleeSpeed;
    private final double approachSpeed;
    
    // Distance thresholds
    private static final double FLEE_DISTANCE = 8.0;
    private static final double TEMPT_DISTANCE = 10.0;  // Increased so llama can see wheat from further away
    private static final double MIN_DISTANCE = 1.5;     // Reduced so llama approaches closer
    
    // Speed threshold for "slow approach" - increased to be more permissive
    private static final double MAX_APPROACH_SPEED = 0.25; // Player must be moving slowly (sneaking or walking)
    
    private int fleeTime = 0;
    private int calmTime = 0;

    public LlamaCautiousTemptGoal(Llama llama, double fleeSpeed, double approachSpeed) {
        this.llama = llama;
        this.fleeSpeed = fleeSpeed;
        this.approachSpeed = approachSpeed;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

@Override
public boolean canUse() {
    // Don't flee if tamed, owned, being ridden, or leashed
    if (this.llama.isTamed()) return false;
    if (this.llama.getOwnerUUID() != null) return false;
    if (this.llama.isVehicle()) return false;
    if (this.llama.isLeashed()) return false;

    this.targetPlayer = this.llama.level().getNearestPlayer(
        this.llama.getX(),
        this.llama.getY(),
        this.llama.getZ(),
        FLEE_DISTANCE,
        false
    );

    if (this.targetPlayer == null) return false;
    if (this.targetPlayer.isCreative()) return false;

    double distance = this.llama.distanceTo(this.targetPlayer);
    boolean hasWheat = this.targetPlayer.getMainHandItem().is(Items.WHEAT)
                    || this.targetPlayer.getOffhandItem().is(Items.WHEAT);
    double playerSpeed = Math.sqrt(
        this.targetPlayer.getDeltaMovement().x * this.targetPlayer.getDeltaMovement().x +
        this.targetPlayer.getDeltaMovement().z * this.targetPlayer.getDeltaMovement().z
    );
    if (hasWheat && playerSpeed < MAX_APPROACH_SPEED && distance < MIN_DISTANCE) {
        return false;
    }

    return true;
}

@Override
public boolean canContinueToUse() {
    return this.canUse();
}
    @Override
    public void start() {
        this.fleeTime = 0;
        this.calmTime = 0;
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
        this.llama.getNavigation().stop();
        this.fleeTime = 0;
        this.calmTime = 0;
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) return;

        double distance = this.llama.distanceTo(this.targetPlayer);
        boolean hasFood = this.targetPlayer.getMainHandItem().is(Items.WHEAT) 
                       || this.targetPlayer.getOffhandItem().is(Items.WHEAT);

        // Calculate player's movement speed (horizontal only)
        double playerSpeed = Math.sqrt(
            this.targetPlayer.getDeltaMovement().x * this.targetPlayer.getDeltaMovement().x +
            this.targetPlayer.getDeltaMovement().z * this.targetPlayer.getDeltaMovement().z
        );

        boolean movingSlowly = playerSpeed < MAX_APPROACH_SPEED;

        // Look at player
        this.llama.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);

        // If player has wheat and is moving slowly within tempt distance, approach them
        if (hasFood && movingSlowly && distance < TEMPT_DISTANCE) {
            this.fleeTime = 0;
            this.calmTime++;
            
            // Approach immediately without long delay
            if (distance > MIN_DISTANCE) {
                this.llama.getNavigation().moveTo(this.targetPlayer, this.approachSpeed);
            } else {
                // Stop approaching if too close
                this.llama.getNavigation().stop();
            }
        } 
        // If player is too close, doesn't have food, or is moving too fast, flee
        else if (distance < FLEE_DISTANCE) {
            this.calmTime = 0;
            this.fleeTime++;

            if (this.fleeTime > 3) { // Brief delay before fleeing (0.15 seconds)
                // Calculate flee direction (away from player)
                double dx = this.llama.getX() - this.targetPlayer.getX();
                double dz = this.llama.getZ() - this.targetPlayer.getZ();
                double length = Math.sqrt(dx * dx + dz * dz);
                
                if (length > 0.001) {
                    dx /= length;
                    dz /= length;
                    
                    // Move away from player
                    double fleeX = this.llama.getX() + dx * 12.0;
                    double fleeZ = this.llama.getZ() + dz * 12.0;
                    
                    this.llama.getNavigation().moveTo(fleeX, this.llama.getY(), fleeZ, this.fleeSpeed);
                }
            }
        } 
        // Player at safe distance - just watch
        else {
            this.llama.getNavigation().stop();
            this.fleeTime = 0;
            this.calmTime = 0;
        }
    }
}
