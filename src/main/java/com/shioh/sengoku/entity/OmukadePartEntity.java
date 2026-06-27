package com.shioh.sengoku.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * OmukadePart - Body segment that follows the main Omukade entity.
 * These are invisible, immobile entities that create a trailing centipede effect.
 */
public class OmukadePartEntity extends Monster {
    
    private static final EntityDataAccessor<Integer> DATA_PARENT_ID = SynchedEntityData.defineId(OmukadePartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART_NUMBER = SynchedEntityData.defineId(OmukadePartEntity.class, EntityDataSerializers.INT);
    
    private static final float SEGMENT_DISTANCE = 1.05F;
    private static final double MIN_SPACING_FACTOR = 0.88D;
    private static final double MOVE_START_STEP_SQR = 9.0E-4D;
    private static final double MOVE_STOP_STEP_SQR = 1.6E-4D;
    
    @Nullable
    private OmukadeEntity parentOmukade;
    private int partNumber = 1; // 1..TRAILING_PART_COUNT
    private boolean chainMotionActive = false;
    
    public OmukadePartEntity(EntityType<? extends OmukadePartEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;  // Parts are non-physical; they don't push or collide
        this.setNoGravity(true);  // Parts don't fall, they're positioned by logic
        this.setPersistenceRequired();
    }
    
    public OmukadePartEntity(EntityType<? extends OmukadePartEntity> type, Level level, OmukadeEntity parent, int partNumber) {
        this(type, level);
        this.parentOmukade = parent;
        this.partNumber = partNumber;
        this.entityData.set(DATA_PARENT_ID, parent.getId());
        this.entityData.set(DATA_PART_NUMBER, partNumber);

        // Spawn each segment immediately behind the parent chain.
        Vec3 behind = parent.getLookAngle().normalize().scale(-SEGMENT_DISTANCE * partNumber);
        this.setPos(parent.getX() + behind.x, parent.getY(), parent.getZ() + behind.z);
        this.setYRot(parent.getYRot());
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PARENT_ID, -1);
        builder.define(DATA_PART_NUMBER, 1);
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.0D) // Parts don't move on their own
            .add(Attributes.FOLLOW_RANGE, 0.0D); // No combat AI
    }
    
    @Override
    protected void registerGoals() {
        // Parts have no AI goals - they just follow
        super.registerGoals();
    }
    
    @Override
    public void tick() {
        super.tick();

        // Align part to actual movement direction and clear fall distance
        net.minecraft.world.phys.Vec3 movement = this.getDeltaMovement();
        double horizSpeedSq = movement.x * movement.x + movement.z * movement.z;
        if (horizSpeedSq > 1.0E-4) {
            float targetYaw = (float) Math.toDegrees(Math.atan2(-movement.x, movement.z));
            float diff = Mth.wrapDegrees(targetYaw - this.getYRot());
            float corrected = this.getYRot() + diff * 0.45F;
            this.setYRot(corrected);
            this.yBodyRot = corrected;
            this.setYHeadRot(corrected);
        }
        this.fallDistance = 0.0F;
        
        if (!this.level().isClientSide) {
            // Update parent reference if lost
            if (this.parentOmukade == null || !this.parentOmukade.isAlive()) {
                int parentId = this.entityData.get(DATA_PARENT_ID);
                if (parentId >= 0 && this.level() instanceof ServerLevel serverLevel) {
                    Entity entity = serverLevel.getEntity(parentId);
                    if (entity instanceof OmukadeEntity omukade) {
                        this.parentOmukade = omukade;
                    } else {
                        // Parent is gone, remove this part
                        this.discard();
                        return;
                    }
                }
            }
            
            // If we have a valid parent, follow it
            if (this.parentOmukade != null && this.parentOmukade.isAlive()) {
                this.followSegment();
            }
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }
    
    private void followSegment() {
        Entity lead = resolveLeadEntity();
        if (lead == null) return;

        // Track whether chain motion should be active; when idle we still solve latch constraints.
        if (this.parentOmukade != null) {
            Vec3 headNow = this.parentOmukade.position();
            Vec3 headPrev = new Vec3(this.parentOmukade.xo, this.parentOmukade.yo, this.parentOmukade.zo);
            double headStepSqr = headNow.distanceToSqr(headPrev);

            if (this.chainMotionActive) {
                if (headStepSqr < MOVE_STOP_STEP_SQR) {
                    this.chainMotionActive = false;
                }
            } else if (headStepSqr > MOVE_START_STEP_SQR) {
                this.chainMotionActive = true;
            }
        }

        Vec3 leadPos = lead.position();
        Vec3 current = this.position();
        Vec3 offsetFromLead = current.subtract(leadPos);
        double currentDistance = offsetFromLead.length();

        // Keep links from ever collapsing into each other.
        double minSpacing = SEGMENT_DISTANCE * MIN_SPACING_FACTOR;
        if (currentDistance > 1.0E-4D && currentDistance < minSpacing) {
            Vec3 away = offsetFromLead.normalize();
            Vec3 uncrunched = leadPos.add(away.scale(SEGMENT_DISTANCE));
            this.setPos(uncrunched.x, uncrunched.y, uncrunched.z);
            current = uncrunched;
            currentDistance = SEGMENT_DISTANCE;
        }

        int partIndex = Math.max(1, this.entityData.get(DATA_PART_NUMBER));
        double tailFactor = Mth.clamp((partIndex - 1) / 6.0D, 0.0D, 1.0D);

        // Blend toward lead motion while moving, but keep each link's local offset near stop.
        // Tail links keep more local offset so they don't drift toward the head when decelerating.
        Vec3 leadMotion = lead.position().subtract(lead.xo, lead.yo, lead.zo);
        double leadMotionSq = leadMotion.lengthSqr();
        Vec3 motionAway;
        if (leadMotionSq >= 1.0E-6D) {
            motionAway = leadMotion.normalize().scale(-1.0D);
        } else {
            Vec3 lookDir = lead.getLookAngle().normalize();
            if (lookDir.lengthSqr() < 1.0E-4D) {
                lookDir = Vec3.directionFromRotation(0.0F, lead.getYRot());
            }
            motionAway = lookDir.scale(-1.0D);
        }

        Vec3 localAway = offsetFromLead.lengthSqr() >= 1.0E-4D ? offsetFromLead.normalize() : motionAway;
        double speedBlend = Mth.clamp((leadMotionSq - 4.0E-5D) / 3.0E-3D, 0.0D, 1.0D);
        double motionWeight = speedBlend * (1.0D - tailFactor * 0.75D);
        Vec3 blendedAway = localAway.lerp(motionAway, motionWeight).normalize();
        Vec3 desired = leadPos.add(blendedAway.scale(SEGMENT_DISTANCE));

        // Slightly looser follow keeps a natural caboose effect.
        double tightness = Mth.clamp(0.96D - (partIndex - 1) * 0.07D, 0.65D, 0.92D);
        Vec3 next = this.chainMotionActive ? current.lerp(desired, tightness) : desired;

        this.setPos(next.x, next.y, next.z);

        // Orient horizontally only; disable pitch for Omukade body segments.
        Vec3 dirToLead = leadPos.subtract(next);
        float targetYaw = (float)(Math.toDegrees(Mth.atan2(dirToLead.z, dirToLead.x)) - 90.0F);

        float maxYawStepBase = (float) Mth.lerp(tailFactor, 12.0F, 3.0F);
        float yawDiff = Math.abs(Mth.wrapDegrees(targetYaw - this.getYRot()));
        float maxYawStep = Mth.clamp(maxYawStepBase + yawDiff * 0.70F, maxYawStepBase, 90.0F);
        float smoothedYaw = Mth.approachDegrees(this.getYRot(), targetYaw, maxYawStep);
        this.setYRot(smoothedYaw);
        this.setYBodyRot(smoothedYaw);
        this.setYHeadRot(smoothedYaw);
    }

    @Nullable
    private Entity resolveLeadEntity() {
        if (this.parentOmukade == null || !this.parentOmukade.isAlive()) return null;

        int partIndex = this.entityData.get(DATA_PART_NUMBER);
        if (partIndex <= 1) {
            return this.parentOmukade;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return this.parentOmukade;
        }

        int previousId = this.parentOmukade.getPartId(partIndex - 1);
        Entity previous = previousId >= 0 ? serverLevel.getEntity(previousId) : null;
        return previous != null ? previous : this.parentOmukade;
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.parentOmukade != null) {
            tag.putInt("ParentId", this.parentOmukade.getId());
        } else {
            tag.putInt("ParentId", this.entityData.get(DATA_PARENT_ID));
        }
        tag.putInt("PartNumber", this.partNumber);
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_PARENT_ID, tag.getInt("ParentId"));
        this.partNumber = tag.getInt("PartNumber");
        this.entityData.set(DATA_PART_NUMBER, this.partNumber);
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Don't transfer clipping/suffocation damage from segments to the head.
        if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING) || source.is(DamageTypes.FALL)) {
            return false;
        }

        // Parts redirect combat damage to parent.
        if (this.parentOmukade != null && this.parentOmukade.isAlive()) {
            return this.parentOmukade.hurt(source, amount * 0.5F);
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
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        // Parts should be silent on death; main head handles the death audio.
        return null;
    }
    
    public void setParentOmukade(OmukadeEntity parent) {
        this.parentOmukade = parent;
        this.entityData.set(DATA_PARENT_ID, parent.getId());
    }
    
    public int getPartNumber() {
        return this.partNumber;
    }
}
