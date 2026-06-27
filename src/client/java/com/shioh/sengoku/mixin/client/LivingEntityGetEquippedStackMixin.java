package com.shioh.sengoku.mixin.client;

import com.shioh.sengoku.item.ShinobiArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityGetEquippedStackMixin {

    @Inject(method = "getEquippedStack", at = @At("RETURN"), cancellable = true)
    private void sengoku_onGetEquippedStack(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if (stack == null || stack.isEmpty()) return;
        if (!((Object) this instanceof Player)) return;
        Player player = (Player) (Object) this;
        if (!player.isInvisible()) return;
        if (stack.getItem() instanceof ShinobiArmorItem) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}
