package com.shioh.sengoku.init;

import com.shioh.sengoku.block.IronSandBlock;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Registry for iron sand block and related items.
 */
public final class IronSandReg {
    
    private IronSandReg() {}
    
    // Iron sand block
    public static final Block IRON_SAND = new IronSandBlock(
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_GRAY)
            .strength(3.0F)
            .sound(SoundType.SAND)
            .requiresCorrectToolForDrops()
            .noLootTable()
    );
    
    // Iron sand block item
    public static final Item IRON_SAND_ITEM = new BlockItem(IRON_SAND, new Item.Properties());
    
    public static void registerBlocks() {
        // Register the iron sand block
        Registry.register(BuiltInRegistries.BLOCK, sengokuFabric.asId("iron_sand"), IRON_SAND);
        
        sengokuFabric.LOGGER.info("Registered iron sand block");
    }
    
    public static void registerItems() {
        // Register the iron sand block item
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("iron_sand"), IRON_SAND_ITEM);
        
        // Add to natural blocks creative tab after gravel
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS)
            .register(entries -> {
                entries.addAfter(Items.GRAVEL, IRON_SAND_ITEM);
            });
        
        sengokuFabric.LOGGER.info("Registered iron sand item");
    }
}
