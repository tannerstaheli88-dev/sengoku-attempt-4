package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import com.shioh.sengoku.util.TrapdoorStiffProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrapDoorBlock.class)
public class BlockUseTrapdoorStiffMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void onUse(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        try {
            if (!(state.getBlock() instanceof TrapDoorBlock)) return;

            ItemStack stack = player.getItemInHand(player.getUsedItemHand() == null ? InteractionHand.MAIN_HAND : player.getUsedItemHand());
            boolean holdingDebug = stack != null && stack.getItem() == Items.DEBUG_STICK;

            if (holdingDebug) {
                if (!world.isClientSide) {
                    boolean curr = state.getValue(TrapdoorStiffProperties.STIFF);
                    world.setBlock(pos, state.setValue(TrapdoorStiffProperties.STIFF, !curr), 3);
                }
                cir.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide));
                return;
            }

            // Slime ball makes a trapdoor stiff when used on it
            if (stack != null && stack.getItem() == Items.SLIME_BALL) {
                if (!world.isClientSide) {
                    try {
                        world.setBlock(pos, state.setValue(TrapdoorStiffProperties.STIFF, true), 3);
                                // play sticky/slime placement sound and emit game event
                                world.playSound(player, pos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                                world.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                                if (!player.getAbilities().instabuild) {
                                    stack.shrink(1);
                                }
                    } catch (Exception ignored) {}
                }
                cir.setReturnValue(InteractionResult.sidedSuccess(world.isClientSide));
                return;
            }

            if (state.getValue(TrapdoorStiffProperties.STIFF)) {
                // Allow placing blocks on top of a stiff trapdoor. If the player
                // is holding a BlockItem (i.e. attempting to place a block),
                // don't cancel the interaction so placement proceeds normally.
                Item held = stack == null ? null : stack.getItem();
                if (held instanceof BlockItem) {
                    // let the normal placement logic run
                    return;
                }

                // Otherwise, block the use (prevents opening/closing)
                cir.setReturnValue(InteractionResult.FAIL);
            }
        } catch (Exception ignored) {}
    }
}
