package com.shioh.sengoku.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Clamp any attempts to set an ItemStack's count above 127 to avoid
 * network serialization overflow (Minecraft uses a signed byte).
 */
@Mixin(net.minecraft.world.item.ItemStack.class)
public abstract class ItemStackSetCountClampMixin {

    @ModifyVariable(method = "setCount", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int sengoku$clampSetCount(int count) {
        // Clamp any attempts to set counts above 99 to avoid protocol overflow and
        // to keep client/server behavior consistent with our desired limit.
        return Math.min(99, count);
    }
}
