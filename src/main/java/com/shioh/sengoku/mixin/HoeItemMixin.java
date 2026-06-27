package com.shioh.sengoku.mixin;

import com.shioh.sengoku.registry.SengokuBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoeItem.class)
public class HoeItemMixin {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void sengoku$tillCoarseFarmland(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!state.is(SengokuBlocks.COARSE_FARMLAND)) {
            return;
        }

        if (context.getClickedFace() == Direction.DOWN || !level.getBlockState(pos.above()).isAir()) {
            cir.setReturnValue(InteractionResult.PASS);
            return;
        }

        Player player = context.getPlayer();

        if (!level.isClientSide) {
            level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), 11);
            level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);

            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        }

        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide));
    }
}