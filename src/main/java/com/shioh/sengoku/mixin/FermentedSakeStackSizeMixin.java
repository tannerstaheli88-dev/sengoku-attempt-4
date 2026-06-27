package com.shioh.sengoku.mixin;

import com.shioh.sengoku.item.FermentedSakeItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class FermentedSakeStackSizeMixin {

    @Shadow
    public abstract net.minecraft.world.item.Item getItem();

    @Inject(method = "getMaxStackSize()I", at = @At("HEAD"), cancellable = true)
    private void limitFermentedSakeStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack)(Object)this;
        // If this is fermented sake, ensure it stacks like other bottles (16)
        if (self.getItem() instanceof FermentedSakeItem) {
            cir.setReturnValue(16);
        }
    }
}
