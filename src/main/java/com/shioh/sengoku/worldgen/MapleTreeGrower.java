package com.shioh.sengoku.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import java.util.Optional;

/**
 * TreeGrower for Maple saplings.
 * Points to the maple_tree configured feature.
 */
public class MapleTreeGrower {
    
    public static final ResourceKey<ConfiguredFeature<?, ?>> MAPLE_TREE = 
        ResourceKey.create(Registries.CONFIGURED_FEATURE, 
            ResourceLocation.fromNamespaceAndPath("minecraft", "maple_tree"));

    public static final TreeGrower MAPLE = new TreeGrower(
        "maple",
        0.0F,  // no mega tree variant
        Optional.empty(),  // no mega tree
        Optional.empty(),  // no mega tree
        Optional.of(MAPLE_TREE),  // normal tree
        Optional.empty(),  // no fancy variant
        Optional.empty(),  // no flowers
        Optional.empty()   // no flowers variant
    );
}
