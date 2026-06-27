package com.shioh.sengoku.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import static com.shioh.sengoku.Constants.ID;

//sharpness but for brute weapons.
public class EnchantmentRegistry {

  public static final ResourceKey<Enchantment> MIGHT = of("might");

  private static ResourceKey<Enchantment> of(String name) {
    return ResourceKey.create(Registries.ENCHANTMENT, ID(name));
  }

  public static void init() {
  }
}
