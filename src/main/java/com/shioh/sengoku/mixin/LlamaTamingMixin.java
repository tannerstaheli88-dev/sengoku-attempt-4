package com.shioh.sengoku.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows llamas to be tamed with wheat instead of hay bales.
 */
@Mixin(AbstractHorse.class)
public class LlamaTamingMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void handleWheatTaming(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        AbstractHorse horse = (AbstractHorse)(Object)this;

        if (!(horse instanceof Llama llama)) return;
        if (!player.getItemInHand(hand).is(Items.WHEAT)) return;
        if (llama.isBaby()) return;

        // Play eat animation and consume wheat with correct sound
        llama.level().broadcastEntityEvent(llama, (byte)4);
        if (!player.isCreative()) {
            player.getItemInHand(hand).shrink(1);
        }

        // Cancel so vanilla never sees the wheat and plays its silent version
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
