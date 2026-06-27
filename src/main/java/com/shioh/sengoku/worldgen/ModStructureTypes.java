package com.shioh.sengoku.worldgen;

import com.mojang.serialization.MapCodec;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Registry for custom structure types.
 */
public final class ModStructureTypes {
    public static final StructureType<CeilingJigsawStructure> CEILING_JIGSAW = register("ceiling_jigsaw", CeilingJigsawStructure.CODEC);

    private ModStructureTypes() {
    }

    private static <S extends Structure> StructureType<S> register(String name, MapCodec<S> codec) {
        return Registry.register(
            BuiltInRegistries.STRUCTURE_TYPE,
            ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, name),
            () -> codec
        );
    }

    public static void init() {
        sengokuFabric.LOGGER.info("Registering custom structure types for {}", sengokuFabric.MODID);
    }
}