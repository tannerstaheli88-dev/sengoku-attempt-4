package com.shioh.sengoku.util;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;

public final class MoonPhaseSpawnUtil {

    private MoonPhaseSpawnUtil() {}

    public static boolean allowedByMoonPhase(LevelAccessor level, BlockPos pos, RandomSource random) {
        if (pos.getY() <= 62) return true;
        if (!(level instanceof ServerLevelAccessor sla)) return true;
        if (!sla.getLevel().dimensionType().natural()) return true;

        int moonPhase = sla.getLevel().getMoonPhase();

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

        return cancelChance <= 0.00f || random.nextFloat() >= cancelChance;
    }
}