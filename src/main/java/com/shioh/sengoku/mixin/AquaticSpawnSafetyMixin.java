package com.shioh.sengoku.mixin;

import com.shioh.sengoku.registry.ModEntities;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ensure IN_WATER spawn placements never allow spawning when the spawn space
 * or underlying block are not water. Mirrors the ground safety mixin but
 * inverted for aquatic entities (KOJIN, NINGYO, KAMIIKE_HIME, AKUGYO).
 */
@Mixin(SpawnPlacements.class)
public class AquaticSpawnSafetyMixin {

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void sengoku$blockNonWaterOnAquaticSpawns(EntityType<T> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (entityType != ModEntities.KOJIN && entityType != ModEntities.NINGYO && entityType != ModEntities.KAMIIKE_HIME && entityType != ModEntities.AKUGYO) return;

            BlockState spaceState = level.getBlockState(pos);
            BlockState groundState = level.getBlockState(pos.below());

            // Require that both the spawn space and the block below are water
            if (!spaceState.getFluidState().is(net.minecraft.tags.FluidTags.WATER) || !groundState.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) {
                sengokuFabric.LOGGER.debug("[sengoku] Blocking aquatic spawn {} at {} because spaceWater={} groundWater={}", entityType, pos, spaceState.getFluidState().is(net.minecraft.tags.FluidTags.WATER), groundState.getFluidState().is(net.minecraft.tags.FluidTags.WATER));
                cir.setReturnValue(false);
                return;
            }
        } catch (Throwable ignored) {}
    }

    @Inject(method = "isSpawnPositionOk", at = @At("HEAD"), cancellable = true)
    private static void sengoku$isSpawnPositionOkAquatic(EntityType<?> entityType, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (entityType != ModEntities.KOJIN && entityType != ModEntities.NINGYO && entityType != ModEntities.KAMIIKE_HIME && entityType != ModEntities.AKUGYO) return;

            BlockState below = level.getBlockState(pos.below());
            if (!below.getFluidState().is(net.minecraft.tags.FluidTags.WATER)) {
                sengokuFabric.LOGGER.debug("[sengoku] isSpawnPositionOk blocking aquatic mod entity {} at {} because below not water", entityType, pos);
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {}
    }
}
