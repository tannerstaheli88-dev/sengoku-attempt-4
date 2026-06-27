package com.shioh.sengoku.init;

import com.shioh.sengoku.block.Tansu;
import com.shioh.sengoku.item.RiceItem;
import com.shioh.sengoku.item.TeaBottleItem;
import net.minecraft.world.item.ItemNameBlockItem;
import com.shioh.sengoku.sengokuFabric;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.HoneyBlock;


public class TansuItemReg {

    // --- Tansu Items ---
    public static final BlockItem OAK_TANSU_I = new BlockItem(TansuBlockReg.OAK_TANSU, new Item.Properties());
    public static final BlockItem SPRUCE_TANSU_I = new BlockItem(TansuBlockReg.SPRUCE_TANSU, new Item.Properties());
    public static final BlockItem BIRCH_TANSU_I = new BlockItem(TansuBlockReg.BIRCH_TANSU, new Item.Properties());
    public static final BlockItem JUNGLE_TANSU_I = new BlockItem(TansuBlockReg.JUNGLE_TANSU, new Item.Properties());
    public static final BlockItem ACACIA_TANSU_I = new BlockItem(TansuBlockReg.ACACIA_TANSU, new Item.Properties());
    public static final BlockItem DARK_OAK_TANSU_I = new BlockItem(TansuBlockReg.DARK_OAK_TANSU, new Item.Properties());
    public static final BlockItem MANGROVE_TANSU_I = new BlockItem(TansuBlockReg.MANGROVE_TANSU, new Item.Properties());
    public static final BlockItem CHERRY_TANSU_I = new BlockItem(TansuBlockReg.CHERRY_TANSU, new Item.Properties());
    public static final BlockItem BAMBOO_TANSU_I = new BlockItem(TansuBlockReg.BAMBOO_TANSU, new Item.Properties());
    public static final BlockItem CRIMSON_TANSU_I = new BlockItem(TansuBlockReg.CRIMSON_TANSU, new Item.Properties().fireResistant());
    public static final BlockItem WARPED_TANSU_I = new BlockItem(TansuBlockReg.WARPED_TANSU, new Item.Properties().fireResistant());

    // --- Sake Barrel Item ---
    public static final BlockItem SAKE_BARREL_I = new BlockItem(TansuBlockReg.SAKE_BARREL, new Item.Properties());

    // --- New simple block items ---
    public static final BlockItem FISHING_NET_I = new BlockItem(TansuBlockReg.FISHING_NET, new Item.Properties());
    public static final BlockItem BASKET_I = new BlockItem(TansuBlockReg.BASKET, new Item.Properties());
    public static final BlockItem BOILING_POT_I = new BlockItem(TansuBlockReg.BOILING_POT, new Item.Properties());

    // --- Crops ---
    public static final Item RICE_I = new RiceItem(TansuBlockReg.RICE_CROP, new Item.Properties());
    public static final Item RAMIE_I = new BlockItem(TansuBlockReg.RAMIE_CROP, new Item.Properties());
        // Ramie fiber item (restored placeholder)
        public static final Item RAMIE_FIBER = new Item(new Item.Properties());
                // --- Tea items (placeholders restored) ---
                        public static final Item TEA_LEAF = new Item(new Item.Properties()); // exact id: tea_leaf
                        public static final Item TEA_SEEDS = new ItemNameBlockItem(TansuBlockReg.TEA_CROP, new Item.Properties());
                        public static final Item TEA_BOTTLE = new TeaBottleItem(new Item.Properties());
                        // Dried tea bottle: inert item was removed (raw_matcha_bottle removed)

    public static void registerItems() {
        // --- Register Tansus ---
        registerItem(OAK_TANSU_I, Items.BARREL);
        registerItem(SPRUCE_TANSU_I, OAK_TANSU_I);
        registerItem(BIRCH_TANSU_I, SPRUCE_TANSU_I);
        registerItem(JUNGLE_TANSU_I, BIRCH_TANSU_I);
        registerItem(ACACIA_TANSU_I, JUNGLE_TANSU_I);
        registerItem(DARK_OAK_TANSU_I, ACACIA_TANSU_I);
        registerItem(MANGROVE_TANSU_I, DARK_OAK_TANSU_I);
        registerItem(CHERRY_TANSU_I, MANGROVE_TANSU_I);
        registerItem(BAMBOO_TANSU_I, CHERRY_TANSU_I);
        registerItem(CRIMSON_TANSU_I, BAMBOO_TANSU_I);
        registerItem(WARPED_TANSU_I, CRIMSON_TANSU_I);

        // --- Register Sake Barrel ---
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_barrel"), SAKE_BARREL_I);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> entries.addAfter(WARPED_TANSU_I, SAKE_BARREL_I));

        // --- Register simple blocks ---
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("fishing_net"), FISHING_NET_I);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("basket"), BASKET_I);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("boiling_pot"), BOILING_POT_I);
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> {
                    entries.addAfter(SAKE_BARREL_I, FISHING_NET_I);
                    entries.addAfter(FISHING_NET_I, BASKET_I);
                    entries.addAfter(BASKET_I, BOILING_POT_I);
                });

        // --- Register Crops ---
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("rice"), RICE_I);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("ramie"), RAMIE_I);
        // Register ramie fiber placeholder
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("ramie_fiber"), RAMIE_FIBER);
        // --- Register tea items ---
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("tea_leaf"), TEA_LEAF);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("tea_seeds"), TEA_SEEDS);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("tea_bottle"), TEA_BOTTLE);
        // Ensure tea leaf appears in Food & Drinks (as an ingredient)
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addAfter(RICE_I, TEA_LEAF));


        // --- Add crops to creative tabs ---
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addAfter(Items.POTATO, RICE_I));
        // Ensure tea seeds are present in Natural blocks before placing ramie after them
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS)
                .register(entries -> entries.addAfter(Items.BEETROOT_SEEDS, TEA_SEEDS));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS)
                .register(entries -> entries.addAfter(TEA_SEEDS, RAMIE_I));
        // Also place Ramie Fiber in the Ingredients tab next to bones
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.addAfter(Items.BONE, RAMIE_FIBER));
        // Place tea leaf after ramie fiber in ingredients
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
                .register(entries -> entries.addAfter(RAMIE_FIBER, TEA_LEAF));
        // Add tea items to relevant tabs
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addAfter(RICE_I, TEA_LEAF));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register(entries -> entries.addAfter(Items.HONEY_BOTTLE, TEA_BOTTLE));
        // (raw_matcha_bottle removed; no dried tea bottle entry)
    }

    private static void registerItem(BlockItem item, Item afterItem) {
        Registry.register(
                BuiltInRegistries.ITEM,
                sengokuFabric.asId(((Tansu) item.getBlock()).tansuWoodType + "_tansu_cabinet"),
                item
        );
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> entries.addAfter(afterItem, item));
    }
}
