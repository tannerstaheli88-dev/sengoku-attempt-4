package com.shioh.sengoku.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.phys.AABB;
import com.shioh.sengoku.util.EntityVisibility;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Target goal: if a nearby ally (as defined by isAlliedTo) was recently hurt by someone,
 * acquire that attacker as this mob's target. This allows mixed-class clans (samurai/ashigaru/sohei)
 * to assist each other when any member is attacked.
 */
public class DefendAlliesTargetGoal<T extends Mob> extends TargetGoal {
    private final T mob;
    private final double radius;
    private LivingEntity attacker;
    private int lastAlertTick;

    public DefendAlliesTargetGoal(T mob, double radius) {
        super(mob, false, true);
        this.mob = mob;
        this.radius = radius;
    }

    @Override
    public boolean canUse() {
        // Don't override current target selection if already has a valid target
        LivingEntity current = this.mob.getTarget();
        if (current != null && current.isAlive()) {
            return false;
        }

        // Scan nearby living entities for allies that were recently hurt
        AABB box = this.mob.getBoundingBox().inflate(this.radius, this.radius / 2.0, this.radius);
        List<LivingEntity> nearby = this.mob.level().getEntitiesOfClass(LivingEntity.class, box);

        for (LivingEntity ally : nearby) {
            if (ally == this.mob) continue;
            if (!(ally instanceof Mob)) continue;

            // Must be allied
            if (!this.mob.isAlliedTo(ally)) continue;

            LivingEntity lastAttacker = ally.getLastHurtByMob();
            if (lastAttacker == null || !lastAttacker.isAlive()) continue;
            if (this.mob.isAlliedTo(lastAttacker)) continue; // don't target same clan

            // Apply the occlusion/visibility heuristic to all attackers (including players).
            // If this mob realistically cannot detect the attacker (occluded by walls/floors,
            // out of sight, or stealthy player), don't be alerted to assist.
            try {
                if (!EntityVisibility.canDetect(this.mob, lastAttacker)) continue;
            } catch (Throwable ignored) {}

            // Use timestamp to avoid re-alerting on stale events
            int ts = ally.getLastHurtByMobTimestamp();
            if (ts <= this.lastAlertTick) continue;

            // Check canAttack rules
            if (!this.mob.canAttack(lastAttacker)) continue;

            this.attacker = lastAttacker;
            this.lastAlertTick = ts;
            return true;
        }
        return false;
    }

    @Override
    public void start() {
        if (this.attacker != null) {
            this.mob.setTarget(this.attacker);
        }
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive() && !this.mob.isAlliedTo(target);
    }
}
