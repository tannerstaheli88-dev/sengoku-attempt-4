package com.shioh.sengoku.init;

import com.shioh.sengoku.item.RepairHintItem;
import com.shioh.sengoku.item.ShinobiArmorItem;
import com.shioh.sengoku.sengokuFabric;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Registers Shinobi armor items (Hood, Shoulder Plates, Sash, Thigh Guards).
 */
public final class ShinobiItemReg {
    private ShinobiItemReg() {}

    // Register static item instances
    public static final Item TATTERED_SHINOBI_CLOTH = new RepairHintItem(
        new Item.Properties(),
        "item.sengoku.tattered_shinobi_cloth.tooltip"
    );
    public static final Item SHINOBI_HOOD = ShinobiArmorItem.createHelmet();
    public static final Item SHINOBI_SHOULDER_PLATES = ShinobiArmorItem.createChestplate();
    public static final Item SHINOBI_SASH = ShinobiArmorItem.createLeggings();
    public static final Item SHINOBI_THIGH_GUARDS = ShinobiArmorItem.createBoots();

    public static void registerItems() {
        // Register items with the registry
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("tattered_shinobi_cloth"), TATTERED_SHINOBI_CLOTH);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("shinobi_hood"), SHINOBI_HOOD);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("shinobi_shoulder_plates"), SHINOBI_SHOULDER_PLATES);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("shinobi_sash"), SHINOBI_SASH);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("shinobi_thigh_guards"), SHINOBI_THIGH_GUARDS);

        // Add cloth to Ingredients tab
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.addAfter(Items.LEATHER, TATTERED_SHINOBI_CLOTH);
        });

        // Add to Combat creative tab
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.addAfter(Items.CHAINMAIL_BOOTS, SHINOBI_HOOD);
            entries.addAfter(SHINOBI_HOOD, SHINOBI_SHOULDER_PLATES);
            entries.addAfter(SHINOBI_SHOULDER_PLATES, SHINOBI_SASH);
            entries.addAfter(SHINOBI_SASH, SHINOBI_THIGH_GUARDS);
        });
    }
}
