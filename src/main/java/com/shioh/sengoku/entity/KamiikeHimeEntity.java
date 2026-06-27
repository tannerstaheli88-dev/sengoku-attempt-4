package com.shioh.sengoku.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.tags.FluidTags;

/**
 * Kamiike Hime – an aquatic guardian variant that mirrors the vanilla Guardian.
 * Uses the same stats, sounds, and AI as a Guardian but has its own identity and texture.
 */
public class KamiikeHimeEntity extends Guardian {
    public KamiikeHimeEntity(EntityType<? extends KamiikeHimeEntity> type, Level level) {
        super(type, level);
    }

    /**
     * Reuse vanilla Guardian attributes for parity (HP, damage, speed, etc.).
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Guardian.createAttributes();
    }

    /**
     * Mirror Guardian spawn rules (aquatic spawns in water).
     */
    public static boolean checkKamiikeHimeSpawnRules(EntityType<KamiikeHimeEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        try {
            // Require water at the spawn position so Kamiike Hime behave as ocean creatures
            if (!level.getFluidState(pos).is(FluidTags.WATER)) return false;
            // Also require the block below to be water to avoid spawning on shallow edges or land.
            if (!level.getFluidState(pos.below()).is(FluidTags.WATER)) return false;

            int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
            if (blockLight > 7) return false; // avoid bright/artificially lit spawns
        } catch (Throwable ignored) {
            // Fall back to generic checks if mappings differ
        }
        return checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    protected void registerGoals() {
        // Use vanilla Guardian goals without additional modifications so Kamiike Hime
        // behavior matches the vanilla Guardian exactly.
        super.registerGoals();
    }
}
