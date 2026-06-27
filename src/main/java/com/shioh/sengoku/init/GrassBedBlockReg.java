package com.shioh.sengoku.init;

import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.block.BoundBambooBedBlockEntity;
import com.shioh.sengoku.block.GrassBedBlock;
import com.shioh.sengoku.block.GrassBedBlockEntity;
import com.shioh.sengoku.poi.GrassBedPointOfInterestTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;

public class GrassBedBlockReg {
    // GRASS
    public static final GrassBedBlock GRASS_BED = new GrassBedBlock(SoundType.GRASS, "grass");

    public static BlockEntityType<GrassBedBlockEntity> MORE_BED_VARIANT_BLOCK_ENTITY;
    public static BlockEntityType<BoundBambooBedBlockEntity> BOUND_BAMBOO_BED_BLOCK_ENTITY;

    public static final List<Block> more_beds = new ArrayList<>();
    public static final List<Block> more_grass_beds = new ArrayList<>();

    public static void registerBedBlocks() {
        // GRASS
        registerGrassBedBlock(GRASS_BED);

        List<Block> planks_beds = more_beds;
        planks_beds.removeAll(more_grass_beds);

        MORE_BED_VARIANT_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            sengokuFabric.asId("more_bed_variants"),
            BlockEntityType.Builder.of(GrassBedBlockEntity::new, planks_beds.toArray(Block[]::new)).build()
        );

        BOUND_BAMBOO_BED_BLOCK_ENTITY = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            sengokuFabric.asId("more_grass_beds"),
            BlockEntityType.Builder.of(BoundBambooBedBlockEntity::new, more_grass_beds.toArray(Block[]::new)).build()
        );
    }

    private static void registerBedBlock(GrassBedBlock bed) {
        // Registry name simplified to bedWoodType + "_bed" only
        Registry.register(
            BuiltInRegistries.BLOCK,
            sengokuFabric.asId(bed.bedWoodType + "_bed"),
            bed
        );
        more_beds.add(bed);
        GrassBedPointOfInterestTypes.registerBedHeadAsPoiBlock(bed);
    }

    private static void registerGrassBedBlock(GrassBedBlock bed) {
        more_grass_beds.add(bed);
        registerBedBlock(bed);
    }
}
