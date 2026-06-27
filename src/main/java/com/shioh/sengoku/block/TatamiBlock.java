package com.shioh.sengoku.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class TatamiBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public TatamiBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction.Axis axis = context.getClickedFace().getAxis();
        return this.defaultBlockState().setValue(AXIS, axis);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction.Axis axis = state.getValue(AXIS);

        switch (rotation) {
            case CLOCKWISE_90:
            case COUNTERCLOCKWISE_90:
                if (axis == Direction.Axis.X) return state.setValue(AXIS, Direction.Axis.Z);
                if (axis == Direction.Axis.Z) return state.setValue(AXIS, Direction.Axis.X);
                return state;
            default:
                return state;
        }
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Direction.Axis axis = state.getValue(AXIS);

        switch (mirror) {
            case LEFT_RIGHT:
                if (axis == Direction.Axis.X) return state.setValue(AXIS, Direction.Axis.X);
                if (axis == Direction.Axis.Z) return state.setValue(AXIS, Direction.Axis.Z);
                return state;
            case FRONT_BACK:
                if (axis == Direction.Axis.X) return state.setValue(AXIS, Direction.Axis.Z);
                if (axis == Direction.Axis.Z) return state.setValue(AXIS, Direction.Axis.X);
                return state;
            default:
                return state;
        }
    }
}
