package com.shioh.sengoku.block;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * A wall-mounted decorative block representing a shide (Shinto paper streamer).
 * Can be placed on any horizontal wall direction.
 */
public class ShideBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ShideBlock> CODEC = simpleCodec(ShideBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    
    // Shapes for each direction (thin wall-mounted decoration)
    private static final Map<Direction, VoxelShape> SHAPES = ImmutableMap.of(
        Direction.NORTH, Block.box(5.0, 3.0, 15.0, 11.0, 16.0, 16.0),
        Direction.SOUTH, Block.box(5.0, 3.0, 0.0, 11.0, 16.0, 1.0),
        Direction.WEST, Block.box(15.0, 3.0, 5.0, 16.0, 16.0, 11.0),
        Direction.EAST, Block.box(0.0, 3.0, 5.0, 1.0, 16.0, 11.0)
    );

    public ShideBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<ShideBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos attachedPos = pos.relative(direction.getOpposite());
        BlockState attachedState = level.getBlockState(attachedPos);
        // Use the same lenient check as signs - just needs a solid face, not a full sturdy face
        // This allows placement on trapdoors, fences, etc.
        return attachedState.isFaceSturdy(level, attachedPos, direction) || 
               !attachedState.isAir() && attachedState.isSolid();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState blockState = this.defaultBlockState();
        LevelReader levelReader = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Direction[] directions = context.getNearestLookingDirections();

        for (Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                Direction opposite = direction.getOpposite();
                blockState = blockState.setValue(FACING, opposite);
                if (blockState.canSurvive(levelReader, blockPos)) {
                    return blockState;
                }
            }
        }

        return null;
    }

    @Override
    protected BlockState updateShape(
        BlockState state,
        Direction direction,
        BlockState neighborState,
        LevelAccessor level,
        BlockPos pos,
        BlockPos neighborPos
    ) {
        return direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos)
            ? Blocks.AIR.defaultBlockState()
            : state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
