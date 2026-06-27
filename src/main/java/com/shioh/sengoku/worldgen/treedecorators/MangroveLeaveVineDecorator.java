package com.shioh.sengoku.worldgen.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.shioh.sengoku.registry.SengokuBlocks;
import com.shioh.sengoku.registry.SengokuTreeDecorators;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class MangroveLeaveVineDecorator extends TreeDecorator {
    public static final MapCodec<MangroveLeaveVineDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
        .fieldOf("probability")
        .xmap(MangroveLeaveVineDecorator::new, decorator -> decorator.probability);

    private final float probability;

    public MangroveLeaveVineDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return SengokuTreeDecorators.SENGOKU_MANGROVE_LEAVE_VINE;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource randomSource = context.random();
        context.leaves().forEach(blockPos -> {
            if (randomSource.nextFloat() < this.probability) {
                BlockPos blockPos2 = blockPos.west();
                if (context.isAir(blockPos2)) {
                    addHangingVine(blockPos2, VineBlock.EAST, context);
                }
            }

            if (randomSource.nextFloat() < this.probability) {
                BlockPos blockPos2 = blockPos.east();
                if (context.isAir(blockPos2)) {
                    addHangingVine(blockPos2, VineBlock.WEST, context);
                }
            }

            if (randomSource.nextFloat() < this.probability) {
                BlockPos blockPos2 = blockPos.north();
                if (context.isAir(blockPos2)) {
                    addHangingVine(blockPos2, VineBlock.SOUTH, context);
                }
            }

            if (randomSource.nextFloat() < this.probability) {
                BlockPos blockPos2 = blockPos.south();
                if (context.isAir(blockPos2)) {
                    addHangingVine(blockPos2, VineBlock.NORTH, context);
                }
            }
        });
    }

    private static void addHangingVine(BlockPos pos, BooleanProperty sideProperty, TreeDecorator.Context context) {
        context.setBlock(pos, SengokuBlocks.MANGROVE_VINES.defaultBlockState().setValue(sideProperty, true));
        int i = 4;

        for (BlockPos mutable = pos.below(); context.isAir(mutable) && i > 0; i--) {
            context.setBlock(mutable, SengokuBlocks.MANGROVE_VINES.defaultBlockState().setValue(sideProperty, true));
            mutable = mutable.below();
        }
    }
}
