package com.shioh.sengoku.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ShirakawaSunaBlock extends AbstractShikiRakedBlock {
    public static final MapCodec<ShirakawaSunaBlock> CODEC = simpleCodec(ShirakawaSunaBlock::new);

    public ShirakawaSunaBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<ShirakawaSunaBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockState getBaseState() {
        return Blocks.WHITE_CONCRETE_POWDER.defaultBlockState();
    }
}
