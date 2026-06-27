package com.shioh.sengoku.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.List;

/**
 * Makes clan mobs target monsters that are actively targeting them.
 * This allows clan mobs to be proactive when a monster picks them as a target,
 * rather than waiting to be hit first.
 */
public class DefendFromTargetedMonsterGoal extends TargetGoal {
    
    private final Mob mob;
    private final double scanRange;
    private LivingEntity targetMonster;

    public DefendFromTargetedMonsterGoal(Mob mob, double scanRange) {
        super(mob, false, false);
        this.mob = mob;
        this.scanRange = scanRange;
    }

    @Override
    public boolean canUse() {
        // Don't override if already has a target
        if (this.mob.getTarget() != null) {
            return false;
        }

        // Scan for monsters that have this mob as their target
        List<Monster> nearbyMonsters = this.mob.level().getEntitiesOfClass(
            Monster.class,
            this.mob.getBoundingBox().inflate(this.scanRange),
            monster -> monster != null 
                && monster.isAlive() 
                && monster.getTarget() == this.mob
        );

        if (!nearbyMonsters.isEmpty()) {
            // Found a monster targeting us - pick the closest one
            this.targetMonster = nearbyMonsters.stream()
                .min((a, b) -> Double.compare(
                    this.mob.distanceToSqr(a), 
                    this.mob.distanceToSqr(b)
                ))
                .orElse(null);
            
            return this.targetMonster != null;
        }

        return false;
    }

    @Override
    public void start() {
        if (this.targetMonster != null) {
            this.mob.setTarget(this.targetMonster);
        }
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity currentTarget = this.mob.getTarget();
        if (currentTarget == null || !currentTarget.isAlive()) {
            return false;
        }
        
        // Continue targeting as long as the monster is alive and within range
        if (this.mob.distanceToSqr(currentTarget) > this.scanRange * this.scanRange) {
            return false;
        }
        
        return true;
    }

    @Override
    public void stop() {
        this.targetMonster = null;
        super.stop();
    }
}
