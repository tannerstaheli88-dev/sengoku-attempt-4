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
 * Registers the Netherite Daimyo ArmorMaterial so worn armor will use
 * textures in `textures/models/armor/daimyo_netherite_layer_*.png`.
 */
public final class DaimyoNetheriteArmorMaterialReg {
    private DaimyoNetheriteArmorMaterialReg() {}

    public static Holder<ArmorMaterial> DAIMYO_NETHERITE;

    public static void register() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.HELMET, 4);
        defense.put(ArmorItem.Type.CHESTPLATE, 9);
        defense.put(ArmorItem.Type.LEGGINGS, 7);
        defense.put(ArmorItem.Type.BOOTS, 4);
        defense.put(ArmorItem.Type.BODY, 12);

        DAIMYO_NETHERITE = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("daimyo_netherite"),
            new ArmorMaterial(
                defense,
                15,
                SoundEvents.ARMOR_EQUIP_NETHERITE,
                () -> Ingredient.of(Items.NETHERITE_INGOT),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("daimyo_netherite"))),
                4.0F,
                0.1F
            )
        );
    }
}
