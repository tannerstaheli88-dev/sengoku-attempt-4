package com.shioh.sengoku.item;

import com.shioh.sengoku.mixinutil.PlayerEntityAccessor;
import com.shioh.sengoku.registry.TagRegistry;
import com.shioh.sengoku.util.AllowDenyPass;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

public class TetsuboItem extends SweeplessItem {
  public TetsuboItem(Tier tier, float attackDamage, float attackSpeed, double reach, Item.Properties properties) {
    super(tier, BlockTags.AIR, attackDamage, attackSpeed, reach, properties);
  }

  @Override
  public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
    Player player = (Player) attacker;
    float f2 = ((PlayerEntityAccessor) player).bw$getCooldown(0.5f);
    if (f2 >= 0.9F) {
      // Remove strong vertical knockup; replace with slight horizontal knockback
      // Compute horizontal direction from attacker to target
      double dx = target.getX() - attacker.getX();
      double dz = target.getZ() - attacker.getZ();
      double len = Math.sqrt(dx * dx + dz * dz);
      double strength = 0.25; // slight knockback strength, tunable
      if (len > 0.0001) {
        double nx = dx / len;
        double nz = dz / len;
        // small upward nudge so target visibly moves but no large knockup
        target.push(nx * strength, 0.08, nz * strength);
      } else {
        // fallback: push in attacker's look direction
        Vec3 look = attacker.getLookAngle();
        target.push(look.x * strength, 0.08, look.z * strength);
      }
      target.hurtMarked = true;
    }
    stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    return true;
  }

  @Override
  public AllowDenyPass bw$canEnchant(ItemStack itemstack, Holder<Enchantment> enchantment) {
    // The item can't be enchanted by enchantments listed here
    return enchantment.is(TagRegistry.SHARPNESS_ENCHANTABLE) || enchantment.is(TagRegistry.SWEEPING_EDGE_ENCHANTABLE) ? AllowDenyPass.DENY : AllowDenyPass.PASS;
  }
}