package com.shioh.sengoku.item;

import com.shioh.sengoku.config.PostureValues;
import com.shioh.sengoku.registry.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * Tetsubo of the Hatamoto - a legendary tetsubo with enhanced stats.
 */
public class KenseiTetsuboItem extends TetsuboItem {

    public KenseiTetsuboItem(Tier tier, float attackDamage, float attackSpeed, double reach, Item.Properties properties) {
        super(tier, attackDamage, attackSpeed, reach, properties);
    }

    public static Item createDiamond() {
        return new KenseiTetsuboItem(
            Tiers.DIAMOND,
            6.0f,
            -2.5f,
            0.5,
            new Properties()
                .rarity(Rarity.RARE)
                .component(ModDataComponents.WEAPON_POSTURE_DAMAGE, PostureValues.KENSEI_TETSUBO_DIAMOND)
                .component(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }

    public static Item createNetherite() {
        return new KenseiTetsuboItem(
            Tiers.NETHERITE,
            6.0f,
            -2.4f,
            1.0,
            new Properties()
                .rarity(Rarity.EPIC)
                .fireResistant()
                .component(ModDataComponents.WEAPON_POSTURE_DAMAGE, PostureValues.KENSEI_TETSUBO_NETHERITE)
                .component(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
}
