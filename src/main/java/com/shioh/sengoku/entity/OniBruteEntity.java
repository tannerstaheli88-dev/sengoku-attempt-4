package com.shioh.sengoku.entity;

import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.sounds.SoundEvent;

/**
 * Oni Brute – a tough zombie-like mob that reuses the vanilla Zombie
 * AI and model but has much higher reinforcement chance and custom sounds.
 */
public class OniBruteEntity extends Zombie {
    // Step-height maintenance (prevents getting stuck on 1-block steps)
    private int stepHeightApplyTimer = 0;

    public OniBruteEntity(EntityType<? extends OniBruteEntity> type, Level level) {
        super(type, level);
        try {
            java.lang.reflect.Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
            stepHeightField.setAccessible(true);
            stepHeightField.setFloat(this, 3.0F);
        } catch (Exception ignored) {
        }
    }

    /**
     * Base attributes from Zombie, with boosted spawn reinforcements chance.
     */
    public static AttributeSupplier.Builder createAttributes() {
        // Make the Oni Brute much tougher and ogre-like
        return Zombie.createAttributes()
            .add(Attributes.MAX_HEALTH, 40.0D) 
            .add(Attributes.ATTACK_DAMAGE, 10.0D) // heavy attack
            .add(Attributes.ARMOR, 6.0D) // some natural armor
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D) 
            .add(Attributes.MOVEMENT_SPEED, 0.3D) 
            .add(Attributes.FOLLOW_RANGE, 40.0D) 
            .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.5D) // high reinforcement chance
            .add(Attributes.STEP_HEIGHT, 3.0D);
    }

    /**
     * Spawn like zombies (darkness check etc.).
     */
    public static boolean checkOniBruteSpawnRules(EntityType<OniBruteEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        try {
            int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
            if (blockLight > 7) return false;
        } catch (Throwable ignored) {}
        return checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        // Ambient playback handled manually to allow elevated volume (see aiStep)
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        // Death sound played manually in die() to control volume
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        // Hurt sound played manually in hurt() to control volume
        return null;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        try {
            if (!this.level().isClientSide) maintainStepHeight();
        } catch (Throwable ignored) {}
        try {
            if (!this.level().isClientSide && this.random.nextInt(200) == 0) {
                // Occasionally play ambient loudly (use aggro ambient if aggressive)
                SoundEvent ambient = (this.isAggressive() || this.getTarget() != null) ? SoundRegistry.ONI_BRUTE_AMBIENT_AGGRO : SoundRegistry.ONI_BRUTE_AMBIENT;
                if (ambient != null) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), ambient, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean res = super.hurt(source, amount);
        try {
            // Play hurt sound only when damage was actually applied
            if (res && !this.level().isClientSide) {
                SoundEvent hurt = SoundRegistry.ONI_BRUTE_HURT;
                if (hurt != null) this.level().playSound(null, this.getX(), this.getY(), this.getZ(), hurt, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        } catch (Throwable ignored) {}
        return res;
    }

    @Override
    public void die(DamageSource source) {
        try {
            if (!this.level().isClientSide) {
                SoundEvent death = SoundRegistry.ONI_BRUTE_DEATH;
                if (death != null) this.level().playSound(null, this.getX(), this.getY(), this.getZ(), death, net.minecraft.sounds.SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        } catch (Throwable ignored) {}
        super.die(source);
    }

    @Override
    protected void playStepSound(BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        // Heavier, lower-pitched footsteps
        try {
            this.playSound(net.minecraft.sounds.SoundEvents.ZOMBIE_STEP, 0.8F, 0.8F);
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);
        try {
            if (result && target instanceof LivingEntity living) {
                // Apply additional knockback to make attacks feel ogre-like
                double dx = target.getX() - this.getX();
                double dz = target.getZ() - this.getZ();
                double mag = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
                living.knockback(1.5F, dx / mag, dz / mag);
            }
        } catch (Throwable ignored) {}
        return result;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        super.populateDefaultEquipmentSlots(random, difficulty);
        try {
            // 25% chance to spawn holding a stone kanabo
            if (random.nextDouble() < 0.25D) {
                var item = BuiltInRegistries.ITEM.get(sengokuFabric.asId("stone_kanabo"));
                if (item != null) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(item));
                    // Give it a decent drop chance so players can obtain it sometimes
                    this.setDropChance(EquipmentSlot.MAINHAND, 0.5F);
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void setBaby(boolean baby) {
        // no baby
    }

    private void maintainStepHeight() {
        if (this.stepHeightApplyTimer <= 0) {
            try {
                java.lang.reflect.Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
                stepHeightField.setAccessible(true);
                float current = stepHeightField.getFloat(this);
                if (Float.compare(current, 3.0F) != 0) {
                    stepHeightField.setFloat(this, 3.0F);
                }
            } catch (Exception ignored) {
            }
            this.stepHeightApplyTimer = 40;
        } else {
            this.stepHeightApplyTimer--;
        }
    }
}
