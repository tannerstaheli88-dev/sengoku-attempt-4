package com.shioh.sengoku.item;

import com.shioh.sengoku.config.PostureValues;
import com.shioh.sengoku.registry.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Unbreakable;

public class KenseiAxeItem extends AxeItem {

    public KenseiAxeItem(Tier tier, Item.Properties properties) {
        super(tier, properties);
    }

    public static Item createDiamond() {
        return new KenseiAxeItem(
            Tiers.DIAMOND,
            new Item.Properties()
                .attributes(AxeItem.createAttributes(Tiers.DIAMOND, 7.0f, -2.9f))
                .rarity(Rarity.RARE)
                .component(ModDataComponents.WEAPON_POSTURE_DAMAGE, PostureValues.KENSEI_AXE_DIAMOND)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
        );
    }

    public static Item createNetherite() {
        return new KenseiAxeItem(
            Tiers.NETHERITE,
            new Item.Properties()
                .attributes(AxeItem.createAttributes(Tiers.NETHERITE, 8.0f, -2.8f))
                .rarity(Rarity.EPIC)
                .fireResistant()
                .component(ModDataComponents.WEAPON_POSTURE_DAMAGE, PostureValues.KENSEI_AXE_NETHERITE)
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
        );
    }
}