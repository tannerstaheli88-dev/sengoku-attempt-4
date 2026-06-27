package com.shioh.sengoku.item;

import com.shioh.sengoku.config.PostureValues;
import com.shioh.sengoku.registry.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * Odachi of the Shugodai - a legendary odachi with enhanced stats.
 */
public class KenseiOdachiItem extends OdachiItem {

    public KenseiOdachiItem(Tier tier, float attackDamage, float attackSpeed, double reach, Item.Properties properties) {
        super(tier, attackDamage, attackSpeed, reach, properties);
    }

    public static Item createDiamond() {
        return new KenseiOdachiItem(
            Tiers.DIAMOND,
            7.0f,
            -2.9f,
            1.75,
            new Properties()
                .rarity(Rarity.RARE)
                .component(ModDataComponents.WEAPON_POSTURE_DAMAGE, PostureValues.KENSEI_ODACHI_DIAMOND)
                .component(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }

    public static Item createNetherite() {
        return new KenseiOdachiItem(
            Tiers.NETHERITE,
            7.0f,
            -2.8f,
            2.0,
            new Properties()
                .rarity(Rarity.EPIC)
                .fireResistant()
                .component(ModDataComponents.WEAPON_POSTURE_DAMAGE, PostureValues.KENSEI_ODACHI_NETHERITE)
                .component(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
}
