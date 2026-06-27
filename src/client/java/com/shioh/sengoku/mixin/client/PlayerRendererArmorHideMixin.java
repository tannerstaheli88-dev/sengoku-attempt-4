package com.shioh.sengoku.mixin.client;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Hides Shinobi armor pieces on the client when the player is invisible by
 * replacing returned armor ItemStacks with ItemStack.EMPTY during rendering.
 */
@Mixin(Entity.class)
public abstract class PlayerRendererArmorHideMixin {

    @Inject(method = "getItemBySlot", at = @At("RETURN"), cancellable = true, require = 0)
    private void sengoku$hideShinobiArmor(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if (stack == null) return;

        Entity self = (Entity) (Object) this;
        // Only run on the client environment to avoid changing server-side logic
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) return;

        if (self instanceof Player) {
            Player player = (Player) self;
            // player.isInvisible() covers armor invisibility flags and potion effects
            if (player.isInvisible() && stack.getItem() instanceof com.shioh.sengoku.item.ShinobiArmorItem) {
                cir.setReturnValue(ItemStack.EMPTY);
            }
        }
    }
}
