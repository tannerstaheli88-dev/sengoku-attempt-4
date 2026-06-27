package com.shioh.sengoku.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class KamaBuffMixin {

    @Inject(
        method = "getDefaultAttributeModifiers",
        at = @At("RETURN"),
        cancellable = true
    )
    private void buffHoeDamage(CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        // Check if this is a HoeItem
        if (!((Object)this instanceof HoeItem)) {
            return;
        }

        ItemAttributeModifiers original = cir.getReturnValue();
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        
        // Copy all original modifiers except ATTACK_DAMAGE for MAINHAND
        boolean foundAttackDamage = false;
        for (ItemAttributeModifiers.Entry entry : original.modifiers()) {
            if (entry.slot().equals(EquipmentSlotGroup.MAINHAND) && 
                entry.attribute().equals(Attributes.ATTACK_DAMAGE)) {
                foundAttackDamage = true;
                // Replace with increased damage (+2 more)
                builder.add(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(
                        ResourceLocation.fromNamespaceAndPath("sengoku", "hoe_damage_buff"),
                        entry.modifier().amount() + 2.0,
                        AttributeModifier.Operation.ADD_VALUE
                    ),
                    EquipmentSlotGroup.MAINHAND
                );
            } else {
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
        
        // If no attack damage was found, add it
        if (!foundAttackDamage) {
            builder.add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath("sengoku", "hoe_damage_buff"),
                    2.0,
                    AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
            );
        }

        cir.setReturnValue(builder.build());
    }
}
