package com.shioh.sengoku.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Ninja-style ambush AI for the shinobi Illusioner.
 *
 * STALK phase — No combat target is set.
 *   Navigates quietly to keep 10–18 blocks from the player, strongly
 *   preferring the player's rear arc (behind their back) so they go
 *   unnoticed. Updates its shadow position every ~2 seconds.
 *
 * Trigger condition — Spotted or stumbled upon:
 *   a) The player's look direction is within SPOT_HALF_ANGLE of the
 *      shinobi AND there is line of sight.
 *   b) The player walks within PROXIMITY_TRIGGER blocks (almost on top
 *      of the shinobi in a bush / tree).
 *
 * POUNCE phase — Target is set, ShinobiCombatGoal takes over next tick.
 *   Brief window (~30 ticks) where the shinobi sprints directly at the
 *   player with a small jumping velocity kick so it looks like it leaps
 *   from cover.
 *
 * Only one shinobi per player stalks at a time (global tracker).
 */
public class ShinobiAmbushGoal extends Goal {

    // ------------------------------------------------------------------
    // Global tracker — one shinobi stalks each player at a time
    // ------------------------------------------------------------------
    private static final Map<UUID, UUID> PLAYER_STALKER = new HashMap<>();

    // ------------------------------------------------------------------
    // Tuning constants
    // ------------------------------------------------------------------
    /** Maximum range to start stalking. */
    private static final double DETECT_RANGE = 40.0;
    /** Ideal stalking distance — shinobi tries to stay in this ring. */
    private static final double STALK_MIN = 10.0;
    private static final double STALK_MAX = 18.0;
    /** Back off if within this distance (got too close). */
    private static final double BACK_OFF_DIST = 8.0;
    /** Instantly pounce if player actually walks this close. */
    private static final double PROXIMITY_TRIGGER = 3.5;
    /** Half-angle (cos value) of the player's front cone that counts as
     *  "spotted".  cos(55°) ≈ 0.574 */
    private static final double SPOT_COS = 0.574;
    /** How often (ticks) to re-evaluate the shadow waypoint. */
    private static final int SHADOW_RECALC_INTERVAL = 40;
    /** Pounce lasts this many ticks before the goal stops. */
    private static final int POUNCE_DURATION = 30;
    /** Stalk movement speed. */
    private static final double STALK_SPEED = 0.58;
    /** Pounce navigation speed. */
    private static final double POUNCE_SPEED = 1.6;

    // ------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------
    private enum Phase { STALK, POUNCE }

    private final Illusioner shinobi;
    private Player prey;
    private Phase phase = Phase.STALK;
    private int pounceTicks = 0;
    private int shadowRecalcTimer = 0;

