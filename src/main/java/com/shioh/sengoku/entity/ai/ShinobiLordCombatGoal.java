package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.registry.SoundRegistry;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;

public class ShinobiLordCombatGoal extends Goal {
    private enum CombatMode { MELEE, RANGED }

    private static final double ENGAGE_MELEE_DIST = 4.0D;
    private static final double DISENGAGE_DIST = 8.5D;
    private static final int MODE_DURATION_MIN = 90;
    private static final int MODE_DURATION_MAX = 160;

    // Retreat smoke bomb (melee -> ranged)
    private static final int SMOKE_BOMB_COOLDOWN_MIN = 160;
    private static final int SMOKE_BOMB_COOLDOWN_MAX = 240;
    private static final int SMOKE_BOMB_MIN_MELEE_TICKS = 45;
    private static final double SMOKE_BOMB_TRIGGER_DISTANCE = 3.25D;
    private static final double SMOKE_BOMB_MIN_DISTANCE = 6.0D;
    private static final double SMOKE_BOMB_MAX_DISTANCE = 9.0D;

    // Approach smoke bomb (ranged -> melee re-engage)
    private static final int APPROACH_BOMB_COOLDOWN_MIN = 100;
    private static final int APPROACH_BOMB_COOLDOWN_MAX = 180;
    private static final double APPROACH_BOMB_TOO_FAR_DIST = 14.0D;
    private static final double APPROACH_BOMB_MIN_DISTANCE = 2.0D;
    private static final double APPROACH_BOMB_MAX_DISTANCE = 4.5D;

    private final Mob shinobi;
    private final AdvancedMeleeAttackGoal meleeGoal;
    private final ShinobiLordKunaiAttackGoal kunaiGoal;

    private CombatMode currentMode = CombatMode.MELEE;
    private int modeSwitchTimer;
    private int smokeBombCooldown;
    private int approachBombCooldown;
    private int meleeTicks;
    private int rangedTicks;

    public ShinobiLordCombatGoal(Mob shinobi) {
        this.shinobi = shinobi;
        this.meleeGoal = new AdvancedMeleeAttackGoal(shinobi, 1.0D, false);
        this.kunaiGoal = new ShinobiLordKunaiAttackGoal(shinobi, 0.9D, 30, 14.0F);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.shinobi.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.shinobi.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.currentMode = chooseModeFromDistance();
        this.modeSwitchTimer = randomRange(MODE_DURATION_MIN, MODE_DURATION_MAX);
        this.smokeBombCooldown = randomRange(SMOKE_BOMB_COOLDOWN_MIN / 2, SMOKE_BOMB_COOLDOWN_MAX / 2);
        this.approachBombCooldown = randomRange(APPROACH_BOMB_COOLDOWN_MIN / 2, APPROACH_BOMB_COOLDOWN_MAX / 2);
        this.meleeTicks = 0;
        this.rangedTicks = 0;
        startSubGoal(this.currentMode);
    }

    @Override
    public void stop() {
        stopSubGoal(this.currentMode);
        this.meleeTicks = 0;
        this.rangedTicks = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.shinobi.getTarget();
        if (target == null) return;

        if (this.modeSwitchTimer > 0) this.modeSwitchTimer--;
        if (this.smokeBombCooldown > 0) this.smokeBombCooldown--;
        if (this.approachBombCooldown > 0) this.approachBombCooldown--;

        if (this.currentMode == CombatMode.MELEE) {
            this.meleeTicks++;
            this.rangedTicks = 0;
        } else {
            this.rangedTicks++;
            this.meleeTicks = 0;
        }

        // Retreat smoke bomb: melee phase, player too close, reposition away
        if (trySmokeBombReposition(target)) {
            stopSubGoal(this.currentMode);
            this.currentMode = CombatMode.RANGED;
            this.modeSwitchTimer = randomRange(MODE_DURATION_MIN, MODE_DURATION_MAX);
            this.meleeTicks = 0;
            startSubGoal(this.currentMode);
            this.kunaiGoal.forceImmediateAttack();
            return;
        }

        // Approach smoke bomb: ranged phase, lost sight or target too far, flank in close
        if (tryApproachSmokeBomb(target)) {
            stopSubGoal(this.currentMode);
            this.currentMode = CombatMode.MELEE;
            this.modeSwitchTimer = randomRange(MODE_DURATION_MIN, MODE_DURATION_MAX);
            this.rangedTicks = 0;
            startSubGoal(this.currentMode);
            return;
        }

        CombatMode desiredMode = evaluateMode(target);
        if (desiredMode != this.currentMode) {
            stopSubGoal(this.currentMode);
            this.currentMode = desiredMode;
            this.modeSwitchTimer = randomRange(MODE_DURATION_MIN, MODE_DURATION_MAX);
            if (this.currentMode != CombatMode.MELEE) this.meleeTicks = 0;
            startSubGoal(this.currentMode);
        }

        if (this.currentMode == CombatMode.MELEE) {
            this.meleeGoal.tick();
        } else {
            this.kunaiGoal.tick();
        }
    }

