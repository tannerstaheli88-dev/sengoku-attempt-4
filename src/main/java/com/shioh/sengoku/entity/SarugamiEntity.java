package com.shioh.sengoku.entity;

import com.shioh.sengoku.registry.SoundRegistry;
import com.shioh.sengoku.util.MoonPhaseSpawnUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Sarugami - agile monkey yokai variant formerly a scaled zombie.
 */
public class SarugamiEntity extends Monster {

    public SarugamiEntity(EntityType<? extends SarugamiEntity> type, Level level) {
        super(type, level);
        this.xpReward = 7;
    }

    public static boolean checkSarugamiSpawnRules(EntityType<SarugamiEntity> type, LevelAccessor level,
            MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;

        // Moon phase surface suppression
        if (spawnType == MobSpawnType.NATURAL && !MoonPhaseSpawnUtil.allowedByMoonPhase(level, pos, random)) {
            return false;
        }

        if (level instanceof ServerLevelAccessor serverLevel) {
            if (!Monster.isDarkEnoughToSpawn(serverLevel, pos, random)) return false;
        } else {
            try {
                if (level.getBrightness(LightLayer.BLOCK, pos) > 7) return false;
            } catch (Exception ignored) {}
        }

        BlockPos groundPos = pos.below();
        BlockState groundState = level.getBlockState(groundPos);
        if (groundState.isAir()) return false;
        if (!groundState.getFluidState().isEmpty()) return false;
        if (groundState.getCollisionShape(level, groundPos).isEmpty()) return false;

        TagKey<Block> requiredTag = TagKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath("sengoku", "sarugami_spawnable_on"));
        if (!groundState.is(requiredTag)) return false;

        if (level instanceof ServerLevelAccessor serverLevel) {
            return Monster.checkMonsterSpawnRules(type, serverLevel, spawnType, pos, random);
        }
        return checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Pillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Vindicator.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Evoker.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, KobayakawaAshigaruEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, KobayakawaSamuraiEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, KobayakawaSoheiEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, TakedaAshigaruEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, TakedaSamuraiEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, TakedaSoheiEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SatomiAshigaruEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SatomiSamuraiEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, SatomiSoheiEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, BanditEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, RoninEntity.class, true));
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide && this.isAlive()) {
            if (this.level().isDay() && this.level().canSeeSky(this.blockPosition()) && !this.level().isRaining()) {
                this.igniteForSeconds(8);
            }
        }
        super.aiStep();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.SARUGAMI_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundRegistry.SARUGAMI_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.SARUGAMI_DEATH;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        if (spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION
                || spawnType == MobSpawnType.STRUCTURE) {
            BlockPos.MutableBlockPos cursor = this.blockPosition().mutable().move(0, -1, 0);
            int steps = 0;
            while (steps < 12 && level.getBlockState(cursor).isAir()) {
                cursor.move(0, -1, 0);
                steps++;
            }
            BlockState ground = level.getBlockState(cursor);
            if (ground.blocksMotion() && !ground.getFluidState().is(FluidTags.LAVA)) {
                this.teleportTo(cursor.getX() + 0.5D, cursor.getY() + 1, cursor.getZ() + 0.5D);
            }
        }
        return spawnData;
    }
}