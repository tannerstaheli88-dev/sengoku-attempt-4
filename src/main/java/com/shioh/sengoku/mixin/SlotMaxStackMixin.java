package com.shioh.sengoku.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMaxStackMixin {

    @Shadow public Container container;

    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at = @At("RETURN"), cancellable = true)
    private void increaseSlotMaxStackSize(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        // Brewing stand potion/result slots must stay at 1 to prevent multi-stack brew output issues.
        if (this.getClass().getName().contains("BrewingStandMenu$PotionSlot")) {
            return;
        }

        Integer original = cir.getReturnValue();
        if (original == null) return;

        int stackCap = stack.getMaxStackSize();
        int containerCap = this.container.getMaxStackSize();

        // Only increase slot limits for containers that support full stacks (>=64).
        // This prevents containers that intentionally limit slots (e.g. sake barrels with max 1)
        // from being overridden by the global 99-stack mixins.
        if (original < stackCap && containerCap >= 64) {
            cir.setReturnValue(Math.min(99, stackCap));
        }
    }
}
