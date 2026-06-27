package com.shioh.sengoku.item;

import com.shioh.sengoku.config.PostureValues;
import com.shioh.sengoku.registry.ModDataComponents;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.Item.Properties;

/**
 * The Blade of the Kensei - a powerful sword wielded by Kensei enemies.
 * Has enhanced damage, attack speed, movement speed, and entity interaction range.
 */
public class KenseiSwordItem extends BasicWeaponItem {

    public KenseiSwordItem(Tier tier, float attackDamage, float attackSpeed, double reach, Item.Properties properties) {
        super(tier, BlockTags.SWORD_EFFICIENT, attackDamage, attackSpeed, reach, properties);
    }

    /**
     * Creates the diamond-tier Blade of the Kensei.
     */
    public static Item createDiamondSword() {
        return new KenseiSwordItem(
            Tiers.DIAMOND,
            6.0f,
            -2.2f,
            1.0,
            new Properties()
                .rarity(Rarity.RARE)
                .component(ModDataComponents.WEAPON_POSTURE_DAMAGE, PostureValues.KENSEI_SWORD_DIAMOND)
                .component(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }

    /**
     * Creates the netherite-tier Blade of the Kensei with improved attributes.
     */
    public static Item createNetheriteSword() {
        return new KenseiSwordItem(
            Tiers.NETHERITE,
            6.0f,
            -2.1f,
            1.5,
            new Properties()
                .rarity(Rarity.EPIC)
                .fireResistant()
                .component(ModDataComponents.WEAPON_POSTURE_DAMAGE, PostureValues.KENSEI_SWORD_NETHERITE)
                .component(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(true))
        );
    }
}
