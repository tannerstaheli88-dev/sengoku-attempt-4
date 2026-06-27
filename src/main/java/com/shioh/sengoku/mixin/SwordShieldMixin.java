package com.shioh.sengoku.mixin;

import com.shioh.sengoku.item.SweeplessItem;
import com.shioh.sengoku.system.PostureHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class SwordShieldMixin {

    private boolean isBlockable(ItemStack stack) {
        return stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof AxeItem
                || stack.getItem() instanceof MaceItem
                || stack.getItem() instanceof SweeplessItem;
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void sengoku$use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (isBlockable(stack)) {

            // ⛔ Prioritize shield: if player has a shield in either hand, don't allow weapon blocking
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            if (mainHand.getItem() instanceof ShieldItem || offHand.getItem() instanceof ShieldItem) {
                // Shield takes priority - let vanilla handle it
                return;
            }

            // ⛔ if posture is broken, don't enter using state
            if (PostureHandler.isGuardBroken(player)) {
                cir.setReturnValue(InteractionResultHolder.pass(stack));
                return;
            }

            player.startUsingItem(hand);
            cir.setReturnValue(InteractionResultHolder.consume(stack));
        }
    }

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void sengoku$getUseAnimation(ItemStack stack, CallbackInfoReturnable<UseAnim> cir) {
        if (isBlockable(stack)) {
            cir.setReturnValue(UseAnim.BLOCK);
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void sengoku$getUseDuration(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (isBlockable(stack)) {
            if (entity instanceof Player p && PostureHandler.isGuardBroken(p)) {
                cir.setReturnValue(0);
                return;
            }
            cir.setReturnValue(72000); // same as shield
        }
    }
}