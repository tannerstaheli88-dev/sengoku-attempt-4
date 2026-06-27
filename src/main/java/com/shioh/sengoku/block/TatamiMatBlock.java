package com.shioh.sengoku.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public class TatamiMatBlock extends SlabBlock {

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public TatamiMatBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(AXIS, Direction.Axis.Y)
                .setValue(TYPE, SlabType.BOTTOM));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(AXIS);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext ctx) {
        // Double slabs cannot be replaced
        if (state.getValue(TYPE) == SlabType.DOUBLE) return false;

        // Only allow replacement if the player is holding the same slab
        if (ctx.getItemInHand().getItem() != this.asItem()) return false;

        Direction side = ctx.getClickedFace();
        double hitY = ctx.getClickLocation().y - ctx.getClickedPos().getY();
        SlabType slabType = state.getValue(TYPE);

        if (slabType == SlabType.BOTTOM) {
            return side == Direction.UP || (side != Direction.DOWN && hitY > 0.5);
        } else { // TOP
            return side == Direction.DOWN || (side != Direction.UP && hitY <= 0.5);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        LevelAccessor world = ctx.getLevel();
        BlockState existingState = world.getBlockState(pos);

        // Merge with existing slab
        if (existingState.is(this)) {
            SlabType existingType = existingState.getValue(TYPE);
            if (existingType == SlabType.DOUBLE) {
                return existingState;
            }

            // Only merge into a double slab when the placement intent actually targets
            // the opposite half (prevents accidental merging from odd click positions
            // or mismatched face directions which could lead to duplication).
            Direction clickedFace = ctx.getClickedFace();
            double hitY = ctx.getClickLocation().y - pos.getY();
            boolean shouldMerge;

            if (existingType == SlabType.BOTTOM) {
                shouldMerge = clickedFace == Direction.UP || (clickedFace != Direction.DOWN && hitY > 0.5);
            } else { // TOP
                shouldMerge = clickedFace == Direction.DOWN || (clickedFace != Direction.UP && hitY <= 0.5);
            }

            if (shouldMerge) {
                return existingState.setValue(TYPE, SlabType.DOUBLE);
            }

            // Otherwise don't change the block state (cancel placement of a single slab)
            return null;
        }

        // Place new slab
        Direction.Axis axis = ctx.getClickedFace().getAxis();
        SlabType type = (ctx.getClickLocation().y - pos.getY() > 0.5) ? SlabType.TOP : SlabType.BOTTOM;

        return this.defaultBlockState()
                .setValue(AXIS, axis)
                .setValue(TYPE, type);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        // Prevent merging slabs across adjacent block positions. Merging should only
        // occur when placing into the same block space (handled in getStateForPlacement).
        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction.Axis axis = state.getValue(AXIS);
        switch (rotation) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> {
                if (axis == Direction.Axis.X) return state.setValue(AXIS, Direction.Axis.Z);
                if (axis == Direction.Axis.Z) return state.setValue(AXIS, Direction.Axis.X);
                return state;
            }
            default -> {
                return state;
            }
        }
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Direction.Axis axis = state.getValue(AXIS);
        switch (mirror) {
            case FRONT_BACK -> {
                if (axis == Direction.Axis.X) return state.setValue(AXIS, Direction.Axis.Z);
                if (axis == Direction.Axis.Z) return state.setValue(AXIS, Direction.Axis.X);
                return state;
            }
            default -> {
                return state;
            }
        }
    }
}
