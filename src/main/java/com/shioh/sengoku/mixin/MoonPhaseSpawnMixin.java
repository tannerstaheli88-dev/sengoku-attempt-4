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
 * Adjusts surface monster spawn rates based on moon phase.
 *
 * Full moon  (0) → 10% rate  (90% cancel)
 * Waning Gibbous (1) → 30% rate  (70% cancel)
 * Last Quarter   (2) → 70% rate  (30% cancel)
 * Waning Crescent(3) → 100% rate (no cancel)
 * New Moon       (4) → 100% rate (no cancel — 120% is beyond what a cancel hook can express)
 * Waxing Crescent(5) → 100% rate (no cancel)
 * First Quarter  (6) → 70% rate  (30% cancel)
 * Waxing Gibbous (7) → 30% rate  (70% cancel)
 *
 * Only affects natural spawns above y=62 in natural dimensions.
 * Composes with SurfaceMonsterSpawnReductionMixin — both hooks run independently.
 */
@Mixin(value = Mob.class, priority = 900)
public class MoonPhaseSpawnMixin {

    @Inject(method = "checkMobSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void applyMoonPhaseSpawnModifier(EntityType<?> entityType, LevelAccessor level,
                                                     MobSpawnType spawnType, BlockPos pos, RandomSource random,
                                                     CallbackInfoReturnable<Boolean> cir) {
        // Natural spawns only
        if (spawnType != MobSpawnType.NATURAL) {
            return;
        }

        // Overworld only
        if (!(level instanceof ServerLevel serverLevel) || !serverLevel.dimensionType().natural()) {
            return;
        }

        // Surface only — above y=62
        if (pos.getY() <= 62) {
            return;
        }

        int moonPhase = serverLevel.getMoonPhase();

        float cancelChance = switch (moonPhase) {
            case 0 -> 1.00f; // Full Moon        → 10%
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
}