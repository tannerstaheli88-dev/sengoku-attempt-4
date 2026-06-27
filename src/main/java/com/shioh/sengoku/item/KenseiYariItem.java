package com.shioh.sengoku.item;

import com.shioh.sengoku.config.PostureValues;
import com.shioh.sengoku.registry.ModDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * Yari of the Taisho - a legendary yari with enhanced stats.
 */
public class KenseiYariItem extends YariItem {

    public KenseiYariItem(Tier tier, float attackDamage, float attackSpeed, double reach, Item.Properties properties) {
        super(tier, attackDamage, attackSpeed, reach, properties);
    }

    public static Item createDiamond() {
        return new KenseiYariItem(
            Tiers.DIAMOND,
            4.0f,
            -2.0f,
            1.75,
            new Properties()
                .rarity(Rarity.RARE)
                .component(ModDataComponents.WEAPON_POSTURE_DAMAGE, PostureValues.KENSEI_YARI_DIAMOND)
                .component(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }

    public static Item createNetherite() {
        return new KenseiYariItem(
            Tiers.NETHERITE,
            4.0f,
            -1.9f,
            2.0,
            new Properties()
                .rarity(Rarity.EPIC)
                .fireResistant()
                .component(ModDataComponents.WEAPON_POSTURE_DAMAGE, PostureValues.KENSEI_YARI_NETHERITE)
                .component(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
}
