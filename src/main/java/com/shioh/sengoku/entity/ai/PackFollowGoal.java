package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.entity.MacaqueEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

import java.util.EnumSet;

/**
 * Simple pack-follow goal: when a nearby adult macaque is moving, follow them so macaques travel in groups.
 * This is intentionally conservative: it only activates if a moving conspecific is found within a radius,
 * and uses a low movement speed to avoid disrupting other goals.
 */
public class PackFollowGoal extends Goal {
    private final MacaqueEntity macaque;
    private Mob leader;
    private final double speed;
    private final int searchRadius;

    public PackFollowGoal(MacaqueEntity macaque, double speed, int searchRadius) {
        this.macaque = macaque;
        this.speed = speed;
        this.searchRadius = searchRadius;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        try {
            if (this.macaque.isBaby()) return false; // babies follow parent via FollowParentGoal
            if (this.macaque.isSitting()) return false;
            // find nearest adult macaque that is moving and is not this
            var level = this.macaque.level();
            var pos = this.macaque.blockPosition();
            double bestDist = Double.MAX_VALUE;
            Mob best = null;
            for (var ent : level.getEntitiesOfClass(MacaqueEntity.class, this.macaque.getBoundingBox().inflate(this.searchRadius, 2.0D, this.searchRadius))) {
                if (ent == this.macaque) continue;
                MacaqueEntity m = (MacaqueEntity) ent;
                if (m.isBaby()) continue;
                if (m.isSitting()) continue;
                // prefer ones that are actually moving (nav not done)
                try {
                    if (m.getNavigation().isDone()) continue;
                } catch (Throwable ignored) {}
                double dx = m.getX() - this.macaque.getX();
                double dz = m.getZ() - this.macaque.getZ();
                double d = dx*dx + dz*dz;
                if (d < bestDist) { bestDist = d; best = m; }
            }
            if (best != null) {
                this.leader = best;
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.leader == null) return false;
        if (!this.leader.isAlive()) return false;
        if (this.macaque.isSitting()) return false;
        double dx = this.leader.getX() - this.macaque.getX();
        double dz = this.leader.getZ() - this.macaque.getZ();
        double distSq = dx*dx + dz*dz;
        return distSq <= (this.searchRadius+4)*(this.searchRadius+4);
    }

    @Override
    public void start() {
        // mark follower and leader as wandering so pack enters WANDER state
        try {
            if (this.leader != null) {
                if (this.leader instanceof MacaqueEntity) {
                    try { ((MacaqueEntity)this.leader).setPackSitting(false); } catch (Throwable ignored) {}
                }
            }
            try { this.macaque.setPackSitting(false); } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }

    @Override
    public void stop() {
        this.leader = null;
        try { this.macaque.getNavigation().stop(); } catch (Throwable ignored) {}
        // when following stops, let this macaque return to pack sit state
        try { this.macaque.setPackSitting(true); } catch (Throwable ignored) {}
    }

    @Override
    public void tick() {
        if (this.leader == null) return;
        try {
            PathNavigation nav = this.macaque.getNavigation();
            double lx = this.leader.getX();
            double ly = this.leader.getY();
            double lz = this.leader.getZ();
            // if too close, do nothing
            double dx = lx - this.macaque.getX();
            double dz = lz - this.macaque.getZ();
            double distSq = dx*dx + dz*dz;
            if (distSq > 4.0D) {
                // try to follow leader position
                try { nav.moveTo(lx, ly, lz, this.speed); } catch (Throwable t) {}
            } else {
                try { nav.stop(); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }
}