    private CombatMode evaluateMode(LivingEntity target) {
        double distanceSq = this.shinobi.distanceToSqr(target);
        if (distanceSq <= ENGAGE_MELEE_DIST * ENGAGE_MELEE_DIST) return CombatMode.MELEE;
        if (distanceSq >= DISENGAGE_DIST * DISENGAGE_DIST) return CombatMode.RANGED;
        if (this.modeSwitchTimer <= 0) return this.currentMode == CombatMode.MELEE ? CombatMode.RANGED : CombatMode.MELEE;
        return this.currentMode;
    }

    private CombatMode chooseModeFromDistance() {
        LivingEntity target = this.shinobi.getTarget();
        if (target == null) return CombatMode.MELEE;
        return this.shinobi.distanceToSqr(target) <= DISENGAGE_DIST * DISENGAGE_DIST ? CombatMode.MELEE : CombatMode.RANGED;
    }

    private void startSubGoal(CombatMode mode) {
        if (mode == CombatMode.MELEE) this.meleeGoal.start(); else this.kunaiGoal.start();
    }

    private void stopSubGoal(CombatMode mode) {
        if (mode == CombatMode.MELEE) this.meleeGoal.stop(); else this.kunaiGoal.stop();
    }

    // --- Retreat smoke bomb ---

    private boolean trySmokeBombReposition(LivingEntity target) {
        if (this.currentMode != CombatMode.MELEE) return false;
        if (this.smokeBombCooldown > 0) return false;
        if (this.meleeTicks < SMOKE_BOMB_MIN_MELEE_TICKS) return false;
        if (!this.shinobi.getSensing().hasLineOfSight(target)) return false;
        if (this.shinobi.distanceToSqr(target) > SMOKE_BOMB_TRIGGER_DISTANCE * SMOKE_BOMB_TRIGGER_DISTANCE) return false;
        if (!teleportToSmokeBombPosition(target, false)) return false;
        this.smokeBombCooldown = randomRange(SMOKE_BOMB_COOLDOWN_MIN, SMOKE_BOMB_COOLDOWN_MAX);
        return true;
    }

    // --- Approach smoke bomb ---

    private boolean tryApproachSmokeBomb(LivingEntity target) {
        if (this.currentMode != CombatMode.RANGED) return false;
        if (this.approachBombCooldown > 0) return false;
        boolean lostSight = !this.shinobi.getSensing().hasLineOfSight(target);
        boolean tooFar = this.shinobi.distanceToSqr(target) > APPROACH_BOMB_TOO_FAR_DIST * APPROACH_BOMB_TOO_FAR_DIST;
        if (!lostSight && !tooFar) return false;
        if (!teleportToSmokeBombPosition(target, true)) return false;
        this.approachBombCooldown = randomRange(APPROACH_BOMB_COOLDOWN_MIN, APPROACH_BOMB_COOLDOWN_MAX);
        return true;
    }

    // --- Shared teleport logic ---

