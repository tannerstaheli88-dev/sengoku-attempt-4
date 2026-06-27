package com.shioh.sengoku.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TripleDoorBlock extends Block {
    public TripleDoorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Direction facing = context.getHorizontalDirection();

        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        BlockPos leftLower = pos.relative(left);
        BlockPos rightLower = pos.relative(right);
        BlockPos above = pos.above();
        BlockPos leftUpper = leftLower.above();
        BlockPos rightUpper = rightLower.above();

        if (pos.getY() < level.getMaxBuildHeight() - 1
                && level.getBlockState(above).canBeReplaced(context)
                && level.getBlockState(leftLower).canBeReplaced(context)
                && level.getBlockState(leftUpper).canBeReplaced(context)
                && level.getBlockState(rightLower).canBeReplaced(context)
                && level.getBlockState(rightUpper).canBeReplaced(context)) {
            return this.defaultBlockState();
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        // place upper middle
        level.setBlock(pos.above(), state, 3);

        Direction facing = placer == null ? Direction.NORTH : placer.getDirection();
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        BlockPos leftLower = pos.relative(left);
        BlockPos leftUpper = leftLower.above();
        BlockPos rightLower = pos.relative(right);
        BlockPos rightUpper = rightLower.above();

        if (level.isEmptyBlock(leftLower) && level.isEmptyBlock(leftUpper)) {
            level.setBlock(leftLower, state, 3);
            level.setBlock(leftUpper, state, 3);
        }
        if (level.isEmptyBlock(rightLower) && level.isEmptyBlock(rightUpper)) {
            level.setBlock(rightLower, state, 3);
            level.setBlock(rightUpper, state, 3);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return belowState.isFaceSturdy(level, below, Direction.UP);
    }
}
