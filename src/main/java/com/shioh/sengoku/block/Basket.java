package com.shioh.sengoku.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Basket extends Block {

    // Number of baskets huddled in one block (1–4)
    public static final IntegerProperty COUNT = IntegerProperty.create("count", 1, 4);

    public Basket(BlockBehaviour.Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(COUNT, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COUNT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        int count = state.getValue(COUNT);

        double height = 8.0D; // base height
        if (count == 4) height += 3.0D; // increase height only at count 4

        double minX, minZ, maxX, maxZ;

        if (count == 1) {
            // 7x7 pixels footprint (0.4375 block)
            minX = 4.5D;
            minZ = 4.5D;
            maxX = 11.5D;
            maxZ = 11.5D;
        } else {
            // 16x16 pixels footprint (full block) for counts 2–4
            minX = 0.0D;
            minZ = 0.0D;
            maxX = 16.0D;
            maxZ = 16.0D;
        }

        return Block.box(minX, 0.0D, minZ, maxX, height, maxZ);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        // Allow stacking if count < 4
        return state.getValue(COUNT) < 4;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState stateAtPos = world.getBlockState(pos);

        if (stateAtPos.getBlock() instanceof Basket) {
            int count = stateAtPos.getValue(COUNT);
            if (count < 4) {
                // Increment count by 1 for new placement
                return stateAtPos.setValue(COUNT, count + 1);
            }
            return stateAtPos; // max reached, do not replace
        }

        // Otherwise, place new basket with count 1
        return this.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        // Must be placed on a solid block
        BlockState below = world.getBlockState(pos.below());
        return below.isFaceSturdy(world, pos.below(), Direction.UP);
    }
}
