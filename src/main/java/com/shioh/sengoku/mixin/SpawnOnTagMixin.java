package com.shioh.sengoku.mixin;

import com.shioh.sengoku.registry.ModEntities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.util.RandomSource;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.SpawnPlacementTypes;

/**
 * Single, clean mixin: restricts yokai spawning to tagged solid ground blocks.
 */
@Mixin(Mob.class)
public class SpawnOnTagMixin {

    @Inject(method = "checkMobSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void sengoku$restrictYokaiSpawns(EntityType<?> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (spawnType != MobSpawnType.NATURAL && spawnType != MobSpawnType.CHUNK_GENERATION && spawnType != MobSpawnType.STRUCTURE) {
            return;
        }

        // Only enforce for our specific mod entity ids (use registry ids to avoid init-order mismatch)
        ResourceLocation eid = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (eid == null) return;
        String p = eid.getPath();
        if (!("onikuma".equals(p) || "hitotsume_nyudo".equals(p) || "sarugami".equals(p) || "crow".equals(p) || "macaque".equals(p))) {
            return;
        }

        BlockPos groundPos = pos.below();
        BlockState spaceState = level.getBlockState(pos);
        BlockState groundState = level.getBlockState(groundPos);

        sengokuFabric.LOGGER.info("[sengoku] SpawnOnTagMixin check for {} at {} spawnType={}", type, pos, spawnType);

        // Must spawn into air and have non-air, non-fluid solid ground beneath
        if (!spaceState.isAir()) { cir.setReturnValue(false); return; }
        if (groundState.isAir()) { cir.setReturnValue(false); return; }
        // Disallow spawning on or in any fluids (water, lava, etc.)
        if (!groundState.getFluidState().isEmpty()) { sengokuFabric.LOGGER.debug("[sengoku] Blocked {} spawn: ground fluid present at {}", type, groundPos); cir.setReturnValue(false); return; }
        // Also disallow spawning into a space that contains fluid
        if (!spaceState.getFluidState().isEmpty()) { sengokuFabric.LOGGER.debug("[sengoku] Blocked {} spawn: space fluid present at {}", type, pos); cir.setReturnValue(false); return; }
        // Ensure the ground has a collision shape (is solid / not replaceable)
        if (groundState.getCollisionShape(level, groundPos).isEmpty()) { sengokuFabric.LOGGER.debug("[sengoku] Blocked {} spawn: ground collision empty at {}", type, groundPos); cir.setReturnValue(false); return; }

        // Passed basic land checks: not in fluid, ground is solid. Allow spawn.
    }
}
