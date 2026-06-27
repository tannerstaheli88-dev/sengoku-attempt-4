package com.shioh.sengoku.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * DragonPart - lightweight trailing body segment attached to the Ender Dragon.
 */
public class DragonPartEntity extends Monster {

    private static final EntityDataAccessor<Integer> DATA_PARENT_ID = SynchedEntityData.defineId(DragonPartEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_PART_NUMBER = SynchedEntityData.defineId(DragonPartEntity.class, EntityDataSerializers.INT);

    // 16 pixels = 1 block. These match requested model lengths.
    private static final float SEGMENT_DISTANCE = 1.5F; // 24 px
    private static final double DRAGON_PART_END_DISTANCE = 1.5D; // 28 px
    private static final double DRAGON_TAIL_DISTANCE = 3.125D; // 50 px
    private static final double MIN_SPACING_FACTOR = 0.98D;
    // F3+B look vector is rendered at 2 blocks long in vanilla debug view.
    private static final double DEBUG_LOOK_LINE_LENGTH = 2.0D;
    private static final double TAIL_ANCHOR_BACK_OFFSET = 6.0D;
    private static final double PERCH_ORBIT_RADIUS = 9.5D;
    private static final double PERCH_ORBIT_RADIUS_STEP = 0.08D;
    private static final float PERCH_ORBIT_SPEED_DEG = 5.0F;
    private static final double PERCH_ORBIT_CENTER_Y = 5.5D;
    private static final double PERCH_ORBIT_HEIGHT_WAVE = 0.6D;
    private static final double PERCH_ORBIT_TRAIL_STEP = 0.24D;
    private static final double LATE_LANDING_PODIUM_RADIUS = 34.0D;
    private static final double LATE_LANDING_MAX_SPEED_SQR = 0.16D;
    private static final double PART_RENDER_DISTANCE_BLOCKS = 320.0D;
    // Lower the anchor slightly from eye height (negative moves downward)
    private static final double TAIL_ANCHOR_Y_OFFSET = 0.0D;

    @Nullable
    private EnderDragon parentDragon;
    @Nullable
    private UUID parentUuid;
    private int partNumber = 1;

    public DragonPartEntity(EntityType<? extends DragonPartEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.setNoAi(true);
    }

    public DragonPartEntity(EntityType<? extends DragonPartEntity> type, Level level, EnderDragon parent, int partNumber) {
        this(type, level);
        this.parentDragon = parent;
        this.parentUuid = parent.getUUID();
        this.partNumber = Math.max(1, partNumber);
        this.entityData.set(DATA_PARENT_ID, parent.getId());
        this.entityData.set(DATA_PART_NUMBER, this.partNumber);

        Vec3 anchor = getDragonTailAnchor();
        Vec3 look = parent.getLookAngle().normalize();
        if (look.lengthSqr() < 1.0E-4D) {
            look = Vec3.directionFromRotation(0.0F, parent.getYRot());
        }

        // Place partNumber==1 at the anchor (no offset), then subsequent parts behind it
        Vec3 spawnPos = anchor.subtract(look.scale(SEGMENT_DISTANCE * (this.partNumber - 1)));
        this.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        this.setYRot(parent.getYRot());
        this.yBodyRot = this.getYRot();
        this.setYHeadRot(this.getYRot());
        this.setXRot(parent.getXRot());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PARENT_ID, -1);
        builder.define(DATA_PART_NUMBER, 1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 12.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.0D)
            .add(Attributes.FOLLOW_RANGE, 0.0D);
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
        if (this.parentDragon != null && this.parentDragon.isAlive()) {
            return true;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        int parentId = this.entityData.get(DATA_PARENT_ID);
        if (parentId >= 0) {
            Entity byId = serverLevel.getEntity(parentId);
            if (byId instanceof EnderDragon dragon && dragon.isAlive()) {
                this.parentDragon = dragon;
                this.parentUuid = dragon.getUUID();
                return true;
            }
        }

        if (this.parentUuid != null) {
            Entity byUuid = serverLevel.getEntity(this.parentUuid);
            if (byUuid instanceof EnderDragon dragon && dragon.isAlive()) {
                this.parentDragon = dragon;
                this.entityData.set(DATA_PARENT_ID, dragon.getId());
                return true;
            }
        }

        return false;
    }

    private void followSegment() {
        if (this.parentDragon == null) {
            return;
        }

        int index = Math.max(1, this.entityData.get(DATA_PART_NUMBER));
        if (isPerching()) {
            orbitDuringPerch(index);
            return;
        }

        Entity previous = null;
        Vec3 leadPos;
        float leadYaw;
        float leadPitch;

        if (index <= 1) {
            leadPos = getDragonTailAnchor();
            Vec3 parentLook = this.parentDragon.getLookAngle().normalize();
            if (parentLook.lengthSqr() < 1.0E-6D) {
                parentLook = Vec3.directionFromRotation(this.parentDragon.getXRot(), this.parentDragon.getYRot());
            }
            double horiz = Math.sqrt(parentLook.x * parentLook.x + parentLook.z * parentLook.z);
            leadYaw = (float)(Mth.atan2(parentLook.z, parentLook.x) * (180D / Math.PI)) - 90F;
            leadPitch = (float)(-Mth.atan2(parentLook.y, horiz) * (180D / Math.PI));
            // Front-most segment should stay exactly on the anchor point.
            this.setPos(leadPos.x, leadPos.y, leadPos.z);
            this.setYRot(leadYaw);
            this.yBodyRot = leadYaw;
            this.setYHeadRot(leadYaw);
            this.setXRot(leadPitch);
            return;
        } else {
            previous = findPreviousPart(index - 1);
            if (previous == null) {
                leadPos = getDragonTailAnchor();
                leadYaw = this.parentDragon.getYRot();
                leadPitch = this.parentDragon.getXRot();
            } else {
                leadPos = previous.position();
                leadYaw = previous.getYRot();
                leadPitch = previous.getXRot();
            }
        }

        Vec3 leadEye = previous != null ? previous.getEyePosition() : leadPos;
        Vec3 currentEye = this.getEyePosition();
        Vec3 eyeToLead = leadEye.subtract(currentEye);
        Vec3 targetLook = eyeToLead.lengthSqr() >= 1.0E-6D
            ? eyeToLead.normalize()
            : Vec3.directionFromRotation(leadPitch, leadYaw);

        Vec3 currentLook = Vec3.directionFromRotation(this.getXRot(), this.getYRot()).normalize();
        double lookDot = Mth.clamp(currentLook.dot(targetLook), -1.0D, 1.0D);
        float angularErrorDeg = (float)Math.toDegrees(Math.acos(lookDot));
        float maxAngularStepDeg = Mth.clamp(10.0F + angularErrorDeg * 0.45F - (index * 0.35F), 8.0F, 42.0F);
        Vec3 smoothedLook = rotateTowards(currentLook, targetLook, maxAngularStepDeg);

        double horizDistance = Math.sqrt(smoothedLook.x * smoothedLook.x + smoothedLook.z * smoothedLook.z);
        float smoothedYaw = (float)(Mth.atan2(smoothedLook.z, smoothedLook.x) * (180D / Math.PI)) - 90F;
        float smoothedPitch = (float)(-Mth.atan2(smoothedLook.y, horizDistance) * (180D / Math.PI));
        smoothedPitch = Mth.clamp(smoothedPitch, -89.9F, 89.9F);

        Vec3 snappedEye = leadEye.subtract(smoothedLook.scale(DEBUG_LOOK_LINE_LENGTH));
        double eyeHeight = this.getEyeHeight(this.getPose());
        Vec3 snappedFeet = snappedEye.subtract(0.0D, eyeHeight, 0.0D);
        this.setPos(snappedFeet.x, snappedFeet.y, snappedFeet.z);

        this.setYRot(smoothedYaw);
        this.yBodyRot = smoothedYaw;
        this.setYHeadRot(smoothedYaw);
        this.setXRot(smoothedPitch);
    }

private void orbitDuringPerch(int index) {
    if (this.parentDragon == null) return;

    if (index <= 1) {
        // Segment 1 still uses the calculated orbit position as its anchor
        double radius = PERCH_ORBIT_RADIUS;
        double orbitAngle = Math.toRadians((this.parentDragon.tickCount * PERCH_ORBIT_SPEED_DEG) % 360.0D);
        Vec3 center = this.parentDragon.position().add(0.0D, PERCH_ORBIT_CENTER_Y, 0.0D);
        double orbitX = Math.cos(orbitAngle) * radius;
        double orbitZ = Math.sin(orbitAngle) * radius;
        double orbitY = center.y + Math.sin(orbitAngle * 2.0D) * PERCH_ORBIT_HEIGHT_WAVE;
        Vec3 targetPos = new Vec3(center.x + orbitX, orbitY, center.z + orbitZ);

        Vec3 tangent = new Vec3(-Math.sin(orbitAngle), 0.15D * Math.cos(orbitAngle * 2.0D), Math.cos(orbitAngle)).normalize();
        double horizDistance = Math.sqrt(tangent.x * tangent.x + tangent.z * tangent.z);
        float yaw = (float)(Mth.atan2(tangent.z, tangent.x) * (180D / Math.PI)) - 90F;
        yaw = Mth.wrapDegrees(yaw + 180.0F);
        float pitch = (float)(-Mth.atan2(tangent.y, horizDistance) * (180D / Math.PI));
        pitch = Mth.clamp(pitch, -89.9F, 89.9F);

        this.setPos(targetPos.x, targetPos.y, targetPos.z);
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.setYHeadRot(yaw);
        this.setXRot(pitch);
        return;
    }

    // All subsequent segments chase the eye of the previous segment, same as followSegment
    Entity previous = findPreviousPart(index - 1);
    if (previous == null) return;

    Vec3 leadEye = previous.getEyePosition();
    Vec3 currentEye = this.getEyePosition();
    Vec3 eyeToLead = leadEye.subtract(currentEye);
    Vec3 targetLook = eyeToLead.lengthSqr() >= 1.0E-6D
        ? eyeToLead.normalize()
        : Vec3.directionFromRotation(previous.getXRot(), previous.getYRot());

    Vec3 currentLook = Vec3.directionFromRotation(this.getXRot(), this.getYRot()).normalize();
    double lookDot = Mth.clamp(currentLook.dot(targetLook), -1.0D, 1.0D);
    float angularErrorDeg = (float)Math.toDegrees(Math.acos(lookDot));
    // Softer turn rate than normal movement so the orbit still looks fluid
    float maxAngularStepDeg = Mth.clamp(6.0F + angularErrorDeg * 0.3F, 4.0F, 25.0F);
    Vec3 smoothedLook = rotateTowards(currentLook, targetLook, maxAngularStepDeg);

    double horizDistance = Math.sqrt(smoothedLook.x * smoothedLook.x + smoothedLook.z * smoothedLook.z);
    float smoothedYaw = (float)(Mth.atan2(smoothedLook.z, smoothedLook.x) * (180D / Math.PI)) - 90F;
    float smoothedPitch = (float)(-Mth.atan2(smoothedLook.y, horizDistance) * (180D / Math.PI));
    smoothedPitch = Mth.clamp(smoothedPitch, -89.9F, 89.9F);

    double segmentLength = getPreferredDistanceToLead(previous);
    Vec3 snappedEye = leadEye.subtract(smoothedLook.scale(segmentLength));
    double eyeHeight = this.getEyeHeight(this.getPose());
    Vec3 snappedFeet = snappedEye.subtract(0.0D, eyeHeight, 0.0D);

    this.setPos(snappedFeet.x, snappedFeet.y, snappedFeet.z);
    this.setYRot(smoothedYaw);
    this.yBodyRot = smoothedYaw;
    this.setYHeadRot(smoothedYaw);
    this.setXRot(smoothedPitch);
}

    private boolean isPerching() {
        if (this.parentDragon == null) {
            return false;
        }

        var currentPhase = this.parentDragon.getPhaseManager().getCurrentPhase().getPhase();
        if (currentPhase == EnderDragonPhase.LANDING || currentPhase == EnderDragonPhase.LANDING_APPROACH) {
            // Start orbiting near the end of landing when the dragon is close to the podium and slowing down.
            Vec3 pos = this.parentDragon.position();
            double horizontalDistToCenter = Math.sqrt(pos.x * pos.x + pos.z * pos.z);
            double speedSqr = this.parentDragon.getDeltaMovement().lengthSqr();
            if (horizontalDistToCenter <= LATE_LANDING_PODIUM_RADIUS && speedSqr <= LATE_LANDING_MAX_SPEED_SQR) {
                return true;
            }
        }

        return currentPhase == EnderDragonPhase.SITTING_SCANNING
            || currentPhase == EnderDragonPhase.SITTING_ATTACKING
            || currentPhase == EnderDragonPhase.SITTING_FLAMING;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double maxDistance = PART_RENDER_DISTANCE_BLOCKS * PART_RENDER_DISTANCE_BLOCKS;
        return distance < maxDistance;
    }

    private static Vec3 rotateTowards(Vec3 from, Vec3 to, float maxDegrees) {
        Vec3 fromNorm = from.lengthSqr() > 1.0E-8D ? from.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 toNorm = to.lengthSqr() > 1.0E-8D ? to.normalize() : fromNorm;

        double dot = Mth.clamp(fromNorm.dot(toNorm), -1.0D, 1.0D);
        if (dot >= 0.999999D) {
            return toNorm;
        }

        double angle = Math.acos(dot);
        double maxRadians = Math.toRadians(maxDegrees);
        if (angle <= maxRadians) {
            return toNorm;
        }

        double t = maxRadians / angle;
        double sinAngle = Math.sin(angle);
        if (sinAngle < 1.0E-8D) {
            return toNorm;
        }

        double fromWeight = Math.sin((1.0D - t) * angle) / sinAngle;
        double toWeight = Math.sin(t * angle) / sinAngle;
        Vec3 blended = fromNorm.scale(fromWeight).add(toNorm.scale(toWeight));
        return blended.lengthSqr() > 1.0E-8D ? blended.normalize() : toNorm;
    }

    @Nullable
    private Entity findPreviousPart(int previousNumber) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        for (DragonPartEntity part : serverLevel.getEntitiesOfClass(DragonPartEntity.class, this.parentDragon.getBoundingBox().inflate(96.0D))) {
            if (part != this
                && part.parentDragon != null
                && this.parentDragon != null
                && part.parentDragon.getUUID().equals(this.parentDragon.getUUID())
                && part.getPartNumber() == previousNumber
                && part.isAlive()) {
                return part;
            }
        }

        return null;
    }