    private boolean teleportToSmokeBombPosition(LivingEntity target, boolean approach) {
        if (!(this.shinobi.level() instanceof ServerLevel serverLevel)) return false;

        double offsetX = this.shinobi.getX() - target.getX();
        double offsetZ = this.shinobi.getZ() - target.getZ();
        double offsetLength = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        if (offsetLength < 0.001D) {
            double fallbackAngle = this.shinobi.getRandom().nextDouble() * Math.PI * 2.0D;
            offsetX = Math.cos(fallbackAngle);
            offsetZ = Math.sin(fallbackAngle);
            offsetLength = 1.0D;
        }
        offsetX /= offsetLength;
        offsetZ /= offsetLength;

        double minDist = approach ? APPROACH_BOMB_MIN_DISTANCE : SMOKE_BOMB_MIN_DISTANCE;
        double maxDist = approach ? APPROACH_BOMB_MAX_DISTANCE : SMOKE_BOMB_MAX_DISTANCE;

        for (int attempt = 0; attempt < 8; attempt++) {
            double angleOffset = (this.shinobi.getRandom().nextDouble() - 0.5D) * (Math.PI * 0.8D);
            double cos = Math.cos(angleOffset);
            double sin = Math.sin(angleOffset);

            double dirX, dirZ;
            if (approach) {
                // Flip direction to land near target instead of away
                dirX = -(offsetX * cos - offsetZ * sin);
                dirZ = -(offsetX * sin + offsetZ * cos);
            } else {
                dirX = offsetX * cos - offsetZ * sin;
                dirZ = offsetX * sin + offsetZ * cos;
            }

            double distance = minDist + this.shinobi.getRandom().nextDouble() * (maxDist - minDist);
            double candidateX = target.getX() + dirX * distance;
            double candidateZ = target.getZ() + dirZ * distance;
            double candidateY = target.getY() + 2.0D;

            if (teleportToLocation(serverLevel, candidateX, candidateY, candidateZ, target)) {
                return true;
            }
        }

        return false;
    }

    private boolean teleportToLocation(ServerLevel serverLevel, double x, double y, double z, LivingEntity target) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);

        while (pos.getY() > this.shinobi.level().getMinBuildHeight() && !this.shinobi.level().getBlockState(pos).blocksMotion()) {
            pos.move(0, -1, 0);
        }

        BlockState blockState = this.shinobi.level().getBlockState(pos);
        if (!blockState.blocksMotion()) return false;

        net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> validFloorTag = net.minecraft.tags.TagKey.create(
            net.minecraft.core.registries.Registries.BLOCK,
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "shinobi_lord_teleports_on")
        );
        if (!blockState.is(validFloorTag)) return false;

        double destinationX = x;
        double destinationY = pos.getY() + 1.0D;
        double destinationZ = z;

        if (!this.shinobi.level().noCollision(this.shinobi, this.shinobi.getBoundingBox().move(
                destinationX - this.shinobi.getX(),
                destinationY - this.shinobi.getY(),
                destinationZ - this.shinobi.getZ()))) {
            return false;
        }

        // Reject positions where a solid block obstructs line of sight to the target
        net.minecraft.world.phys.Vec3 candidateEye = new net.minecraft.world.phys.Vec3(
            destinationX, destinationY + this.shinobi.getEyeHeight(), destinationZ);
        net.minecraft.world.phys.Vec3 targetEye = target.getEyePosition();
        net.minecraft.world.level.ClipContext clipContext = new net.minecraft.world.level.ClipContext(
            candidateEye, targetEye,
            net.minecraft.world.level.ClipContext.Block.COLLIDER,
            net.minecraft.world.level.ClipContext.Fluid.NONE,
            this.shinobi
        );
        net.minecraft.world.phys.BlockHitResult hitResult = serverLevel.clip(clipContext);
        if (hitResult.getType() != net.minecraft.world.phys.HitResult.Type.MISS) return false;

        double previousX = this.shinobi.getX();
        double previousY = this.shinobi.getY();
        double previousZ = this.shinobi.getZ();

        this.shinobi.teleportTo(destinationX, destinationY, destinationZ);
        this.shinobi.getNavigation().stop();
        spawnSmokeBombParticles(serverLevel, previousX, previousY, previousZ);
        spawnSmokeBombParticles(serverLevel, this.shinobi.getX(), this.shinobi.getY(), this.shinobi.getZ());
        this.shinobi.playSound(SoundRegistry.SMOKE_BOMB, 1.0F, 0.95F + this.shinobi.getRandom().nextFloat() * 0.15F);
        return true;
    }

    private void spawnSmokeBombParticles(ServerLevel level, double x, double y, double z) {
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 0.6D, z, 18, 0.45D, 0.45D, 0.45D, 0.02D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 0.2D, z, 10, 0.3D, 0.25D, 0.3D, 0.0D);
        level.sendParticles(ParticleTypes.CLOUD, x, y + 0.2D, z, 8, 0.25D, 0.2D, 0.25D, 0.0D);
    }

    private int randomRange(int min, int max) {
        if (max <= min) return min;
        return min + this.shinobi.getRandom().nextInt(max - min + 1);
    }
}