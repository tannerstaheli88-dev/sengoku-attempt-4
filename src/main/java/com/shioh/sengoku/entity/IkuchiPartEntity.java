package com.shioh.sengoku.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * IkuchiPartEntity – a body segment that follows the Ikuchi head.
 * Uses look-vector smoothing (same pattern as DragonPartEntity) for stable chain movement.
 */
public class IkuchiPartEntity extends Monster {

    private static final EntityDataAccessor<Integer> DATA_PARENT_ID  = SynchedEntityData.defineId(IkuchiPartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART_NUMBER = SynchedEntityData.defineId(IkuchiPartEntity.class, EntityDataSerializers.INT);

    public static final float SEGMENT_DISTANCE = 2.2F;

    @Nullable
    private IkuchiEntity parentIkuchi;
    private int partNumber = 1;

    public IkuchiPartEntity(EntityType<? extends IkuchiPartEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.setNoAi(true);
    }

    public IkuchiPartEntity(EntityType<? extends IkuchiPartEntity> type, Level level, IkuchiEntity parent, int partNumber) {
        this(type, level);
        this.parentIkuchi = parent;
        this.partNumber = partNumber;
        this.entityData.set(DATA_PARENT_ID, parent.getId());
        this.entityData.set(DATA_PART_NUMBER, partNumber);

        Vec3 behind = parent.getLookAngle().normalize().scale(-SEGMENT_DISTANCE * partNumber);
        this.setPos(parent.getX() + behind.x, parent.getY(), parent.getZ() + behind.z);
        this.setYRot(parent.getYRot());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PARENT_ID,  -1);
        builder.define(DATA_PART_NUMBER, 1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 30.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.0D)
            .add(Attributes.FOLLOW_RANGE, 0.0D);
    }

    @Override
    protected void registerGoals() {
        // No AI
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canCollideWith(Entity other) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        this.fallDistance = 0.0F;

        if (!this.level().isClientSide) {
            if (!resolveParent()) {
                this.discard();
                return;
            }
            followSegment();
        }
    }

    private boolean resolveParent() {
        if (this.parentIkuchi != null && this.parentIkuchi.isAlive()) return true;

        int parentId = this.entityData.get(DATA_PARENT_ID);
        if (parentId >= 0 && this.level() instanceof ServerLevel serverLevel) {
            Entity e = serverLevel.getEntity(parentId);
            if (e instanceof IkuchiEntity ikuchi && ikuchi.isAlive()) {
                this.parentIkuchi = ikuchi;
                return true;
            }
        }
        return false;
    }

    private void followSegment() {
        if (this.parentIkuchi == null) return;

        int index = Math.max(1, this.entityData.get(DATA_PART_NUMBER));
        Entity lead = resolveLeadEntity(index);
        if (lead == null) return;

        Vec3 leadEye    = lead.getEyePosition();
        Vec3 currentEye = this.getEyePosition();
        Vec3 eyeToLead  = leadEye.subtract(currentEye);

        // Determine the target look direction toward the lead
        Vec3 targetLook = eyeToLead.lengthSqr() >= 1.0E-6D
            ? eyeToLead.normalize()
            : Vec3.directionFromRotation(lead.getXRot(), lead.getYRot());

        Vec3 currentLook = Vec3.directionFromRotation(this.getXRot(), this.getYRot()).normalize();

        // Allow faster turning for front segments, slower for tail — eel undulation
        double tailFactor = Mth.clamp((index - 1.0) / (IkuchiEntity.TRAILING_PART_COUNT - 1.0), 0.0, 1.0);
        double dot = Mth.clamp(currentLook.dot(targetLook), -1.0D, 1.0D);
        float angularError = (float) Math.toDegrees(Math.acos(dot));
        float maxStep = Mth.clamp(
            (float) Mth.lerp(tailFactor, 18.0F, 6.0F) + angularError * 0.4F,
            6.0F, 60.0F
        );

        Vec3 smoothedLook = rotateTowards(currentLook, targetLook, maxStep);

        // Position this segment exactly SEGMENT_DISTANCE behind the lead's eye
        double horiz = Math.sqrt(smoothedLook.x * smoothedLook.x + smoothedLook.z * smoothedLook.z);
        float newYaw   = (float)(Mth.atan2(smoothedLook.z, smoothedLook.x) * (180.0 / Math.PI)) - 90.0F;
        float newPitch = (float)(-Mth.atan2(smoothedLook.y, horiz) * (180.0 / Math.PI));
        newPitch = Mth.clamp(newPitch, -89.9F, 89.9F);

        Vec3 newEye  = leadEye.subtract(smoothedLook.scale(SEGMENT_DISTANCE));
        double eyeH  = this.getEyeHeight(this.getPose());
        Vec3 newFeet = newEye.subtract(0.0D, eyeH, 0.0D);

        this.setPos(newFeet.x, newFeet.y, newFeet.z);
        this.setYRot(newYaw);
        this.yBodyRot = newYaw;
        this.setYHeadRot(newYaw);
        this.setXRot(newPitch);
    }

