package com.shioh.sengoku.item;

import com.shioh.sengoku.config.PostureValues;
import com.shioh.sengoku.registry.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * Naginata of the Nobushi - a legendary naginata with enhanced stats.
 */
public class KenseiNaginataItem extends NaginataItem {

    public KenseiNaginataItem(Tier tier, float attackDamage, float attackSpeed, double reach, Item.Properties properties) {
        super(tier, attackDamage, attackSpeed, reach, properties);
    }

    public static Item createDiamond() {
        return new KenseiNaginataItem(
            Tiers.DIAMOND,
            5.0f,
            -2.5f,
            2.5,
            new Properties()
                .rarity(Rarity.RARE)
                .component(ModDataComponents.WEAPON_POSTURE_DAMAGE, PostureValues.KENSEI_NAGINATA_DIAMOND)
                .component(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }

    public static Item createNetherite() {
        return new KenseiNaginataItem(
            Tiers.NETHERITE,
            5.0f,
            -2.4f,
            3.0,
            new Properties()
                .rarity(Rarity.EPIC)
                .fireResistant()
                .component(ModDataComponents.WEAPON_POSTURE_DAMAGE, PostureValues.KENSEI_NAGINATA_NETHERITE)
                .component(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
}
