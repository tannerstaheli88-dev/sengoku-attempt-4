package com.shioh.sengoku.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class FishingNet extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty UPPER = BooleanProperty.create("upper");

    private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(7.0D, 0.0D, 0.0D, 9.0D, 16.0D, 16.0D);
    private static final VoxelShape SHAPE_EAST_WEST = Block.box(0.0D, 0.0D, 7.0D, 16.0D, 16.0D, 9.0D);

    public FishingNet(BlockBehaviour.Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(UPPER, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, UPPER);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);
        return (dir == Direction.NORTH || dir == Direction.SOUTH) ? SHAPE_NORTH_SOUTH : SHAPE_EAST_WEST;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getHorizontalDirection().getClockWise(); // flat face points at player
        BlockPos pos = ctx.getClickedPos();
        return this.defaultBlockState().setValue(FACING, facing).setValue(UPPER, false);
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
        if (!world.isClientSide && !state.getValue(UPPER)) {
            BlockPos above = pos.above();
            if (world.getBlockState(above).isAir()) {
                world.setBlock(above, state.setValue(UPPER, true), 3);
            }
        }
        super.onPlace(state, world, pos, oldState, moved);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (state.getValue(UPPER)) {
                BlockPos below = pos.below();
                if (world.getBlockState(below).getBlock() == this) {
                    world.removeBlock(below, false);
                }
            } else {
                BlockPos above = pos.above();
                if (world.getBlockState(above).getBlock() == this) {
                    world.removeBlock(above, false);
                }
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        // Treat fishing nets as obstacles for land pathfinding so villagers try to walk around them
        if (type == PathComputationType.LAND) return false;
        return super.isPathfindable(state, type);
    }
}
