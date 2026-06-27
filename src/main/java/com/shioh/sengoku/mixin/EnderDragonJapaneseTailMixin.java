package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.DragonPartEntity;
import com.shioh.sengoku.entity.DragonPartEndEntity;
import com.shioh.sengoku.entity.DragonNeckEntity;
import com.shioh.sengoku.entity.DragonArmsEntity;
import com.shioh.sengoku.entity.DragonHeadEntity;
import com.shioh.sengoku.entity.DragonPartThinEntity;
import com.shioh.sengoku.entity.DragonTailEntity;
import com.shioh.sengoku.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

/**
 * Adds a trailing chain of custom dragon_part entities behind Ender Dragon.
 * Chain order: head(1) > neck(1) > arms(1) > body(8) > arms(1) > body(6) > end(1) > tail(3) = 22 segments.
 */
@Mixin(EnderDragon.class)
public abstract class EnderDragonJapaneseTailMixin extends Mob {

    // Total segments: 1 head + 1 neck + 1 arms + 8 body + 1 arms + 6 body + 1 end + 3 tail = 22
    @Unique
    private static final int SENGOKU_DRAGON_PART_COUNT = 22;

    @Unique
    private static final double SENGOKU_DRAGON_SEGMENT_SPACING = 2.0D;
    @Unique
    private static final double SENGOKU_HEAD_ANCHOR_Y_ADJUST = 0.0D;
    /** Maximum yaw change allowed per tick (degrees). Lower = smoother but slower turns. */
    @Unique
    private static final float SENGOKU_MAX_TURN_DEG_PER_TICK = 3.5F;

    @Unique
    private final int[] sengoku$dragonPartIds = new int[SENGOKU_DRAGON_PART_COUNT];

    @Unique
    private boolean sengoku$partsSpawned = false;
    @Unique
    private float sengoku$yawBeforeTick = 0.0F;

    protected EnderDragonJapaneseTailMixin(EntityType<? extends Mob> type, Level level) {
        super(type, level);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sengoku$initTailParts(EntityType<? extends EnderDragon> type, Level level, CallbackInfo ci) {
        Arrays.fill(this.sengoku$dragonPartIds, -1);
    }

    @Inject(method = "tick", at = @At("HEAD"), require = 0)
    private void sengoku$captureYawBeforeTick(CallbackInfo ci) {
        sengoku$yawBeforeTick = this.getYRot();
    }

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void sengoku$clampTurnRate(CallbackInfo ci) {
        float delta = Mth.wrapDegrees(this.getYRot() - sengoku$yawBeforeTick);
        if (Math.abs(delta) > SENGOKU_MAX_TURN_DEG_PER_TICK) {
            float clamped = sengoku$yawBeforeTick + Mth.clamp(delta, -SENGOKU_MAX_TURN_DEG_PER_TICK, SENGOKU_MAX_TURN_DEG_PER_TICK);
            this.setYRot(clamped);
            this.yBodyRot = clamped;
            this.yHeadRot = clamped;
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"), require = 0)
    private void sengoku$aiStepDragonParts(CallbackInfo ci) {
        sengoku$updateDragonParts();
    }

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void sengoku$tickDragonPartsFallback(CallbackInfo ci) {
        sengoku$updateDragonParts();
    }

    @Unique
    private void sengoku$updateDragonParts() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!this.isAlive()) {
            sengoku$discardAllParts(serverLevel);
            return;
        }

        if (!this.sengoku$partsSpawned || this.tickCount <= 2 || this.tickCount % 20 == 0) {
            sengoku$ensureParts(serverLevel);
        }
    }

    @Inject(method = "remove", at = @At("HEAD"), require = 0)
    private void sengoku$removeDragonParts(Entity.RemovalReason reason, CallbackInfo ci) {
        if (this.level() instanceof ServerLevel serverLevel) {
            sengoku$discardAllParts(serverLevel);
        }
    }

    @Unique
    private void sengoku$ensureParts(ServerLevel serverLevel) {
        for (int i = 0; i < SENGOKU_DRAGON_PART_COUNT; i++) {
            DragonPartEntity part = sengoku$getPartById(serverLevel, this.sengoku$dragonPartIds[i]);
            if (part == null || !part.isAlive()) {
                sengoku$respawnParts(serverLevel);
                return;
            }
        }

        this.sengoku$partsSpawned = true;
    }

    /**
     * Returns the entity type for the given segment index (0-based).
     * Chain layout:
     *   0       = head
     *   1       = neck
     *   2       = arms (front/shoulder)
     *   3-10    = body x8
     *   11      = arms (rear/hip)
    *   12-17   = thin body x6
     *   18      = end
     *   19-21   = tail x3
     */
    @Unique
    private EntityType<? extends DragonPartEntity> sengoku$getTypeForIndex(int index) {
        if (index == 0) return ModEntities.DRAGON_HEAD;
        if (index == 1) return ModEntities.DRAGON_NECK;
        if (index == 2) return ModEntities.DRAGON_ARMS;
        if (index >= 3 && index <= 10) return ModEntities.DRAGON_PART;
        if (index == 11) return ModEntities.DRAGON_ARMS;
        if (index >= 12 && index <= 17) return ModEntities.DRAGON_PART_THIN;
        if (index == 18) return ModEntities.DRAGON_PART_END;
        if (index >= 19 && index <= 21) return ModEntities.DRAGON_TAIL;
        return ModEntities.DRAGON_PART;
    }

    @Unique
    private DragonPartEntity sengoku$createPartForIndex(int index, EnderDragon dragon, int partNumber) {
        EntityType<? extends DragonPartEntity> type = sengoku$getTypeForIndex(index);
        if (type == ModEntities.DRAGON_HEAD) {
            return new DragonHeadEntity(ModEntities.DRAGON_HEAD, this.level(), dragon, partNumber);
        } else if (type == ModEntities.DRAGON_NECK) {
            return new DragonNeckEntity(ModEntities.DRAGON_NECK, this.level(), dragon, partNumber);
        } else if (type == ModEntities.DRAGON_ARMS) {
            return new DragonArmsEntity(ModEntities.DRAGON_ARMS, this.level(), dragon, partNumber);
        } else if (type == ModEntities.DRAGON_PART_THIN) {
            return new DragonPartThinEntity(ModEntities.DRAGON_PART_THIN, this.level(), dragon, partNumber);
        } else if (type == ModEntities.DRAGON_PART_END) {
            return new DragonPartEndEntity(ModEntities.DRAGON_PART_END, this.level(), dragon, partNumber);
        } else if (type == ModEntities.DRAGON_TAIL) {
            return new DragonTailEntity(ModEntities.DRAGON_TAIL, this.level(), dragon, partNumber);
        } else {
            return new DragonPartEntity(ModEntities.DRAGON_PART, this.level(), dragon, partNumber);
        }
    }

    @Unique
    private void sengoku$respawnParts(ServerLevel serverLevel) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        // Hard cleanup: remove any stale chain parts that belong to this dragon
        // even if they are no longer tracked by sengoku$dragonPartIds.
        sengoku$discardAllOwnedParts(serverLevel, dragon);
        sengoku$discardAllParts(serverLevel);

        Vec3 anchor = sengoku$getHeadAnchor();
        Vec3 look = dragon.getLookAngle().normalize();
        if (look.lengthSqr() < 1.0E-4D) {
            look = Vec3.directionFromRotation(0.0F, dragon.getYRot());
        }

        DragonPartEntity previous = null;
        for (int i = 0; i < SENGOKU_DRAGON_PART_COUNT; i++) {
            int partNumber = i + 1;
            DragonPartEntity part = sengoku$createPartForIndex(i, dragon, partNumber);
            // Spawn first segment at anchor, then extend backwards using each part's desired follow distance.
            double spacing = previous == null
                ? 0.0D
                : part.getPreferredDistanceToLead(previous);
            Vec3 pos = previous == null
                ? anchor
                : previous.position().subtract(look.scale(spacing));
            part.setPos(pos.x, pos.y, pos.z);
            part.setYRot(dragon.getYRot());
            part.yBodyRot = dragon.getYRot();
            part.setYHeadRot(dragon.getYRot());
            serverLevel.addFreshEntity(part);
            this.sengoku$dragonPartIds[i] = part.getId();
            previous = part;
        }

        this.sengoku$partsSpawned = true;
    }

