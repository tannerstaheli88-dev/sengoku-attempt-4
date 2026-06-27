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
 * Registers an ArmorMaterial for hats so they can have their own armor texture layer.
 */
public final class HatArmorMaterialReg {
    private HatArmorMaterialReg() {}

    public static Holder<ArmorMaterial> STRAW;
    public static Holder<ArmorMaterial> STRAW_BLACK;
    public static Holder<ArmorMaterial> STRAW_WHITE;
    public static Holder<ArmorMaterial> SANDO;
    public static Holder<ArmorMaterial> SANDO_BLACK;
    public static Holder<ArmorMaterial> SANDO_WHITE;
    public static Holder<ArmorMaterial> TENGAI;
    public static Holder<ArmorMaterial> STRAW_CAPE;
    public static Holder<ArmorMaterial> STRAW_CAPE_BLACK;
    public static Holder<ArmorMaterial> STRAW_CAPE_WHITE;

    public static void register() {
        EnumMap<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.HELMET, 1);
        defense.put(ArmorItem.Type.CHESTPLATE, 0);
        defense.put(ArmorItem.Type.LEGGINGS, 0);
        defense.put(ArmorItem.Type.BOOTS, 0);
        defense.put(ArmorItem.Type.BODY, 0);

        // Straw hat material -> textures/models/armor/straw_layer_1.png
        STRAW = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("straw"),
            new ArmorMaterial(
                defense,
                5,
                SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(Items.LEATHER),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("straw"))),
                0.0F,
                0.0F
            )
        );

        // Sando hat material -> textures/models/armor/sando_layer_1.png
        SANDO = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("sando"),
            new ArmorMaterial(
                defense,
                5,
                SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(Items.LEATHER),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("sando"))),
                0.0F,
                0.0F
            )
        );

        // Black sando hat material -> textures/models/armor/sando_black_layer_1.png
        SANDO_BLACK = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("sando_black"),
            new ArmorMaterial(
                defense,
                5,
                SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(Items.LEATHER),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("sando_black"))),
                0.0F,
                0.0F
            )
        );

        // White sando hat material -> textures/models/armor/sando_white_layer_1.png
        SANDO_WHITE = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("sando_white"),
            new ArmorMaterial(
                defense,
                5,
                SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(Items.LEATHER),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("sando_white"))),
                0.0F,
                0.0F
            )
        );

        // Tengai hat material -> textures/models/armor/tengai_layer_1.png
        TENGAI = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("tengai"),
            new ArmorMaterial(
                defense,
                5,
                SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(Items.LEATHER),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("tengai"))),
                0.0F,
                0.0F
            )
        );

        // Straw cape material -> textures/models/armor/straw_cape_layer_1.png
        EnumMap<ArmorItem.Type, Integer> cape_defense = new EnumMap<>(ArmorItem.Type.class);
        cape_defense.put(ArmorItem.Type.HELMET, 0);
        cape_defense.put(ArmorItem.Type.CHESTPLATE, 1);
        cape_defense.put(ArmorItem.Type.LEGGINGS, 0);
        cape_defense.put(ArmorItem.Type.BOOTS, 0);
        cape_defense.put(ArmorItem.Type.BODY, 0);
        
        STRAW_CAPE = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("straw_cape"),
            new ArmorMaterial(
                cape_defense,
                5,
                SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(Items.LEATHER),
                // Reuse the straw hat texture for the cape layer
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("straw"))),
                0.0F,
                0.0F
            )
        );

        STRAW_BLACK = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("straw_black"),
            new ArmorMaterial(
                defense,
                5,
                SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(Items.LEATHER),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("straw_black"))),
                0.0F,
                0.0F
            )
        );

        STRAW_WHITE = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("straw_white"),
            new ArmorMaterial(
                defense,
                5,
                SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(Items.LEATHER),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("straw_white"))),
                0.0F,
                0.0F
            )
        );

        STRAW_CAPE_BLACK = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("straw_cape_black"),
            new ArmorMaterial(
                cape_defense,
                5,
                SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(Items.LEATHER),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("straw_black"))),
                0.0F,
                0.0F
            )
        );

        STRAW_CAPE_WHITE = Registry.registerForHolder(
            BuiltInRegistries.ARMOR_MATERIAL,
            ResourceLocation.withDefaultNamespace("straw_cape_white"),
            new ArmorMaterial(
                cape_defense,
                5,
                SoundEvents.ARMOR_EQUIP_LEATHER,
                () -> Ingredient.of(Items.LEATHER),
                List.of(new ArmorMaterial.Layer(ResourceLocation.withDefaultNamespace("straw_white"))),
                0.0F,
                0.0F
            )
        );
    }
}
