package com.shioh.sengoku.init;

// sengokuFabric not needed here; use vanilla ResourceLocation.withDefaultNamespace
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
 * Registers the custom Daimyo ArmorMaterial so worn armor will use
 * textures in `textures/models/armor/daimyo_layer_*.png`.
 */
public final class DaimyoArmorMaterialReg {
    private DaimyoArmorMaterialReg() {}

    public static Holder<ArmorMaterial> DAIMYO;

    public static void register() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.HELMET, 4);
        defense.put(ArmorItem.Type.CHESTPLATE, 8);
        defense.put(ArmorItem.Type.LEGGINGS, 6);
        defense.put(ArmorItem.Type.BOOTS, 3);
        defense.put(ArmorItem.Type.BODY, 11);

        DAIMYO = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("daimyo"),
            new ArmorMaterial(
                defense,
                15,
                SoundEvents.ARMOR_EQUIP_DIAMOND,
                () -> Ingredient.of(Items.DIAMOND),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("daimyo"))),
                3.0F,
                0.0F
            )
        );
    }
}
