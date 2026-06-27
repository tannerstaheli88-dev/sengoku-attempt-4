package com.shioh.sengoku.block;

import com.shioh.sengoku.init.TansuItemReg;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.util.RandomSource;

public class RiceCropBlock extends CropBlock implements BonemealableBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

private static final VoxelShape[] SHAPES = new VoxelShape[]{
        Block.box(7, 0, 7, 9, 13, 9),        // size 2x13x2
        Block.box(6.25, 0, 6.25, 9.75, 14, 9.75),  // size 3.5x14x3.5
        Block.box(5.75, 0, 6, 10.25, 16, 10),      // size 4.5x16x4
        Block.box(3, 0, 3, 13, 18, 13),            // size 10x18x10 (already centered)
        Block.box(2.5, 0, 2.5, 13.5, 22, 13.5),    // size 11x22x11
        Block.box(1.5, 0, 1.5, 14.5, 27, 14.5),    // size 13x27x13
        Block.box(0.25, 0, 0.25, 15.75, 28, 15.75),// size 15.5x28x15.5
        Block.box(0, 0, 0, 16, 30, 16)   
};

    public RiceCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(this.getAgeProperty(), 0)
                .setValue(WATERLOGGED, false)
        );
    }

    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        int age = state.getValue(this.getAgeProperty());
        age = Math.min(age, SHAPES.length - 1);
        return SHAPES[age];
    }

    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockState below = world.getBlockState(pos.below());
        boolean waterlogged = state.getValue(WATERLOGGED);

        if (waterlogged) {
            return below.is(Blocks.MUD) || below.is(Blocks.DIRT) || below.is(Blocks.COARSE_DIRT);
        } else {
            return below.is(Blocks.MUD);
        }
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState belowState = context.getLevel().getBlockState(pos.below());
        FluidState fluid = context.getLevel().getFluidState(pos);

        boolean waterlogged = fluid.is(Fluids.WATER);

        if (waterlogged) {
            if (belowState.is(Blocks.MUD) || belowState.is(Blocks.DIRT) || belowState.is(Blocks.COARSE_DIRT)) {
                return this.defaultBlockState().setValue(WATERLOGGED, true);
            }
        } else {
            if (belowState.is(Blocks.MUD)) {
                return this.defaultBlockState().setValue(WATERLOGGED, false);
            }
        }
        return null;
    }

    public void growCrops(ServerLevel world, BlockPos pos, BlockState state) {
        int newAge = Math.min(this.getAge(state) + 1, this.getMaxAge());
        BlockState newState = state.setValue(this.getAgeProperty(), newAge);
        world.setBlock(pos, newState, 3);
    }

    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = world.getBlockState(abovePos);
        if (!aboveState.isAir()) {
            world.destroyBlock(pos, true);
            return;
        }

        if (this.getAge(state) < this.getMaxAge() && random.nextInt(7) == 0) {
            this.growCrops(world, pos, state);
        }
    }

    // Bonemeal handling
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state, boolean isClient) {
        return this.getAge(state) < this.getMaxAge();
    }

    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = world.getBlockState(abovePos);
        if (!aboveState.isAir()) {
            world.destroyBlock(pos, true);
            return;
        }
        this.growCrops(world, pos, state);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // --- Ensure creative pick block gives rice item ---
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        return new ItemStack(TansuItemReg.RICE_I);
    }

    // Optional: for asItem() calls in code that convert block to item
    public Item asItem() {
        return TansuItemReg.RICE_I;
    }

    // --- Base seed for CropBlock (used by vanilla pick-block internally) ---
    public Item getBaseSeedId() {
        return TansuItemReg.RICE_I;
}
}
