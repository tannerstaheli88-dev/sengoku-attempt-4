package com.shioh.sengoku.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.pathfinder.PathComputationType;

/**
 * Stone lantern block inspired by Japanese ishidōrō (stone lanterns).
 * - Ground-only placement (cannot be hung)
 * - Must be lit with flint and steel similar to candles
 * - Larger hitbox than vanilla lanterns for more presence
 */
public class StoneLanternBlock extends Block {
    public static final MapCodec<StoneLanternBlock> CODEC = simpleCodec(StoneLanternBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    
    // Larger shape for substantial stone lantern (10x14x10 pixels)
    protected static final VoxelShape SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 14.0D, 13.0D);

    public StoneLanternBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.FALSE).setValue(WATERLOGGED, Boolean.FALSE));
    }

    @Override
    public MapCodec<StoneLanternBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        // If the player is using flint and steel or fire charge
        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
            return this.light(stack, state, level, pos, player, hand);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private ItemInteractionResult light(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        // Cannot light if waterlogged
        if (state.getValue(WATERLOGGED)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!state.getValue(LIT)) {
            // Light the lantern
            level.setBlock(pos, state.setValue(LIT, Boolean.TRUE), 11);
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
            
            if (!level.isClientSide) {
                // Damage the item
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                } else {
                    stack.shrink(1);
                }
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
            }
            
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        // Light if hit by fire projectile while unlit
        if (!level.isClientSide && projectile.isOnFire() && !state.getValue(LIT)) {
            // Light the lantern if hit by fire projectile
            BlockPos pos = hit.getBlockPos();
            level.setBlock(pos, state.setValue(LIT, Boolean.TRUE), 11);
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT) && !state.getValue(WATERLOGGED)) {
            // Spawn flame particles in the center of the lantern when lit
            double x = pos.getX() + 0.5D; // Center X
            double y = pos.getY() + 0.7D; // Slightly above center (about 11.2 pixels up, which is roughly where the flame would be)
            double z = pos.getZ() + 0.5D; // Center Z
            
            // Add small random offset for natural flame movement
            double offsetX = (random.nextDouble() - 0.5D) * 0.1D;
            double offsetZ = (random.nextDouble() - 0.5D) * 0.1D;
            
            // Check if this is a soul stone lantern by getting the block from the level
            boolean isSoulVariant = level.getBlockState(pos).getBlock().toString().contains("soul_stone_lantern");
            
            if (isSoulVariant) {
                // Spawn soul flame particle for soul stone lantern
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + offsetX, y, z + offsetZ, 0.0D, 0.0D, 0.0D);
                
                // Occasionally spawn soul flame particles too for extra effect
                if (random.nextDouble() < 0.25D) {
                    level.addParticle(ParticleTypes.SOUL, x + offsetX, y + 0.1D, z + offsetZ, 0.0D, 0.0D, 0.0D);
                }
            } else {
                // Spawn regular flame particle for regular stone lantern
                level.addParticle(ParticleTypes.FLAME, x + offsetX, y, z + offsetZ, 0.0D, 0.0D, 0.0D);
                
                // Occasionally spawn smoke particles too for realism
                if (random.nextDouble() < 0.25D) {
                    level.addParticle(ParticleTypes.SMOKE, x + offsetX, y + 0.1D, z + offsetZ, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        boolean waterlogged = fluid.is(Fluids.WATER);
        return this.defaultBlockState().setValue(WATERLOGGED, waterlogged).setValue(LIT, Boolean.FALSE);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Can only be placed on top of a solid block
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        // Break if the support block is removed
        if (facing == Direction.DOWN && !this.canSurvive(state, level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        // Treat the stone lantern as an obstacle for land-based pathfinding so villagers walk around it
        if (type == PathComputationType.LAND) return false;
        return super.isPathfindable(state, type);
    }

}
