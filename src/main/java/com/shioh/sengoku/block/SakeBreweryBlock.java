package com.shioh.sengoku.block;

import com.mojang.serialization.MapCodec;
import com.shioh.sengoku.block.entity.SakeBreweryEntity;
import com.shioh.sengoku.init.TansuBlockReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SakeBreweryBlock extends BaseEntityBlock {
    public static final MapCodec<SakeBreweryBlock> CODEC = simpleCodec(SakeBreweryBlock::new);
    public static final BooleanProperty[] HAS_BOTTLE = new BooleanProperty[]{
            BlockStateProperties.HAS_BOTTLE_0,
            BlockStateProperties.HAS_BOTTLE_1,
            BlockStateProperties.HAS_BOTTLE_2
    };
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    // Cauldron-like hitbox
    protected static final VoxelShape SHAPE = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 12.0, 15.0) // taller walls like cauldron
    );

    @Override
    public MapCodec<SakeBreweryBlock> codec() {
        return CODEC;
    }

    public SakeBreweryBlock(BlockBehaviour.Properties properties) {
        super(properties);
    this.registerDefaultState(this.stateDefinition.any()
        .setValue(HAS_BOTTLE[0], false)
        .setValue(HAS_BOTTLE[1], false)
        .setValue(HAS_BOTTLE[2], false)
        .setValue(LIT, false));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SakeBreweryEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, TansuBlockReg.SAKE_BREWERY_BLOCK_ENTITY, SakeBreweryEntity::serverTick);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SakeBreweryEntity) {
                final SakeBreweryEntity be = (SakeBreweryEntity) blockEntity;
                net.minecraft.world.MenuProvider provider = new net.minecraft.world.MenuProvider() {
                    @Override
                    public net.minecraft.network.chat.Component getDisplayName() {
                        return Component.translatable("container.sengoku.sake_brewery");
                    }

                    @Override
                    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id, net.minecraft.world.entity.player.Inventory playerInventory, net.minecraft.world.entity.player.Player player) {
                        // Create our custom SakeBreweryMenu on the server so the client and server share the same menu class.
                        return new com.shioh.sengoku.screen.SakeBreweryMenu(id, playerInventory, be, be.getDataAccess());
                    }
                };
                player.openMenu(provider);
                player.awardStat(Stats.INTERACT_WITH_BREWINGSTAND);
            }
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Only show smoke/flame and play crackle when the brewery is lit
        try {
            if (!state.getValue(LIT)) return;
        } catch (Throwable t) {
            // if property missing, default to previous behavior
        }

        double d = pos.getX() + 0.5 + (random.nextFloat() - 0.5) * 0.3;
        double e = pos.getY() + 0.65 + (random.nextFloat() - 0.5) * 0.2;
        double f = pos.getZ() + 0.5 + (random.nextFloat() - 0.5) * 0.3;
        level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.01, 0.0);

        // play campfire crackle sound more often and louder so it's easier to hear
        if (random.nextInt(5) == 0) {
            float pitch = 0.9F + random.nextFloat() * 0.2F;
            try { level.playSound(null, pos, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.7F, pitch); } catch (Throwable t) {}
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_BOTTLE[0], HAS_BOTTLE[1], HAS_BOTTLE[2], LIT);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
