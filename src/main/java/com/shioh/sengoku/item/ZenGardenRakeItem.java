package com.shioh.sengoku.item;

import com.shioh.sengoku.block.AbstractShikiRakedBlock;
import com.shioh.sengoku.init.ZenGardenReg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;

import static com.shioh.sengoku.block.AbstractShikiRakedBlock.SHAPE;

public class ZenGardenRakeItem extends Item {

    public ZenGardenRakeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        // If clicking on existing shiki block, cycle its shape
        if (state.getBlock() instanceof AbstractShikiRakedBlock) {
            if (!level.isClientSide) {
                RailShape currentShape = state.getValue(SHAPE);
                RailShape nextShape = getNextShape(currentShape);
                BlockState newState = state.setValue(SHAPE, nextShape).setValue(AbstractShikiRakedBlock.LOCKED, true);
                level.setBlockAndUpdate(pos, newState);
                level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.5F, 1.0F);
                if (player != null && !player.getAbilities().instabuild) {
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Otherwise, try to convert raw materials to shiki blocks
        BlockState replacement = null;
        if (state.is(Blocks.SAND)) {
            replacement = ZenGardenReg.SHIKI_SAND.defaultBlockState();
        } else if (state.is(Blocks.GRAVEL)) {
            replacement = ZenGardenReg.SHIKI_GRAVEL.defaultBlockState();
        } else if (state.is(Blocks.WHITE_CONCRETE_POWDER)) {
            replacement = ZenGardenReg.SHIRAKAWA_SUNA.defaultBlockState();
        }

        if (replacement == null) {
            return InteractionResult.PASS;
        }

        Direction facing = player != null ? player.getDirection() : context.getHorizontalDirection();
        RailShape initialShape = (facing == Direction.EAST || facing == Direction.WEST)
            ? RailShape.EAST_WEST
            : RailShape.NORTH_SOUTH;
        replacement = replacement.setValue(SHAPE, initialShape).setValue(AbstractShikiRakedBlock.LOCKED, false);

        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, replacement);
            if (state.is(Blocks.GRAVEL)) {
                level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else if (state.is(Blocks.WHITE_CONCRETE_POWDER)) {
                level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            } else {
                level.playSound(null, pos, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            if (player != null && !player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static RailShape getNextShape(RailShape current) {
        return switch (current) {
            case NORTH_SOUTH -> RailShape.EAST_WEST;
            case EAST_WEST -> RailShape.SOUTH_EAST;
            case SOUTH_EAST -> RailShape.SOUTH_WEST;
            case SOUTH_WEST -> RailShape.NORTH_WEST;
            case NORTH_WEST -> RailShape.NORTH_EAST;
            case NORTH_EAST -> RailShape.NORTH_SOUTH;
            default -> RailShape.NORTH_SOUTH;
        };
    }
}
