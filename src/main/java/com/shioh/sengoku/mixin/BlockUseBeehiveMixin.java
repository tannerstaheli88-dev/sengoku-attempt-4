package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BeehiveBlock.class)
public class BlockUseBeehiveMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        try {
            ItemStack stack = player.getItemInHand(hand);
            if (stack == null) return;

            // Disable glass bottle -> honey bottle behavior entirely
            if (stack.getItem() == Items.GLASS_BOTTLE) {
                cir.setReturnValue(InteractionResult.FAIL);
                return;
            }

            // If player is using shears and the hive is full, give paper instead of honeycomb
            if (stack.getItem() == Items.SHEARS) {
                int honey = 0;
                try {
                    honey = state.getValue(BeehiveBlock.HONEY_LEVEL);
                } catch (Exception e) {
                    // If property missing, abort (let vanilla handle)
                    return;
                }

                // Vanilla honey level max is 5 — require full hive
                if (honey >= 5) {
                    if (!world.isClientSide) {
                        // Drop paper (give same count as honeycomb would normally give)
                        ItemStack drop = new ItemStack(Items.PAPER, 3);
                        ItemEntity ent = new ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, drop);
                        world.addFreshEntity(ent);

                        // Damage the shears (use EquipmentSlot overload so compilation matches project usages)
                        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

                        // Reset honey level to 0
                        world.setBlock(pos, state.setValue(BeehiveBlock.HONEY_LEVEL, 0), 3);
                    }

                    cir.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide));
                    return;
                }
            }
        } catch (Throwable ignored) {}
    }
}
