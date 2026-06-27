package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.entity.MacaqueEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simple sit goal: when idle, macaques prefer to sit for a while.
 */
public class SitGoal extends Goal {
    private final MacaqueEntity macaque;
    private int sitTime;
    private int sitCooldown; // ticks remaining before next sit allowed

    public SitGoal(MacaqueEntity macaque) {
        this.macaque = macaque;
        this.setFlags(EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE, net.minecraft.world.entity.ai.goal.Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.sitCooldown > 0) return false; // back-off after a recent sit
        if (!this.macaque.onGround()) return false;
        if (this.macaque.isInWater() || this.macaque.isInLava()) return false;
        if (this.macaque.getNavigation().isDone()) {
            // High chance to sit when idle — makes them lazy.
            return ThreadLocalRandom.current().nextFloat() < 0.45F && !this.macaque.isSitting();
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.macaque.isSitting() && this.sitTime > 0;
    }

    @Override
    public void start() {
        // Stop movement and sit for a while (ticks)
        this.sitTime = 20 * (10 + ThreadLocalRandom.current().nextInt(41)); // 10-50 seconds (longer sits)
        this.macaque.getNavigation().stop();
        // zero velocity and disable gravity while sitting to create a stable "sit physics" feel
        this.macaque.setDeltaMovement(Vec3.ZERO);
        this.macaque.setNoGravity(true);
        this.macaque.setSitting(true);
        // signal pack sit state and choose half of nearby adults to actually sit
        try {
            int radius = 12;
            var list = this.macaque.level().getEntitiesOfClass(MacaqueEntity.class, this.macaque.getBoundingBox().inflate(radius, 2.0D, radius));
            java.util.List<MacaqueEntity> adults = new java.util.ArrayList<>();
            for (MacaqueEntity m : list) {
                if (m == this.macaque) continue;
                try { if (m.isBaby()) continue; } catch (Throwable ignored) { continue; }
                adults.add(m);
            }
            if (!adults.isEmpty()) {
                java.util.Collections.shuffle(adults);
                int toSit = (adults.size() + 1) / 2;
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
            try { this.macaque.setPackSitting(true); } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }

    @Override
    public void stop() {
        this.macaque.setSitting(false);
        // cooldown before next automatic sit (30-90 seconds)
        this.sitCooldown = 20 * (30 + ThreadLocalRandom.current().nextInt(61));
        // restore gravity in case it was disabled
        this.macaque.setNoGravity(false);
        // leaving a sit transitions this macaque (and nearby ones) to wander
        try { this.macaque.setPackSitting(false); } catch (Throwable ignored) {}
    }

    @Override
    public void tick() {
        if (this.sitTime > 0) this.sitTime--;
        if (this.sitCooldown > 0) this.sitCooldown--;

        // If the macaque starts moving or navigation is not done, stop sitting early.
        if (this.macaque.isSitting()) {
            if (!this.macaque.getNavigation().isDone()) {
                this.macaque.setSitting(false);
                this.macaque.setNoGravity(false);
            } else {
                // If entity has non-trivial motion, end sitting
                double dx = this.macaque.getDeltaMovement().x;
                double dy = this.macaque.getDeltaMovement().y;
                double dz = this.macaque.getDeltaMovement().z;
                if (dx*dx + dy*dy + dz*dz > 1.0E-6D) {
                    this.macaque.setSitting(false);
                    this.macaque.setNoGravity(false);
                }
            }
        }
    }
}
