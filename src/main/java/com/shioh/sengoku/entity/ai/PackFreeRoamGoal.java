package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.entity.MacaqueEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.Random;

/**
 * PackFreeRoamGoal: allows an individual macaque to freely roam within the pack
 * while the pack is in PACK_STATE_SIT. This does not interrupt grooming pairs.
 */
public class PackFreeRoamGoal extends Goal {
    private final MacaqueEntity macaque;
    private final double speed;
    private final int radius;
    private final Random rand = new Random();
    private int roamTicks = 0;

    public PackFreeRoamGoal(MacaqueEntity macaque, double speed, int radius) {
        this.macaque = macaque;
        this.speed = speed;
        this.radius = radius;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        try {
            if (!this.macaque.isPackSitting()) return false; // only during pack sit phase
            if (this.macaque.isSitting()) return false; // physically sitting should not roam
            if (this.macaque.isGrooming() || this.macaque.isBeingGroomed()) return false; // do not disturb grooming
            if (!this.macaque.getNavigation().isDone()) return false; // already moving
            // deterministic: if pack is sitting and this macaque is not physically sitting,
            // it should free-roam during the sit phase (no random chance).
            return true;
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        try {
            if (this.macaque.isSitting()) return false;
            if (this.macaque.isGrooming() || this.macaque.isBeingGroomed()) return false;
            return this.roamTicks > 0;
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public void start() {
        this.roamTicks = 20 * (5 + this.rand.nextInt(11)); // 5-15 seconds
        this.pickTargetAndMove();
    }

    private void pickTargetAndMove() {
        try {
            double tx = this.macaque.getX() + (this.macaque.getRandom().nextDouble() * 2.0D - 1.0D) * this.radius;
            double tz = this.macaque.getZ() + (this.macaque.getRandom().nextDouble() * 2.0D - 1.0D) * this.radius;
            double ty = this.macaque.getY();
            this.macaque.getNavigation().moveTo(tx, ty, tz, this.speed);
        } catch (Throwable ignored) {}
    }

    @Override
    public void tick() {
        try {
            this.roamTicks--;
            if (this.roamTicks > 0) {
                if (this.macaque.getNavigation().isDone()) pickTargetAndMove();
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void stop() {
        try { this.macaque.getNavigation().stop(); } catch (Throwable ignored) {}
        this.roamTicks = 0;
    }
}
