package com.shioh.sengoku.init;

import com.shioh.sengoku.block.ShikiRakedGravelBlock;
import com.shioh.sengoku.block.ShikiRakedSandBlock;
import com.shioh.sengoku.block.ShirakawaSunaBlock;
import com.shioh.sengoku.item.ZenGardenRakeItem;
import com.shioh.sengoku.sengokuFabric;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ZenGardenReg {

    private ZenGardenReg() {}

    public static final Block SHIKI_SAND = new ShikiRakedSandBlock(
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.SAND)
            .strength(0.5F)
            .sound(SoundType.SAND)
            .noLootTable()
    );

    public static final Block SHIKI_GRAVEL = new ShikiRakedGravelBlock(
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .strength(0.6F)
            .sound(SoundType.GRAVEL)
            .noLootTable()
    );

    public static final Block SHIRAKAWA_SUNA = new ShirakawaSunaBlock(
        BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_CONCRETE_POWDER)
            .noLootTable()
    );

    public static final Item SHIKI_SAND_ITEM = new BlockItem(SHIKI_SAND, new Item.Properties());
    public static final Item SHIKI_GRAVEL_ITEM = new BlockItem(SHIKI_GRAVEL, new Item.Properties());
    public static final Item SHIRAKAWA_SUNA_ITEM = new BlockItem(SHIRAKAWA_SUNA, new Item.Properties());
    public static final Item ZEN_GARDEN_RAKE = new ZenGardenRakeItem(new Item.Properties().durability(512));

    public static void registerBlocks() {
        Registry.register(BuiltInRegistries.BLOCK, sengokuFabric.asId("shiki_sand"), SHIKI_SAND);
        Registry.register(BuiltInRegistries.BLOCK, sengokuFabric.asId("shiki_gravel"), SHIKI_GRAVEL);
        Registry.register(BuiltInRegistries.BLOCK, sengokuFabric.asId("shirakawa_suna"), SHIRAKAWA_SUNA);
        sengokuFabric.LOGGER.info("Registered zen garden shiki blocks");
    }

    public static void registerItems() {
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("shiki_sand"), SHIKI_SAND_ITEM);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("shiki_gravel"), SHIKI_GRAVEL_ITEM);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("shirakawa_suna"), SHIRAKAWA_SUNA_ITEM);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("zen_garden_rake"), ZEN_GARDEN_RAKE);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.addAfter(Items.SUSPICIOUS_SAND, SHIKI_SAND_ITEM);
            entries.addAfter(Items.SUSPICIOUS_GRAVEL, SHIKI_GRAVEL_ITEM);
            entries.addAfter(SHIKI_SAND_ITEM, SHIRAKAWA_SUNA_ITEM);
        });

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.addAfter(Items.BRUSH, ZEN_GARDEN_RAKE);
        });

        sengokuFabric.LOGGER.info("Registered zen garden items");
    }
}
