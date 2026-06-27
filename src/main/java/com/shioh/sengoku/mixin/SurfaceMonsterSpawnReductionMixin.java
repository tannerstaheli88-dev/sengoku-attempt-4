package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reduces surface monster spawning by 50% to balance with patrol spawns.
 */
@Mixin(Mob.class)
public class SurfaceMonsterSpawnReductionMixin {
    
    @Inject(method = "checkMobSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void reduceSurfaceMonsterSpawns(EntityType<?> entityType, LevelAccessor level, 
                                                    MobSpawnType spawnType, BlockPos pos, RandomSource random,
                                                    CallbackInfoReturnable<Boolean> cir) {
        // Only affect natural spawns on the surface
        if (spawnType != MobSpawnType.NATURAL) {
            return;
        }
        
        // Only affect overworld
        if (!(level instanceof ServerLevel serverLevel) || !serverLevel.dimensionType().natural()) {
            return;
        }
        
        // Check if spawning on surface (y > 50 is a reasonable threshold for "surface")
        if (pos.getY() <= 50) {
            return;
        }
        
        // 50% chance to cancel the spawn
        if (random.nextFloat() < 0.5f) {
            cir.setReturnValue(false);
        }
    }
}
