package com.shioh.sengoku.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MaxStackSizeMixin {

    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void increaseDefaultStackSize(CallbackInfoReturnable<Integer> cir) {
        Integer original = cir.getReturnValue();
        if (original != null && original.intValue() == 64) {
            // Use 99 to match container limits and avoid protocol issues
            cir.setReturnValue(99);
        }
    }
}
