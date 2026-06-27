package com.shioh.sengoku.entity.ai;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

/**
 * Custom bow attack goal for mobs that properly handles bow animation.
 * Based on RangedBowAttackGoal but with proper item usage state management.
 */
public class BanditBowAttackGoal extends Goal {
    private final Mob bandit;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public BanditBowAttackGoal(Mob bandit, double speedModifier, int attackInterval, float attackRadius) {
        this.bandit = bandit;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackInterval;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.bandit.getTarget() != null && this.hasValidRangedWeapon();
    }

    private boolean hasValidRangedWeapon() {
        ItemStack mainHand = this.bandit.getMainHandItem();
        return mainHand.getItem() == Items.BOW;
    }

    @Override
    public boolean canContinueToUse() {
        return (this.canUse() || !this.bandit.getNavigation().isDone()) && this.hasValidRangedWeapon();
    }

    @Override
    public void start() {
        super.start();
        if (this.bandit instanceof AbstractIllager illager) illager.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        if (this.bandit instanceof AbstractIllager illager) illager.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
        try { this.bandit.stopUsingItem(); } catch (Throwable ignored) {}
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.bandit.getTarget();
        if (target == null) {
            return;
        }

        double distanceToTarget = this.bandit.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean canSeeTarget = this.bandit.getSensing().hasLineOfSight(target);
        boolean wasAbleToSeeTarget = this.seeTime > 0;

        if (canSeeTarget != wasAbleToSeeTarget) {
            this.seeTime = 0;
        }

        if (canSeeTarget) {
            ++this.seeTime;
        } else {
            --this.seeTime;
        }

        if (!(distanceToTarget > (double)this.attackRadiusSqr) && this.seeTime >= 20) {
            this.bandit.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.bandit.getNavigation().moveTo(target, this.speedModifier);
            this.strafingTime = -1;
        }

        if (this.strafingTime >= 20) {
            if ((double)this.bandit.getRandom().nextFloat() < 0.3) {
                this.strafingClockwise = !this.strafingClockwise;
            }

            if ((double)this.bandit.getRandom().nextFloat() < 0.3) {
                this.strafingBackwards = !this.strafingBackwards;
            }

            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (distanceToTarget > (double)(this.attackRadiusSqr * 0.75F)) {
                this.strafingBackwards = false;
            } else if (distanceToTarget < (double)(this.attackRadiusSqr * 0.25F)) {
                this.strafingBackwards = true;
            }

            this.bandit.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.bandit.lookAt(target, 30.0F, 30.0F);
        } else {
            this.bandit.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // Handle bow charging and shooting
        if (this.bandit.isUsingItem()) {
            if (!canSeeTarget && this.seeTime < -60) {
                this.bandit.stopUsingItem();
            } else if (canSeeTarget) {
                int bowUseTicks = this.bandit.getTicksUsingItem();
                // Bandits are slower at charging bows - require 35 ticks instead of 20
                if (bowUseTicks >= 35) {
                    this.bandit.stopUsingItem();
                    if (this.bandit instanceof RangedAttackMob ram) {
                        ram.performRangedAttack(target, BowItem.getPowerForTime(bowUseTicks));
                    }
                    this.attackTime = this.attackIntervalMin;
                }
            }
        } else if (--this.attackTime <= 0 && this.seeTime >= -60) {
            // Start using bow to trigger animation
            this.bandit.startUsingItem(InteractionHand.MAIN_HAND);
        }
    }
}