package com.shioh.sengoku.init;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import com.shioh.sengoku.item.FermentedSakeItem;
import com.shioh.sengoku.item.SakeBottleItem;
import net.minecraft.world.item.Items;

/**
 * Minimal item registration stub for Sake-related items.
 * This recreates the previously-missing FERMENTED_SAKE_BOTTLE constant
 * and exposes a registerItems() method matching the project's style.
 *
 * Replace or extend this with your original implementation as needed.
 */
public final class SakeItemReg {

    private SakeItemReg() {}

    public static final Item FERMENTED_SAKE_BOTTLE = new FermentedSakeItem(new Item.Properties());
    // Healing variants (distinct items)
    public static final Item FERMENTED_SAKE_HEALING = new FermentedSakeItem(new Item.Properties());
    public static final Item FERMENTED_SAKE_REGEN = new FermentedSakeItem(new Item.Properties());
    public static final Item FERMENTED_SAKE_SWIFTNESS = new FermentedSakeItem(new Item.Properties());
    public static final Item FERMENTED_SAKE_LEAPING = new FermentedSakeItem(new Item.Properties());
    public static final Item FERMENTED_SAKE_FIRE_RESIST = new FermentedSakeItem(new Item.Properties());
    public static final Item FERMENTED_SAKE_INVISIBILITY = new FermentedSakeItem(new Item.Properties());
    public static final Item FERMENTED_SAKE_SLOW_FALLING = new FermentedSakeItem(new Item.Properties());
    public static final Item FERMENTED_SAKE_ABSORPTION = new FermentedSakeItem(new Item.Properties());
    public static final Item FERMENTED_SAKE_WATER_BREATHING = new FermentedSakeItem(new Item.Properties());
    public static final Item FERMENTED_SAKE_LUCK = new FermentedSakeItem(new Item.Properties());
    public static final Item SAKE_BOTTLE = new SakeBottleItem(new Item.Properties());
    public static final Item SAKE_HEALING = new SakeBottleItem(new Item.Properties());
    public static final Item SAKE_REGEN = new SakeBottleItem(new Item.Properties());
    public static final Item SAKE_SWIFTNESS = new SakeBottleItem(new Item.Properties());
    public static final Item SAKE_LEAPING = new SakeBottleItem(new Item.Properties());
    public static final Item SAKE_FIRE_RESIST = new SakeBottleItem(new Item.Properties());
    public static final Item SAKE_INVISIBILITY = new SakeBottleItem(new Item.Properties());
    public static final Item SAKE_SLOW_FALLING = new SakeBottleItem(new Item.Properties());
    public static final Item SAKE_ABSORPTION = new SakeBottleItem(new Item.Properties());
    public static final Item SAKE_WATER_BREATHING = new SakeBottleItem(new Item.Properties());
    public static final Item SAKE_LUCK = new SakeBottleItem(new Item.Properties());

    public static void registerItems() {
        // Register fermented sake bottle item; uses a simple Item placeholder.
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("fermented_sake_bottle"), FERMENTED_SAKE_BOTTLE);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("fermented_sake_of_healing"), FERMENTED_SAKE_HEALING);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("fermented_sake_of_regen"), FERMENTED_SAKE_REGEN);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("fermented_sake_of_swiftness"), FERMENTED_SAKE_SWIFTNESS);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("fermented_sake_of_leaping"), FERMENTED_SAKE_LEAPING);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("fermented_sake_of_fire_resist"), FERMENTED_SAKE_FIRE_RESIST);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("fermented_sake_of_invisibility"), FERMENTED_SAKE_INVISIBILITY);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("fermented_sake_of_slow_falling"), FERMENTED_SAKE_SLOW_FALLING);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("fermented_sake_of_absorption"), FERMENTED_SAKE_ABSORPTION);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("fermented_sake_of_water_breathing"), FERMENTED_SAKE_WATER_BREATHING);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("fermented_sake_of_luck"), FERMENTED_SAKE_LUCK);
        // Place fermented sake in the Food & Drinks creative tab before the water bottle/potions
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(net.minecraft.world.item.Items.POTION, FERMENTED_SAKE_BOTTLE));
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(net.minecraft.world.item.Items.POTION, FERMENTED_SAKE_HEALING));
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(net.minecraft.world.item.Items.POTION, FERMENTED_SAKE_REGEN));
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(net.minecraft.world.item.Items.POTION, FERMENTED_SAKE_SWIFTNESS));
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(net.minecraft.world.item.Items.POTION, FERMENTED_SAKE_LEAPING));
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(net.minecraft.world.item.Items.POTION, FERMENTED_SAKE_FIRE_RESIST));
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(net.minecraft.world.item.Items.POTION, FERMENTED_SAKE_INVISIBILITY));
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(net.minecraft.world.item.Items.POTION, FERMENTED_SAKE_SLOW_FALLING));
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(net.minecraft.world.item.Items.POTION, FERMENTED_SAKE_ABSORPTION));
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(net.minecraft.world.item.Items.POTION, FERMENTED_SAKE_WATER_BREATHING));
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(net.minecraft.world.item.Items.POTION, FERMENTED_SAKE_LUCK));

                // Register fermented sake bottle item; uses a simple Item placeholder.
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_bottle"), SAKE_BOTTLE);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_of_healing"), SAKE_HEALING);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_of_regen"), SAKE_REGEN);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_of_swiftness"), SAKE_SWIFTNESS);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_of_leaping"), SAKE_LEAPING);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_of_fire_resist"), SAKE_FIRE_RESIST);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_of_invisibility"), SAKE_INVISIBILITY);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_of_slow_falling"), SAKE_SLOW_FALLING);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_of_absorption"), SAKE_ABSORPTION);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_of_water_breathing"), SAKE_WATER_BREATHING);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_of_luck"), SAKE_LUCK);
        // Place fermented sake in the Food & Drinks creative tab before the water bottle/potions
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(FERMENTED_SAKE_BOTTLE, SAKE_BOTTLE));
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
            .register(entries -> entries.addBefore(SAKE_BOTTLE, SAKE_HEALING));
            net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addBefore(SAKE_HEALING, SAKE_REGEN));
            net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addBefore(SAKE_REGEN, SAKE_SWIFTNESS));
            net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addBefore(SAKE_SWIFTNESS, SAKE_LEAPING));
            net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addBefore(SAKE_LEAPING, SAKE_FIRE_RESIST));
            net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addBefore(SAKE_FIRE_RESIST, SAKE_INVISIBILITY));
            net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addBefore(SAKE_INVISIBILITY, SAKE_SLOW_FALLING));
            net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addBefore(SAKE_SLOW_FALLING, SAKE_ABSORPTION));
            net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addBefore(SAKE_ABSORPTION, SAKE_WATER_BREATHING));
            net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addBefore(SAKE_WATER_BREATHING, SAKE_LUCK));
    }
    
}
