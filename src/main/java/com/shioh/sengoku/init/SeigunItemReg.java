package com.shioh.sengoku.init;

import com.shioh.sengoku.item.RepairHintItem;
import com.shioh.sengoku.sengokuFabric;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

/**
 * Registry for Seigun crafting ingredient items.
 * Seigun (精軍) refers to elite crafting materials used in high-tier smithing.
 */
public final class SeigunItemReg {

    private SeigunItemReg() {}

    public static final Item SEIGUN_UPGRADE_SMITHING_TEMPLATE = new RepairHintItem(
        new Item.Properties().rarity(Rarity.EPIC),
        "item.sengoku.seigun_upgrade_smithing_template.applies_to",
        "item.sengoku.seigun_upgrade_smithing_template.ingredients",
        "upgrade.sengoku.seigun_upgrade"
    );

    public static final Item RYOSHIN_UPGRADE_SMITHING_TEMPLATE = new RepairHintItem(
        new Item.Properties().rarity(Rarity.EPIC),
        "item.sengoku.ryoshin_upgrade_smithing_template.applies_to",
        "item.sengoku.ryoshin_upgrade_smithing_template.ingredients",
        "upgrade.sengoku.ryoshin_upgrade"
    );

    public static final Item BROKEN_ARMOR_PIECE = new RepairHintItem(
        new Item.Properties().rarity(Rarity.RARE),
        "item.sengoku.broken_armor_piece.tooltip"
    );

    public static final Item BROKEN_HILT_PIECE = new RepairHintItem(
        new Item.Properties().rarity(Rarity.RARE),
        "item.sengoku.broken_hilt_piece.tooltip"
    );

    public static void registerItems() {
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("seigun_upgrade_smithing_template"), SEIGUN_UPGRADE_SMITHING_TEMPLATE);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("ryoshin_upgrade_smithing_template"), RYOSHIN_UPGRADE_SMITHING_TEMPLATE);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("broken_armor_piece"), BROKEN_ARMOR_PIECE);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("broken_hilt_piece"), BROKEN_HILT_PIECE);

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.INGREDIENTS).register(entries -> {
            entries.addAfter(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, SEIGUN_UPGRADE_SMITHING_TEMPLATE);
            entries.addAfter(SEIGUN_UPGRADE_SMITHING_TEMPLATE, RYOSHIN_UPGRADE_SMITHING_TEMPLATE);
            entries.addAfter(RYOSHIN_UPGRADE_SMITHING_TEMPLATE, BROKEN_ARMOR_PIECE);
            entries.addAfter(BROKEN_ARMOR_PIECE, BROKEN_HILT_PIECE);
        });

        sengokuFabric.LOGGER.info("Registered Seigun ingredient items");
    }
}
