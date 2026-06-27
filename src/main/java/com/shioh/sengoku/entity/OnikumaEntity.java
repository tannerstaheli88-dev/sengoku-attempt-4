package com.shioh.sengoku.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.LightLayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import org.jetbrains.annotations.Nullable;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Onikuma - heavy bear-like brute yokai (former zombie datapack variant).
 * Higher health, slower movement, strong melee damage.
 */
public class OnikumaEntity extends Monster {

    // Step-height maintenance (prevents getting stuck on single-block obstacles)
    private int stepHeightApplyTimer = 0;

    public OnikumaEntity(EntityType<? extends OnikumaEntity> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        try {
            java.lang.reflect.Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
            stepHeightField.setAccessible(true);
            stepHeightField.setFloat(this, 1.5F);
        } catch (Exception ignored) {}
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        // Correct mid-air underground spawns: snap down up to 12 blocks to first solid, non-lava block.
        if (spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION || spawnType == MobSpawnType.STRUCTURE) {
            BlockPos.MutableBlockPos cursor = this.blockPosition().mutable().move(0, -1, 0);
            int steps = 0;
            while (steps < 12 && level.getBlockState(cursor).isAir()) {
                cursor.move(0, -1, 0);
                steps++;
            }
            BlockState ground = level.getBlockState(cursor);
            if (ground.blocksMotion() && !ground.getFluidState().is(FluidTags.LAVA)) {
                // Teleport entity to sit just above ground center
                this.teleportTo(cursor.getX() + 0.5D, cursor.getY() + 1, cursor.getZ() + 0.5D);
            }
        }
        return spawnData;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D) 
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 2.0D) 
                .add(Attributes.STEP_HEIGHT, 1.5D);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        try {
            if (!this.level().isClientSide) maintainStepHeight();
        } catch (Throwable ignored) {}
    }

    private void maintainStepHeight() {
        if (this.stepHeightApplyTimer <= 0) {
            try {
                java.lang.reflect.Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
                stepHeightField.setAccessible(true);
                float current = stepHeightField.getFloat(this);
                if (Float.compare(current, 1.5F) != 0) {
                    stepHeightField.setFloat(this, 1.5F);
                }
            } catch (Exception ignored) {}
            this.stepHeightApplyTimer = 40;
        } else {
            this.stepHeightApplyTimer--;
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        // Target vanilla illagers
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Pillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Vindicator.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Evoker.class, true));
        // Target clan units (all three clans)
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, KobayakawaAshigaruEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, KobayakawaSamuraiEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, KobayakawaSoheiEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, TakedaAshigaruEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, TakedaSamuraiEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, TakedaSoheiEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SatomiAshigaruEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SatomiSamuraiEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SatomiSoheiEntity.class, true));
        // Target bandits and ronin
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, BanditEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, RoninEntity.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Cow.class, true));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.POLAR_BEAR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.POLAR_BEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.POLAR_BEAR_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Use the zombie step sound for Onikuma to give a heavier, undead footstep
        // Increase volume from 0.15 to 0.5 for more audible footsteps
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.5F, 1.0F);
    }

    /**
     * Surface-only spawning like husks (only spawn in daylight)
     */
    public static boolean checkOnikumaSpawnRules(EntityType<OnikumaEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        try {
            int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
            if (blockLight > 7) return false;
        } catch (Exception ignored) {
        }
        // Explicit ground checks to prevent spawning on water or non-solid blocks
        BlockPos groundPos = pos.below();
        BlockState groundState = level.getBlockState(groundPos);
        if (groundState.isAir()) return false;
        if (!groundState.getFluidState().isEmpty()) return false; // disallow water/lava/fluid
        if (groundState.getCollisionShape(level, groundPos).isEmpty()) return false;

        TagKey<Block> requiredTag = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("sengoku", "onikuma_spawnable_on"));
        if (!groundState.is(requiredTag)) return false;

        return checkMobSpawnRules(type, level, spawnType, pos, random);
    }

}
