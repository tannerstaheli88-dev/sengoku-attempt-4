package com.shioh.sengoku.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin {

    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void keepBottleSlotsSingleStack(int index, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (index >= 0 && index <= 2 && sengoku$isPotionBottle(stack)) {
            BrewingStandBlockEntity self = (BrewingStandBlockEntity) (Object) this;
            cir.setReturnValue(self.getItem(index).isEmpty());
        }
    }

    @Inject(method = "canPlaceItemThroughFace", at = @At("HEAD"), cancellable = true)
    private void keepBottleSlotsSingleStackThroughAutomation(int index, ItemStack stack, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (index >= 0 && index <= 2 && sengoku$isPotionBottle(stack)) {
            BrewingStandBlockEntity self = (BrewingStandBlockEntity) (Object) this;
            cir.setReturnValue(self.getItem(index).isEmpty());
        }
    }

    private static boolean sengoku$isPotionBottle(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }
}