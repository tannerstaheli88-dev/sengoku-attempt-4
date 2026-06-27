package com.shioh.sengoku.init;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

/**
 * Registers the custom Shinobi ArmorMaterial so worn armor will use
 * textures in `textures/models/armor/shinobi_layer_*.png`.
 */
public final class ShinobiArmorMaterialReg {
    private ShinobiArmorMaterialReg() {}

    public static Holder<ArmorMaterial> SHINOBI;

    public static void register() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.HELMET, 2);
        defense.put(ArmorItem.Type.CHESTPLATE, 5);
        defense.put(ArmorItem.Type.LEGGINGS, 4);
        defense.put(ArmorItem.Type.BOOTS, 1);
        defense.put(ArmorItem.Type.BODY, 5);

        SHINOBI = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("shinobi"),
            new ArmorMaterial(
                defense,
                12,
                SoundEvents.ARMOR_EQUIP_CHAIN,
                () -> Ingredient.of(ShinobiItemReg.TATTERED_SHINOBI_CLOTH),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("shinobi"))),
                0.0F,
                0.0F
            )
        );
    }
}
