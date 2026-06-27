package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public abstract class PlayerInventoryMaxStackMixin {

    private static final int SENGOKU_MAX_STACK_SIZE = 999;

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void increasePlayerInventoryMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(SENGOKU_MAX_STACK_SIZE);
    }
}