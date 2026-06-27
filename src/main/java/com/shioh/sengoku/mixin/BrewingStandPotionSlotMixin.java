package com.shioh.sengoku.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.BrewingStandMenu$PotionSlot")
public abstract class BrewingStandPotionSlotMixin {

    @Inject(method = "getMaxStackSize()I", at = @At("HEAD"), cancellable = true)
    private void keepPotionSlotsSingleItem(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(1);
    }

    private static boolean sengoku$isPotionBottle(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }
}