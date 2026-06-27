package com.shioh.sengoku.worldgen;

import com.mojang.serialization.MapCodec;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;

/**
 * Registry for custom structure pool element types.
 */
public class ModStructurePoolElements {
    
    /**
     * Custom terrain-filling pool element type.
     * Use this in template pool JSONs with: "element_type": "sengoku:terrain_filling"
     * Fills air gaps but leaves water untouched.
     */
    public static final StructurePoolElementType<TerrainFillingPoolElement> TERRAIN_FILLING = 
        register("terrain_filling", TerrainFillingPoolElement.CODEC);
    
    /**
     * Custom terrain-filling pool element type that also creates support through water.
     * Use this in template pool JSONs with: "element_type": "sengoku:terrain_filling_with_water"
     * Fills air gaps AND creates support pillars through water to ocean floor.
     * Prevents floating structures when spawning in water.
     */
    public static final StructurePoolElementType<TerrainFillingWithWaterPoolElement> TERRAIN_FILLING_WITH_WATER = 
        register("terrain_filling_with_water", TerrainFillingWithWaterPoolElement.CODEC);

    /**
     * Pool element with configurable beard blending controls.
     * Use this in template pool JSONs with: "element_type": "sengoku:adjustable_beard_pool_element"
     */
    public static final StructurePoolElementType<AdjustableBeardPoolElement> ADJUSTABLE_BEARD_POOL_ELEMENT =
        register("adjustable_beard_pool_element", AdjustableBeardPoolElement.CODEC);

    /**
     * Generic wrapper that overrides the effective terrain adaptation used by Beardifier.
     * Use this in template pool JSONs with: "element_type": "sengoku:terrain_adaptation_override"
     */
    public static final StructurePoolElementType<TerrainAdaptationOverridePoolElement> TERRAIN_ADAPTATION_OVERRIDE =
        register("terrain_adaptation_override", TerrainAdaptationOverridePoolElement.CODEC);
    
    /**
     * Registers a structure pool element type.
     */
    private static <P extends net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement> 
            StructurePoolElementType<P> register(String name, MapCodec<P> codec) {
        return Registry.register(
            BuiltInRegistries.STRUCTURE_POOL_ELEMENT,
            ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, name),
            () -> codec
        );
    }
    
    /**
     * Initializes the structure pool elements. Call this during mod initialization.
     */
    public static void init() {
        sengokuFabric.LOGGER.info("Registering custom structure pool elements for " + sengokuFabric.MODID);
    }
}