    @Unique
    private void sengoku$discardAllOwnedParts(ServerLevel serverLevel, EnderDragon dragon) {
        for (DragonPartEntity part : serverLevel.getEntitiesOfClass(DragonPartEntity.class, dragon.getBoundingBox().inflate(256.0D))) {
            if (part.isOwnedBy(dragon)) {
                part.discard();
            }
        }
    }

    @Unique
    private void sengoku$discardAllParts(ServerLevel serverLevel) {
        for (int i = 0; i < this.sengoku$dragonPartIds.length; i++) {
            int id = this.sengoku$dragonPartIds[i];
            if (id >= 0) {
                Entity e = serverLevel.getEntity(id);
                if (e instanceof DragonPartEntity) {
                    e.discard();
                }
            }
            this.sengoku$dragonPartIds[i] = -1;
        }
        this.sengoku$partsSpawned = false;
    }

    @Unique
    private DragonPartEntity sengoku$getPartById(ServerLevel level, int id) {
        if (id < 0) {
            return null;
        }
        Entity e = level.getEntity(id);
        return e instanceof DragonPartEntity part ? part : null;
    }

    /**
     * Anchor point at the dragon's head (forward of body center).
     */
    @Unique
    private Vec3 sengoku$getHeadAnchor() {
        EnderDragon dragon = (EnderDragon) (Object) this;
        Vec3 look = dragon.getLookAngle().normalize();
        if (look.lengthSqr() < 1.0E-4D) {
            look = Vec3.directionFromRotation(0.0F, dragon.getYRot());
        }
        // Use the dragon's base/head position (not the eye), project BACKWARD along look to reach head
        Vec3 headPos = new Vec3(dragon.getX(), dragon.getY(), dragon.getZ());
        return headPos.subtract(look.scale(6.0D)).add(0.0D, SENGOKU_HEAD_ANCHOR_Y_ADJUST, 0.0D);
    }
}
