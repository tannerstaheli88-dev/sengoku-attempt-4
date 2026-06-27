package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to replace white banners with plain red banners on patrol leaders.
 */
@Mixin(PatrollingMonster.class)
public abstract class RaidBannerReplacementMixin {
    
    /**
     * Replace white banner with plain red banner for patrol leaders.
     */
    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void replacePatrolBanner(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        // Check if this is a white banner in the head slot
        if (slot == EquipmentSlot.HEAD && stack.getItem() == Items.WHITE_BANNER) {
            PatrollingMonster entity = (PatrollingMonster)(Object)this;
            
            // Create a plain red banner (no patterns)
            ItemStack redBanner = new ItemStack(Items.RED_BANNER);
            
            // Set the red banner instead
            entity.setItemSlot(slot, redBanner);
            ci.cancel();
        }
    }
}
