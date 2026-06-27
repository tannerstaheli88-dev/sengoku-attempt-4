package com.shioh.sengoku.mixin;

import com.shioh.sengoku.item.TantoItem;
import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.platform.Services;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies Swift Sneak speed while crouching if the player is holding a Tanto with Swift Sneak.
 * This mirrors the vanilla Swift Sneak effect (0.15 bonus per level) but keyed off held Tantos.
 */
@Mixin(Player.class)
public class PlayerSwiftSneakHoldMixin {

    private static final ResourceLocation SENGOKU_TANTO_SWIFT_SNEAK = sengokuFabric.asId("tanto_swift_sneak");

    @Inject(method = "tick", at = @At("HEAD"))
    private void sengoku$applyTantoSwiftSneak(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        // Only matters while crouching
        if (!player.isCrouching()) {
            removeModifier(player);
            return;
        }

        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        int level = getSwiftSneakLevel(player, main);
        if (level <= 0) level = getSwiftSneakLevel(player, off);

        if (level > 0) {
            applyModifier(player, level);
        } else {
            removeModifier(player);
        }
    }

    private static int getSwiftSneakLevel(Player player, ItemStack stack) {
        if (!(stack.getItem() instanceof TantoItem)) return 0;
        return player.level().registryAccess()
            .registry(Registries.ENCHANTMENT)
            .flatMap(reg -> reg.getHolder(Enchantments.SWIFT_SNEAK))
            .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
            .orElse(0);
    }

    private static void applyModifier(Player player, int level) {
        AttributeInstance attr = player.getAttribute(Attributes.SNEAKING_SPEED);
        if (attr == null) return;
        double bonus = 0.15D * level; // vanilla bonus per level
        AttributeModifier modifier = new AttributeModifier(SENGOKU_TANTO_SWIFT_SNEAK, bonus, AttributeModifier.Operation.ADD_VALUE);
        if (attr.getModifier(SENGOKU_TANTO_SWIFT_SNEAK) != null) {
            attr.removeModifier(SENGOKU_TANTO_SWIFT_SNEAK);
        }
        attr.addPermanentModifier(modifier);
    }

    private static void removeModifier(Player player) {
        AttributeInstance attr = player.getAttribute(Attributes.SNEAKING_SPEED);
        if (attr != null && attr.getModifier(SENGOKU_TANTO_SWIFT_SNEAK) != null) {
            attr.removeModifier(SENGOKU_TANTO_SWIFT_SNEAK);
        }
    }
}
