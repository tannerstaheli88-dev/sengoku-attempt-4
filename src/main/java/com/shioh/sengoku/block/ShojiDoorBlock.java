package com.shioh.sengoku.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import com.shioh.sengoku.util.ShojiProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class ShojiDoorBlock extends DoorBlock {
    private static final double SLIDE = 13.0;
    // use centralized ShojiProperties for shared properties
    public static final BooleanProperty HANDLE = ShojiProperties.HANDLE;

    public ShojiDoorBlock(BlockSetType setType, BlockBehaviour.Properties properties) {
        super(setType, properties);
        // ensure HANDLE has a default value
        this.registerDefaultState(this.defaultBlockState().setValue(HANDLE, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Always resolve to the lower-half state for hinge/facing/open info
        BlockState actualState = state;
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState lower = level.getBlockState(pos.below());
            if (lower.getBlock() instanceof ShojiDoorBlock) {
                actualState = lower;
            }
        }

        Direction facing = actualState.getValue(FACING);
        boolean open = actualState.getValue(OPEN);
        DoorHingeSide hinge = actualState.getValue(HINGE);

        if (!open) {
            // Closed (thin 2px panel)
            switch (facing) {
                case NORTH: return box(0, 0, 14, 16, 16, 16);
                case SOUTH: return box(0, 0, 0, 16, 16, 2);
                case WEST:  return box(14, 0, 0, 16, 16, 16);
                case EAST:
                default:    return box(0, 0, 0, 2, 16, 16);
            }
        }

        // OPENED state
        switch (facing) {
            case NORTH:
                if (hinge == DoorHingeSide.LEFT) {
                    return box(-SLIDE, 0, 14, 16 - SLIDE, 16, 16); // hinge west
                } else {
                    return box(SLIDE, 0, 14, 16 + SLIDE, 16, 16);  // hinge east
                }

            case SOUTH:
                if (hinge == DoorHingeSide.LEFT) {
                    return box(SLIDE, 0, 0, 16 + SLIDE, 16, 2);    // hinge east
                } else {
                    return box(-SLIDE, 0, 0, 16 - SLIDE, 16, 2);   // hinge west
                }

            case WEST:
                // IMPORTANT: vanilla treats LEFT hinge here as *south*
                if (hinge == DoorHingeSide.LEFT) {
                    return box(14, 0, SLIDE, 16, 16, 16 + SLIDE); // hinge south
                } else {
                    return box(14, 0, -SLIDE, 16, 16, 16 - SLIDE);// hinge north
                }

            case EAST:
            default:
                // IMPORTANT: vanilla treats LEFT hinge here as *north*
                if (hinge == DoorHingeSide.LEFT) {
                    return box(0, 0, -SLIDE, 2, 16, 16 - SLIDE);  // hinge north
                } else {
                    return box(0, 0, SLIDE, 2, 16, 16 + SLIDE);   // hinge south
                }
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // When open, no collision for anyone (players and mobs can pass freely)
        BlockState actualState = state;
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState lower = level.getBlockState(pos.below());
            if (lower.getBlock() instanceof ShojiDoorBlock) {
                actualState = lower;
            }
        }
        if (actualState.getValue(OPEN)) {
            return Shapes.empty();
        }
        return getShape(state, level, pos, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HANDLE);
    }

    @Override
    public void onPlace(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState oldState, boolean moved) {
        // ensure default handle value is present when placed; harmless no-op if already set
        // no-op: property will default to false from base block state; keep for future logic
        super.onPlace(state, level, pos, oldState, moved);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        switch (type) {
            case LAND:
            case AIR:
                return state.getValue(OPEN);
            case WATER:
            default:
                return false;
        }
    }
}
