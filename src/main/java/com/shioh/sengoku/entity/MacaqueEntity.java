package com.shioh.sengoku.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Wolf;
import com.shioh.sengoku.entity.ai.MountLlamaGoal;
import com.shioh.sengoku.entity.ai.PackFollowGoal;
import com.shioh.sengoku.registry.ModEntities;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.util.RandomSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Clean Macaque entity implementation.
 * Keeps state for sitting and clears sitting when the entity starts moving.
 */
public class MacaqueEntity extends Animal {
    private static final Ingredient BREED_FOOD = Ingredient.of(Items.APPLE);

    private static final EntityDataAccessor<Boolean> DATA_SITTING = SynchedEntityData.defineId(MacaqueEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_GROOMING = SynchedEntityData.defineId(MacaqueEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_PLAYING = SynchedEntityData.defineId(MacaqueEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_BEING_GROOMED = SynchedEntityData.defineId(MacaqueEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_PACK_STATE = SynchedEntityData.defineId(MacaqueEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_GROOM_COOLDOWN = SynchedEntityData.defineId(MacaqueEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_GROOM_REQUEST = SynchedEntityData.defineId(MacaqueEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_FORCE_SITTING = SynchedEntityData.defineId(MacaqueEntity.class, EntityDataSerializers.BOOLEAN);
    // Per-pack role for settle phase: when pack enters SETTLE, each macaque receives
    // a deterministic 50% chance to be "pack-sitter" for the duration of the settle phase.
    // This is stable (derived from UUID) and won't change until the pack leaves settle.
    private static final EntityDataAccessor<Boolean> DATA_PACK_ROLE_SITTING = SynchedEntityData.defineId(MacaqueEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int PACK_STATE_WANDER = 0;
    public static final int PACK_STATE_SIT = 1;

    public MacaqueEntity(EntityType<? extends Animal> type, Level world) {
        super(type, world);
        try {
            java.lang.reflect.Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
            stepHeightField.setAccessible(true);
            stepHeightField.setFloat(this, 1.0F);
        } catch (Throwable ignored) {}
    }

    // (idle->sit auto-transition removed to avoid sitting while moving)

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 10.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SITTING, false);
        builder.define(DATA_GROOMING, false);
        builder.define(DATA_PLAYING, false);
        builder.define(DATA_BEING_GROOMED, false);
        builder.define(DATA_PACK_STATE, PACK_STATE_SIT);
        builder.define(DATA_GROOM_COOLDOWN, 0);
        builder.define(DATA_GROOM_REQUEST, 0);
        builder.define(DATA_FORCE_SITTING, false);
        builder.define(DATA_PACK_ROLE_SITTING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Flee from wolves who hunt them
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Wolf.class, 12.0F, 1.0D, 1.6D));
        this.goalSelector.addGoal(2, new PanicGoal(this, 1.5D));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0D, BREED_FOOD, false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
        // Pack behavior: follow a nearby moving macaque so they travel in groups
        this.goalSelector.addGoal(6, new PackFollowGoal(this, 0.8D, 10));
        // While the pack is in the sit phase, allow non-grooming members to free-roam
        this.goalSelector.addGoal(11, new com.shioh.sengoku.entity.ai.PackFreeRoamGoal(this, 0.9D, 12));
        // If the pack is idle for a while, occasionally wander together (lower priority)
        this.goalSelector.addGoal(16, new com.shioh.sengoku.entity.ai.PackWanderGoal(this, 0.9D, 12));
        // Occasionally mount llamas as a playful/riding behavior
        this.goalSelector.addGoal(7, new MountLlamaGoal(this, 1.0D, 10.0D, 20 * 6, 0.15));
        // Grooming / socializing
        this.goalSelector.addGoal(8, new com.shioh.sengoku.entity.ai.GroomGoal(this, 6));
        // Playful chasing
        //shitting ape
        this.goalSelector.addGoal(9, new com.shioh.sengoku.entity.ai.PlayWithPackGoal(this, 1.2D, 8));
        // Warm-water attraction (onsen)
        this.goalSelector.addGoal(10, new com.shioh.sengoku.entity.ai.WarmWaterAttractGoal(this));
        this.goalSelector.addGoal(12, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(13, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(14, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return BREED_FOOD.test(stack);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        try {
            return ModEntities.MACAQUE.create(level);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        try {
            tag.putBoolean("Sitting", this.entityData.get(DATA_SITTING));
        } catch (Throwable ignored) {}
        try {
            tag.putInt("PackState", this.entityData.get(DATA_PACK_STATE));
        } catch (Throwable ignored) {}
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        try {
            if (tag.contains("Sitting")) this.entityData.set(DATA_SITTING, tag.getBoolean("Sitting"));
        } catch (Throwable ignored) {}
        try {
            if (tag.contains("PackState")) this.entityData.set(DATA_PACK_STATE, tag.getInt("PackState"));
        } catch (Throwable ignored) {}
    }

    public boolean isSitting() {
        try {
            return this.entityData.get(DATA_SITTING);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public void setSitting(boolean sitting) {
        try {
            this.entityData.set(DATA_SITTING, sitting);
        } catch (Throwable ignored) {}
    }

    public int getPackState() {
        try { return this.entityData.get(DATA_PACK_STATE); } catch (Throwable ignored) { return PACK_STATE_SIT; }
    }

    public void setPackState(int state) {
        try {
            int prev = this.entityData.get(DATA_PACK_STATE);
            this.entityData.set(DATA_PACK_STATE, state);
            if (prev != state) {
                if (state == PACK_STATE_SIT) assignPackRolesForSettle();
                else assignClearPackRoles();
            }
        } catch (Throwable ignored) {}
    }

    private void assignPackRolesForSettle() {
        try {
            int radius = 10; // pack perimeter for settle
            var bbox = this.getBoundingBox().inflate(radius, 2.0D, radius);
            var list = this.level().getEntitiesOfClass(MacaqueEntity.class, bbox);
            // include self as well
            for (MacaqueEntity m : list) {
                try {
                    // deterministic 50% chance using UUID hash (stable across the settle phase)
                    boolean sitRole = ((m.getUUID().hashCode() & 1) == 0);
                    m.setPackRoleSitting(sitRole);
                    if (sitRole) {
                        m.setSitting(true);
                        m.setForceSitting(true);
                    } else {
                        m.setSitting(false);
                        m.setForceSitting(false);
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    private void assignClearPackRoles() {
        try {
            int radius = 12;
            var bbox = this.getBoundingBox().inflate(radius, 2.0D, radius);
            var list = this.level().getEntitiesOfClass(MacaqueEntity.class, bbox);
            for (MacaqueEntity m : list) {
                try {
                    m.setPackRoleSitting(false);
                    m.setForceSitting(false);
                    m.setSitting(false);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    public boolean isPackSitting() {
        return this.getPackState() == PACK_STATE_SIT;
    }

    public void setPackSitting(boolean sitting) {
        this.setPackState(sitting ? PACK_STATE_SIT : PACK_STATE_WANDER);
    }

    public boolean isGrooming() {
        try { return this.entityData.get(DATA_GROOMING); } catch (Throwable ignored) { return false; }
    }

    public void setGrooming(boolean grooming) {
        try { this.entityData.set(DATA_GROOMING, grooming); } catch (Throwable ignored) {}
    }

    public boolean isBeingGroomed() {
        try { return this.entityData.get(DATA_BEING_GROOMED); } catch (Throwable ignored) { return false; }
    }

    public void setBeingGroomed(boolean being) {
        try { this.entityData.set(DATA_BEING_GROOMED, being); } catch (Throwable ignored) {}
    }

    public boolean isPlaying() {
        try { return this.entityData.get(DATA_PLAYING); } catch (Throwable ignored) { return false; }
    }

    public void setPlaying(boolean playing) {
        try { this.entityData.set(DATA_PLAYING, playing); } catch (Throwable ignored) {}
    }

    public int getGroomCooldown() {
        try { return this.entityData.get(DATA_GROOM_COOLDOWN); } catch (Throwable ignored) { return 0; }
    }

    public void setGroomCooldown(int ticks) {
        try { this.entityData.set(DATA_GROOM_COOLDOWN, Math.max(0, ticks)); } catch (Throwable ignored) {}
    }

    public boolean isForceSitting() {
        try { return this.entityData.get(DATA_FORCE_SITTING); } catch (Throwable ignored) { return false; }
    }

    public void setForceSitting(boolean force) {
        try { this.entityData.set(DATA_FORCE_SITTING, force); } catch (Throwable ignored) {}
    }

    public boolean isPackRoleSitting() {
        try { return this.entityData.get(DATA_PACK_ROLE_SITTING); } catch (Throwable ignored) { return false; }
    }

    public void setPackRoleSitting(boolean sit) {
        try { this.entityData.set(DATA_PACK_ROLE_SITTING, sit); } catch (Throwable ignored) {}
    }

    @Override
    public void tick() {
        super.tick();
        try {
            // Ensure each macaque deterministically adopts its pack-role while pack is settled.
            try {
                if (this.isPackSitting()) {
                    boolean sitRole = ((this.getUUID().hashCode() & 1) == 0);
                    // persist the role locally for debug/inspection
                    this.setPackRoleSitting(sitRole);
                    if (sitRole) {
                        // enforce sitting visuals while settled
                        if (!this.isSitting()) this.setSitting(true);
                        if (!this.isForceSitting()) this.setForceSitting(true);
                    } else {
                        // roaming-role: ensure not forced-sitting; allow roaming behaviors
                        if (this.isSitting() && !this.isBeingGroomed()) this.setSitting(false);
                        if (this.isForceSitting()) this.setForceSitting(false);
                    }
                } else {
                    // when leaving settle, clear any per-pack role UI state
                    if (this.isPackRoleSitting()) {
                        this.setPackRoleSitting(false);
                        this.setForceSitting(false);
                        this.setSitting(false);
                    }
                }
            } catch (Throwable ignored) {}

            if (this.isSitting()) {
                Vec3 delta = this.getDeltaMovement();
                double dx = delta.x;
                double dy = delta.y;
                double dz = delta.z;
                boolean wouldClear = (dx*dx + dy*dy + dz*dz > 1.0E-6D || !this.onGround());
                // don't clear sitting if the entity is being groomed or sitting is forced
                if (wouldClear && !this.isBeingGroomed() && !this.isForceSitting()) {
                    this.setSitting(false);
                }
            }
            // decrement groom cooldown if present
            try {
                int cd = this.entityData.get(DATA_GROOM_COOLDOWN);
                if (cd > 0) this.entityData.set(DATA_GROOM_COOLDOWN, cd - 1);
            } catch (Throwable ignored) {}
            // (long-idle -> sit auto-transition removed)
            // collision -> request grooming: only apply when pack sit and this is a roaming macaque
            try {
                if (this.isPackSitting() && !this.isSitting() && !this.isGrooming() && !this.isBeingGroomed()) {
                    var nearby = this.level().getEntitiesOfClass(MacaqueEntity.class, this.getBoundingBox().inflate(1.0D, 1.0D, 1.0D));
                    for (MacaqueEntity m : nearby) {
                        if (m == this) continue;
                        try {
                            if (m.isSitting() && !m.isGrooming() && !m.isBeingGroomed() && m.getGroomRequest() == 0 && m.getGroomCooldown() == 0) {
                                // sitting monkey should start grooming this roaming macaque
                                m.setGroomRequest(this.getId());
                                // mark this macaque as being groomed so it stops moving immediately
                                this.setBeingGroomed(true);
                                break;
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }

    public int getGroomRequest() {
        try { return this.entityData.get(DATA_GROOM_REQUEST); } catch (Throwable ignored) { return 0; }
    }

    public void setGroomRequest(int id) {
        try { this.entityData.set(DATA_GROOM_REQUEST, id); } catch (Throwable ignored) {}
    }

    public static boolean checkMacaqueSpawnRules(EntityType<MacaqueEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        try {
            if (level.getBlockState(pos.below()).getFluidState().is(net.minecraft.tags.FluidTags.WATER)) return false;
            if (spawnType == MobSpawnType.SPAWN_EGG) return checkMobSpawnRules(type, level, spawnType, pos, random);
            int maxLook = 8;
            boolean foundSolidNearby = false;
            for (int dx = -1; dx <= 1 && !foundSolidNearby; dx++) {
                for (int dz = -1; dz <= 1 && !foundSolidNearby; dz++) {
                    BlockPos base = pos.offset(dx, 0, dz);
                    for (int i = 1; i <= maxLook; i++) {
                        BlockPos check = base.below(i);
                        net.minecraft.world.level.block.state.BlockState bs = level.getBlockState(check);
                        if (bs.isAir()) continue;
                        if (bs.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) break;
                        foundSolidNearby = true;
                        break;
                    }
                }
            }
            if (!foundSolidNearby) return false;
        } catch (Throwable ignored) {}
        return checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        try { return SoundRegistry.MACAQUE_AMBIENT; } catch (Throwable ignored) { return null; }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        try { return SoundRegistry.MACAQUE_HURT; } catch (Throwable ignored) { return null; }
    }

    @Override
    protected SoundEvent getDeathSound() {
        try { return SoundRegistry.MACAQUE_DEATH; } catch (Throwable ignored) { return null; }
    }
}
