package com.shioh.sengoku.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Patrol-specific engagement control: makes patrols watch players from distance
 * and only engage when target gets close enough. Mimics vanilla pillager patrol behavior.
 */
public class PatrolCautiousEngagementGoal extends Goal {
    private final Mob mob;
    private static final double WATCH_DISTANCE = 22.0D;  // Watch from this far away
    private static final double ENGAGE_DISTANCE = 8.0D;  // Engage when this close
    private static final double ENGAGE_DISTANCE_AGGRESSIVE = 6.0D; // Closer if already aggressive
    private int aggression_timer = 0;
    private static final int AGGRESSION_BUILDUP = 40; // ticks to build up aggression from watching

    public PatrolCautiousEngagementGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.noneOf(Goal.Flag.class)); // Non-blocking; just gates other goals
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target == null) return false;
        
        // Only apply to patrols (has "sengoku_patrol" tag)
        if (!mob.getTags().contains("sengoku_patrol")) return false;
        
        if (!(target instanceof Player)) return false;
        return target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (!mob.getTags().contains("sengoku_patrol")) return false;
        if (!(target instanceof Player)) return false;
        return true;
    }

    @Override
    public void start() {
        aggression_timer = 0;
        mob.setAggressive(false);
    }

    @Override
    public void stop() {
        aggression_timer = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        double distSqr = mob.distanceToSqr(target);
        double engageDist = mob.isAggressive() ? ENGAGE_DISTANCE_AGGRESSIVE : ENGAGE_DISTANCE;

        // If target is close enough, become aggressive
        if (distSqr <= engageDist * engageDist) {
            mob.setAggressive(true);
            aggression_timer = AGGRESSION_BUILDUP;
            return;
        }

        // If already aggressive, build up aggression even at distance (watching intently)
        if (mob.isAggressive()) {
            aggression_timer--;
            if (aggression_timer <= 0) {
                // Aggression fades if target stays far
                if (distSqr > WATCH_DISTANCE * WATCH_DISTANCE) {
                    mob.setAggressive(false);
                    aggression_timer = 0;
                    mob.getNavigation().stop();
                    return;
                }
            }
            return;
        }

        // If within watch distance but not aggressive yet, follow from distance
        if (distSqr <= WATCH_DISTANCE * WATCH_DISTANCE) {
            // Walk toward player slowly to maintain watch distance
            double idealDist = (WATCH_DISTANCE + ENGAGE_DISTANCE) * 0.5D;
            double currentDist = Math.sqrt(distSqr);
            
            if (currentDist < idealDist * 0.7D) {
                // Too close; back away slightly
                mob.getNavigation().stop();
            } else if (currentDist > idealDist * 1.3D) {
                // Too far; move closer slowly
                mob.getNavigation().moveTo(target, 0.6D);
            } else {
                // Ideal distance; just watch
                mob.getNavigation().stop();
            }
        } else {
            // Out of watch range; stop engaging
            mob.setAggressive(false);
            aggression_timer = 0;
            mob.getNavigation().stop();
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
