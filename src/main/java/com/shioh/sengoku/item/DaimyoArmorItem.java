package com.shioh.sengoku.item;

import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.init.DaimyoArmorMaterialReg;
import com.shioh.sengoku.init.DaimyoNetheriteArmorMaterialReg;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
// use our custom holder so textures resolve to daimyo_layer_*.png
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Custom Daimyo armor pieces worn by Kensei enemies.
 * Each piece has custom attributes matching the mcfunction definition.
 */
public class DaimyoArmorItem extends ArmorItem {
    
    public DaimyoArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }
    
    /**
     * Creates the Daimyo Kabuto (helmet) with custom attributes.
     */
    public static Item createHelmet() {
        return new DaimyoArmorItem(
            DaimyoArmorMaterialReg.DAIMYO,
            Type.HELMET,
            new Item.Properties()
                // provide durability so the item is considered damageable and can be enchanted
                .durability(ArmorItem.Type.HELMET.getDurability(33))
                .rarity(Rarity.RARE)
                .component(DataComponents.ATTRIBUTE_MODIFIERS, 
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR, 
                            new AttributeModifier(sengokuFabric.asId("daimyo_kabuto_armor"), 4.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .add(Attributes.ARMOR_TOUGHNESS,
                            new AttributeModifier(sengokuFabric.asId("daimyo_kabuto_toughness"), 3.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .build()
                )
                .component(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
    
    /**
     * Creates the Netherite Daimyo Kabuto (helmet) with improved attributes.
     */
    public static Item createNetheriteHelmet() {
        return new DaimyoArmorItem(
            DaimyoNetheriteArmorMaterialReg.DAIMYO_NETHERITE,
            Type.HELMET,
            new Item.Properties()
                .durability(ArmorItem.Type.HELMET.getDurability(37))
                .rarity(Rarity.EPIC)
                .fireResistant()
                .component(DataComponents.ATTRIBUTE_MODIFIERS, 
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR, 
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_kabuto_armor"), 4.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .add(Attributes.ARMOR_TOUGHNESS,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_kabuto_toughness"), 4.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .add(Attributes.KNOCKBACK_RESISTANCE,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_kabuto_kb"), 0.1, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .build()
                )
                .component(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
    
    /**
     * Creates the Daimyo Dō (chestplate) with custom attributes.
     */
    public static Item createChestplate() {
        return new DaimyoArmorItem(
            DaimyoArmorMaterialReg.DAIMYO,
            Type.CHESTPLATE,
            new Item.Properties()
                .durability(ArmorItem.Type.CHESTPLATE.getDurability(33))
                .rarity(Rarity.RARE)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                            new AttributeModifier(sengokuFabric.asId("daimyo_do_armor"), 8.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .add(Attributes.ARMOR_TOUGHNESS,
                            new AttributeModifier(sengokuFabric.asId("daimyo_do_toughness"), 3.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .build()
                )
                .component(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
    
    /**
     * Creates the Netherite Daimyo Dō (chestplate) with improved attributes.
     */
    public static Item createNetheriteChestplate() {
        return new DaimyoArmorItem(
            DaimyoNetheriteArmorMaterialReg.DAIMYO_NETHERITE,
            Type.CHESTPLATE,
            new Item.Properties()
                .durability(ArmorItem.Type.CHESTPLATE.getDurability(37))
                .rarity(Rarity.EPIC)
                .fireResistant()
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_do_armor"), 9.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .add(Attributes.ARMOR_TOUGHNESS,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_do_toughness"), 4.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .add(Attributes.KNOCKBACK_RESISTANCE,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_do_kb"), 0.1, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .build()
                )
                .component(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
    
    /**
     * Creates the Daimyo Haidate (leggings) with custom attributes.
     */
    public static Item createLeggings() {
        return new DaimyoArmorItem(
            DaimyoArmorMaterialReg.DAIMYO,
            Type.LEGGINGS,
            new Item.Properties()
                .durability(ArmorItem.Type.LEGGINGS.getDurability(33))
                .rarity(Rarity.RARE)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                            new AttributeModifier(sengokuFabric.asId("daimyo_haidate_armor"), 6.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.LEGS)
                        .add(Attributes.ARMOR_TOUGHNESS,
                            new AttributeModifier(sengokuFabric.asId("daimyo_haidate_toughness"), 3.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.LEGS)
                        .add(Attributes.SAFE_FALL_DISTANCE,
                            new AttributeModifier(sengokuFabric.asId("daimyo_haidate_fall"), 3.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.LEGS)
                        .build()
                )
                .component(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
    
    /**
     * Creates the Netherite Daimyo Haidate (leggings) with improved attributes.
     */
    public static Item createNetheriteLeggings() {
        return new DaimyoArmorItem(
            DaimyoNetheriteArmorMaterialReg.DAIMYO_NETHERITE,
            Type.LEGGINGS,
            new Item.Properties()
                .durability(ArmorItem.Type.LEGGINGS.getDurability(37))
                .rarity(Rarity.EPIC)
                .fireResistant()
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_haidate_armor"), 7.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.LEGS)
                        .add(Attributes.ARMOR_TOUGHNESS,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_haidate_toughness"), 4.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.LEGS)
                        .add(Attributes.SAFE_FALL_DISTANCE,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_haidate_fall"), 4.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.LEGS)
                        .add(Attributes.KNOCKBACK_RESISTANCE,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_haidate_kb"), 0.1, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.LEGS)
                        .build()
                )
                .component(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
    
    /**
     * Creates the Daimyo Kusazuri (boots) with custom attributes.
     */
    public static Item createBoots() {
        return new DaimyoArmorItem(
            DaimyoArmorMaterialReg.DAIMYO,
            Type.BOOTS,
            new Item.Properties()
                .durability(ArmorItem.Type.BOOTS.getDurability(33))
                .rarity(Rarity.RARE)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                            new AttributeModifier(sengokuFabric.asId("daimyo_kusazuri_armor"), 3.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .add(Attributes.ARMOR_TOUGHNESS,
                            new AttributeModifier(sengokuFabric.asId("daimyo_kusazuri_toughness"), 3.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .add(Attributes.JUMP_STRENGTH,
                            new AttributeModifier(sengokuFabric.asId("daimyo_kusazuri_jump"), 0.15, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .add(Attributes.MOVEMENT_SPEED,
                            new AttributeModifier(sengokuFabric.asId("daimyo_kusazuri_speed"), 0.02, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .add(Attributes.STEP_HEIGHT,
                            new AttributeModifier(sengokuFabric.asId("daimyo_kusazuri_step"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .build()
                )
                .component(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
    
    /**
     * Creates the Netherite Daimyo Kusazuri (boots) with improved attributes.
     */
    public static Item createNetheriteBoots() {
        return new DaimyoArmorItem(
            DaimyoNetheriteArmorMaterialReg.DAIMYO_NETHERITE,
            Type.BOOTS,
            new Item.Properties()
                .durability(ArmorItem.Type.BOOTS.getDurability(37))
                .rarity(Rarity.EPIC)
                .fireResistant()
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_kusazuri_armor"), 4.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .add(Attributes.ARMOR_TOUGHNESS,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_kusazuri_toughness"), 4.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .add(Attributes.JUMP_STRENGTH,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_kusazuri_jump"), 0.2, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .add(Attributes.MOVEMENT_SPEED,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_kusazuri_speed"), 0.03, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .add(Attributes.KNOCKBACK_RESISTANCE,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_kusazuri_kb"), 0.1, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .add(Attributes.STEP_HEIGHT,
                            new AttributeModifier(sengokuFabric.asId("daimyo_netherite_kusazuri_step"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.FEET)
                        .build()
                )
                .component(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
}
