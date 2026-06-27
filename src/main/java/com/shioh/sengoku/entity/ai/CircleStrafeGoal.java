package com.shioh.sengoku.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * AI Goal that makes entities circle around their target similar to skeletons.
 * Uses proper strafe mechanics for smooth circling movement during combat.
 * This goal is designed to work alongside AdvancedMeleeAttackGoal by not claiming MOVE flag.
 */
public class CircleStrafeGoal extends Goal {
    private final Mob mob;
    private final double speedModifier;
    
    // Strafing variables like RangedBowAttackGoal
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;
    // New phased behaviour: alternate between rest (guard) and strafe bursts
    private boolean resting = false;
    private int phaseTime = 0; // counts down current phase
    private static final int MIN_REST_TICKS = 30; // 1.5s
    private static final int MAX_REST_TICKS = 60; // 3s
    private static final int MIN_STRAFE_TICKS = 40; // 2s
    private static final int MAX_STRAFE_TICKS = 80; // 4s
    
    public CircleStrafeGoal(Mob mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        // Don't use this goal - it causes sliding. Combat movement should be handled by attack goal only
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }
    
    public CircleStrafeGoal(Mob mob) {
        this(mob, 1.0D);
    }
    
    @Override
    public boolean canUse() {
        // Don't run strafing while mounted; mounted mobs should use vanilla attack goal
        if (this.mob.isPassenger()) return false;
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        // Prefer vanilla melee for villagers and wandering traders
        if (target instanceof net.minecraft.world.entity.npc.Villager || target instanceof net.minecraft.world.entity.npc.WanderingTrader) return false;
        return true;
    }
    
    @Override
    public boolean canContinueToUse() {
        if (this.mob.isPassenger()) return false;
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (target instanceof net.minecraft.world.entity.npc.Villager || target instanceof net.minecraft.world.entity.npc.WanderingTrader) return false;
        return true;
    }
    
    @Override
    public void start() {
        super.start();
        // Initialize strafe direction randomly
        this.strafingClockwise = this.mob.getRandom().nextBoolean();
        this.strafingBackwards = this.mob.getRandom().nextBoolean();
        this.strafingTime = 0;
        // Begin in a short rest/guard phase to let player orient
        this.resting = true;
        this.phaseTime = MIN_REST_TICKS + this.mob.getRandom().nextInt(MAX_REST_TICKS - MIN_REST_TICKS + 1);
    }
    
    @Override
    public void stop() {
        this.strafingTime = -1;
        // Don't stop navigation - let attack goal handle it
    }
    
    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return;
        }
        // If target is a villager-like NPC, stop strafing so vanilla melee can handle it
        if (target instanceof net.minecraft.world.entity.npc.Villager || target instanceof net.minecraft.world.entity.npc.WanderingTrader) { stop(); return; }
        // If the mob mounts mid-combat, stop this goal immediately
        if (this.mob.isPassenger()) { stop(); return; }
        
        double distSq = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean canSeeTarget = this.mob.getSensing().hasLineOfSight(target);
        
        // Manage strafing behavior like skeleton bow attack
        if (canSeeTarget) {
            ++this.strafingTime;
        } else {
            this.strafingTime = 0;
        }
        
        // Change strafe direction every 20 ticks (1 second)
        if (this.strafingTime >= 20) {
            if (this.mob.getRandom().nextDouble() < 0.3) {
                // 30% chance to switch clockwise/counterclockwise
                this.strafingClockwise = !this.strafingClockwise;
            }
            if (this.mob.getRandom().nextDouble() < 0.3) {
                // 30% chance to switch forward/backward movement
                this.strafingBackwards = !this.strafingBackwards;
            }
            this.strafingTime = 0;
        }
        
        // Phase handling
        if (this.phaseTime > 0) {
            this.phaseTime--;
        }
        if (this.phaseTime <= 0) {
            // Switch phase
            this.resting = !this.resting;
            if (this.resting) {
                this.phaseTime = MIN_REST_TICKS + this.mob.getRandom().nextInt(MAX_REST_TICKS - MIN_REST_TICKS + 1);
            } else {
                this.phaseTime = MIN_STRAFE_TICKS + this.mob.getRandom().nextInt(MAX_STRAFE_TICKS - MIN_STRAFE_TICKS + 1);
                // Refresh strafe direction on new burst
                this.strafingClockwise = this.mob.getRandom().nextBoolean();
                this.strafingBackwards = this.mob.getRandom().nextBoolean();
            }
        }

        if (this.resting) {
            // Guard posture: face target, minimal lateral movement; slight retreat if too close
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (distSq < 4.0D) {
                // Nudge backwards to maintain spacing
                this.mob.getMoveControl().strafe(-0.2F, 0.0F);
            }
            return; // Skip strafing mechanics while resting
        }

        // Apply strafing movement only during strafe phase and within range (3-7 blocks)
        if (distSq < 49.0D && distSq > 9.0D) {
            this.mob.getMoveControl().strafe(
                this.strafingBackwards ? -0.5F : 0.5F,
                this.strafingClockwise ? 0.5F : -0.5F
            );
            this.mob.lookAt(target, 30.0F, 30.0F);
        } else {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }
    
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
