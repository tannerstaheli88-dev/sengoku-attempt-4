package com.shioh.sengoku.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class VindicatorEliteEntityMixin {
    @Unique
    private static final float SENGOKU_ELITE_SPAWN_CHANCE = 0.08F;

    @Unique
    private static final double SENGOKU_ELITE_MAX_HEALTH = 48.0D;

    @Unique
    private static final String SENGOKU_ELITE_TAG = "sengoku_elite";

    @Unique
    private boolean sengoku$vindicatorElite = false;

    @Unique
    private boolean sengoku$vindicatorEliteInitialized = false;

    @Inject(method = "tick", at = @At("TAIL"))
    private void sengoku$initVindicatorEliteOnTick(CallbackInfo ci) {
        if (!((Object) this instanceof Vindicator vindicator) || vindicator.level().isClientSide) {
            return;
        }

        // Only apply to vanilla Vindicators — skip all custom subclasses
        if (vindicator.getType() != net.minecraft.world.entity.EntityType.VINDICATOR) {
            return;
        }

        if (!this.sengoku$vindicatorEliteInitialized) {
            this.sengoku$vindicatorEliteInitialized = true;
            boolean elite = this.sengoku$vindicatorElite
                || vindicator.getTags().contains(SENGOKU_ELITE_TAG)
                || vindicator.getAttributeValue(Attributes.MAX_HEALTH) >= (SENGOKU_ELITE_MAX_HEALTH - 0.1D)
                || vindicator.getRandom().nextFloat() < SENGOKU_ELITE_SPAWN_CHANCE;
            if (elite) {
                this.sengoku$vindicatorElite = true;
                vindicator.addTag(SENGOKU_ELITE_TAG);
                this.sengoku$applyEliteStatsAndWeapon(vindicator);
            }
            return;
        }

        if (this.sengoku$vindicatorElite && vindicator.getMainHandItem().isEmpty()) {
            this.sengoku$applyEliteStatsAndWeapon(vindicator);
        }
    }

    @Unique
    private void sengoku$applyEliteStatsAndWeapon(Vindicator vindicator) {
        try {
            var maxHealth = vindicator.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null && maxHealth.getBaseValue() < SENGOKU_ELITE_MAX_HEALTH) {
                maxHealth.setBaseValue(SENGOKU_ELITE_MAX_HEALTH);
                vindicator.setHealth(vindicator.getMaxHealth());
            }

            ItemStack weapon = vindicator.getItemBySlot(EquipmentSlot.MAINHAND);
            if (weapon.isEmpty()) {
                weapon = new ItemStack(Items.IRON_SWORD);
                vindicator.setItemSlot(EquipmentSlot.MAINHAND, weapon);
                vindicator.setDropChance(EquipmentSlot.MAINHAND, 0.05F);
            }

            ItemEnchantments current = weapon.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);
            var enchantments = vindicator.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            enchantments.getHolder(Enchantments.SHARPNESS).ifPresent(holder -> mutable.set(holder, 3));
            enchantments.getHolder(Enchantments.UNBREAKING).ifPresent(holder -> mutable.set(holder, 2));
            weapon.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        } catch (Throwable ignored) {}
    }
}
