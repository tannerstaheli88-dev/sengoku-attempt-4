package com.shioh.sengoku.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Custom melee attack goal for pigs that don't have the attack_damage attribute.
 * Uses a fixed damage value instead of reading from entity attributes.
 */
public class PigMeleeAttackGoal extends Goal {
    private final PathfinderMob mob;
    private final double speedModifier;
    private final boolean followingTargetEvenIfNotSeen;
    private Path path;
    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;
    private int ticksUntilNextPathRecalculation;
    private int ticksUntilNextAttack;
    private long lastCanUseCheck;
    private final int attackInterval = 20;
    private final float fixedDamage = 3.0F; // Pigs deal 3 damage (1.5 hearts)

    public PigMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        long gameTime = this.mob.level().getGameTime();
        if (gameTime - this.lastCanUseCheck < 20L) {
            return false;
        } else {
            this.lastCanUseCheck = gameTime;
            LivingEntity target = this.mob.getTarget();
            if (target == null) {
                return false;
            } else if (!target.isAlive()) {
                return false;
            } else {
                this.path = this.mob.getNavigation().createPath(target, 0);
                if (this.path != null) {
                    return true;
                } else {
                    return this.getAttackReachSqr(target) >= this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
                }
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            return false;
        } else if (!target.isAlive()) {
            return false;
        } else if (!this.followingTargetEvenIfNotSeen) {
            return !this.mob.getNavigation().isDone();
        } else if (!this.mob.isWithinRestriction(target.blockPosition())) {
            return false;
        } else {
            return !(target instanceof net.minecraft.world.entity.player.Player) || !target.isSpectator() && !((net.minecraft.world.entity.player.Player)target).isCreative();
        }
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
        this.mob.setAggressive(true);
        this.ticksUntilNextPathRecalculation = 0;
        this.ticksUntilNextAttack = 0;
    }

    @Override
    public void stop() {
        LivingEntity target = this.mob.getTarget();
        if (!net.minecraft.world.entity.EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)) {
            this.mob.setTarget(null);
        }
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distanceToSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
            
            if ((this.followingTargetEvenIfNotSeen || this.mob.getSensing().hasLineOfSight(target))
                && this.ticksUntilNextPathRecalculation <= 0
                && (this.pathedTargetX == 0.0 && this.pathedTargetY == 0.0 && this.pathedTargetZ == 0.0
                    || target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0
                    || this.mob.getRandom().nextFloat() < 0.05F)) {
                this.pathedTargetX = target.getX();
                this.pathedTargetY = target.getY();
                this.pathedTargetZ = target.getZ();
                this.ticksUntilNextPathRecalculation = 4 + this.mob.getRandom().nextInt(7);
                
                if (distanceToSqr > 1024.0) {
                    this.ticksUntilNextPathRecalculation += 10;
                } else if (distanceToSqr > 256.0) {
                    this.ticksUntilNextPathRecalculation += 5;
                }
                
                if (!this.mob.getNavigation().moveTo(target, this.speedModifier)) {
                    this.ticksUntilNextPathRecalculation += 15;
                }
            }
            
            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
            this.checkAndPerformAttack(target, distanceToSqr);
        }
    }

    protected void checkAndPerformAttack(LivingEntity target, double distanceToSqr) {
        double attackReachSqr = this.getAttackReachSqr(target);
        if (distanceToSqr <= attackReachSqr && this.ticksUntilNextAttack <= 0) {
            this.resetAttackCooldown();
            // Use fixed damage instead of reading from attributes
            target.hurt(this.mob.damageSources().mobAttack(this.mob), this.fixedDamage);
        }
    }

    protected void resetAttackCooldown() {
        this.ticksUntilNextAttack = this.attackInterval;
    }

    protected double getAttackReachSqr(LivingEntity target) {
        return this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + target.getBbWidth();
    }
}
