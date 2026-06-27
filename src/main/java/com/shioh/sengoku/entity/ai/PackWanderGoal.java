package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.entity.MacaqueEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/**
 * PackWanderGoal: when a small group of macaques is together but no leader is moving,
 * occasionally pick a nearby random location and wander as a pack to avoid idling forever.
 */
public class PackWanderGoal extends Goal {
    private final MacaqueEntity macaque;
    private final double speed;
    private final int radius;
    private final Random rand = new Random();
    private int roamTicks = 0;
    private int roamMaxTicks = 0;
    private int roamCooldown = 0;

    public PackWanderGoal(MacaqueEntity macaque, double speed, int radius) {
        this.macaque = macaque;
        this.speed = speed;
        this.radius = radius;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        try {
            if (this.macaque.isBaby()) return false;
            if (this.macaque.isSitting()) return false;
            // decrement per-entity roam cooldown
            if (this.roamCooldown > 0) this.roamCooldown--;
            // require at least two nearby macaques to consider pack wandering
            List<MacaqueEntity> nearby = this.macaque.level().getEntitiesOfClass(MacaqueEntity.class, this.macaque.getBoundingBox().inflate(this.radius, 2.0D, this.radius));
            int count = 0;
            boolean anyMoving = false;
            int packSittingCount = 0;
            for (MacaqueEntity m : nearby) {
                if (m == this.macaque) continue;
                if (m.isBaby()) continue;
                count++;
                try { if (!m.getNavigation().isDone()) anyMoving = true; } catch (Throwable ignored) {}
                try { if (m.isPackSitting()) packSittingCount++; } catch (Throwable ignored) {}
            }
            if (count < 1) return false; // need at least a small group
            // If the pack is mostly sitting, allow non-sitting macaques to occasionally wander
            boolean packMostlySitting = packSittingCount > (count / 2);
            if (packMostlySitting) {
                // give a higher chance for non-sitting individuals to roam while pack sits
                if (this.macaque.isGrooming() || this.macaque.isBeingGroomed()) return false;
                if (this.roamCooldown == 0 && this.macaque.getRandom().nextInt(120) == 0) {
                    // start a roam lasting 5-15 seconds
                    this.roamMaxTicks = 20 * (5 + this.rand.nextInt(11));
                    this.roamTicks = this.roamMaxTicks;
                    return true;
                }
                return false;
            }
            // otherwise, only wander if no one is actively leading/moving
            if (anyMoving) return false;
            // small random chance to start wandering
            return this.macaque.getRandom().nextInt(800) == 0;
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        try {
            if (this.macaque.isSitting()) return false;
            if (this.roamTicks > 0) return true;
            if (!this.macaque.getNavigation().isDone()) return true;
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public void start() {
        try {
            double tx = this.macaque.getX() + (this.macaque.getRandom().nextDouble() * 2.0D - 1.0D) * this.radius;
            double tz = this.macaque.getZ() + (this.macaque.getRandom().nextDouble() * 2.0D - 1.0D) * this.radius;
            double ty = this.macaque.getY();
            try { this.macaque.getNavigation().moveTo(tx, ty, tz, this.speed); } catch (Throwable ignored) {}
            // mark this macaque (and nearby pack members) as wandering
            try { this.macaque.setPackSitting(false); } catch (Throwable ignored) {}
            try {
                for (MacaqueEntity m : this.macaque.level().getEntitiesOfClass(MacaqueEntity.class, this.macaque.getBoundingBox().inflate(this.radius, 2.0D, this.radius))) {
                    if (m == this.macaque) continue;
                    try { m.setPackSitting(false); } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }

    @Override
    public void stop() {
        try { this.macaque.getNavigation().stop(); } catch (Throwable ignored) {}
        // when pack wandering stops, choose half of nearby adults to physically sit and mark pack as sitting
        try {
            var list = this.macaque.level().getEntitiesOfClass(MacaqueEntity.class, this.macaque.getBoundingBox().inflate(this.radius, 2.0D, this.radius));
            java.util.List<MacaqueEntity> adults = new java.util.ArrayList<>();
            for (MacaqueEntity m : list) {
                if (m == this.macaque) continue;
                try { if (m.isBaby()) continue; } catch (Throwable ignored) { continue; }
                adults.add(m);
            }
            if (!adults.isEmpty()) {
                // shuffle and pick half to sit
                java.util.Collections.shuffle(adults, this.rand);
                int toSit = (adults.size() + 1) / 2; // round up
                for (int i = 0; i < adults.size(); i++) {
                    MacaqueEntity m = adults.get(i);
                    try {
                        m.setPackSitting(true);
                        if (i < toSit) {
                            m.setSitting(true);
                            m.getNavigation().stop();
                        } else {
                            m.setSitting(false);
                        }
                    } catch (Throwable ignored) {}
                }
            }
            // ensure this macaque is also in pack sitting
            try { this.macaque.setPackSitting(true); } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
        // set a small roam cooldown so the same macaque doesn't immediately roam again
        try { this.roamCooldown = 100 + this.rand.nextInt(200); } catch (Throwable ignored) {}
    }

    @Override
    public void tick() {
        try {
            if (this.roamTicks > 0) {
                this.roamTicks--;
                // if navigation is idle, pick another nearby random point to continue roaming
                try {
                    if (this.macaque.getNavigation().isDone()) {
                        double tx = this.macaque.getX() + (this.macaque.getRandom().nextDouble() * 2.0D - 1.0D) * this.radius;
                        double tz = this.macaque.getZ() + (this.macaque.getRandom().nextDouble() * 2.0D - 1.0D) * this.radius;
                        double ty = this.macaque.getY();
                        this.macaque.getNavigation().moveTo(tx, ty, tz, this.speed);
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }
}
