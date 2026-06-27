package com.shioh.sengoku.block;

import com.shioh.sengoku.util.ShojiProperties;
// debug/logging removed
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.jetbrains.annotations.Nullable;

/**
 * Shoji frame variant that includes DAMAGED (1..3) and AGED (boolean) properties.
 * Intended for the normal, checkered, and paly frame variants only.
 */
public class ShojiFrameVariantBlock extends ShojiFrameBlock {
    private final boolean placeAsMaxDamaged;

    public ShojiFrameVariantBlock(BlockBehaviour.Properties settings) {
        this(settings, false);
    }

    public ShojiFrameVariantBlock(BlockBehaviour.Properties settings, boolean placeAsMaxDamaged) {
        super(settings);
        this.placeAsMaxDamaged = placeAsMaxDamaged;
        // default damaged = 1, aged = false
        this.registerDefaultState(this.defaultBlockState()
                .setValue(ShojiProperties.DAMAGED, 1)
                .setValue(ShojiProperties.AGED, false));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState placementState = super.getStateForPlacement(context);
        if (placementState == null) {
            return null;
        }
        if (this.placeAsMaxDamaged) {
            return placementState.setValue(ShojiProperties.DAMAGED, 3);
        }
        return placementState;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ShojiProperties.DAMAGED, ShojiProperties.AGED);
    }

    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        // debug removed; method now performs actions without logging
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == Items.SHEARS) {
            int current = state.getValue(ShojiProperties.DAMAGED);
            if (current < 3) {
                if (!world.isClientSide) {
                    world.setBlock(pos, state.setValue(ShojiProperties.DAMAGED, current + 1), 3);
                    // damage the shears item on server side only
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    // play painting break sound
                    world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, net.minecraft.sounds.SoundEvents.PAINTING_BREAK, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
            return InteractionResult.PASS;
        }

        // Paper -> repair by one stage (reverse of shears damage)
        if (stack.getItem() == Items.PAPER) {
            int current = state.getValue(ShojiProperties.DAMAGED);
            if (current > 1) {
                if (!world.isClientSide) {
                    world.setBlock(pos, state.setValue(ShojiProperties.DAMAGED, current - 1), 3);
                    if (!player.isCreative()) stack.shrink(1);
                    world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, net.minecraft.sounds.SoundEvents.PAINTING_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(world.isClientSide);
            }
            return InteractionResult.PASS;
        }

        // Yellow dye -> set aged = true
        if (stack.getItem() == Items.YELLOW_DYE) {
            if (!state.getValue(ShojiProperties.AGED)) {
                if (!world.isClientSide) {
                    world.setBlock(pos, state.setValue(ShojiProperties.AGED, true), 3);
                    if (!player.isCreative()) stack.shrink(1);
                    world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, net.minecraft.sounds.SoundEvents.PAINTING_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        // White dye -> set aged = false
        if (stack.getItem() == Items.WHITE_DYE) {
            if (state.getValue(ShojiProperties.AGED)) {
                if (!world.isClientSide) {
                    world.setBlock(pos, state.setValue(ShojiProperties.AGED, false), 3);
                    if (!player.isCreative()) stack.shrink(1);
                    world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, net.minecraft.sounds.SoundEvents.PAINTING_BREAK, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
