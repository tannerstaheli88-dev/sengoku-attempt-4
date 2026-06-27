package com.shioh.sengoku.entity.ai;

import java.util.EnumSet;
import java.util.List;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import com.shioh.sengoku.util.EntityVisibility;
import com.shioh.sengoku.entity.GoryoEntity;
import com.shioh.sengoku.entity.KobayakawaSamuraiEntity;
import com.shioh.sengoku.entity.RoninEntity;
import com.shioh.sengoku.entity.SatomiSamuraiEntity;
import com.shioh.sengoku.entity.ShinobiLordEntity;
import com.shioh.sengoku.entity.TakedaSamuraiEntity;
import com.shioh.sengoku.entity.UmiNyoboEntity;
import com.shioh.sengoku.entity.WarlordEntity;

/**
 * evil fucked up combat system that is not clunky at all
 * Duel-style melee goal implementing spaced, readable combat phases:
 * GUARD -> PRESSURE -> WINDUP -> RETREAT -> RECOVERY -> GUARD
 * Maintains spacing ring (3.2–4.8 blocks); only breaches inward late in windup.
 */
public class AdvancedMeleeAttackGoal extends Goal {

    private enum Phase { GUARD, PRESSURE, WINDUP, RETREAT, RECOVERY }

    private final Mob mob;
    private final double speedModifier; // retained for legacy constructor compatibility
    private final boolean followingTargetEvenIfNotSeen; // unused but kept for existing calls
    private Phase phase = Phase.GUARD;

    // Spacing parameters — very close melee engagement
    private static final double PREFERRED_MIN = 0.5D;
    private static final double PREFERRED_MAX = 1.5D;
    private static final double BREACH_DISTANCE = 1.8D;
    private static final double HARD_MIN_DISTANCE = 0.3D;

    // Windup / timing
    private int windupRemaining = 0;
    private int windupTotal = 0;
    // Keep at least a 1-tick lead so the sweep telegraph can precede the hit consistently.
    private static final int WINDUP_MIN = 1;
    private static final int WINDUP_MAX = 14;
    private static final int SWEEP_LEAD_TICKS = 3;
    // Randomized attack behavior
    private boolean feint = false;
    private static final float QUICK_ATTACK_CHANCE = 0.12F; // small chance for fast strike
    private static final float FEINT_CHANCE = 0.0F; // chance to feint instead of committing, probably a stupid idea and may remove it

