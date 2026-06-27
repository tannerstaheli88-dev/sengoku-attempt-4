package com.shioh.sengoku.mixin;

import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(BowItem.class)
public abstract class BowItemMixin extends ProjectileWeaponItem {
    public BowItemMixin(Properties settings) {
        super(settings);
    }

    @Inject(method = "getAllSupportedProjectiles", at = @At("HEAD"), cancellable = true)
    private void onlyNormalArrows(CallbackInfoReturnable<Predicate<ItemStack>> cir) {
        // Only allow normal arrows, not spectral
        cir.setReturnValue(stack -> stack.is(Items.ARROW) || stack.is(Items.TIPPED_ARROW));
    }
}
