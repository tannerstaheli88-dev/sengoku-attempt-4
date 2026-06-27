package com.shioh.sengoku.entity;

import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import org.jetbrains.annotations.Nullable;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hitotsume Nyudo - towering one-eyed monk yokai, high health and reach.
 */
public class HitotsumeNyudoEntity extends Monster {
    // Step-height maintenance (prevents getting stuck on 1-block steps)
    private int stepHeightApplyTimer = 0;

    public HitotsumeNyudoEntity(EntityType<? extends HitotsumeNyudoEntity> type, Level level) {
        super(type, level);
        this.xpReward = 15;
        // Very tall, needs extra step height using reflection
        try {
            java.lang.reflect.Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
            stepHeightField.setAccessible(true);
            stepHeightField.setFloat(this, 2.0F);
        } catch (Exception ignored) {
            // Reflection failed (field may not exist in mappings); silently ignore to avoid noisy logs
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
        if (spawnType == MobSpawnType.NATURAL || spawnType == MobSpawnType.CHUNK_GENERATION || spawnType == MobSpawnType.STRUCTURE) {
            BlockPos.MutableBlockPos cursor = this.blockPosition().mutable().move(0, -1, 0);
            int steps = 0;
            while (steps < 16 && level.getBlockState(cursor).isAir()) { // taller entity, search a bit further
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

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.STEP_HEIGHT, 2.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, false));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        // Target vanilla illagers
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Pillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Vindicator.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Evoker.class, true));
        // Target clan units
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
    }
    @Override
    protected SoundEvent getAmbientSound() {
        // Ambient played manually to allow elevated volume
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        // Hurt played manually to control volume
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        // Death played manually to control volume
        return null;
    }

@Override
public void aiStep() {
    if (!this.level().isClientSide && this.isAlive()) {
        if (this.level().isDay() && this.level().canSeeSky(this.blockPosition()) && !this.level().isRaining()) {
            this.igniteForSeconds(8);
        }
    }
    super.aiStep();
    try {
        if (!this.level().isClientSide) maintainStepHeight();
        if (this.random.nextInt(300) == 0) {
            SoundEvent ambient = SoundRegistry.HITOTSUME_NYUDO_AMBIENT;
            if (ambient != null) this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ambient, net.minecraft.sounds.SoundSource.HOSTILE, 3.0F, 1.0F);
        }
    } catch (Throwable ignored) {}
}

    private void maintainStepHeight() {
        if (this.stepHeightApplyTimer <= 0) {
            try {
                java.lang.reflect.Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
                stepHeightField.setAccessible(true);
                float current = stepHeightField.getFloat(this);
                if (Float.compare(current, 2.0F) != 0) {
                    stepHeightField.setFloat(this, 2.0F);
                }
            } catch (Exception ignored) {}
            this.stepHeightApplyTimer = 40;
        } else {
            this.stepHeightApplyTimer--;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean r = super.hurt(source, amount);
        try {
            // Play hurt sound only when damage was actually applied
            if (r && !this.level().isClientSide) {
                SoundEvent hurt = SoundRegistry.HITOTSUME_NYUDO_HURT;
                if (hurt != null) this.level().playSound(null, this.getX(), this.getY(), this.getZ(), hurt, net.minecraft.sounds.SoundSource.HOSTILE, 3.0F, 1.0F);
            }
        } catch (Throwable ignored) {}
        return r;
    }

    @Override
    public void die(DamageSource source) {
        try {
            if (!this.level().isClientSide) {
                SoundEvent death = SoundRegistry.HITOTSUME_NYUDO_DEATH;
                if (death != null) this.level().playSound(null, this.getX(), this.getY(), this.getZ(), death, net.minecraft.sounds.SoundSource.HOSTILE, 3.0F, 1.0F);
            }
        } catch (Throwable ignored) {}
        super.die(source);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        // Use zombie step sound for Hitotsume to match undead yokai footsteps
        // Increase volume from 0.15 to 0.5 for louder footsteps
        this.playSound(net.minecraft.sounds.SoundEvents.ZOMBIE_STEP, 0.5F, 1.0F);
    }

    /**
     * Surface-only spawning (only spawn in daylight like husks)
     */
    public static boolean checkHitotsumeNyudoSpawnRules(EntityType<HitotsumeNyudoEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        try {
            int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
            if (blockLight > 7) return false;
        } catch (Exception ignored) {
        }
        return checkMobSpawnRules(type, level, spawnType, pos, random);
    }

}