    private int retreatTicks = 0;
    private int recoveryTicks = 0;
    private int guardTicks = 0;
    private int pressureTicks = 0;
    private boolean sweepSoundPlayed = false; // true once the pre-strike telegraph fires; reset at each new windup
    private int attackCooldown = 0;
    private int attackVoiceCooldown = 0;
    private int pathUpdateTimer = 0; // throttle moveTo calls to avoid per-tick pathfinder recalculation
    private static final int PATH_UPDATE_INTERVAL = 10; // recalculate path at most every 10 ticks (vanilla standard)
    private int chainCount = 0; // how many attacks chained so far in current combo
    private int comboFollowupLimit = 0;
    private int smokeBombCooldown = 0;
    private boolean openingSmokeBombPending = false;
    private static final int MAX_CHAIN = 3; // up to 3 attacks in a chain
    private static final int GENERIC_COMBO_FOLLOWUPS_MIN = 1;
    private static final int GENERIC_COMBO_FOLLOWUPS_MAX = 4;
    private static final int GENERIC_CHAIN_WINDUP_MIN = 14;
    private static final int GENERIC_CHAIN_WINDUP_MAX = 20;
    private static final double MIN_STRIKE_REACH = 2.25D;
    private static final double STRIKE_REACH_PADDING = 0.75D;
    private static final int COOLDOWN_MIN = 5;
    private static final int COOLDOWN_MAX = 10;
    // Increased cooldown to reduce overlap with ambient/aggro ambient sounds
    private static final int ATTACK_VOICE_COOLDOWN_MIN = 20;
    private static final int ATTACK_VOICE_COOLDOWN_MAX = 100;
    private static final int SAMURAI_COOLDOWN_MIN = 5;
    private static final int SAMURAI_COOLDOWN_MAX = 10;
    private static final int SHINOBI_LORD_COOLDOWN_MIN = 5;
    private static final int SHINOBI_LORD_COOLDOWN_MAX = 10;
    private static final int WARLORD_PHASE_TWO_COOLDOWN_MIN = 2;
    private static final int WARLORD_PHASE_TWO_COOLDOWN_MAX = 5;
    private static final int SAMURAI_QUICK_WINDUP_MIN = 2;
    private static final int SAMURAI_QUICK_WINDUP_MAX = 4;
    private static final int SHINOBI_LORD_QUICK_WINDUP_MIN = 1;
    private static final int SHINOBI_LORD_QUICK_WINDUP_MAX = 3;
    private static final int WARLORD_PHASE_TWO_QUICK_WINDUP_MIN = 1;
    private static final int WARLORD_PHASE_TWO_QUICK_WINDUP_MAX = 3;
    private static final int SAMURAI_WINDUP_MIN = 2;
    private static final int SAMURAI_WINDUP_MAX = 9;
    private static final int SHINOBI_LORD_WINDUP_MIN = 1;
    private static final int SHINOBI_LORD_WINDUP_MAX = 7;
    private static final int WARLORD_PHASE_TWO_WINDUP_MIN = 2;
    private static final int WARLORD_PHASE_TWO_WINDUP_MAX = 6;
    private static final int SAMURAI_CHAIN_WINDUP_MIN = 10;
    private static final int SAMURAI_CHAIN_WINDUP_MAX = 16;
    private static final int SHINOBI_LORD_CHAIN_WINDUP_MIN = 8;
    private static final int SHINOBI_LORD_CHAIN_WINDUP_MAX = 12;
    private static final int WARLORD_PHASE_TWO_CHAIN_WINDUP_MIN = 5;
    private static final int WARLORD_PHASE_TWO_CHAIN_WINDUP_MAX = 10;
    private static final int SAMURAI_CHAIN_FOLLOWUPS_MIN = 1;
    private static final int SAMURAI_CHAIN_FOLLOWUPS_MAX = 2;
    private static final int SHINOBI_LORD_CHAIN_FOLLOWUPS_MIN = 2;
    private static final int SHINOBI_LORD_CHAIN_FOLLOWUPS_MAX = 2;
    private static final int WARLORD_CHAIN_FOLLOWUPS_MIN = 2;
    private static final int WARLORD_CHAIN_FOLLOWUPS_MAX = 3;
    private static final double WARLORD_FINISHER_KNOCKBACK_STRENGTH = 1.2D;
    private static final double WARLORD_FINISHER_VERTICAL_BOOST = 0.18D;
    private static final int SMOKE_BOMB_COOLDOWN_MIN = 90;
    private static final int SMOKE_BOMB_COOLDOWN_MAX = 150;
    private static final float SMOKE_BOMB_FOLLOWUP_CHANCE = 0.35F;
    private static final int SHINOBI_SMOKE_BOMB_COOLDOWN_MIN = 35;
    private static final int SHINOBI_SMOKE_BOMB_COOLDOWN_MAX = 70;
    private static final float SHINOBI_SMOKE_BOMB_FOLLOWUP_CHANCE = 0.70F;
    private static final double SMOKE_BOMB_MIN_DISTANCE = 6.0D;
    private static final double SMOKE_BOMB_MAX_DISTANCE = 9.0D;

    // Coordination
    private static final double ALLY_SCAN_RADIUS = 5.0D;
    private static final int MAX_SIMULTANEOUS_WINDUPS = 1;

    public AdvancedMeleeAttackGoal(Mob mob) {
        this(mob, 1.0D, false);
    }

    public AdvancedMeleeAttackGoal(Mob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.followingTargetEvenIfNotSeen = followingTargetEvenIfNotSeen;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        if (target == null) return false;
        if (WeaponBlockGoal.isPoiseBroken(mob) || WeaponBlockGoal.isStunned(mob)) return false;
        // If the mob is mounted (on a horse/boat/etc.), prefer vanilla attack goals.
        if (mob.isPassenger()) return false;
        // Prefer vanilla melee AI for villagers / trader-like NPCs
        if (isVanillaPreferredTarget(target)) return false;
        return target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (WeaponBlockGoal.isPoiseBroken(mob) || WeaponBlockGoal.isStunned(mob)) return false;
        // If the mob becomes mounted while using this goal, stop so vanilla AI can take over.
        if (mob.isPassenger()) return false;
        // If target becomes a vanilla-preferred target, stop this goal so vanilla AI can take over
        if (isVanillaPreferredTarget(target)) return false;
        if (target instanceof Player) {
            Player p = (Player) target;
            if (p.isCreative() || p.isSpectator()) return false;
        }
        return true;
    }

    /**
     * Returns true when we want the vanilla melee goal to handle this type of target
     * (e.g. villagers and wandering traders) instead of the advanced phased combat.
     */
    private boolean isVanillaPreferredTarget(LivingEntity target) {
        return target instanceof Villager || target instanceof WanderingTrader;
    }

