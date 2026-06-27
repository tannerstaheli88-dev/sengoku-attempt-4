package com.shioh.sengoku.poi;

import com.google.common.collect.Sets;
import com.shioh.sengoku.block.GrassBedBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BedPart;

import java.util.stream.Collectors;

public class GrassBedPointOfInterestTypes {
    public static void registerBedHeadAsPoiBlock(GrassBedBlock bedVariantBlock)
    {
        var blockStates = bedVariantBlock.getStateDefinition().getPossibleStates().stream().filter((blockState) -> {
            return blockState.getValue(GrassBedBlock.PART) == BedPart.HEAD;
        }).collect(Collectors.toSet());
        var holder = BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolder(PoiTypes.HOME).orElseThrow();
        var poiType = holder.value();
        PoiTypes.BEDS = Sets.newHashSet(PoiTypes.BEDS);
        PoiTypes.registerBlockStates(holder, blockStates);
        PoiTypes.BEDS.addAll(blockStates);
    }
}