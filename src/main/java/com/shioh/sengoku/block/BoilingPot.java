package com.shioh.sengoku.block;

import com.shioh.sengoku.block.entity.BoilingPotEntity;
import com.shioh.sengoku.init.TansuBlockReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SmokerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import com.shioh.sengoku.block.entity.BoilingPotEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.stats.Stats;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BoilingPot extends SmokerBlock {

    // Base hitbox: 10px x 10px x 6px
    protected static final VoxelShape BASE_SHAPE = Shapes.box(
            3.0 / 16.0, 3.0 / 16.0, 3.0 / 16.0,
            13.0 / 16.0, 9.0 / 16.0, 13.0 / 16.0
    );

    // Grounded hitbox: 3 pixels taller (6 + 3 = 9 px)
    protected static final VoxelShape GROUNDED_SHAPE = Shapes.box(
            3.0 / 16.0, 3.0 / 16.0, 3.0 / 16.0,
            13.0 / 16.0, 12.0 / 16.0, 13.0 / 16.0
    );

    public static final BooleanProperty HANGING = BooleanProperty.create("hanging");

    public BoilingPot() {
        super(BlockBehaviour.Properties.of()
                .strength(3.5f)
                .sound(net.minecraft.world.level.block.SoundType.STONE)
                .lightLevel(state -> state.getValue(LIT) ? 13 : 0)
                .noOcclusion()
        );

        this.registerDefaultState(this.defaultBlockState()
                .setValue(HANGING, false));
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BoilingPotEntity pot) {
                // Drop contained items when the block is broken using the block entity helper
                try {
                    pot.dropAllContents(level, pos);
                } catch (Throwable ignored) {}
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HANGING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        Direction facing = context.getHorizontalDirection().getOpposite();

        boolean hanging;

        if (clickedFace == Direction.UP) {
            hanging = false; // standing on top
        } else if (clickedFace == Direction.DOWN) {
            hanging = true; // hanging from bottom
        } else {
            if (Block.canSupportCenter(world, pos.above(), Direction.DOWN)) {
                hanging = true;
            } else {
                hanging = false;
            }
        }

    return this.defaultBlockState()
        .setValue(HANGING, hanging)
        .setValue(FACING, facing)
        .setValue(LIT, false);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return state.getValue(HANGING)
                ? Block.canSupportCenter(world, pos.above(), Direction.DOWN)
                : Block.canSupportCenter(world, pos.below(), Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!state.canSurvive(world, pos)) {
            world.destroyBlock(pos, true);
        }
        super.neighborChanged(state, world, pos, block, fromPos, notify);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoilingPotEntity(pos, state);
    }

    // Open custom boiling pot menu
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BoilingPotEntity boiling) {
            player.openMenu(boiling);
            player.awardStat(Stats.INTERACT_WITH_SMOKER); // reuse smoker stat
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
    if (type != TansuBlockReg.BOILING_POT_BLOCK_ENTITY) return null;
        if (world.isClientSide) {
            // No client-specific ticker needed for furnace blocks in 1.21.1
            return null;
        }
        // Server processing ticker (vanilla smoker behavior)
    return (lvl, pos, st, be) -> BoilingPotEntity.serverTick(lvl, pos, st, (BoilingPotEntity) be);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rand) {
        if (state.getValue(LIT)) {
            double x = pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
            double y = pos.getY() + 0.6D + rand.nextDouble() * 0.2D;
            double z = pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.01D, 0.0D);
            level.addParticle(ParticleTypes.FLAME, x, y - 0.08D, z, 0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? BASE_SHAPE : GROUNDED_SHAPE;
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }
}