    @Override
    public void start() {
        phase = Phase.GUARD;
        guardTicks = initialGuardTicks();
        attackCooldown = 0;
        attackVoiceCooldown = 0;
        pathUpdateTimer = 0;
        chainCount = 0;
        comboFollowupLimit = 0;
        smokeBombCooldown = 0;
        openingSmokeBombPending = false;
        mob.setAggressive(false);
        mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        mob.setAggressive(false);
        windupRemaining = 0;
        sweepSoundPlayed = false;
        phase = Phase.GUARD;
        chainCount = 0;
        comboFollowupLimit = 0;
        openingSmokeBombPending = false;
        try {
                if (mob instanceof RoninEntity ronin) {
                ronin.setWindingUp(false);
                ronin.setWindupTicks(0);
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean requiresUpdateEveryTick() { return true; }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;
        if (WeaponBlockGoal.isPoiseBroken(mob) || WeaponBlockGoal.isStunned(mob)) {
            stop();
            mob.getNavigation().stop();
            return;
        }

        // If the mob mounts mid-combat, immediately stop this goal and let vanilla AI handle attacks.
        if (mob.isPassenger()) { stop(); return; }

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        double dx = target.getX() - mob.getX();
        double dy = target.getY() - mob.getY();
        double dz = target.getZ() - mob.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (attackCooldown > 0) attackCooldown--;
        if (attackVoiceCooldown > 0) attackVoiceCooldown--;
        if (smokeBombCooldown > 0) smokeBombCooldown--;
        if (pathUpdateTimer > 0) pathUpdateTimer--;

        // Simplified melee behavior for non-player targets: no windups, just approach and attack freely.
        if (!(target instanceof Player)) {
            // Move into melee range
            if (dist > PREFERRED_MAX) {
                throttledMoveTo(target, 1.2D * speedModifier);
            } else {
                mob.getNavigation().stop();
            }

            // Attack when in range and cooldown elapsed
            if (dist <= PREFERRED_MAX && attackCooldown <= 0) {
                try {
                    if (EntityVisibility.canDetect(mob, target)) {
                        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
                        mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                        mob.doHurtTarget(target);
                    }
                } catch (Throwable ignored) {}
                attackCooldown = randomRange(COOLDOWN_MIN, COOLDOWN_MAX);
            }
            return; // skip the advanced phased logic
        }

        switch (phase) {
            case GUARD:
                handleGuard(target, dist);
                break;
            case PRESSURE:
                handlePressure(target, dist);
                break;
            case WINDUP:
                handleWindup(target, dist);
                break;
            case RETREAT:
                handleRetreat(target, dist);
                break;
            case RECOVERY:
                handleRecovery(target, dist);
                break;
            default:
                break;
        }
    }

    private void handleGuard(LivingEntity target, double dist) {
        // Close into melee range rather than holding far back
        if (dist > PREFERRED_MAX) {
            throttledMoveTo(target, 1.2D * speedModifier);
            return;
        }
        
        // Stop navigation when in range to prevent sliding
        mob.getNavigation().stop();

        if (guardTicks > 0) { guardTicks--; return; }

        if (nearbyWindups(target) >= MAX_SIMULTANEOUS_WINDUPS) { guardTicks = delayedGuardTicks(); return; }
        if (canStartWindup(dist)) {
            float r = this.mob.getRandom().nextFloat();
            float quickAttackChance = getQuickAttackChance();
            if (r < quickAttackChance) startQuickWindup();
            else if (r < quickAttackChance + FEINT_CHANCE) startFeint();
            else startWindup();
        } else startPressure();
    }

    private void handlePressure(LivingEntity target, double dist) {
        // Aggressively close into melee range; pressure means move in and threaten
        if (dist > PREFERRED_MAX) {
            throttledMoveTo(target, 1.2D * speedModifier);
        } else {
            mob.getNavigation().stop();
        }

        if (pressureTicks > 0) { pressureTicks--; return; }
        if (canStartWindup(dist) && nearbyWindups(target) < MAX_SIMULTANEOUS_WINDUPS) {
            float r = this.mob.getRandom().nextFloat();
            float quickAttackChance = getQuickAttackChance();
            if (r < quickAttackChance) startQuickWindup();
            else if (r < quickAttackChance + FEINT_CHANCE) startFeint();
            else startWindup();
        } else {
            phase = Phase.GUARD;
            guardTicks = recoveryGuardTicks();
        }
    }

    private void handleWindup(LivingEntity target, double dist) {
        // Fire the sweep cue right before impact so the tell lines up with the actual hit.
        if (!sweepSoundPlayed && windupRemaining <= SWEEP_LEAD_TICKS) {
            playSweepTelegraph();
        }
        if (windupRemaining <= 0) { performStrike(target); return; }
        mob.setAggressive(true);
        int halfway = windupTotal / 2;
        if (windupRemaining > halfway) {
            // First half: hold position and telegraph
            if (dist > PREFERRED_MAX) {
                throttledMoveTo(target, 0.8D * speedModifier);
            } else {
                mob.getNavigation().stop();
            }
        } else {
            // Second half: aggressively close in for the strike
            if (dist > BREACH_DISTANCE) {
                throttledMoveTo(target, 1.0D * speedModifier);
            } else {
                mob.getNavigation().stop();
            }
        }
        if (windupRemaining == halfway) { spawnParticle(ParticleTypes.CRIT, 1); }
        windupRemaining--;
        // If this windup was a feint and we've reached the end, cancel strike
        if (feint && windupRemaining <= 0) {
            feint = false;
            mob.setAggressive(false);
            phase = Phase.RETREAT;
            retreatTicks = randomRange(6, 10);
            return;
        }
    }

    private void startQuickWindup() {
        phase = Phase.WINDUP;
        feint = false;
        this.chainCount = 0;
        primeComboProfile();
        windupTotal = getQuickWindupTicks();
        windupRemaining = windupTotal;
        mob.setAggressive(true);
        mob.getNavigation().stop();
        sweepSoundPlayed = false;
        playComboAttackVoice();
        try {
            if (mob instanceof RoninEntity ronin) {
                ronin.setWindingUp(true);
                ronin.setWindupTicks(windupTotal);
            }
        } catch (Throwable ignored) {}
        try { WeaponBlockGoal.disableBlockingFor(this.mob, windupTotal + 2); } catch (Throwable ignored) {}
    }

    private void startFeint() {
        phase = Phase.WINDUP;
        feint = true;
        this.chainCount = 0;
        comboFollowupLimit = 0;
        windupTotal = randomRange(3, 6);
        windupRemaining = windupTotal;
        mob.setAggressive(true);
        mob.getNavigation().stop();
        playComboAttackVoice();
        playSweepTelegraph();
        spawnParticle(ParticleTypes.CRIT, 1);
        try {
            if (mob instanceof RoninEntity ronin) {
                ronin.setWindingUp(true);
                ronin.setWindupTicks(windupTotal);
            }
        } catch (Throwable ignored) {}
        try { WeaponBlockGoal.disableBlockingFor(this.mob, windupTotal + 2); } catch (Throwable ignored) {}
    }

    private void handleRetreat(LivingEntity target, double dist) {
        if (retreatTicks <= 0) { phase = Phase.RECOVERY; recoveryTicks = getRecoveryTicks(); return; }
        retreatTicks--;
        // Just maintain distance, don't actively strafe
        if (dist < PREFERRED_MIN) {
            mob.getNavigation().stop();
        } else if (dist > PREFERRED_MAX * 1.5) {
            throttledMoveTo(target, 0.8D * speedModifier);
        } else {
            mob.getNavigation().stop();
        }
    }

    private void handleRecovery(LivingEntity target, double dist) {
        if (recoveryTicks <= 0) { phase = Phase.GUARD; guardTicks = recoveryGuardTicks(); return; }
        recoveryTicks--;
        maintainSpacing(target, dist, true);
    }

    private void maintainSpacing(LivingEntity target, double dist, boolean stopNavInside) {
        if (dist > PREFERRED_MAX) {
            throttledMoveTo(target, 0.9D * speedModifier);
        } else if (stopNavInside) {
            mob.getNavigation().stop();
        }
    }

    /**
     * Throttled moveTo: only recalculates the path every PATH_UPDATE_INTERVAL ticks.
     * Prevents per-tick pathfinder recalculation that causes jittery/janky movement.
     */
    private void throttledMoveTo(LivingEntity target, double speed) {
        if (pathUpdateTimer <= 0) {
            mob.getNavigation().moveTo(target, speed);
            pathUpdateTimer = PATH_UPDATE_INTERVAL;
        }
    }

    private boolean canStartWindup(double dist) {
        // Deterministic: start windup whenever cooldown is ready and we're within preferred melee distance
        if (attackCooldown > 0) return false;
        return dist <= PREFERRED_MAX;
    }

    private void startWindup() {
        phase = Phase.WINDUP;
        this.chainCount = 0;
        primeComboProfile();
        windupTotal = getStandardWindupTicks();
        windupRemaining = windupTotal;
        mob.setAggressive(true);
        mob.getNavigation().stop();
        pathUpdateTimer = 0; // allow immediate path calc when closing in second half
        sweepSoundPlayed = false;
        playComboAttackVoice();
        // If this mob is a Ronin, notify the entity (synched data) so the client can play animations
        try {
            if (mob instanceof RoninEntity ronin) {
                ronin.setWindingUp(true);
                ronin.setWindupTicks(windupTotal);
            }
        } catch (Throwable ignored) {}
        // Disable weapon-blocking while winding up and for a short window after
        try {
            WeaponBlockGoal.disableBlockingFor(this.mob, windupTotal + 3);
        } catch (Throwable ignored) {}
    }

    private void performStrike(LivingEntity target) {
        mob.setAggressive(false);
        boolean landedHit = false;
        try {
            if (canLandStrike(target)) {
                mob.doHurtTarget(target);
                landedHit = true;
            }
        } catch (Throwable ignored) {}
        boolean continueCombo = landedHit && shouldContinueCombo();
        boolean warlordFinisher = landedHit && isWarlord() && !continueCombo;
        if (warlordFinisher) {
            applyWarlordFinisherKnockback(target);
            if (isPhaseTwoWarlord() && this.comboFollowupLimit > 0 && this.chainCount > 0 && mob instanceof WarlordEntity warlord) {
                warlord.playPhaseTwoFinisherSound();
            }
        }
        attackCooldown = randomRange(getAttackCooldownMin(), getAttackCooldownMax());
        // Default to retreat after a strike
        phase = Phase.RETREAT;
        retreatTicks = randomRange(10, 14);
        try {
            if (mob instanceof RoninEntity ronin) {
                ronin.setWindingUp(false);
                ronin.setWindupTicks(0);
            }
        } catch (Throwable ignored) {}
        // Keep blocking disabled for a couple ticks after striking to avoid instant re-block
        try {
            WeaponBlockGoal.disableBlockingFor(this.mob, 4);
        } catch (Throwable ignored) {}

        if (landedHit && trySmokeBombReposition(target)) {
            this.chainCount = 0;
            this.phase = Phase.RECOVERY;
            this.recoveryTicks = randomRange(18, 28);
            this.mob.getNavigation().stop();
            return;
        }

        // Chance to follow up with another attack (chain). If chained, start a short windup immediately
        try {
            if (continueCombo) {
                this.chainCount++;
                // Immediate short windup for follow-up
                phase = Phase.WINDUP;
                feint = false;
                sweepSoundPlayed = false; // allow telegraph to fire again for this chain strike
                windupTotal = getChainWindupTicks();
                windupRemaining = windupTotal;
                mob.setAggressive(true);
                mob.getNavigation().stop();
                playComboAttackVoice();
                try { WeaponBlockGoal.disableBlockingFor(this.mob, windupTotal + 2); } catch (Throwable ignored2) {}
                return;
            }
        } catch (Throwable ignored) {}

        // If not chaining, reset chain count so next combo can start fresh
        this.chainCount = 0;
        this.comboFollowupLimit = 0;
    }

    private boolean canLandStrike(LivingEntity target) {
        if (!EntityVisibility.canDetect(mob, target)) {
            return false;
        }

        double strikeReach = Math.max(MIN_STRIKE_REACH, mob.getBbWidth() + target.getBbWidth() + STRIKE_REACH_PADDING);
        if (mob.distanceToSqr(target) <= strikeReach * strikeReach) {
            return true;
        }

        return mob.getBoundingBox().inflate(0.2D).intersects(target.getBoundingBox());
    }

    private void startPressure() {
        phase = Phase.PRESSURE;
        pressureTicks = getPressureTicks();
        pathUpdateTimer = 0; // allow immediate moveTo on next tick
        mob.getNavigation().stop();
    }

    private boolean isPhaseTwoWarlord() {
        return mob instanceof WarlordEntity warlord && warlord.isPhaseTwo();
    }

    private boolean isShinobiLord() {
        return mob instanceof ShinobiLordEntity;
    }

    private boolean isUmiNyobo() {
        return mob instanceof UmiNyoboEntity;
    }

    private boolean isAggressiveSamurai() {
        return mob instanceof RoninEntity
            || mob instanceof GoryoEntity
            || mob instanceof UmiNyoboEntity
            || mob instanceof TakedaSamuraiEntity
            || mob instanceof SatomiSamuraiEntity
            || mob instanceof KobayakawaSamuraiEntity
            || isShinobiLord();
    }

    private int initialGuardTicks() {
        if (isPhaseTwoWarlord()) {
            return randomRange(2, 6);
        }
        if (isUmiNyobo()) {
            return randomRange(1, 4);
        }
        if (isShinobiLord()) {
            return randomRange(2, 5);
        }
        if (isAggressiveSamurai()) {
            return randomRange(3, 8);
        }
        return randomRange(5, 12);
    }

    private int delayedGuardTicks() {
        if (isPhaseTwoWarlord()) {
            return randomRange(3, 7);
        }
        if (isUmiNyobo()) {
            return randomRange(2, 5);
        }
        if (isShinobiLord()) {
            return randomRange(2, 6);
        }
        if (isAggressiveSamurai()) {
            return randomRange(4, 10);
        }
        return randomRange(6, 14);
    }

    private int recoveryGuardTicks() {
        if (isPhaseTwoWarlord()) {
            return randomRange(4, 10);
        }
        if (isUmiNyobo()) {
            return randomRange(3, 7);
        }
        if (isShinobiLord()) {
            return randomRange(4, 8);
        }
        if (isAggressiveSamurai()) {
            return randomRange(5, 12);
        }
        return randomRange(8, 20);
    }

    private int getAttackCooldownMin() {
        if (isPhaseTwoWarlord()) {
            return WARLORD_PHASE_TWO_COOLDOWN_MIN;
        }
        if (isUmiNyobo()) {
            return 3;
        }
        if (isShinobiLord()) {
            return SHINOBI_LORD_COOLDOWN_MIN;
        }
        if (isAggressiveSamurai()) {
            return SAMURAI_COOLDOWN_MIN;
        }
        return COOLDOWN_MIN;
    }

    private int getAttackCooldownMax() {
        if (isPhaseTwoWarlord()) {
            return WARLORD_PHASE_TWO_COOLDOWN_MAX;
        }
        if (isUmiNyobo()) {
            return 7;
        }
        if (isShinobiLord()) {
            return SHINOBI_LORD_COOLDOWN_MAX;
        }
        if (isAggressiveSamurai()) {
            return SAMURAI_COOLDOWN_MAX;
        }
        return COOLDOWN_MAX;
    }

    private int getQuickWindupTicks() {
        if (isPhaseTwoWarlord()) {
            return randomRange(WARLORD_PHASE_TWO_QUICK_WINDUP_MIN, WARLORD_PHASE_TWO_QUICK_WINDUP_MAX);
        }
        if (isUmiNyobo()) {
            return randomRange(1, 3);
        }
        if (isShinobiLord()) {
            return randomRange(SHINOBI_LORD_QUICK_WINDUP_MIN, SHINOBI_LORD_QUICK_WINDUP_MAX);
        }
        if (isAggressiveSamurai()) {
            return randomRange(SAMURAI_QUICK_WINDUP_MIN, SAMURAI_QUICK_WINDUP_MAX);
        }
        return randomRange(3, 5);
    }

    private int getStandardWindupTicks() {
        if (isPhaseTwoWarlord()) {
            return randomRange(WARLORD_PHASE_TWO_WINDUP_MIN, WARLORD_PHASE_TWO_WINDUP_MAX);
        }
        if (isUmiNyobo()) {
            return randomRange(2, 6);
        }
        if (isShinobiLord()) {
            return randomRange(SHINOBI_LORD_WINDUP_MIN, SHINOBI_LORD_WINDUP_MAX);
        }
        if (isAggressiveSamurai()) {
            return randomRange(SAMURAI_WINDUP_MIN, SAMURAI_WINDUP_MAX);
        }
        return randomRange(WINDUP_MIN, WINDUP_MAX);
    }

    private int getChainWindupTicks() {
        if (isPhaseTwoWarlord()) {
            return randomRange(WARLORD_PHASE_TWO_CHAIN_WINDUP_MIN, WARLORD_PHASE_TWO_CHAIN_WINDUP_MAX);
        }
        if (isUmiNyobo()) {
            return randomRange(6, 10);
        }
        if (isShinobiLord()) {
            return randomRange(SHINOBI_LORD_CHAIN_WINDUP_MIN, SHINOBI_LORD_CHAIN_WINDUP_MAX);
        }
        if (isAggressiveSamurai()) {
            return randomRange(SAMURAI_CHAIN_WINDUP_MIN, SAMURAI_CHAIN_WINDUP_MAX);
        }
        return randomRange(GENERIC_CHAIN_WINDUP_MIN, GENERIC_CHAIN_WINDUP_MAX);
    }

    private int getPressureTicks() {
        if (isPhaseTwoWarlord()) {
            return randomRange(12, 22);
        }
        if (isUmiNyobo()) {
            return randomRange(8, 16);
        }
        if (isShinobiLord()) {
            return randomRange(10, 20);
        }
        if (isAggressiveSamurai()) {
            return randomRange(18, 32);
        }
        return randomRange(30, 55);
    }

    private int getRecoveryTicks() {
        if (isPhaseTwoWarlord()) {
            return randomRange(14, 24);
        }
        if (isUmiNyobo()) {
            return randomRange(10, 16);
        }
        if (isShinobiLord()) {
            return randomRange(12, 20);
        }
        if (isAggressiveSamurai()) {
            return randomRange(14, 24);
        }
        return randomRange(20, 35);
    }

    private float getQuickAttackChance() {
        if (isPhaseTwoWarlord()) {
            return 0.22F;
        }
        if (isUmiNyobo()) {
            return 0.25F;
        }
        if (isShinobiLord()) {
            return 0.20F;
        }
        if (isAggressiveSamurai()) {
            return 0.17F;
        }
        return QUICK_ATTACK_CHANCE;
    }

    private void primeComboProfile() {
        if (this.chainCount != 0) {
            return;
        }

        if (isWarlord()) {
            this.comboFollowupLimit = randomRange(WARLORD_CHAIN_FOLLOWUPS_MIN, WARLORD_CHAIN_FOLLOWUPS_MAX);
            return;
        }

        if (isShinobiLord()) {
            this.comboFollowupLimit = randomRange(SHINOBI_LORD_CHAIN_FOLLOWUPS_MIN, SHINOBI_LORD_CHAIN_FOLLOWUPS_MAX);
            return;
        }

        if (isAggressiveSamurai()) {
            this.comboFollowupLimit = randomRange(SAMURAI_CHAIN_FOLLOWUPS_MIN, SAMURAI_CHAIN_FOLLOWUPS_MAX);
            return;
        }

        this.comboFollowupLimit = Math.min(MAX_CHAIN, randomRange(GENERIC_COMBO_FOLLOWUPS_MIN, GENERIC_COMBO_FOLLOWUPS_MAX));
    }

    private boolean shouldContinueCombo() {
        if (this.comboFollowupLimit > 0) {
            return this.chainCount < this.comboFollowupLimit;
        }
        return false;
    }

    private boolean isWarlord() {
        return mob instanceof WarlordEntity;
    }

    private void applyWarlordFinisherKnockback(LivingEntity target) {
        if (target == null || mob.level().isClientSide) {
            return;
        }

        Vec3 push = target.position().subtract(mob.position());
        double horizontalLengthSqr = push.x * push.x + push.z * push.z;
        if (horizontalLengthSqr < 1.0E-4D) {
            push = new Vec3(mob.getRandom().nextDouble() - 0.5D, 0.0D, mob.getRandom().nextDouble() - 0.5D);
            horizontalLengthSqr = push.x * push.x + push.z * push.z;
        }

        if (horizontalLengthSqr < 1.0E-4D) {
            return;
        }

        Vec3 normalized = new Vec3(push.x, 0.0D, push.z).normalize().scale(WARLORD_FINISHER_KNOCKBACK_STRENGTH);
        target.push(normalized.x, WARLORD_FINISHER_VERTICAL_BOOST, normalized.z);
        target.hurtMarked = true;
    }

    private int nearbyWindups(LivingEntity target) {
        List<Mob> allies = mob.level().getEntitiesOfClass(Mob.class, mob.getBoundingBox().inflate(ALLY_SCAN_RADIUS));
        int count = 0;
        for (Mob other : allies) {
            if (other == mob) continue;
            LivingEntity ot = other.getTarget();
            if (ot == target && other.isAggressive()) count++;
        }
        return count;
    }

    private void spawnParticle(ParticleOptions type, int count) {
        if (mob.level() instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel) mob.level();
            sl.sendParticles(type, mob.getX(), mob.getY() + mob.getEyeHeight() * 0.5D, mob.getZ(), count, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void playSweepTelegraph() {
        mob.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.7F, 1.15F);
        spawnParticle(ParticleTypes.SWEEP_ATTACK, 1);
        sweepSoundPlayed = true;
    }

    private void playComboAttackVoice() {
        if (attackVoiceCooldown > 0) return;
        if (mob instanceof WarlordEntity) return;

        // Choose the attack sound for this mob
        net.minecraft.sounds.SoundEvent attackSound = null;
        if (mob instanceof ShinobiLordEntity) attackSound = SoundRegistry.SHINOBI_LORD_ATTACK;
        else if (mob instanceof RoninEntity) attackSound = SoundRegistry.RONIN_ATTACK;
        else if (mob instanceof GoryoEntity) attackSound = SoundRegistry.GORYO_ATTACK;
        else if (mob instanceof TakedaSamuraiEntity
                || mob instanceof SatomiSamuraiEntity
                || mob instanceof KobayakawaSamuraiEntity) attackSound = SoundRegistry.SAMURAI_ATTACK;
        else return;

        // Prevent attack sound if it would be identical to the entity's ambient sounds
        if (attackSound == SoundRegistry.RONIN_AMBIENT || attackSound == SoundRegistry.RONIN_AMBIENT_AGGRO) return;
        if (attackSound == SoundRegistry.GORYO_AMBIENT || attackSound == SoundRegistry.GORYO_AMBIENT_AGGRO) return;

        mob.playSound(attackSound, 1.0F, 0.95F + mob.getRandom().nextFloat() * 0.15F);
        attackVoiceCooldown = randomRange(ATTACK_VOICE_COOLDOWN_MIN, ATTACK_VOICE_COOLDOWN_MAX);
    }

    private int randomRange(int min, int max) {
        if (max <= min) return min;
        return min + mob.getRandom().nextInt(max - min + 1);
    }

    private boolean trySmokeBombReposition(LivingEntity target) {
        if (target == null || !target.isAlive()) return false;
        if (smokeBombCooldown > 0) return false;

        // Restrict smoke-bomb repositioning to the Shinobi Lord only.
        if (!(mob instanceof ShinobiLordEntity)) return false;

        double distSqr = mob.distanceToSqr(target);
        float chance = distSqr <= 9.0D
            ? Math.min(0.9F, SHINOBI_SMOKE_BOMB_FOLLOWUP_CHANCE + 0.15F)
            : SHINOBI_SMOKE_BOMB_FOLLOWUP_CHANCE;

        if (mob.getRandom().nextFloat() > chance) return false;

        if (teleportToSmokeBombPosition(target)) {
            smokeBombCooldown = randomRange(SHINOBI_SMOKE_BOMB_COOLDOWN_MIN, SHINOBI_SMOKE_BOMB_COOLDOWN_MAX);
            return true;
        }

        return false;
    }

    private boolean teleportToSmokeBombPosition(LivingEntity target) {
        if (!(mob.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        double offsetX = mob.getX() - target.getX();
        double offsetZ = mob.getZ() - target.getZ();
        double offsetLength = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        if (offsetLength < 0.001D) {
            double fallbackAngle = mob.getRandom().nextDouble() * Math.PI * 2.0D;
            offsetX = Math.cos(fallbackAngle);
            offsetZ = Math.sin(fallbackAngle);
            offsetLength = 1.0D;
        }

        offsetX /= offsetLength;
        offsetZ /= offsetLength;

        for (int attempt = 0; attempt < 8; attempt++) {
            double angleOffset = (mob.getRandom().nextDouble() - 0.5D) * (Math.PI * 0.8D);
            double cos = Math.cos(angleOffset);
            double sin = Math.sin(angleOffset);
            double dirX = offsetX * cos - offsetZ * sin;
            double dirZ = offsetX * sin + offsetZ * cos;
            double distance = SMOKE_BOMB_MIN_DISTANCE + mob.getRandom().nextDouble() * (SMOKE_BOMB_MAX_DISTANCE - SMOKE_BOMB_MIN_DISTANCE);

            double candidateX = target.getX() + dirX * distance;
            double candidateZ = target.getZ() + dirZ * distance;
            double candidateY = target.getY() + 2.0D;

            if (teleportToLocation(serverLevel, candidateX, candidateY, candidateZ)) {
                return true;
            }
        }

        return false;
    }

    private boolean teleportToLocation(ServerLevel serverLevel, double x, double y, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);

        while (pos.getY() > mob.level().getMinBuildHeight() && !mob.level().getBlockState(pos).blocksMotion()) {
            pos.move(0, -1, 0);
        }

        BlockState blockState = mob.level().getBlockState(pos);
        if (!blockState.blocksMotion()) {
            return false;
        }

        double destinationX = x;
        double destinationY = pos.getY() + 1.0D;
        double destinationZ = z;
        if (!mob.level().noCollision(mob, mob.getBoundingBox().move(destinationX - mob.getX(), destinationY - mob.getY(), destinationZ - mob.getZ()))) {
            return false;
        }

        double previousX = mob.getX();
        double previousY = mob.getY();
        double previousZ = mob.getZ();

        mob.teleportTo(destinationX, destinationY, destinationZ);
        spawnSmokeBombParticles(serverLevel, previousX, previousY, previousZ);
        spawnSmokeBombParticles(serverLevel, mob.getX(), mob.getY(), mob.getZ());
        mob.playSound(SoundRegistry.SMOKE_BOMB, 1.0F, 0.95F + mob.getRandom().nextFloat() * 0.15F);
        return true;
    }

    private void spawnSmokeBombParticles(ServerLevel level, double x, double y, double z) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.6D, z, 18, 0.45D, 0.45D, 0.45D, 0.02D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 0.2D, z, 10, 0.3D, 0.25D, 0.3D, 0.0D);
        level.sendParticles(ParticleTypes.CLOUD, x, y + 0.2D, z, 8, 0.25D, 0.2D, 0.25D, 0.0D);
    }
}
