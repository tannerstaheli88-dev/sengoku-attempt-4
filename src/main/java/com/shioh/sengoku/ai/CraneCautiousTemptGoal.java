package com.shioh.sengoku.ai;

import com.shioh.sengoku.entity.RedCrownedCraneEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * Parrot-like cautious tempt/flee goal for the Red-Crowned Crane.
 *
 * Behavior (mirrors `ParrotCautiousTemptGoal`):
 * - Activates when a player is within `temptDistance`.
 * - If the player is close and moving too fast, the crane will flee using
 *   the entity's `fleeAwayFrom(Player)` helper (short flight and landing).
 * - If the player holds tempting food and is moving slowly, the crane will
 *   allow approach; otherwise it will keep distance or flee.
 */
public class CraneCautiousTemptGoal extends Goal {
    private final RedCrownedCraneEntity crane;
    private Player targetPlayer;
    private final double fleeSpeed;
    private final double approachSpeed;
    private final double fleeDistance;
    private final double temptDistance;
    private int delayCounter;

    // Speed thresholds
    private static final double SLOW_APPROACH_THRESHOLD = 0.08D;
    private static final double RUN_SPEED_THRESHOLD = 0.09D; // horizontal speed to consider "running" (includes mounts)
    private static final double HORSE_MOVE_THRESHOLD = 0.02D; // minimal horizontal speed to treat a ridden horse as "moving"

    public CraneCautiousTemptGoal(RedCrownedCraneEntity crane, double fleeSpeed, double approachSpeed) {
        this.crane = crane;
        this.fleeSpeed = fleeSpeed;
        this.approachSpeed = approachSpeed;
        this.fleeDistance = 7.0D;   // mirror parrot's skittish distance
        this.temptDistance = 12.0D; // detection distance
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    /**
     * Backwards-compatible constructor: keep single-double usage (approachSpeed)
     * by supplying a sensible default flee speed.
     */
    public CraneCautiousTemptGoal(RedCrownedCraneEntity crane, double approachSpeed) {
        this(crane, 1.4D, approachSpeed);
    }

    @Override
    public boolean canUse() {
        if (this.crane == null || this.crane.level() == null) return false;
        // Don't activate while crane is in active controlled flight
        try { if (this.crane.isFlying()) return false; } catch (Throwable ignored) {}

        this.targetPlayer = this.crane.level().getNearestPlayer(this.crane, this.temptDistance);
        if (this.targetPlayer == null) return false;
        if (this.targetPlayer.isCreative()) return false;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPlayer == null || !this.targetPlayer.isAlive()) return false;
        if (this.targetPlayer.isCreative()) return false;
        try { if (this.crane.isFlying()) return false; } catch (Throwable ignored) {}

        return this.crane.distanceToSqr(this.targetPlayer) <= this.temptDistance * this.temptDistance;
    }

    @Override
    public void start() {
        this.delayCounter = 0;
    }

    @Override
    public void stop() {
        this.targetPlayer = null;
        try { this.crane.getNavigation().stop(); } catch (Throwable ignored) {}
    }

    @Override
    public void tick() {
        if (this.targetPlayer == null) return;

        // Scan nearby entities (within 3 blocks) for any fast-moving entity
        // and flee from it — mirrors armadillo behavior for high-velocity entities.
        List<Entity> nearby = this.crane.level().getEntities(this.crane, this.crane.getBoundingBox().inflate(3.0D), e -> e != this.crane && e.isAlive());
        for (Entity ent : nearby) {
            double hs = getEntityHorizontalSpeed(ent);
            if (hs > RUN_SPEED_THRESHOLD) {
                try { this.crane.fleeAwayFrom(ent); } catch (Throwable ignored) { try { this.crane.getNavigation().stop(); } catch (Throwable ignored2) {} }
                return;
            }
        }

        try { this.crane.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F); } catch (Throwable ignored) {}

