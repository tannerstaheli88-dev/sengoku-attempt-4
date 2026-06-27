package com.shioh.sengoku.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

import java.util.Optional;

/**
 * TreeGrower for Ginkgo saplings.
 * Points to the ginkgo_tree configured feature.
 */
public class GinkgoTreeGrower {
    
    public static final ResourceKey<ConfiguredFeature<?, ?>> GINKGO_TREE = 
        ResourceKey.create(Registries.CONFIGURED_FEATURE, 
            ResourceLocation.fromNamespaceAndPath("minecraft", "ginkgo_tree"));

    public static final TreeGrower GINKGO = new TreeGrower(
        "ginkgo",
        0.0F,  // no mega tree variant
        Optional.empty(),  // no mega tree
        Optional.empty(),  // no mega tree
        Optional.of(GINKGO_TREE),  // normal tree
        Optional.empty(),  // no fancy variant
        Optional.empty(),  // no flowers
        Optional.empty()   // no flowers variant
    );
}
