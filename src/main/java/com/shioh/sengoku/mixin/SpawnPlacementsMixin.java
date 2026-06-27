package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.registry.ModEntities;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.ServerLevelAccessor;

@Mixin(SpawnPlacements.class)
public class SpawnPlacementsMixin {

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void sengoku$blockFluidOnGroundSpawns(EntityType<T> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        try {
            ResourceLocation eid = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            if (eid == null) return;
            String p = eid.getPath();
            if (!("onikuma".equals(p) || "hitotsume_nyudo".equals(p) || "sarugami".equals(p) || "crow".equals(p) || "macaque".equals(p) || "omukade".equals(p))) return;

            BlockPos groundPos = pos.below();
            BlockState spaceState = level.getBlockState(pos);
            BlockState groundState = level.getBlockState(groundPos);

            if (!spaceState.getFluidState().isEmpty() || !groundState.getFluidState().isEmpty()) {
                sengokuFabric.LOGGER.debug("[sengoku] Blocking spawn {} at {} because spaceFluid={} groundFluid={}", entityType, pos, !spaceState.getFluidState().isEmpty(), !groundState.getFluidState().isEmpty());
                cir.setReturnValue(false);
                return;
            }

            if (groundState.isAir() || groundState.getCollisionShape(level, groundPos).isEmpty()) {
                sengokuFabric.LOGGER.debug("[sengoku] Blocking spawn {} at {} because groundAir={} collisionEmpty={}", entityType, pos, groundState.isAir(), groundState.getCollisionShape(level, groundPos).isEmpty());
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {}
    }

    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private static <T extends Entity> void sengoku$moonPhaseCheck(EntityType<T> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (spawnType != MobSpawnType.NATURAL) return;
        if (!level.getLevel().dimensionType().natural()) return;
        if (pos.getY() <= 62) return;
        if (entityType.getCategory() != MobCategory.MONSTER) return;

        int moonPhase = level.getLevel().getMoonPhase();

        float cancelChance = switch (moonPhase) {
            case 0 -> 1.00f; // Full Moon (testing: 0% pass)
            case 1 -> 0.70f; // Waning Gibbous   → 30%
            case 2 -> 0.50f; // Last Quarter     → 70%
            case 3 -> 0.30f; // Waning Crescent  → 100%
            case 4 -> 0.00f; // New Moon         → 100%
            case 5 -> 0.30f; // Waxing Crescent  → 100%
            case 6 -> 0.50f; // First Quarter    → 70%
            case 7 -> 0.70f; // Waxing Gibbous   → 30%
            default -> 0.00f;
        };

        if (cancelChance > 0.00f && random.nextFloat() < cancelChance) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isSpawnPositionOk", at = @At("HEAD"), cancellable = true)
    private static void sengoku$isSpawnPositionOk(EntityType<?> entityType, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (entityType != ModEntities.ONIKUMA && entityType != ModEntities.HITOTSUME_NYUDO && entityType != ModEntities.SARUGAMI && entityType != ModEntities.CROW && entityType != ModEntities.MACAQUE && entityType != ModEntities.OMUKADE) return;
            if (SpawnPlacements.getPlacementType(entityType) != SpawnPlacementTypes.ON_GROUND) return;
            BlockState ground = level.getBlockState(pos.below());
            if (!ground.getFluidState().isEmpty()) {
                sengokuFabric.LOGGER.debug("[sengoku] isSpawnPositionOk blocking mod entity {} at {} because ground fluid present", entityType, pos);
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {}
    }
}