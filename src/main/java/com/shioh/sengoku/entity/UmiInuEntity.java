package com.shioh.sengoku.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import com.shioh.sengoku.entity.UmiInuEntity.WanderGoal.FearDolphinGoal;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.Difficulty;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import java.util.EnumSet;
import java.util.List;

/**
 * Umi Inu – literally "sea dog".
 * A fast, aggressive aquatic predator that hunts players in water.
 * No territory, no fire, no dimension logic — just relentless pursuit and melee.
 */
public class UmiInuEntity extends Monster {

    public UmiInuEntity(EntityType<? extends UmiInuEntity> type, Level level) {
        super(type, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 45, 10, 0.28F, 0.08F, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 4);
        this.setPersistenceRequired();
    }

    // -------------------------------------------------------------------------
    // Sounds
    // -------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.UMI_INU_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.UMI_INU_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.UMI_INU_DEATH;
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    // -------------------------------------------------------------------------
    // Attributes
    // -------------------------------------------------------------------------

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.FOLLOW_RANGE, 30.0)
            .add(Attributes.MOVEMENT_SPEED, 0.4)
            .add(Attributes.ARMOR, 2.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.3);
    }

    // -------------------------------------------------------------------------
    // Spawn rules
    // -------------------------------------------------------------------------

    public static boolean checkUmiInuSpawnRules(EntityType<UmiInuEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        try {
            // Require water at the spawn position so Umi Inu behave as ocean creatures
            if (!level.getFluidState(pos).is(FluidTags.WATER)) return false;
            // Also require the block below to be water to avoid spawning on shallow edges or land.
            if (!level.getFluidState(pos.below()).is(FluidTags.WATER)) return false;

            int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
            if (blockLight > 7) return false; // avoid bright/artificially lit spawns
        } catch (Throwable ignored) {
            // Fall back to generic checks if mappings differ
        }
        return checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    // -------------------------------------------------------------------------
    // Goals
    // -------------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FearDolphinGoal(this));
        this.goalSelector.addGoal(0, new DetectPlayerGoal(this));
        this.goalSelector.addGoal(1, new PursueGoal(this, 0.8D));
        this.goalSelector.addGoal(2, new WanderGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // -------------------------------------------------------------------------
    // aiStep – keep air supply topped, stabilize rotation
    // -------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isInWater()) {
            this.setAirSupply(300);
        }

        // Align yaw to motion direction for natural-looking swimming
        if (this.isInWater()) {
            Vec3 vel = this.getDeltaMovement();
            double horiz = vel.x * vel.x + vel.z * vel.z;
            if (horiz > 1.0E-6) {
                float desiredYaw = (float)(Mth.atan2(vel.z, vel.x) * 180.0D / Math.PI) - 90.0F;
                float yawDiff = Mth.wrapDegrees(desiredYaw - this.getYRot());
                float newYaw = this.getYRot() + Mth.clamp(yawDiff, -6.0F, 6.0F);
                this.setYRot(newYaw);
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();
            }
        }
    }

    // =========================================================================
    // Inner goal classes
    // =========================================================================

    /**
     * Scans for nearby players in water or boats and sets them as the target.
     */
    static class DetectPlayerGoal extends Goal {
        private final UmiInuEntity umiInu;
        private int cooldown = 0;
        private static final double DETECT_RANGE = 48.0D;

        public DetectPlayerGoal(UmiInuEntity umiInu) {
            this.umiInu = umiInu;
            this.setFlags(EnumSet.noneOf(Flag.class));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown-- > 0) return false;
            this.cooldown = 20;

            AABB box = new AABB(
                this.umiInu.getX() - DETECT_RANGE, this.umiInu.getY() - 24, this.umiInu.getZ() - DETECT_RANGE,
                this.umiInu.getX() + DETECT_RANGE, this.umiInu.getY() + 24, this.umiInu.getZ() + DETECT_RANGE
            );

            List<Player> candidates = this.umiInu.level().getEntitiesOfClass(Player.class, box,
                p -> !p.isSpectator() && !p.isCreative()
                    && (p.isInWater() || p.getVehicle() instanceof Boat));

            if (candidates.isEmpty()) return false;

            // Pick the nearest
            Player nearest = null;
            double bestDist = Double.MAX_VALUE;
            for (Player p : candidates) {
                double d = this.umiInu.distanceToSqr(p);
                if (d < bestDist) { bestDist = d; nearest = p; }
            }

            if (nearest == null) return false;
            this.umiInu.setTarget(nearest);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.umiInu.getTarget();
            if (target == null || !target.isAlive()) return false;
            // Drop target if the player leaves the water and isn't in a boat
            if (target instanceof Player p) {
                if (!p.isInWater() && !(p.getVehicle() instanceof Boat)) {
                    this.umiInu.setTarget(null);
                    return false;
                }
            }
            return true;
        }

        @Override
        public void stop() {}
    }

    /**
     * Fast direct pursuit in water, melee attack on contact.
     */
    static class PursueGoal extends Goal {
        private final UmiInuEntity umiInu;
        private final double speed;
        private int attackCooldown = 0;

        public PursueGoal(UmiInuEntity umiInu, double speed) {
            this.umiInu = umiInu;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity t = this.umiInu.getTarget();
            return t != null && t.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity t = this.umiInu.getTarget();
            return t != null && t.isAlive();
        }

        @Override
        public void start() {
            this.attackCooldown = 0;
        }

        @Override
        public void stop() {
            this.umiInu.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity t = this.umiInu.getTarget();
            if (t == null) return;

            if (this.attackCooldown > 0) this.attackCooldown--;

            // Direct velocity-based chase in water for responsive movement
            if (this.umiInu.isInWater()) {
                double dx = t.getX() - this.umiInu.getX();
                double dy = t.getY() - this.umiInu.getY();
                double dz = t.getZ() - this.umiInu.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > 1.0E-6) {
                    double nx = dx / dist;
                    double ny = dy / dist;
                    double nz = dz / dist;
                    Vec3 vel = this.umiInu.getDeltaMovement();
                    double targetSpeed = 0.7D;
                    double ax = (nx * targetSpeed - vel.x) * 0.24;
                    double ay = (ny * targetSpeed - vel.y) * 0.24;
                    double az = (nz * targetSpeed - vel.z) * 0.24;
                    this.umiInu.setDeltaMovement(vel.add(ax, ay, az));
                }
                if (!this.umiInu.getNavigation().isDone()) {
                    this.umiInu.getNavigation().stop();
                }
            } else {
                this.umiInu.getNavigation().moveTo(t, this.speed);
            }

            this.umiInu.getLookControl().setLookAt(t, 8.0F, 8.0F);
            this.umiInu.yBodyRot = this.umiInu.getYRot();

            // Melee attack
if (this.umiInu.isWithinMeleeAttackRange(t) && this.attackCooldown <= 0) {
    this.attackCooldown = 20;
    if (!this.umiInu.level().isClientSide) {
        if (t instanceof Player p && (p.isCreative() || p.isSpectator())) return;

        if (t instanceof Player p && p.getVehicle() instanceof Boat boat) {
            try { boat.hurt(this.umiInu.damageSources().mobAttack(this.umiInu), 2.0F); } catch (Throwable ignored) {}
        }

        try { this.umiInu.swing(net.minecraft.world.InteractionHand.MAIN_HAND); } catch (Throwable ignored) {}
        float dmg = (float) this.umiInu.getAttributeValue(Attributes.ATTACK_DAMAGE);
        try { t.hurt(this.umiInu.damageSources().mobAttack(this.umiInu), dmg); } catch (Throwable ignored) {}
    }
}
        }
    }

    /**
     * Simple idle wandering — stays in water, drifts around naturally.
     */
    static class WanderGoal extends Goal {
        private final UmiInuEntity umiInu;
        private Vec3 wanderTarget;
        private int wanderTime = 0;

        public WanderGoal(UmiInuEntity umiInu) {
            this.umiInu = umiInu;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.umiInu.getTarget() != null) return false;
            double angle = this.umiInu.random.nextDouble() * Math.PI * 2.0;
            double dist = 6.0 + this.umiInu.random.nextDouble() * 12.0;
            double tx = this.umiInu.getX() + Math.cos(angle) * dist;
            double tz = this.umiInu.getZ() + Math.sin(angle) * dist;
            double ty = this.umiInu.getY() + (this.umiInu.random.nextInt(5) - 2);
            // Only wander to water blocks
            BlockPos check = new BlockPos((int)tx, (int)ty, (int)tz);
            boolean inWater = false;
            for (int dy = -2; dy <= 2; dy++) {
                try {
                    if (this.umiInu.level().getFluidState(check.offset(0, dy, 0)).is(FluidTags.WATER)) {
                        check = check.offset(0, dy, 0);
                        inWater = true;
                        break;
                    }
                } catch (Throwable ignored) {}
            }
            if (!inWater) return false;
            this.wanderTarget = new Vec3(check.getX() + 0.5, check.getY() + 0.5, check.getZ() + 0.5);
            this.wanderTime = 0;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.umiInu.getTarget() != null) return false;
            if (this.wanderTarget == null) return false;
            double dx = this.wanderTarget.x - this.umiInu.getX();
            double dy = this.wanderTarget.y - this.umiInu.getY();
            double dz = this.wanderTarget.z - this.umiInu.getZ();
            return (dx*dx + dy*dy + dz*dz) > 1.0D && this.wanderTime < 200;
        }

        @Override
        public void start() {
            this.wanderTime = 0;
        }

        @Override
        public void stop() {
            this.wanderTarget = null;
            this.umiInu.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.wanderTarget == null) return;
            this.wanderTime++;
            if (this.umiInu.isInWater()) {
                double dx = this.wanderTarget.x - this.umiInu.getX();
                double dy = this.wanderTarget.y - this.umiInu.getY();
                double dz = this.wanderTarget.z - this.umiInu.getZ();
                double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
                if (dist > 1.0E-6) {
                    double nx = dx / dist;
                    double ny = dy / dist;
                    double nz = dz / dist;
                    Vec3 vel = this.umiInu.getDeltaMovement();
                    double ax = (nx * 0.3 - vel.x) * 0.08;
                    double ay = (ny * 0.3 - vel.y) * 0.08;
                    double az = (nz * 0.3 - vel.z) * 0.08;
                    this.umiInu.setDeltaMovement(vel.add(ax, ay, az));
                }
                this.umiInu.getLookControl().setLookAt(this.wanderTarget.x, this.wanderTarget.y, this.wanderTarget.z);
                if (!this.umiInu.getNavigation().isDone()) {
                    this.umiInu.getNavigation().stop();
                }
            }
        }
        static class FearDolphinGoal extends Goal {
    private final UmiInuEntity umiInu;
    private LivingEntity nearestDolphin;
    private static final double FEAR_RANGE = 16.0D;
    private static final double FLEE_SPEED = 0.9D;

    public FearDolphinGoal(UmiInuEntity umiInu) {
        this.umiInu = umiInu;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        List<net.minecraft.world.entity.animal.Dolphin> dolphins = this.umiInu.level().getEntitiesOfClass(
            net.minecraft.world.entity.animal.Dolphin.class,
            this.umiInu.getBoundingBox().inflate(FEAR_RANGE),
            d -> d.isAlive()
        );
        if (dolphins.isEmpty()) return false;
        this.nearestDolphin = dolphins.get(0);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.nearestDolphin == null || !this.nearestDolphin.isAlive()) return false;
        return this.umiInu.distanceToSqr(this.nearestDolphin) < FEAR_RANGE * FEAR_RANGE * 2.0D;
    }

    @Override
    public void stop() {
        this.nearestDolphin = null;
    }

    @Override
    public void tick() {
        if (this.nearestDolphin == null) return;
        // Run directly away from the dolphin
        double dx = this.umiInu.getX() - this.nearestDolphin.getX();
        double dy = this.umiInu.getY() - this.nearestDolphin.getY();
        double dz = this.umiInu.getZ() - this.nearestDolphin.getZ();
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (dist > 1.0E-6) {
            double nx = dx / dist;
            double ny = dy / dist;
            double nz = dz / dist;
            Vec3 vel = this.umiInu.getDeltaMovement();
            double ax = (nx * FLEE_SPEED - vel.x) * 0.2;
            double ay = (ny * FLEE_SPEED - vel.y) * 0.2;
            double az = (nz * FLEE_SPEED - vel.z) * 0.2;
            this.umiInu.setDeltaMovement(vel.add(ax, ay, az));
        }
        // Also drop the current target while fleeing
        this.umiInu.setTarget(null);
    }
}
    }
}