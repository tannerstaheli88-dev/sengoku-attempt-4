package com.shioh.sengoku.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ShikiRakedSandBlock extends AbstractShikiRakedBlock {
    public static final MapCodec<ShikiRakedSandBlock> CODEC = simpleCodec(ShikiRakedSandBlock::new);

    public ShikiRakedSandBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<ShikiRakedSandBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockState getBaseState() {
        return Blocks.SAND.defaultBlockState();
    }
}
