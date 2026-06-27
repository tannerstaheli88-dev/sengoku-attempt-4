package com.shioh.sengoku.init;

import com.shioh.sengoku.item.RepairHintItem;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

/**
 * Registry for Tamahagane - refined iron from iron sand.
 * Tamahagane (玉鋼) is the traditional Japanese steel used for sword-making.
 */
public final class TamahaganeItemReg {
    
    private TamahaganeItemReg() {}
    
    // Raw and refined Tamahagane items
    public static final Item RAW_TAMAHAGANE = new Item(
        new Item.Properties()
            .rarity(Rarity.UNCOMMON)
    );

    public static final Item TAMAHAGANE = new RepairHintItem(
        new Item.Properties()
            .rarity(Rarity.UNCOMMON),
        "item.sengoku.tamahagane_ingot.tooltip"
    );
    
    // Smithing template item to allow tamahagane upgrades
    public static final Item TAMAHAGANE_UPGRADE_TEMPLATE = new RepairHintItem(
        new Item.Properties(),
        "item.sengoku.tamahagane_upgrade_smithing_template.applies_to",
        "item.sengoku.tamahagane_upgrade_smithing_template.ingredients",
        "upgrade.sengoku.tamahagane_upgrade"
    );
    
    public static void registerItems() {
    // Register raw and refined tamahagane items
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("raw_tamahagane"), RAW_TAMAHAGANE);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("tamahagane_ingot"), TAMAHAGANE);
    Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("tamahagane_upgrade_smithing_template"), TAMAHAGANE_UPGRADE_TEMPLATE);

        // Add to ingredients creative tab after iron ingot: raw then ingot
        net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS)
            .register(entries -> {
                // raw tamahagane after raw iron
                entries.addAfter(Items.RAW_IRON, RAW_TAMAHAGANE);
                // tamahagane ingot after iron ingot
                entries.addAfter(Items.IRON_INGOT, TAMAHAGANE);
                // put the smithing template near ingredients as well (after tamahagane ingot)
                entries.addAfter(TAMAHAGANE, TAMAHAGANE_UPGRADE_TEMPLATE);
            });

        sengokuFabric.LOGGER.info("Registered raw_tamahagane and tamahagane_ingot items");
    }
}
