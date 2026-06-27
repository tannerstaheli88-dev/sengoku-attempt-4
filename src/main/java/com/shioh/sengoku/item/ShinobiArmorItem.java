package com.shioh.sengoku.item;

import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.init.ShinobiArmorMaterialReg;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Custom Shinobi armor pieces worn by Illusioner/Shinobi enemies.
 * Each piece has custom attributes matching the mcfunction definition.
 */
public class ShinobiArmorItem extends ArmorItem {
    
    public ShinobiArmorItem(Type type, Properties properties) {
        super(ShinobiArmorMaterialReg.SHINOBI, type, properties);
    }
    
    /**
     * Creates the Shinobi Hood (helmet) with custom attributes.
     * - Armor: 2
     * - Sneaking speed: +1
     */
    public static Item createHelmet() {
        return new ShinobiArmorItem(
            Type.HELMET,
            new Item.Properties()
                .durability(ArmorItem.Type.HELMET.getDurability(15))
                .rarity(Rarity.UNCOMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS, 
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR, 
                            new AttributeModifier(sengokuFabric.asId("shinobi_hood_armor"), 2.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .add(Attributes.SNEAKING_SPEED,
                            new AttributeModifier(sengokuFabric.asId("shinobi_hood_sneak"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .build()
                )
        );
    }
    
    /**
     * Creates the Shinobi Shoulder Plates (chestplate) with custom attributes.
     * - Armor: 5
     * - Follow range: -7
     */
    public static Item createChestplate() {
        return new ShinobiArmorItem(
            Type.CHESTPLATE,
            new Item.Properties()
                .durability(ArmorItem.Type.CHESTPLATE.getDurability(15))
                .rarity(Rarity.UNCOMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                            new AttributeModifier(sengokuFabric.asId("shinobi_chest_armor"), 5.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .add(Attributes.FOLLOW_RANGE,
                            new AttributeModifier(sengokuFabric.asId("shinobi_chest_range"), -7.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .build()
                )
        );
    }
    
    /**
     * Creates the Shinobi Sash (leggings) with custom attributes.
     * - Armor: 4
     * - Safe fall distance: +6
     */
    public static Item createLeggings() {
        return new ShinobiArmorItem(
            Type.LEGGINGS,
            new Item.Properties()
                .durability(ArmorItem.Type.LEGGINGS.getDurability(15))
                .rarity(Rarity.UNCOMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                            new AttributeModifier(sengokuFabric.asId("shinobi_legs_armor"), 4.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.LEGS)
                        .add(Attributes.SAFE_FALL_DISTANCE,
                            new AttributeModifier(sengokuFabric.asId("shinobi_legs_fall"), 6.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.LEGS)
                        .build()
                )
        );
    }
    
    /**
     * Creates the Shinobi Thigh Guards (boots) with custom attributes.
     * - Armor: 1
     * - Jump strength: +0.2
     * - Movement speed: +0.02
     */
    public static Item createBoots() {
        return new ShinobiArmorItem(
            Type.BOOTS,
            new Item.Properties()
                .durability(ArmorItem.Type.BOOTS.getDurability(15))
                .rarity(Rarity.UNCOMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                            new AttributeModifier(sengokuFabric.asId("shinobi_boots_armor"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .add(Attributes.JUMP_STRENGTH,
                            new AttributeModifier(sengokuFabric.asId("shinobi_boots_jump"), 0.2, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .add(Attributes.MOVEMENT_SPEED,
                            new AttributeModifier(sengokuFabric.asId("shinobi_boots_speed"), 0.02, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .add(Attributes.STEP_HEIGHT,
                            new AttributeModifier(sengokuFabric.asId("shinobi_boots_step"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .build()
                )
        );
    }
}
