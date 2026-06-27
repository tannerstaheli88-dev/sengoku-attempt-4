package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.entity.MacaqueEntity;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

/**
 * MountLlamaGoal: occasionally find a nearby llama and mount it for a short ride.
 */
public class MountLlamaGoal extends Goal {
    private final MacaqueEntity macaque;
    private Llama targetLlama;
    private int cooldown = 0;
    private int mountTicks = 0;
    private final int maxMountTicks;
    private final double approachSpeed;
    private final double rideDurationChance; // chance each opportunity to attempt a mount
    private final double searchRadius;

    public MountLlamaGoal(MacaqueEntity macaque, double approachSpeed, double searchRadius, int maxMountTicks, double rideChance) {
        this.macaque = macaque;
        this.maxMountTicks = maxMountTicks;
        this.approachSpeed = approachSpeed;
        this.rideDurationChance = rideChance;
        this.searchRadius = searchRadius;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        try {
            if (this.macaque.isBaby()) return false;
            if (this.macaque.isGrooming() || this.macaque.isBeingGroomed()) return false;
            if (this.macaque.isVehicle()) return false;
            if (this.cooldown > 0) return false;
            if (this.macaque.level().isClientSide()) return false;

            // small random chance to attempt mounting when an eligible llama is nearby
            if (this.macaque.getRandom().nextDouble() > this.rideDurationChance) return false;

            List<Llama> nearby = this.macaque.level().getEntitiesOfClass(Llama.class, this.macaque.getBoundingBox().inflate(this.searchRadius, 2.0D, this.searchRadius));
            Llama best = null;
            double bestDist = Double.MAX_VALUE;
            for (Llama l : nearby) {
                if (!l.isAlive()) continue;
                // mapping: use getFirstPassenger() to check if llama already has a rider
                if (l.getFirstPassenger() != null) continue;
                double dx = l.getX() - this.macaque.getX();
                double dz = l.getZ() - this.macaque.getZ();
                double d2 = dx*dx + dz*dz;
                if (d2 < bestDist) { best = l; bestDist = d2; }
            }
            if (best == null) return false;
            this.targetLlama = best;
            return true;
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public void start() {
        this.mountTicks = 0;
        if (this.targetLlama != null) {
            try { this.macaque.getNavigation().moveTo(this.targetLlama, this.approachSpeed); } catch (Throwable ignored) {}
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetLlama == null) return false;
        if (!this.targetLlama.isAlive()) return false;
        if (this.macaque.isVehicle()) return true; // while riding keep goal
        // continue while still pathing to the llama
        try { return !this.macaque.getNavigation().isDone(); } catch (Throwable ignored) { return false; }
    }

    @Override
    public void tick() {
        if (this.cooldown > 0) this.cooldown--;
        this.mountTicks++;
        try {
            if (this.targetLlama == null) return;
            if (!this.macaque.isVehicle()) {
                // if close enough, mount
                double dx = this.targetLlama.getX() - this.macaque.getX();
                double dz = this.targetLlama.getZ() - this.macaque.getZ();
                double distSq = dx*dx + dz*dz;
                if (distSq <= 2.25D) {
                    this.macaque.startRiding(this.targetLlama, true);
                } else {
                    // continue moving toward llama
                    try { this.macaque.getNavigation().moveTo(this.targetLlama, this.approachSpeed); } catch (Throwable ignored) {}
                }
            } else {
                // currently riding — dismount after maxMountTicks
                if (this.mountTicks >= this.maxMountTicks) {
                    try { this.macaque.stopRiding(); } catch (Throwable ignored) {}
                    this.cooldown = 20 * 30; // 30s cooldown
                    this.targetLlama = null;
                }
            }
            // fail-safe: if mount attempt takes too long, give up
            if (this.mountTicks > 20 * 20) { // 20s
                try { if (!this.macaque.isVehicle()) this.macaque.getNavigation().stop(); } catch (Throwable ignored) {}
                this.cooldown = 20 * 10;
                this.targetLlama = null;
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void stop() {
        this.targetLlama = null;
    }
}
