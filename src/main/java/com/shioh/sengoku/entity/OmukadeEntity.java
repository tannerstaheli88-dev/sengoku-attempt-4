package com.shioh.sengoku.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.level.Level;

/**
 * Omukade - A giant centipede-like creature that uses spider AI and model.
 * Consists of a main body with 3 trailing part entities for a segmented appearance.
 */
public class OmukadeEntity extends Spider {

    public static final int TRAILING_PART_COUNT = 7;
    private static final float STEP_HEIGHT_VALUE = 2.0F;
    
    private static final EntityDataAccessor<Integer> DATA_PART1_ID = SynchedEntityData.defineId(OmukadeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART2_ID = SynchedEntityData.defineId(OmukadeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART3_ID = SynchedEntityData.defineId(OmukadeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART4_ID = SynchedEntityData.defineId(OmukadeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART5_ID = SynchedEntityData.defineId(OmukadeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART6_ID = SynchedEntityData.defineId(OmukadeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART7_ID = SynchedEntityData.defineId(OmukadeEntity.class, EntityDataSerializers.INT);
    
    private boolean partsSpawned = false;
    private int stepHeightApplyTimer = 0;
    
    public OmukadeEntity(EntityType<? extends OmukadeEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        try {
            java.lang.reflect.Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
            stepHeightField.setAccessible(true);
            stepHeightField.setFloat(this, STEP_HEIGHT_VALUE);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return com.shioh.sengoku.registry.SoundRegistry.OMUKADE_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSource) {
        return com.shioh.sengoku.registry.SoundRegistry.OMUKADE_HURT;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PART1_ID, -1);
        builder.define(DATA_PART2_ID, -1);
        builder.define(DATA_PART3_ID, -1);
        builder.define(DATA_PART4_ID, -1);
        builder.define(DATA_PART5_ID, -1);
        builder.define(DATA_PART6_ID, -1);
        builder.define(DATA_PART7_ID, -1);
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Spider.createAttributes()
            .add(Attributes.MAX_HEALTH, 50.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.35D)
            .add(Attributes.ATTACK_DAMAGE, 8.0D)
            .add(Attributes.FOLLOW_RANGE, 32.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
            .add(Attributes.STEP_HEIGHT, STEP_HEIGHT_VALUE);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            int poisonDuration = switch (this.level().getDifficulty()) {
                case EASY -> 60;
                case NORMAL -> 120;
                case HARD -> 200;
                default -> 0;
            };
            if (poisonDuration > 0) {
                living.addEffect(new MobEffectInstance(MobEffects.POISON, poisonDuration, 0), this);
            }
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof LlamaSpit) {
            amount *= 12.0F;
        }
        return super.hurt(source, amount);
    }
    
    @Override
    public void tick() {
        super.tick();

        try {
            if (!this.level().isClientSide) {
                maintainStepHeight();
            }
        } catch (Throwable ignored) {
        }

        // Always face the direction of actual movement to prevent backwards-facing glitch
        net.minecraft.world.phys.Vec3 movement = this.getDeltaMovement();
        double horizSpeedSq = movement.x * movement.x + movement.z * movement.z;
        if (horizSpeedSq > 1.0E-4) {
            float targetYaw = (float) Math.toDegrees(Math.atan2(-movement.x, movement.z));
            float diff = Mth.wrapDegrees(targetYaw - this.getYRot());
            float corrected = this.getYRot() + diff * 0.08F;  // Wider, more gradual turns
            this.setYRot(corrected);
            this.yBodyRot = corrected;
            this.setYHeadRot(corrected);
        }
        // Prevent spurious fall/landing sounds by clearing recorded fall distance each tick
        this.fallDistance = 0.0F;

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            // Maintain multipart chain: spawn on first tick and periodically repair if any part is missing.
            // Reduce check frequency to avoid frequent server-side entity creation which caused visible stutter.
            if (!partsSpawned || this.tickCount <= 2 || this.tickCount % 200 == 0) {
                ensureParts(serverLevel);
            }
        }
    }

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

        for (int i = 1; i <= TRAILING_PART_COUNT; i++) {
            discardExistingPart(serverLevel, getPartId(i));
            setPartId(i, -1);
        }

        Entity lead = this;
        for (int i = 1; i <= TRAILING_PART_COUNT; i++) {
            OmukadePartEntity part = (i == TRAILING_PART_COUNT)
                ? new OmukadeEndEntity(com.shioh.sengoku.registry.ModEntities.OMUKADE_END, this.level(), this, i)
                : new OmukadePartEntity(com.shioh.sengoku.registry.ModEntities.OMUKADE_PART, this.level(), this, i);
            placePartBehind(lead, part, 1.05D);
            serverLevel.addFreshEntity(part);
            setPartId(i, part.getId());
            lead = part;
        }

        this.partsSpawned = true;
    }

    private static void placePartBehind(Entity lead, OmukadePartEntity part, double distance) {
        net.minecraft.world.phys.Vec3 dir = lead.getLookAngle().normalize();
        if (dir.lengthSqr() < 1.0E-4D) {
            dir = net.minecraft.world.phys.Vec3.directionFromRotation(0.0F, lead.getYRot());
        }
        net.minecraft.world.phys.Vec3 pos = lead.position().subtract(dir.scale(distance));
        part.setPos(pos.x, pos.y, pos.z);
        part.setYRot(lead.getYRot());
    }

    private static OmukadePartEntity getPartEntity(ServerLevel level, int id) {
        if (id < 0) return null;
        Entity e = level.getEntity(id);
        return e instanceof OmukadePartEntity part && part.isAlive() ? part : null;
    }

    private static void discardExistingPart(ServerLevel level, int id) {
        if (id < 0) return;
        Entity e = level.getEntity(id);
        // Safety: only discard trailing part entities. If an ID is stale/corrupt and
        // points to the head (or anything else), skipping prevents recursive remove().
        if (e instanceof OmukadePartEntity) {
            e.discard();
        }
    }

    private void maintainStepHeight() {
        if (this.stepHeightApplyTimer <= 0) {
            try {
                java.lang.reflect.Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
                stepHeightField.setAccessible(true);
                float current = stepHeightField.getFloat(this);
                if (Float.compare(current, STEP_HEIGHT_VALUE) != 0) {
                    stepHeightField.setFloat(this, STEP_HEIGHT_VALUE);
                }
            } catch (Exception ignored) {
            }
            this.stepHeightApplyTimer = 40;
        } else {
            this.stepHeightApplyTimer--;
        }
    }
    
    @Override
    public void remove(RemovalReason reason) {
        // Remove attached parts when main entity is removed
        if (!this.level().isClientSide) {
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int i = 1; i <= TRAILING_PART_COUNT; i++) {
                    int partId = getPartId(i);
                    if (partId == this.getId()) {
                        // Corrupt/self-referential ID guard: never discard self here.
                        continue;
                    }
                    discardExistingPart(serverLevel, partId);
                }
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

        // Backward compatibility with earlier 3-part saves.
        if (tag.contains("PartIds")) {
            int[] ids = tag.getIntArray("PartIds");
            for (int i = 1; i <= TRAILING_PART_COUNT; i++) {
                setPartId(i, i <= ids.length ? ids[i - 1] : -1);
            }
        } else {
            setPartId(1, tag.getInt("Part1Id"));
            setPartId(2, tag.getInt("Part2Id"));
            setPartId(3, tag.getInt("Part3Id"));
            for (int i = 4; i <= TRAILING_PART_COUNT; i++) {
                setPartId(i, -1);
            }
        }

        this.partsSpawned = getPartId(1) >= 0;
    }

    public int getPartId(int index) {
        return switch (index) {
            case 1 -> this.entityData.get(DATA_PART1_ID);
            case 2 -> this.entityData.get(DATA_PART2_ID);
            case 3 -> this.entityData.get(DATA_PART3_ID);
            case 4 -> this.entityData.get(DATA_PART4_ID);
            case 5 -> this.entityData.get(DATA_PART5_ID);
            case 6 -> this.entityData.get(DATA_PART6_ID);
            case 7 -> this.entityData.get(DATA_PART7_ID);
            default -> -1;
        };
    }

    private void setPartId(int index, int id) {
        switch (index) {
            case 1 -> this.entityData.set(DATA_PART1_ID, id);
            case 2 -> this.entityData.set(DATA_PART2_ID, id);
            case 3 -> this.entityData.set(DATA_PART3_ID, id);
            case 4 -> this.entityData.set(DATA_PART4_ID, id);
            case 5 -> this.entityData.set(DATA_PART5_ID, id);
            case 6 -> this.entityData.set(DATA_PART6_ID, id);
            case 7 -> this.entityData.set(DATA_PART7_ID, id);
            default -> {
            }
        }
    }
}