    @Nullable
    private Entity resolveLeadEntity(int index) {
        if (index <= 1) return this.parentIkuchi;
        if (!(this.level() instanceof ServerLevel serverLevel)) return this.parentIkuchi;
        int previousId = this.parentIkuchi.getPartId(index - 1);
        Entity previous = previousId >= 0 ? serverLevel.getEntity(previousId) : null;
        return previous != null ? previous : this.parentIkuchi;
    }

    /** Spherical interpolation toward a target direction, capped at maxDegrees per tick. */
    private static Vec3 rotateTowards(Vec3 from, Vec3 to, float maxDegrees) {
        Vec3 fromN = from.lengthSqr() > 1.0E-8D ? from.normalize() : new Vec3(0, 0, 1);
        Vec3 toN   = to.lengthSqr()   > 1.0E-8D ? to.normalize()   : fromN;
        double dot = Mth.clamp(fromN.dot(toN), -1.0D, 1.0D);
        if (dot >= 0.999999D) return toN;
        double angle    = Math.acos(dot);
        double maxRad   = Math.toRadians(maxDegrees);
        if (angle <= maxRad) return toN;
        double t        = maxRad / angle;
        double sinAngle = Math.sin(angle);
        if (sinAngle < 1.0E-8D) return toN;
        double wFrom = Math.sin((1.0 - t) * angle) / sinAngle;
        double wTo   = Math.sin(t * angle) / sinAngle;
        Vec3 blended = fromN.scale(wFrom).add(toN.scale(wTo));
        return blended.lengthSqr() > 1.0E-8D ? blended.normalize() : toN;
    }

    // -------------------------------------------------------------------------
    // Damage — redirect to head
    // -------------------------------------------------------------------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.FALL)) return false;
        if (this.parentIkuchi != null && this.parentIkuchi.isAlive()) {
            return this.parentIkuchi.hurt(source, amount * 0.5F);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypes.IN_WALL)
            || source.is(DamageTypes.CRAMMING)
            || source.is(DamageTypes.FALL)
            || super.isInvulnerableTo(source);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }

    @Override protected net.minecraft.sounds.SoundEvent getDeathSound()                          { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound()                        { return null; }
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource s)             { return null; }

    // -------------------------------------------------------------------------
    // Save / Load
    // -------------------------------------------------------------------------

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ParentId", this.parentIkuchi != null ? this.parentIkuchi.getId() : this.entityData.get(DATA_PARENT_ID));
        tag.putInt("PartNumber", this.partNumber);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_PARENT_ID, tag.getInt("ParentId"));
        this.partNumber = tag.getInt("PartNumber");
        this.entityData.set(DATA_PART_NUMBER, this.partNumber);
    }

    public void setParentIkuchi(IkuchiEntity parent) {
        this.parentIkuchi = parent;
        this.entityData.set(DATA_PARENT_ID, parent.getId());
    }

    public int getPartNumber() { return this.partNumber; }
}