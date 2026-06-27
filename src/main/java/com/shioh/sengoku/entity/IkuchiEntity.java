package com.shioh.sengoku.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
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
import net.minecraft.tags.FluidTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import java.util.EnumSet;
import java.util.List;

import com.shioh.sengoku.registry.SoundRegistry;

/**
 * Ikuchi – a massive sea eel made of a head and 12 trailing body segments.
 * Hunts players in water; no poison, no land AI, no dimension logic.
 */
public class IkuchiEntity extends Monster {

    public static final int TRAILING_PART_COUNT = 12;

    private static final EntityDataAccessor<Integer> DATA_PART1_ID  = SynchedEntityData.defineId(IkuchiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART2_ID  = SynchedEntityData.defineId(IkuchiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART3_ID  = SynchedEntityData.defineId(IkuchiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART4_ID  = SynchedEntityData.defineId(IkuchiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART5_ID  = SynchedEntityData.defineId(IkuchiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART6_ID  = SynchedEntityData.defineId(IkuchiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART7_ID  = SynchedEntityData.defineId(IkuchiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART8_ID  = SynchedEntityData.defineId(IkuchiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART9_ID  = SynchedEntityData.defineId(IkuchiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART10_ID = SynchedEntityData.defineId(IkuchiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART11_ID = SynchedEntityData.defineId(IkuchiEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART12_ID = SynchedEntityData.defineId(IkuchiEntity.class, EntityDataSerializers.INT);

    private boolean partsSpawned = false;

    public IkuchiEntity(EntityType<? extends IkuchiEntity> type, Level level) {
        super(type, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 45, 10, 0.28F, 0.08F, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 4);
        this.setPersistenceRequired();
        this.xpReward = 20;
    }

    // -------------------------------------------------------------------------
    // Sounds
    // -------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.IKUCHI_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.IKUCHI_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.IKUCHI_DEATH;
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
            .add(Attributes.MAX_HEALTH, 120.0)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
            .add(Attributes.FOLLOW_RANGE, 48.0)
            .add(Attributes.MOVEMENT_SPEED, 1.8)
            .add(Attributes.ARMOR, 4.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.6);
    }

    // -------------------------------------------------------------------------
    // Spawn rules
    // -------------------------------------------------------------------------

    public static boolean checkIkuchiSpawnRules(EntityType<IkuchiEntity> type, LevelAccessor level,
            MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        if (!level.getFluidState(pos).is(FluidTags.WATER)) return false;
        if (!level.getFluidState(pos.below()).is(FluidTags.WATER)) return false;
        return Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    // -------------------------------------------------------------------------
    // Goals
    // -------------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new DetectPlayerGoal(this));
        this.goalSelector.addGoal(1, new PursueGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new WanderGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    // -------------------------------------------------------------------------
    // Synched data
    // -------------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PART1_ID,  -1);
        builder.define(DATA_PART2_ID,  -1);
        builder.define(DATA_PART3_ID,  -1);
        builder.define(DATA_PART4_ID,  -1);
        builder.define(DATA_PART5_ID,  -1);
        builder.define(DATA_PART6_ID,  -1);
        builder.define(DATA_PART7_ID,  -1);
        builder.define(DATA_PART8_ID,  -1);
        builder.define(DATA_PART9_ID,  -1);
        builder.define(DATA_PART10_ID, -1);
        builder.define(DATA_PART11_ID, -1);
        builder.define(DATA_PART12_ID, -1);
    }

    // -------------------------------------------------------------------------
    // aiStep
    // -------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isInWater()) {
            this.setAirSupply(300);
        }

        // Align yaw to motion for natural eel swimming
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

        // Maintain segment chain
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            if (!partsSpawned || this.tickCount <= 2 || this.tickCount % 200 == 0) {
                ensureParts(serverLevel);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Segment management
    // -------------------------------------------------------------------------

    private void ensureParts(ServerLevel serverLevel) {
        boolean allValid = true;
        for (int i = 1; i <= TRAILING_PART_COUNT; i++) {
            if (getPartEntity(serverLevel, getPartId(i)) == null) {
                allValid = false;
                break;
            }
        }
        if (allValid) {
            this.partsSpawned = true;
            return;
        }

        // Discard any stale parts and respawn the full chain
        for (int i = 1; i <= TRAILING_PART_COUNT; i++) {
            discardExistingPart(serverLevel, getPartId(i));
            setPartId(i, -1);
        }

        Entity lead = this;
        for (int i = 1; i <= TRAILING_PART_COUNT; i++) {
            IkuchiPartEntity part = (i == TRAILING_PART_COUNT)
                ? new IkuchiEndEntity(com.shioh.sengoku.registry.ModEntities.IKUCHI_END, this.level(), this, i)
                : new IkuchiPartEntity(com.shioh.sengoku.registry.ModEntities.IKUCHI_PART, this.level(), this, i);
            placePartBehind(lead, part, 1.2D);
            serverLevel.addFreshEntity(part);
            setPartId(i, part.getId());
            lead = part;
        }

        this.partsSpawned = true;
    }

    private static void placePartBehind(Entity lead, IkuchiPartEntity part, double distance) {
        Vec3 dir = lead.getLookAngle().normalize();
        if (dir.lengthSqr() < 1.0E-4D) {
            dir = Vec3.directionFromRotation(0.0F, lead.getYRot());
        }
        Vec3 pos = lead.position().subtract(dir.scale(distance));
        part.setPos(pos.x, pos.y, pos.z);
        part.setYRot(lead.getYRot());
    }

    private static IkuchiPartEntity getPartEntity(ServerLevel level, int id) {
        if (id < 0) return null;
        Entity e = level.getEntity(id);
        return e instanceof IkuchiPartEntity part && part.isAlive() ? part : null;
    }

    private static void discardExistingPart(ServerLevel level, int id) {
        if (id < 0) return;
        Entity e = level.getEntity(id);
        if (e instanceof IkuchiPartEntity) {
            e.discard();
        }
    }

    // -------------------------------------------------------------------------
    // Part ID accessors
    // -------------------------------------------------------------------------

    public int getPartId(int index) {
        return switch (index) {
            case 1  -> this.entityData.get(DATA_PART1_ID);
            case 2  -> this.entityData.get(DATA_PART2_ID);
            case 3  -> this.entityData.get(DATA_PART3_ID);
            case 4  -> this.entityData.get(DATA_PART4_ID);
            case 5  -> this.entityData.get(DATA_PART5_ID);
            case 6  -> this.entityData.get(DATA_PART6_ID);
            case 7  -> this.entityData.get(DATA_PART7_ID);
            case 8  -> this.entityData.get(DATA_PART8_ID);
            case 9  -> this.entityData.get(DATA_PART9_ID);
            case 10 -> this.entityData.get(DATA_PART10_ID);
            case 11 -> this.entityData.get(DATA_PART11_ID);
            case 12 -> this.entityData.get(DATA_PART12_ID);
            default -> -1;
        };
    }

    private void setPartId(int index, int id) {
        switch (index) {
            case 1  -> this.entityData.set(DATA_PART1_ID,  id);
            case 2  -> this.entityData.set(DATA_PART2_ID,  id);
            case 3  -> this.entityData.set(DATA_PART3_ID,  id);
            case 4  -> this.entityData.set(DATA_PART4_ID,  id);
            case 5  -> this.entityData.set(DATA_PART5_ID,  id);
            case 6  -> this.entityData.set(DATA_PART6_ID,  id);
            case 7  -> this.entityData.set(DATA_PART7_ID,  id);
            case 8  -> this.entityData.set(DATA_PART8_ID,  id);
            case 9  -> this.entityData.set(DATA_PART9_ID,  id);
            case 10 -> this.entityData.set(DATA_PART10_ID, id);
            case 11 -> this.entityData.set(DATA_PART11_ID, id);
            case 12 -> this.entityData.set(DATA_PART12_ID, id);
        }
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            for (int i = 1; i <= TRAILING_PART_COUNT; i++) {
                int partId = getPartId(i);
                if (partId == this.getId()) continue; // safety guard
                discardExistingPart(serverLevel, partId);
            }
        }
        super.remove(reason);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        int[] ids = new int[TRAILING_PART_COUNT];
        for (int i = 0; i < TRAILING_PART_COUNT; i++) {
            ids[i] = getPartId(i + 1);
        }
        tag.putIntArray("PartIds", ids);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PartIds")) {
            int[] ids = tag.getIntArray("PartIds");
            for (int i = 1; i <= TRAILING_PART_COUNT; i++) {
                setPartId(i, i <= ids.length ? ids[i - 1] : -1);
            }
        }
        this.partsSpawned = getPartId(1) >= 0;
    }

    // =========================================================================
    // Inner goal classes (same pattern as UmiInu)
    // =========================================================================

    static class DetectPlayerGoal extends Goal {
        private final IkuchiEntity ikuchi;
        private int cooldown = 0;
        private static final double DETECT_RANGE = 48.0D;

        public DetectPlayerGoal(IkuchiEntity ikuchi) {
            this.ikuchi = ikuchi;
            this.setFlags(EnumSet.noneOf(Flag.class));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown-- > 0) return false;
            this.cooldown = 20;

            AABB box = new AABB(
                this.ikuchi.getX() - DETECT_RANGE, this.ikuchi.getY() - 24, this.ikuchi.getZ() - DETECT_RANGE,
                this.ikuchi.getX() + DETECT_RANGE, this.ikuchi.getY() + 24, this.ikuchi.getZ() + DETECT_RANGE
            );

            List<Player> candidates = this.ikuchi.level().getEntitiesOfClass(Player.class, box,
                p -> !p.isSpectator() && !p.isCreative()
                    && (p.isInWater() || p.getVehicle() instanceof Boat));

            if (candidates.isEmpty()) return false;

            Player nearest = null;
            double bestDist = Double.MAX_VALUE;
            for (Player p : candidates) {
                double d = this.ikuchi.distanceToSqr(p);
                if (d < bestDist) { bestDist = d; nearest = p; }
            }

            if (nearest == null) return false;
            this.ikuchi.setTarget(nearest);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.ikuchi.getTarget();
            if (target == null || !target.isAlive()) return false;
            if (target instanceof Player p) {
                if (!p.isInWater() && !(p.getVehicle() instanceof Boat)) {
                    this.ikuchi.setTarget(null);
                    return false;
                }
            }
            return true;
        }

        @Override
        public void stop() {}
    }

static class PursueGoal extends Goal {
    private final IkuchiEntity ikuchi;
    private final double speed;
    private int attackCooldown = 0;

    // Circling state
    private int circleTimer = 0;
    private float circleSide = 1.0F; // 1.0 = right, -1.0 = left
    private boolean ramming = false;
    private int ramTimer = 0;
    private static final int CIRCLE_DURATION = 60;  // ticks circling before switching or ramming
    private static final int RAM_DURATION = 30;      // ticks of straight ram pass

    public PursueGoal(IkuchiEntity ikuchi, double speed) {
        this.ikuchi = ikuchi;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity t = this.ikuchi.getTarget();
        return t != null && t.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity t = this.ikuchi.getTarget();
        return t != null && t.isAlive();
    }

    @Override
    public void start() {
        this.attackCooldown = 0;
        this.circleTimer = 0;
        this.ramming = false;
        this.ramTimer = 0;
        this.circleSide = this.ikuchi.random.nextBoolean() ? 1.0F : -1.0F;
    }

    @Override
    public void stop() {
        this.ikuchi.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity t = this.ikuchi.getTarget();
        if (t == null) return;

        if (this.attackCooldown > 0) this.attackCooldown--;

        double dx = t.getX() - this.ikuchi.getX();
        double dy = t.getY() - this.ikuchi.getY();
        double dz = t.getZ() - this.ikuchi.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (this.ikuchi.isInWater()) {
            Vec3 vel = this.ikuchi.getDeltaMovement();
            Vec3 moveTarget;

            if (this.ramming) {
                // Ram straight through the target
                ramTimer++;
                if (dist > 1.0E-6) {
                    double nx = dx / dist;
                    double ny = dy / dist;
                    double nz = dz / dist;
                    double ax = (nx * 1.1 - vel.x) * 0.10;
                    double ay = (ny * 1.1 - vel.y) * 0.10;
                    double az = (nz * 1.1 - vel.z) * 0.10;
                    this.ikuchi.setDeltaMovement(vel.add(ax, ay, az));
                }
                // End ram after duration or if we've passed through
                if (ramTimer >= RAM_DURATION || dist < 1.5) {
                    this.ramming = false;
                    this.ramTimer = 0;
                    this.circleTimer = 0;
                    // Switch sides after each ram
                    this.circleSide = -this.circleSide;
                }
            } else {
                // Circling behavior: aim at a point offset perpendicular to the target
                circleTimer++;

                // Switch to ram occasionally
                if (circleTimer >= CIRCLE_DURATION) {
                    // 60% chance to ram, 40% chance to keep circling on other side
                    if (this.ikuchi.random.nextFloat() < 0.6F) {
                        this.ramming = true;
                        this.ramTimer = 0;
                    } else {
                        this.circleSide = -this.circleSide;
                        this.circleTimer = 0;
                    }
                }

                // Compute perpendicular offset in XZ plane
                double orbitRadius = Math.min(dist * 1.2, 20.0);
                // Perpendicular to (dx, dz) is (-dz, dx) or (dz, -dx)
                double perpX = -dz * circleSide;
                double perpZ = dx * circleSide;
                double perpLen = Math.sqrt(perpX * perpX + perpZ * perpZ);
                if (perpLen > 1.0E-6) {
                    perpX = (perpX / perpLen) * orbitRadius;
                    perpZ = (perpZ / perpLen) * orbitRadius;
                }

                // Aim at target position + perpendicular offset
                double targetX = t.getX() + perpX - this.ikuchi.getX();
                double targetZ = t.getZ() + perpZ - this.ikuchi.getZ();
                double targetY = dy;
                double targetDist = Math.sqrt(targetX * targetX + targetY * targetY + targetZ * targetZ);

                if (targetDist > 1.0E-6) {
                    double nx = targetX / targetDist;
                    double ny = targetY / targetDist;
                    double nz = targetZ / targetDist;
                    double circleSpeed = 0.65;
                    double ax = (nx * circleSpeed - vel.x) * 0.5;
                    double ay = (ny * circleSpeed - vel.y) * 0.5;
                    double az = (nz * circleSpeed - vel.z) * 0.5;
                    this.ikuchi.setDeltaMovement(vel.add(ax, ay, az));
                }
            }

            if (!this.ikuchi.getNavigation().isDone()) {
                this.ikuchi.getNavigation().stop();
            }
        } else {
            this.ikuchi.getNavigation().moveTo(t, this.speed);
        }

        this.ikuchi.getLookControl().setLookAt(t, 8.0F, 8.0F);
        this.ikuchi.yBodyRot = this.ikuchi.getYRot();

        // Melee attack on close pass
        if (this.ikuchi.isWithinMeleeAttackRange(t) && this.attackCooldown <= 0) {
            this.attackCooldown = 20;
            if (!this.ikuchi.level().isClientSide) {
                if (t instanceof Player p && (p.isCreative() || p.isSpectator())) return;

                if (t instanceof Player p && p.getVehicle() instanceof Boat boat) {
                    try { boat.hurt(this.ikuchi.damageSources().mobAttack(this.ikuchi), 2.0F); } catch (Throwable ignored) {}
                }

                try { this.ikuchi.swing(net.minecraft.world.InteractionHand.MAIN_HAND); } catch (Throwable ignored) {}
                float dmg = (float) this.ikuchi.getAttributeValue(Attributes.ATTACK_DAMAGE);
                try { t.hurt(this.ikuchi.damageSources().mobAttack(this.ikuchi), dmg); } catch (Throwable ignored) {}
            }
        }
    }
}

static class WanderGoal extends Goal {
    private final IkuchiEntity ikuchi;
    private Vec3 wanderTarget;
    private int wanderTime = 0;
    private int idleTicks = 0;

        public WanderGoal(IkuchiEntity ikuchi) {
            this.ikuchi = ikuchi;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
    if (this.ikuchi.getTarget() != null) return false;
    if (this.idleTicks++ < 5) return false; // try again after 5 ticks idle
    this.idleTicks = 0;
            double angle = this.ikuchi.random.nextDouble() * Math.PI * 2.0;
            double dist = 16.0 + this.ikuchi.random.nextDouble() * 32.0;
            double tx = this.ikuchi.getX() + Math.cos(angle) * dist;
            double tz = this.ikuchi.getZ() + Math.sin(angle) * dist;
            double ty = this.ikuchi.getY() + (this.ikuchi.random.nextInt(5) - 2);
            BlockPos check = new BlockPos((int)tx, (int)ty, (int)tz);
            boolean inWater = false;
            for (int dy = -2; dy <= 2; dy++) {
                try {
                    if (this.ikuchi.level().getFluidState(check.offset(0, dy, 0)).is(FluidTags.WATER)) {
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
            if (this.ikuchi.getTarget() != null) return false;
            if (this.wanderTarget == null) return false;
            double dx = this.wanderTarget.x - this.ikuchi.getX();
            double dy = this.wanderTarget.y - this.ikuchi.getY();
            double dz = this.wanderTarget.z - this.ikuchi.getZ();
            return (dx*dx + dy*dy + dz*dz) > 1.0D && this.wanderTime < 600;
        }

@Override
public void start() {
    this.wanderTime = 0;
}

        @Override
        public void stop() {
    this.wanderTarget = null;
    this.idleTicks = 0;
    this.ikuchi.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.wanderTarget == null) return;
            this.wanderTime++;
            if (this.ikuchi.isInWater()) {
                double dx = this.wanderTarget.x - this.ikuchi.getX();
                double dy = this.wanderTarget.y - this.ikuchi.getY();
                double dz = this.wanderTarget.z - this.ikuchi.getZ();
                double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
                if (dist > 1.0E-6) {
                    double nx = dx / dist;
                    double ny = dy / dist;
                    double nz = dz / dist;
                    Vec3 vel = this.ikuchi.getDeltaMovement();
                    double ax = (nx * 0.45 - vel.x) * 0.12;
                    double ay = (ny * 0.45 - vel.y) * 0.12;
                    double az = (nz * 0.45 - vel.z) * 0.12;
                    this.ikuchi.setDeltaMovement(vel.add(ax, ay, az));
                }
                this.ikuchi.getLookControl().setLookAt(this.wanderTarget.x, this.wanderTarget.y, this.wanderTarget.z);
                if (!this.ikuchi.getNavigation().isDone()) {
                    this.ikuchi.getNavigation().stop();
                }
            }
        }
    }
}