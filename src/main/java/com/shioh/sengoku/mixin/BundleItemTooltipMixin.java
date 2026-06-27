package com.shioh.sengoku.mixin;

import net.minecraft.world.item.BundleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BundleItem.class)
public abstract class BundleItemTooltipMixin {

    @ModifyConstant(method = "appendHoverText", constant = @Constant(intValue = 64))
    private int sengoku$useBundleCapacityOf99(int original) {
        return 99;
    }
}