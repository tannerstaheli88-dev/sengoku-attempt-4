package com.shioh.sengoku.block;

import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.DyeColor;

/**
 * Shoji frame block implemented as a simple stained glass pane subclass.
 * Uses a default DyeColor so we can construct with only Properties in our code.
 */
public class ShojiFrameBlock extends StainedGlassPaneBlock {
    public ShojiFrameBlock(BlockBehaviour.Properties settings) {
        super(DyeColor.WHITE, settings);
    }
}
