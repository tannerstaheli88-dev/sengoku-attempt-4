package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.entity.MacaqueEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

import java.util.EnumSet;
import java.util.Random;

/**
 * PlayWithPackGoal: occasionally chase a nearby macaque for a short playful burst.
 */
public class PlayWithPackGoal extends Goal {
    private final MacaqueEntity macaque;
    private MacaqueEntity target;
    private int playTicks = 0;
    private final int maxPlayTicks = 60; // ~3 seconds
    private final int cooldownMax = 200; // cooldown between plays
    private int cooldown = 0;
    private final double speed;
    private final int searchRadius;

    public PlayWithPackGoal(MacaqueEntity macaque, double speed, int searchRadius) {
        this.macaque = macaque;
        this.speed = speed;
        this.searchRadius = searchRadius;
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.cooldown = new Random().nextInt(this.cooldownMax);
    }

    @Override
    public boolean canUse() {
        try {
            if (this.cooldown > 0) { this.cooldown--; return false; }
            if (this.macaque.isSitting()) return false;
            var level = this.macaque.level();
            for (var e : level.getEntitiesOfClass(MacaqueEntity.class, this.macaque.getBoundingBox().inflate(this.searchRadius, 1.5D, this.searchRadius))) {
                if (e == this.macaque) continue;
                MacaqueEntity m = (MacaqueEntity) e;
                // prefer ones not currently being played with
                if (m.isPlaying()) continue;
                this.target = m;
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public void start() {
        this.playTicks = 0;
        try { this.macaque.setPlaying(true); } catch (Throwable ignored) {}
        try { if (this.target != null) this.target.setPlaying(true); } catch (Throwable ignored) {}
    }

    @Override
    public boolean canContinueToUse() {
        if (this.target == null) return false;
        if (!this.target.isAlive()) return false;
        if (this.playTicks >= this.maxPlayTicks) return false;
        double dx = this.target.getX() - this.macaque.getX();
        double dz = this.target.getZ() - this.macaque.getZ();
        double distSq = dx*dx + dz*dz;
        return distSq <= (this.searchRadius+4)*(this.searchRadius+4);
    }

    @Override
    public void tick() {
        this.playTicks++;
        try {
            PathNavigation nav = this.macaque.getNavigation();
            double tx = this.target.getX();
            double ty = this.target.getY();
            double tz = this.target.getZ();
            if (this.macaque.distanceToSqr(this.target) > 2.0D) {
                try { nav.moveTo(tx, ty, tz, this.speed); } catch (Throwable ignored) {}
            } else {
                try { nav.stop(); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        if (this.playTicks >= this.maxPlayTicks) this.cooldown = this.cooldownMax + this.macaque.getRandom().nextInt(this.cooldownMax);
    }

    @Override
    public void stop() {
        try { this.macaque.setPlaying(false); } catch (Throwable ignored) {}
        try { if (this.target != null) this.target.setPlaying(false); } catch (Throwable ignored) {}
        this.target = null;
        this.playTicks = 0;
    }
}
