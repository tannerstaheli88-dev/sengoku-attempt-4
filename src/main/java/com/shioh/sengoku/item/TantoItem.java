package com.shioh.sengoku.item;

import com.shioh.sengoku.registry.TagRegistry;
import com.shioh.sengoku.util.AllowDenyPass;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class TantoItem extends SweeplessItem {
  public TantoItem(Tier tier, float attackDamage, float attackSpeed, double reach, Item.Properties properties) {
    super(tier, BlockTags.SWORD_EFFICIENT, attackDamage, attackSpeed, reach, properties);
  }

  @Override
  public AllowDenyPass bw$canEnchant(ItemStack itemstack, Holder<Enchantment> enchantment) {
    if (enchantment.is(Enchantments.SWIFT_SNEAK)) {
      return AllowDenyPass.ALLOW;
    }
    // The item can't be enchanted by enchantments listed here
    return enchantment.is(TagRegistry.MIGHT_ENCHANTABLE) || enchantment.is(TagRegistry.SWEEPING_EDGE_ENCHANTABLE) ? AllowDenyPass.DENY : AllowDenyPass.PASS;
  }
}
