package com.shioh.sengoku.init;

import com.shioh.sengoku.item.HatArmorItem;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

/**
 * Registers straw, sando, and tengai hat items.
 */
public final class HatItemReg {
    private HatItemReg() {}

    public static Item STRAW_HAT;
    public static Item BLACK_STRAW_HAT;
    public static Item WHITE_STRAW_HAT;
    public static Item SANDO_HAT;
    public static Item BLACK_SANDO_HAT;
    public static Item WHITE_SANDO_HAT;
    public static Item TENGAI_HAT;
    public static Item STRAW_CAPE;
    public static Item BLACK_STRAW_CAPE;
    public static Item WHITE_STRAW_CAPE;

    public static void registerItems() {
        // Construct items here to avoid static-init ordering issues with registries
        STRAW_HAT = HatArmorItem.createStrawHat();
        BLACK_STRAW_HAT = HatArmorItem.createBlackStrawHat();
        WHITE_STRAW_HAT = HatArmorItem.createWhiteStrawHat();
        SANDO_HAT = HatArmorItem.createSandoHat();
        BLACK_SANDO_HAT = HatArmorItem.createBlackSandoHat();
        WHITE_SANDO_HAT = HatArmorItem.createWhiteSandoHat();
        TENGAI_HAT = HatArmorItem.createTengaiHat();
        STRAW_CAPE = HatArmorItem.createStrawCape();
        BLACK_STRAW_CAPE = HatArmorItem.createBlackStrawCape();
        WHITE_STRAW_CAPE = HatArmorItem.createWhiteStrawCape();

        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("straw_hat"), STRAW_HAT);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("black_straw_hat"), BLACK_STRAW_HAT);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("white_straw_hat"), WHITE_STRAW_HAT);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sando_hat"), SANDO_HAT);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("black_sando_hat"), BLACK_SANDO_HAT);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("white_sando_hat"), WHITE_SANDO_HAT);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("tengai_hat"), TENGAI_HAT);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("straw_cape"), STRAW_CAPE);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("black_straw_cape"), BLACK_STRAW_CAPE);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("white_straw_cape"), WHITE_STRAW_CAPE);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            // Place hats next to the turtle helmet so they appear with other helmets
            entries.addAfter(Items.TURTLE_HELMET, STRAW_HAT);
            entries.addAfter(STRAW_HAT, BLACK_STRAW_HAT);
            entries.addAfter(BLACK_STRAW_HAT, WHITE_STRAW_HAT);
            entries.addAfter(WHITE_STRAW_HAT, SANDO_HAT);
            entries.addAfter(SANDO_HAT, BLACK_SANDO_HAT);
            entries.addAfter(BLACK_SANDO_HAT, WHITE_SANDO_HAT);
            entries.addAfter(WHITE_SANDO_HAT, TENGAI_HAT);
            // Place cape after hats
            entries.addAfter(TENGAI_HAT, STRAW_CAPE);
            entries.addAfter(STRAW_CAPE, BLACK_STRAW_CAPE);
            entries.addAfter(BLACK_STRAW_CAPE, WHITE_STRAW_CAPE);
        });
    }
}
