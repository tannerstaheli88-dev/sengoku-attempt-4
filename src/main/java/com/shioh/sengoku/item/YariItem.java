package com.shioh.sengoku.item;

import com.shioh.sengoku.util.AllowDenyPass;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;

public class YariItem extends SweeplessItem {

    public YariItem(
            Tier tier,
            float attackDamage,
            float attackSpeed,
            double reach,
            Item.Properties properties
    ) {
        super(tier, BlockTags.SWORD_EFFICIENT, attackDamage, attackSpeed, reach, properties);
    }

    @Override
    public AllowDenyPass bw$canEnchant(ItemStack itemstack, Holder<Enchantment> enchantment) {
        return AllowDenyPass.PASS;
    }
}
