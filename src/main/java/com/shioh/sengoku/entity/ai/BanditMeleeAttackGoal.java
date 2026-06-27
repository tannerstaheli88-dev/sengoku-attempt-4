package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.entity.BanditEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

/**
 * Custom melee attack goal for bandits using custom weapons.
 * Simplified version that extends Goal directly.
 */
public class BanditMeleeAttackGoal extends Goal {
    
    private final BanditEntity bandit;
    private final double speedModifier;
    private final boolean pauseWhenMobIdle;
    private int ticksUntilNextAttack;
    
    public BanditMeleeAttackGoal(BanditEntity bandit, double speedModifier, boolean pauseWhenMobIdle) {
        this.bandit = bandit;
        this.speedModifier = speedModifier;
        this.pauseWhenMobIdle = pauseWhenMobIdle;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }
    
    @Override
    public boolean canUse() {
        LivingEntity target = this.bandit.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        
        return this.hasValidMeleeWeapon();
    }
    
    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.bandit.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        
        return this.hasValidMeleeWeapon() &&
               (!this.pauseWhenMobIdle || !this.bandit.getNavigation().isDone());
    }
    
    private boolean hasValidMeleeWeapon() {
        ItemStack mainHand = this.bandit.getMainHandItem();
        ItemStack offHand = this.bandit.getOffhandItem();
        
        return this.bandit.isMeleeWeapon(mainHand) || this.bandit.isMeleeWeapon(offHand);
    }
    
    @Override
    public void start() {
        this.bandit.setAggressive(true);
    }
    
    @Override
    public void stop() {
        this.bandit.setAggressive(false);
        this.bandit.getNavigation().stop();
    }
    
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
    
    @Override
    public void tick() {
        LivingEntity target = this.bandit.getTarget();
        if (target == null) return;
        
        double distance = this.bandit.distanceTo(target);
        boolean canSeeTarget = this.bandit.getSensing().hasLineOfSight(target);
        
        // Look at target
        this.bandit.getLookControl().setLookAt(target, 30.0F, 30.0F);
        
        // Move toward target if not in attack range
        if (distance > 1.5D) {
            this.bandit.getNavigation().moveTo(target, this.speedModifier);
        } else {
            this.bandit.getNavigation().stop();
        }
        
        // Handle attack timing
        this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
        
        // Attack if close enough and can see target
        if (distance <= 1.5D && canSeeTarget && this.ticksUntilNextAttack <= 0) {
            this.performAttack(target);
        }
    }
    
    private void performAttack(LivingEntity target) {
        this.bandit.swing(this.bandit.getUsedItemHand());
        this.bandit.doHurtTarget(target);
        this.ticksUntilNextAttack = 20; // 1 second cooldown
    }
}