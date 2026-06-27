package com.shioh.sengoku.mixin;

import com.shioh.sengoku.util.PlayerNoiseTracker;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds noise when a player finishes consuming an edible item (food/drink).
 */
@Mixin(Item.class)
public abstract class ItemFinishUsingNoiseMixin {
    @Inject(method = "finishUsingItem", at = @At("RETURN"))
    private void sengoku$addConsumeNoise(ItemStack stack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        try {
            Item self = (Item)(Object)this;
            UseAnim anim = stack.getUseAnimation();
            if (anim == UseAnim.EAT || anim == UseAnim.DRINK) {
                PlayerNoiseTracker.getInstance().addNoise(player, PlayerNoiseTracker.EAT_NOISE);
            }
        } catch (Throwable ignored) {}
    }
}
