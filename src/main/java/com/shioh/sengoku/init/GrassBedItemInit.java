package com.shioh.sengoku.init;

import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.block.GrassBedBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.Registry;

import java.util.ArrayList;
import java.util.List;

public class GrassBedItemInit {

    public static final Item.Properties bedProperties = new Item.Properties().stacksTo(1);
    public static final Item.Properties boundBambooBedProperties = new Item.Properties().stacksTo(16);

    public static final BlockItem GRASS_BED_I = new BlockItem(GrassBedBlockReg.GRASS_BED, boundBambooBedProperties);

    public static final List<Item> more_bed_items = new ArrayList<>();

    public static void registerBedItems() {
        registerBedItem(GRASS_BED_I, Items.WHITE_BED);
        more_bed_items.add(Items.WHITE_BED);
    }

    private static void registerBedItem(BlockItem bedItem, Item bedAfter) {
        GrassBedBlock block = (GrassBedBlock) bedItem.getBlock();
        String id = block.bedWoodType + "_bed";

        // Register the item
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId(id), bedItem);

        // Add to creative tab
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> {
                    // addAfter doesn’t exist? fallback: add items in order manually
                    entries.accept(bedItem);
                });

        more_bed_items.add(bedItem);
    }

    private static void registerOtherBedItem(BlockItem otherBed) {
        GrassBedBlock block = (GrassBedBlock) otherBed.getBlock();
        String id = block.bedWoodType + "_bed";

        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId(id), otherBed);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COLORED_BLOCKS)
                .register(entries -> entries.accept(otherBed));

        more_bed_items.add(otherBed);
    }
}
