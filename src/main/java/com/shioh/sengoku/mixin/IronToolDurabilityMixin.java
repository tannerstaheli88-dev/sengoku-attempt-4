package com.shioh.sengoku.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class IronToolDurabilityMixin {

    // Multiply iron-tier tool durability by this factor. Change as desired.
    private static final float IRON_DURABILITY_MULTIPLIER = 2.0f;

    @Inject(method = "getMaxDamage", at = @At("RETURN"), cancellable = true)
    private void sengoku$boostIronDurability(CallbackInfoReturnable<Integer> cir) {
        Integer original = cir.getReturnValue();
        if (original == null) return;
        try {
            ItemStack stack = (ItemStack)(Object)this;
            Item item = stack.getItem();
            if (item instanceof TieredItem) {
                TieredItem tiered = (TieredItem) item;
                if (tiered.getTier() == Tiers.IRON) {
                    int boosted = Math.max(1, Math.round(original * IRON_DURABILITY_MULTIPLIER));
                    cir.setReturnValue(boosted);
                }
            }
        } catch (Throwable ignored) {}
    }
}
