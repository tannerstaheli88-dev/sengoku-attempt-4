package com.shioh.sengoku.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class BowlItemStackSizeMixin {

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void allowStackableBowlItems(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack)(Object)this;
        Item item = self.getItem();

        // Make all bowl-based food items stackable to 16
        if (item == Items.MUSHROOM_STEW || 
            item == Items.RABBIT_STEW || 
            item == Items.BEETROOT_SOUP || 
            item == Items.SUSPICIOUS_STEW) {
            cir.setReturnValue(16);
        }
    }
}
