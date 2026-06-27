package com.shioh.sengoku.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;

/**
 * Uses the same stats, sounds, and AI as a Husk but has its own identity and texture.
 */
public class GakiEntity extends Husk {
    public GakiEntity(EntityType<? extends GakiEntity> type, Level level) {
        super(type, level);
    }

    /**
     * Reuse vanilla Husk attributes for parity (HP, damage, speed, etc.).
     */
    public static AttributeSupplier.Builder createAttributes() {
        return Husk.createAttributes();
    }

    /**
     * Mirror Husk spawn rules (same as zombies but immune to daylight burning).
     */
    public static boolean checkGakiSpawnRules(EntityType<GakiEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        try {
            int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
            if (blockLight > 7) return false; // avoid bright/artificially lit spawns
        } catch (Throwable ignored) {
            // Fall back to generic checks if mappings differ
        }
        return checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        // Avoid zombified piglins (like piglins do)
        try {
            this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, ZombifiedPiglin.class, 8.0F, 1.0D, 1.2D));
        } catch (Throwable ignored) {}
    }
}
