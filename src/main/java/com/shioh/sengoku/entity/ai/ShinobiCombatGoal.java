package com.shioh.sengoku.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Composite combat goal for the shinobi-reimagined Illusioner.
 *
 * Switches between close-quarters melee (AdvancedMeleeAttackGoal) and
 * ranged kunai throwing (IllusionerKunaiAttackGoal) depending on distance
 * to the target and a periodic mode timer.
 *
 * - Within ENGAGE_MELEE_DIST  : always switches to MELEE
 * - Beyond DISENGAGE_DIST     : always switches to RANGED
 * - Mid-range                 : sticks with current mode until the timer
 *                               expires, then randomly picks a new mode
 */
public class ShinobiCombatGoal extends Goal {

    private enum CombatMode { MELEE, RANGED }

    // Distance thresholds (blocks)
    private static final double ENGAGE_MELEE_DIST = 5.0;   // switch to melee within this range
    private static final double DISENGAGE_DIST    = 10.0;  // switch to ranged beyond this range

    // How long to hold a mode before reconsidering in mid-range (ticks)
    private static final int MODE_DURATION_MIN = 160;
    private static final int MODE_DURATION_MAX = 320;

    private final Mob shinobi;
    private final AdvancedMeleeAttackGoal meleeGoal;
    private final IllusionerKunaiAttackGoal kunaiGoal;

    private CombatMode currentMode = CombatMode.RANGED;
    private int modeSwitchTimer = 0;

    /** UUIDs of mobs that should be forced into RANGED mode on next evaluateMode() tick. */
    private static final Set<UUID> FORCE_RANGED_ENTITIES = new HashSet<>();

    /** Called externally (e.g. after a teleport) to guarantee a switch to ranged on next tick. */
    public static void requestRangedMode(Mob mob) {
        FORCE_RANGED_ENTITIES.add(mob.getUUID());
    }

    public ShinobiCombatGoal(Mob shinobi) {
        this.shinobi = shinobi;
        this.meleeGoal  = new AdvancedMeleeAttackGoal(shinobi, 0.65D, false);
        this.kunaiGoal  = new IllusionerKunaiAttackGoal(shinobi, 0.55D, 40, 20.0F);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = shinobi.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = shinobi.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        currentMode = chooseModeFromDistance();
        modeSwitchTimer = randomDuration();
        startSubGoal(currentMode);
    }

    @Override
    public void stop() {
        stopSubGoal(currentMode);
    }

    @Override
    public void tick() {
        LivingEntity target = shinobi.getTarget();
        if (target == null) return;

        if (modeSwitchTimer > 0) modeSwitchTimer--;

        CombatMode desired = evaluateMode(target);
        if (desired != currentMode) {
            stopSubGoal(currentMode);
            currentMode = desired;
            modeSwitchTimer = randomDuration();
            startSubGoal(currentMode);
        }

        if (currentMode == CombatMode.MELEE) {
            meleeGoal.tick();
        } else {
            kunaiGoal.tick();
        }
    }

    // ---- helpers ----

    private CombatMode evaluateMode(LivingEntity target) {
        double distSq = shinobi.distanceToSqr(target);

        // Forced ranged (e.g. after a teleport)
        if (FORCE_RANGED_ENTITIES.remove(shinobi.getUUID())) {
            modeSwitchTimer = randomDuration(); // hold ranged for a full duration
            kunaiGoal.forceImmediateAttack();   // fire one kunai straight away
            return CombatMode.RANGED;
        }

        // Hard distance overrides
        if (distSq < ENGAGE_MELEE_DIST * ENGAGE_MELEE_DIST) {
            return CombatMode.MELEE;
        }
        if (distSq > DISENGAGE_DIST * DISENGAGE_DIST) {
            return CombatMode.RANGED;
        }

        // Mid-range: hold current mode until timer expires, then randomly pick
        if (modeSwitchTimer <= 0) {
            return shinobi.getRandom().nextBoolean() ? CombatMode.MELEE : CombatMode.RANGED;
        }
        return currentMode;
    }

    private CombatMode chooseModeFromDistance() {
        LivingEntity target = shinobi.getTarget();
        if (target == null) return CombatMode.RANGED;
        double distSq = shinobi.distanceToSqr(target);
        return distSq < ENGAGE_MELEE_DIST * ENGAGE_MELEE_DIST ? CombatMode.MELEE : CombatMode.RANGED;
    }

    private void startSubGoal(CombatMode mode) {
        if (mode == CombatMode.MELEE) meleeGoal.start();
        else                          kunaiGoal.start();
    }

    private void stopSubGoal(CombatMode mode) {
        if (mode == CombatMode.MELEE) meleeGoal.stop();
        else                          kunaiGoal.stop();
    }

    private int randomDuration() {
        return MODE_DURATION_MIN + shinobi.getRandom().nextInt(MODE_DURATION_MAX - MODE_DURATION_MIN + 1);
    }
}
