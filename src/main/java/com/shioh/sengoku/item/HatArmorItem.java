package com.shioh.sengoku.item;

// Patched: removed stray character and ensured file timestamp updated

import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.init.HatArmorMaterialReg;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Simple hat/helmet items (straw, sando, tengai).
 * These use the Shinobi armor material for texture layers but are light helmets.
 */
public class HatArmorItem extends ArmorItem {
    
    public HatArmorItem(Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties properties) {
        super(material, type, properties);
    }

    public static Item createStrawHat() {
        return new HatArmorItem(
            HatArmorMaterialReg.STRAW,
            ArmorItem.Type.HELMET,
                new Item.Properties()
                .durability(ArmorItem.Type.HELMET.getDurability(8))
                .rarity(Rarity.COMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.BLOCK_BREAK_SPEED,
                            new AttributeModifier(sengokuFabric.asId("straw_hat_mining_speed"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .build()
                )
        );
    }

    public static Item createBlackStrawHat() {
        return new HatArmorItem(
            HatArmorMaterialReg.STRAW_BLACK,
            ArmorItem.Type.HELMET,
                new Item.Properties()
                .durability(ArmorItem.Type.HELMET.getDurability(8))
                .rarity(Rarity.COMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.BLOCK_BREAK_SPEED,
                            new AttributeModifier(sengokuFabric.asId("black_straw_hat_mining_speed"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .build()
                )
        );
    }

    public static Item createWhiteStrawHat() {
        return new HatArmorItem(
            HatArmorMaterialReg.STRAW_WHITE,
            ArmorItem.Type.HELMET,
                new Item.Properties()
                .durability(ArmorItem.Type.HELMET.getDurability(8))
                .rarity(Rarity.COMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.BLOCK_BREAK_SPEED,
                            new AttributeModifier(sengokuFabric.asId("white_straw_hat_mining_speed"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .build()
                )
        );
    }

    public static Item createSandoHat() {
        return new HatArmorItem(
            HatArmorMaterialReg.SANDO,
            ArmorItem.Type.HELMET,
                new Item.Properties()
                .durability(ArmorItem.Type.HELMET.getDurability(8))
                .rarity(Rarity.COMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.BLOCK_INTERACTION_RANGE,
                            new AttributeModifier(sengokuFabric.asId("sando_hat_block_range"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .build()
                )
        );
    }

    public static Item createBlackSandoHat() {
        return new HatArmorItem(
            HatArmorMaterialReg.SANDO_BLACK,
            ArmorItem.Type.HELMET,
                new Item.Properties()
                .durability(ArmorItem.Type.HELMET.getDurability(8))
                .rarity(Rarity.COMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.BLOCK_INTERACTION_RANGE,
                            new AttributeModifier(sengokuFabric.asId("black_sando_hat_block_range"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .build()
                )
        );
    }

    public static Item createWhiteSandoHat() {
        return new HatArmorItem(
            HatArmorMaterialReg.SANDO_WHITE,
            ArmorItem.Type.HELMET,
                new Item.Properties()
                .durability(ArmorItem.Type.HELMET.getDurability(8))
                .rarity(Rarity.COMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.BLOCK_INTERACTION_RANGE,
                            new AttributeModifier(sengokuFabric.asId("white_sando_hat_block_range"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .build()
                )
        );
    }

    public static Item createTengaiHat() {
        return new HatArmorItem(
            HatArmorMaterialReg.TENGAI,
            ArmorItem.Type.HELMET,
                new Item.Properties()
                .durability(ArmorItem.Type.HELMET.getDurability(8))
                .rarity(Rarity.COMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.SNEAKING_SPEED,
                            new AttributeModifier(sengokuFabric.asId("tengai_hat_sneak_speed"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.HEAD)
                        .build()
                )
        );
    }

    public static Item createStrawCape() {
        return new HatArmorItem(
            HatArmorMaterialReg.STRAW_CAPE,
            ArmorItem.Type.CHESTPLATE,
                new Item.Properties()
                .durability(ArmorItem.Type.CHESTPLATE.getDurability(16))
                .rarity(Rarity.COMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                            new AttributeModifier(sengokuFabric.asId("straw_cape_armor"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .add(Attributes.WATER_MOVEMENT_EFFICIENCY,
                            new AttributeModifier(sengokuFabric.asId("straw_cape_water_movement"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .build()
                )
        );
    }

    public static Item createBlackStrawCape() {
        return new HatArmorItem(
            HatArmorMaterialReg.STRAW_CAPE_BLACK,
            ArmorItem.Type.CHESTPLATE,
                new Item.Properties()
                .durability(ArmorItem.Type.CHESTPLATE.getDurability(16))
                .rarity(Rarity.COMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                            new AttributeModifier(sengokuFabric.asId("black_straw_cape_armor"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .add(Attributes.WATER_MOVEMENT_EFFICIENCY,
                            new AttributeModifier(sengokuFabric.asId("black_straw_cape_water_movement"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .build()
                )
        );
    }

    public static Item createWhiteStrawCape() {
        return new HatArmorItem(
            HatArmorMaterialReg.STRAW_CAPE_WHITE,
            ArmorItem.Type.CHESTPLATE,
                new Item.Properties()
                .durability(ArmorItem.Type.CHESTPLATE.getDurability(16))
                .rarity(Rarity.COMMON)
                .component(DataComponents.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.builder()
                        .add(Attributes.ARMOR,
                            new AttributeModifier(sengokuFabric.asId("white_straw_cape_armor"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .add(Attributes.WATER_MOVEMENT_EFFICIENCY,
                            new AttributeModifier(sengokuFabric.asId("white_straw_cape_water_movement"), 1.0, AttributeModifier.Operation.ADD_VALUE),
                            EquipmentSlotGroup.CHEST)
                        .build()
                )
        );
    }

    // Color handling removed from Java - handled externally when reintroduced.
}