    private Vec3 getDragonTailAnchor() {
        if (this.parentDragon == null) {
            return this.position();
        }
        Vec3 look = this.parentDragon.getLookAngle().normalize();
        if (look.lengthSqr() < 1.0E-4D) {
            look = Vec3.directionFromRotation(0.0F, this.parentDragon.getYRot());
        }
        // Anchor off the dragon head tip (use base/head position, project BACKWARD along look)
        Vec3 headPos = new Vec3(this.parentDragon.getX(), this.parentDragon.getY(), this.parentDragon.getZ());
        return headPos.subtract(look.scale(TAIL_ANCHOR_BACK_OFFSET)).add(0.0D, TAIL_ANCHOR_Y_OFFSET, 0.0D);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.parentDragon != null && this.parentDragon.isAlive()) {
            return this.parentDragon.hurt(source, amount * 0.35F);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        int id = this.parentDragon != null ? this.parentDragon.getId() : this.entityData.get(DATA_PARENT_ID);
        tag.putInt("ParentId", id);
        if (this.parentUuid != null) {
            tag.putUUID("ParentUuid", this.parentUuid);
        }
        tag.putInt("PartNumber", this.entityData.get(DATA_PART_NUMBER));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_PARENT_ID, tag.getInt("ParentId"));
        if (tag.hasUUID("ParentUuid")) {
            this.parentUuid = tag.getUUID("ParentUuid");
        }
        this.partNumber = Math.max(1, tag.getInt("PartNumber"));
        this.entityData.set(DATA_PART_NUMBER, this.partNumber);
    }

    public int getPartNumber() {
        return this.entityData.get(DATA_PART_NUMBER);
    }

    public void setParentDragon(EnderDragon dragon, int number) {
        this.parentDragon = dragon;
        this.parentUuid = dragon.getUUID();
        this.partNumber = Math.max(1, number);
        this.entityData.set(DATA_PARENT_ID, dragon.getId());
        this.entityData.set(DATA_PART_NUMBER, this.partNumber);
    }

    public double getPreferredDistanceToLead(@Nullable Entity lead) {
        if (this instanceof DragonNeckEntity) {
            return 3.0D; // 48 px (3 blocks)
        }

        if (lead instanceof DragonNeckEntity) {
            return 3.0D; // Keep the segment after neck spaced for the long neck piece
        }

        if (this instanceof DragonPartEndEntity) {
            return DRAGON_PART_END_DISTANCE;
        }

        if (this instanceof DragonTailEntity) {
            return DRAGON_TAIL_DISTANCE;
        }

        return SEGMENT_DISTANCE;
    }

    public boolean isOwnedBy(EnderDragon dragon) {
        if (dragon == null) {
            return false;
        }

        if (this.parentDragon != null && this.parentDragon.getUUID().equals(dragon.getUUID())) {
            return true;
        }

        if (this.parentUuid != null && this.parentUuid.equals(dragon.getUUID())) {
            return true;
        }

        return this.entityData.get(DATA_PARENT_ID) == dragon.getId();
    }
}
