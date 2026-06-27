package com.shioh.sengoku.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.level.Level;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;

/**
 * Kojin – an aquatic demon variant that mirrors the vanilla Drowned.
 * Uses the same stats, sounds, and AI as a Drowned but has its own identity and texture.
 */
public class KojinEntity extends Drowned {

    private static final double WATER_SPEED = 0.5D;
    private static final double LAND_SPEED = 0.15D;

    public KojinEntity(EntityType<? extends KojinEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Drowned.createAttributes()
            .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 0.6)
            .add(Attributes.MOVEMENT_SPEED, WATER_SPEED);
    }

    public static boolean checkKojinSpawnRules(EntityType<KojinEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        try {
            if (!level.getFluidState(pos).is(FluidTags.WATER)) return false;
            if (!level.getFluidState(pos.below()).is(FluidTags.WATER)) return false;
            int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
            if (blockLight > 7) return false;
        } catch (Throwable ignored) {}
        return checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            var speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                double target = this.isInWater() ? WATER_SPEED : LAND_SPEED;
                if (speedAttr.getBaseValue() != target) {
                    speedAttr.setBaseValue(target);
                }
            }
        }
    }
}