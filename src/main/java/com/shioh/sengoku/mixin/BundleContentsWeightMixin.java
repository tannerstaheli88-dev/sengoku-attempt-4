package com.shioh.sengoku.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleContents.class)
public abstract class BundleContentsWeightMixin {

    private static final Fraction SENGOKU_SPECIAL_ITEM_WEIGHT = Fraction.getFraction(16, 99);
    private static final Fraction SENGOKU_SINGLE_SLOT_WEIGHT = Fraction.getFraction(1, 99);
    private static final Fraction SENGOKU_BUNDLE_IN_BUNDLE_WEIGHT = Fraction.getFraction(4, 99);

    @Inject(method = "getWeight", at = @At("RETURN"), cancellable = true)
    private static void sengoku$reduceSpecialItemBundleWeight(ItemStack stack, CallbackInfoReturnable<Fraction> cir) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        BundleContents bundleContents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents != null) {
            cir.setReturnValue(SENGOKU_BUNDLE_IN_BUNDLE_WEIGHT.add(bundleContents.weight()));
            return;
        }

        Item item = stack.getItem();
        String descriptionId = item.getDescriptionId();
        boolean isMusicDisc = descriptionId != null && descriptionId.contains("music_disc");
        if (isMusicDisc || item == Items.ENCHANTED_BOOK) {
            cir.setReturnValue(SENGOKU_SINGLE_SLOT_WEIGHT);
            return;
        }

        if (item instanceof TieredItem || item instanceof ArmorItem) {
            cir.setReturnValue(SENGOKU_SPECIAL_ITEM_WEIGHT);
            return;
        }

        // Any remaining item that would consume the entire bundle gets reduced
        // to a single slot in the 99-capacity system.
        if (Fraction.ONE.equals(cir.getReturnValue())) {
            cir.setReturnValue(SENGOKU_SINGLE_SLOT_WEIGHT);
        }
    }
}