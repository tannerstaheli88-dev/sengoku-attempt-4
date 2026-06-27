package com.shioh.sengoku.item;

import com.shioh.sengoku.mixinutil.PlayerEntityAccessor;
import com.shioh.sengoku.registry.TagRegistry;
import com.shioh.sengoku.util.AllowDenyPass;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;

public class OdachiItem extends BasicWeaponItem {
  public OdachiItem(Tier tier, float attackDamage, float attackSpeed, double reach, Item.Properties properties) {
    super(tier, BlockTags.SWORD_EFFICIENT, attackDamage, attackSpeed, reach, properties);
  }

  @Override
  public AllowDenyPass bw$canEnchant(ItemStack itemstack, Holder<Enchantment> enchantment) {
    // The item can't be enchanted by enchantments listed here
    return enchantment.is(TagRegistry.MIGHT_ENCHANTABLE) ? AllowDenyPass.DENY : AllowDenyPass.PASS;
  }

  @Override
  public boolean hurtEnemy(ItemStack stack, net.minecraft.world.entity.LivingEntity target, net.minecraft.world.entity.LivingEntity attacker) {
    if (attacker instanceof Player) {
      Player player = (Player) attacker;
      float f2 = ((PlayerEntityAccessor) player).bw$getCooldown(0.5f);
      if (f2 >= 0.9F) {
        // Apply a short horizontal knockback away from the attacker and a small upward nudge
        double dx = target.getX() - attacker.getX();
        double dz = target.getZ() - attacker.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 0.0001) {
          double nx = dx / dist;
          double nz = dz / dist;
          double strength = 0.6; // tunable knockback strength
          target.push(nx * strength, 0.12, nz * strength);
        } else {
          // Fallback: use attacker's look vector
          Vec3 look = attacker.getLookAngle();
          target.push(look.x * 0.6, 0.12, look.z * 0.6);
        }
        target.hurtMarked = true;
      }
    }

    stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    return true;
  }
}