package com.shioh.sengoku.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ShikiRakedGravelBlock extends AbstractShikiRakedBlock {
    public static final MapCodec<ShikiRakedGravelBlock> CODEC = simpleCodec(ShikiRakedGravelBlock::new);

    public ShikiRakedGravelBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<ShikiRakedGravelBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockState getBaseState() {
        return Blocks.GRAVEL.defaultBlockState();
    }
}
