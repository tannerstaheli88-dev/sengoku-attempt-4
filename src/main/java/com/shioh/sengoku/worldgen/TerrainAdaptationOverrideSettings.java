package com.shioh.sengoku.worldgen;

import java.util.Optional;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;

public interface TerrainAdaptationOverrideSettings {
    Optional<TerrainAdjustment> getTerrainAdjustmentOverride();
}