        double distanceSq = this.crane.distanceToSqr(this.targetPlayer);
        double distance = Math.sqrt(distanceSq);

        boolean hasFood = isTemptingItem(this.targetPlayer.getMainHandItem()) || isTemptingItem(this.targetPlayer.getOffhandItem());

        double playerSpeed = getPlayerHorizontalSpeed(this.targetPlayer);
        boolean playerMovingSlow = playerSpeed < SLOW_APPROACH_THRESHOLD || this.targetPlayer.isCrouching();
        boolean playerBeingGentle = hasFood && playerMovingSlow;
        // Treat ridden horses specially: if the vehicle is a Horse and is moving above the threshold,
        // always consider it a running approach so the crane flees.
        Entity vehicle = this.targetPlayer.getVehicle();
        boolean horseMoving = false;
        if (vehicle instanceof Horse) {
            double vx = vehicle.getDeltaMovement().x;
            double vz = vehicle.getDeltaMovement().z;
            double vhs = Math.sqrt(vx * vx + vz * vz);
            horseMoving = vhs > HORSE_MOVE_THRESHOLD;
        }
        boolean playerIsSprinting = this.targetPlayer.isSprinting() || playerSpeed > RUN_SPEED_THRESHOLD || horseMoving;

        // If player is very close and sprinting, trigger short flee flight
        if (distance < this.fleeDistance && playerIsSprinting) {
            try { this.crane.fleeAwayFrom(this.targetPlayer); } catch (Throwable ignored) { try { this.crane.getNavigation().stop(); } catch (Throwable ignored2) {} }
            return;
        }

        // If player is being gentle and has food, allow approach
        if (playerBeingGentle) {
            if (distance > 3.0D) {
                if (this.delayCounter++ > 20) {
                    this.delayCounter = 0;
                    try { this.crane.getNavigation().moveTo(this.targetPlayer, this.approachSpeed * 0.5D); } catch (Throwable ignored) {}
                }
            } else if (distance > 1.5D) {
                if (this.delayCounter++ > 40) {
                    this.delayCounter = 0;
                    try { this.crane.getNavigation().stop(); } catch (Throwable ignored) {}
                }
            } else {
                try { this.crane.getNavigation().stop(); } catch (Throwable ignored) {}
            }
            return;
        }

        // If player is too close and sprinting (no food), flee using flight helper
        if (distance < this.fleeDistance && playerIsSprinting) {
            try { this.crane.fleeAwayFrom(this.targetPlayer); } catch (Throwable ignored) { try { this.crane.getNavigation().stop(); } catch (Throwable ignored2) {} }
            return;
        }

        // Otherwise do nothing special — keep watching the player
        try { this.crane.getNavigation().stop(); } catch (Throwable ignored) {}
    }

    private double getPlayerHorizontalSpeed(Player p) {
        if (p == null) return 0.0D;
        try {
            Entity vehicle = p.getVehicle();
            Vec3 vel;
            if (vehicle != null) {
                vel = vehicle.getDeltaMovement();
            } else {
                vel = p.getDeltaMovement();
            }
            return Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        } catch (Throwable ignored) {
            Vec3 v = p.getDeltaMovement();
            return Math.sqrt(v.x * v.x + v.z * v.z);
        }
    }

    private double getEntityHorizontalSpeed(Entity e) {
        if (e == null) return 0.0D;
        try {
            Entity vehicle = e.getVehicle();
            Vec3 vel = vehicle != null ? vehicle.getDeltaMovement() : e.getDeltaMovement();
            return Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        } catch (Throwable ignored) {
            Vec3 v = e.getDeltaMovement();
            return Math.sqrt(v.x * v.x + v.z * v.z);
        }
    }

    private boolean isTemptingItem(ItemStack stack) {
        // Crane is tempted by raw fish for breeding: cod or salmon
        return stack.is(Items.COD) || stack.is(Items.SALMON);
    }
}