    public ShinobiAmbushGoal(Illusioner shinobi) {
        this.shinobi = shinobi;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // ------------------------------------------------------------------
    // Goal lifecycle
    // ------------------------------------------------------------------

    @Override
    public boolean canUse() {
        // Only stalk when not already in combat
        if (shinobi.getTarget() != null) return false;

        prey = findPrey();
        if (prey == null) return false;

        // Enforce one-stalker-per-player
        UUID pid = prey.getUUID();
        UUID claimedBy = PLAYER_STALKER.get(pid);
        if (claimedBy != null && !claimedBy.equals(shinobi.getUUID())) {
            // Check if the registered stalker is still alive nearby
            boolean stillActive = !shinobi.level().getEntitiesOfClass(
                Illusioner.class,
                shinobi.getBoundingBox().inflate(64.0),
                e -> e.getUUID().equals(claimedBy) && e.isAlive()
            ).isEmpty();
            if (stillActive) return false;
            PLAYER_STALKER.remove(pid);
        }
        PLAYER_STALKER.put(pid, shinobi.getUUID());
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (shinobi.getTarget() != null) return false; // combat took over
        if (prey == null || !prey.isAlive()) return false;
        if (prey.isCreative() || prey.isSpectator()) return false;
        // POUNCE phase: let it finish its timer
        if (phase == Phase.POUNCE) return pounceTicks > 0;
        return true;
    }

    @Override
    public void start() {
        phase = Phase.STALK;
        pounceTicks = 0;
        shadowRecalcTimer = 0;
        shinobi.setAggressive(false);
    }

    @Override
    public void stop() {
        releaseClaim();
        prey = null;
        shinobi.getNavigation().stop();
        shinobi.setAggressive(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() { return true; }

    @Override
    public void tick() {
        if (prey == null) return;
        shinobi.getLookControl().setLookAt(prey, 20.0F, (float) shinobi.getMaxHeadXRot());

        if (phase == Phase.STALK) {
            tickStalk();
        } else {
            tickPounce();
        }
    }

    // ------------------------------------------------------------------
    // STALK
    // ------------------------------------------------------------------

    private void tickStalk() {
        double distSq = shinobi.distanceToSqr(prey);

        // Check spotted conditions first
        if (isSpotted() || distSq < PROXIMITY_TRIGGER * PROXIMITY_TRIGGER) {
            beginPounce();
            return;
        }

        double dist = Math.sqrt(distSq);

        if (dist > STALK_MAX) {
            // Too far — move toward shadow position (behind prey)
            if (--shadowRecalcTimer <= 0) {
                shadowRecalcTimer = SHADOW_RECALC_INTERVAL;
                Vec3 shadow = computeShadowPos(14.0);
                if (shadow != null) {
                    shinobi.getNavigation().moveTo(shadow.x, shadow.y, shadow.z, STALK_SPEED);
                }
            }
        } else if (dist < BACK_OFF_DIST) {
            // Too close — slip away toward the player's side / rear
            shadowRecalcTimer = 0;
            Vec3 retreat = computeShadowPos(STALK_MIN + 2.0);
            if (retreat != null) {
                shinobi.getNavigation().moveTo(retreat.x, retreat.y, retreat.z, STALK_SPEED * 0.8);
            } else {
                // Fallback: just move directly away
                Vec3 away = prey.position().subtract(shinobi.position()).normalize().scale(-STALK_MIN);
                shinobi.getNavigation().moveTo(
                    shinobi.getX() + away.x, shinobi.getY() + away.y, shinobi.getZ() + away.z,
                    STALK_SPEED * 0.8
                );
            }
        } else {
            // Inside the sweet spot — hold still, wait in the shadows
            shinobi.getNavigation().stop();
            shadowRecalcTimer = 0; // recalc immediately next time we need to move
        }
    }

    // ------------------------------------------------------------------
    // POUNCE
    // ------------------------------------------------------------------

    private void beginPounce() {
        phase = Phase.POUNCE;
        pounceTicks = POUNCE_DURATION;
        shinobi.setAggressive(true);

        // Set the player as the combat target so ShinobiCombatGoal can pick up next
        shinobi.setTarget(prey);

        // Velocity kick — leap toward the player
        Vec3 dir = prey.position()
            .add(0, prey.getBbHeight() * 0.5, 0)
            .subtract(shinobi.position())
            .normalize();
        shinobi.setDeltaMovement(dir.x * 0.75, 0.38, dir.z * 0.75);

        // Play a kiai / battle cry sound
        shinobi.level().playSound(
            null,
            shinobi.getX(), shinobi.getY(), shinobi.getZ(),
            SoundEvents.VINDICATOR_CELEBRATE,
            shinobi.getSoundSource(),
            1.0F, 0.85F + shinobi.getRandom().nextFloat() * 0.3F
        );
    }

    private void tickPounce() {
        if (pounceTicks > 0) {
            pounceTicks--;
            // Drive navigation toward prey at sprint speed during pounce
            shinobi.getNavigation().moveTo(prey, POUNCE_SPEED);
        }
        // When timer expires, canContinueToUse returns false → goal stops
        // ShinobiCombatGoal (priority 1) picks up since target is now set
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Returns true if the prey's eye is within SPOT_COS of their look
     * direction (i.e. they're facing the shinobi) AND there is line of sight.
     */
    private boolean isSpotted() {
        Vec3 look = prey.getLookAngle();
        Vec3 toShinobi = new Vec3(
            shinobi.getX() - prey.getX(),
            shinobi.getEyeY() - prey.getEyeY(),
            shinobi.getZ() - prey.getZ()
        ).normalize();
        double dot = look.dot(toShinobi);
        if (dot < SPOT_COS) return false;
        // Line of sight (symmetric ray-cast)
        return shinobi.getSensing().hasLineOfSight(prey);
    }

    /**
     * Computes a target position ~{@code dist} blocks behind the player
     * (in their rear arc, ±40° random spread so the shinobi doesn't always
     * sit in exactly the same spot).
     */
    private Vec3 computeShadowPos(double dist) {
        // Player's rear direction = yaw + 180°
        double baseAngle = Math.toRadians(prey.getYRot() + 180.0);
        // Small random spread to vary the flanking angle
        double spread = (shinobi.getRandom().nextDouble() - 0.5) * Math.toRadians(80.0);
        double angle = baseAngle + spread;

        double tx = prey.getX() + Math.cos(angle) * dist;
        double tz = prey.getZ() + Math.sin(angle) * dist;
        double ty = prey.getY();

        // Snap to ground if possible
        net.minecraft.core.BlockPos bp = net.minecraft.core.BlockPos.containing(tx, ty, tz);
        for (int dy = 0; dy >= -3; dy--) {
            net.minecraft.core.BlockPos check = bp.offset(0, dy, 0);
            if (!shinobi.level().getBlockState(check).isAir()
                && shinobi.level().getBlockState(check.above()).isAir()) {
                ty = check.getY() + 1;
                break;
            }
        }

        return new Vec3(tx, ty, tz);
    }

    private Player findPrey() {
        return shinobi.level().getNearestPlayer(
            shinobi.getX(), shinobi.getY(), shinobi.getZ(),
            DETECT_RANGE,
            p -> p instanceof Player pl && !pl.isSpectator() && !pl.isCreative()
        );
    }

    private void releaseClaim() {
        if (prey == null) return;
        UUID pid = prey.getUUID();
        UUID claimed = PLAYER_STALKER.get(pid);
        if (claimed != null && claimed.equals(shinobi.getUUID())) {
            PLAYER_STALKER.remove(pid);
        }
    }
}
