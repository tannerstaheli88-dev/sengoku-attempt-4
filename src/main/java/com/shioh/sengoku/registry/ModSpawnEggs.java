package com.shioh.sengoku.registry;

import com.shioh.sengoku.sengokuFabric;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.entity.EntityType;

/**
 * Registry for spawn eggs.
 */
public class ModSpawnEggs {
    
    //idk why i even bothered with the spots 
    // Bandit spawn egg - dark brown with red spots
    public static final Item BANDIT_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "bandit_spawn_egg"),
        new SpawnEggItem(ModEntities.BANDIT, 0x5C4033, 0x8B0000, new Item.Properties())
    );
    
    // Ronin spawn egg - gray with blue spots
    public static final Item RONIN_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ronin_spawn_egg"),
        new SpawnEggItem(ModEntities.RONIN, 0x696969, 0x4169E1, new Item.Properties())
    );

    // Warlord spawn egg - dark steel with crimson highlights
    public static final Item WARLORD_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "warlord_spawn_egg"),
        new SpawnEggItem(ModEntities.WARLORD, 0x2C2F33, 0x8B0000, new Item.Properties())
    );
    
    // Shinobi (Illusioner) spawn egg - dark blue with purple spots
    public static final Item SHINOBI_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "shinobi_spawn_egg"),
        new SpawnEggItem(EntityType.ILLUSIONER, 0x1A1A2E, 0x9D4EDD, new Item.Properties())
    );

    // Shinobi Lord spawn egg - dark indigo with violet highlights
    public static final Item SHINOBI_LORD_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "shinobi_lord_spawn_egg"),
        new SpawnEggItem(ModEntities.SHINOBI_LORD, 0x111827, 0x7C3AED, new Item.Properties())
    );

    // Yuki Onna spawn egg - pale blue with icy highlights
    public static final Item YUKI_ONNA_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "yuki_onna_spawn_egg"),
        new SpawnEggItem(ModEntities.YUKI_ONNA, 0xE0F7FF, 0x86B9FF, new Item.Properties())
    );

    // Red-Crowned Crane spawn egg - white with red crown
    public static final Item RED_CROWNED_CRANE_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "red_crowned_crane_spawn_egg"),
        new SpawnEggItem(ModEntities.RED_CROWNED_CRANE, 0xFFFFFF, 0xE03B3B, new Item.Properties())
    );

    // Onikuma spawn egg - bear-like brute: dark brown with orange highlights
    public static final Item ONIKUMA_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "onikuma_spawn_egg"),
        new SpawnEggItem(ModEntities.ONIKUMA, 0x6B2E1B, 0xD2691E, new Item.Properties())
    );

    // Oni Brute spawn egg - giant ogre-like zombie: dark crimson with gray highlights
    public static final Item ONI_BRUTE_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "oni_brute_spawn_egg"),
        new SpawnEggItem(ModEntities.ONI_BRUTE, 0x5A1A1A, 0x9EA3A8, new Item.Properties())
    );

    // Gaki spawn egg - desiccated husk variant: sand/brown
    public static final Item GAKI_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "gaki_spawn_egg"),
        new SpawnEggItem(ModEntities.GAKI, 0xC2A36D, 0x7A5A2A, new Item.Properties())
    );

    // Kojin spawn egg - aquatic demon variant: blue-green/dark blue
    public static final Item KOJIN_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kojin_spawn_egg"),
        new SpawnEggItem(ModEntities.KOJIN, 0x4A8A9E, 0x1F3D52, new Item.Properties())
    );

    // Ningyo spawn egg - same palette as Kojin
    public static final Item NINGYO_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ningyo_spawn_egg"),
        new SpawnEggItem(ModEntities.NINGYO, 0x4A8A9E, 0x1F3D52, new Item.Properties())
    );

    // Kamiike Hime spawn egg - guardian variant: cyan/dark teal
    public static final Item KAMIIKE_HIME_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kamiike_hime_spawn_egg"),
        new SpawnEggItem(ModEntities.KAMIIKE_HIME, 0x5AB4A4, 0x2C5F5A, new Item.Properties())
    );

    // Akugyo spawn egg - elder guardian variant: purple/dark magenta
    public static final Item AKUGYO_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "akugyo_spawn_egg"),
        new SpawnEggItem(ModEntities.AKUGYO, 0x9B7FB0, 0x5C3E6B, new Item.Properties())
    );

        public static final Item UMI_INU_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "umi_inu_spawn_egg"),
        new SpawnEggItem(ModEntities.UMI_INU, 0x9B7FB0, 0x5C3E6B, new Item.Properties())
    );

    // Sarugami spawn egg - monkey yokai: tan/brown
    public static final Item SARUGAMI_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "sarugami_spawn_egg"),
        new SpawnEggItem(ModEntities.SARUGAMI, 0xA67B5B, 0x6B4226, new Item.Properties())
    );

    // Macaque spawn egg - brown/tan macaque
    public static final Item MACAQUE_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "macaque_spawn_egg"),
        new SpawnEggItem(ModEntities.MACAQUE, 0x8B5A2B, 0xD2B48C, new Item.Properties())
    );
    
    // Crow spawn egg - black with dark gray highlights
    public static final Item CROW_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "crow_spawn_egg"),
        new SpawnEggItem(ModEntities.CROW, 0x0A0A0A, 0x444444, new Item.Properties())
    );

    // Hitotsume Nyudo spawn egg - towering monk: pale/beige with dark markings
    public static final Item HITOTSUME_NYUDO_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "hitotsume_nyudo_spawn_egg"),
        new SpawnEggItem(ModEntities.HITOTSUME_NYUDO, 0xD9C7A3, 0x3B2F2F, new Item.Properties())
    );

    // Gashadokuro (Giant) spawn egg - bone/black theme
    public static final Item GASHADOKU_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "gashadokuro_spawn_egg"),
        new SpawnEggItem(net.minecraft.world.entity.EntityType.GIANT, 0x1F1F1F, 0xE6E2D3, new Item.Properties())
    );

    // Goryo spawn egg - ghostly translucent cyan over dark base
    public static final Item GORYO_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "goryo_spawn_egg"),
        new SpawnEggItem(ModEntities.GORYO, 0x3B3F42, 0xA8FFFF, new Item.Properties())
    );

    public static final Item SHIRYO_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "shiryo_spawn_egg"),
        new SpawnEggItem(ModEntities.SHIRYO, 0x302A31, 0x8EE7E7, new Item.Properties())
    );

    // Umi Nyobo spawn egg - deep teal with seafoam highlights
    public static final Item UMI_NYOBO_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "umi_nyobo_spawn_egg"),
        new SpawnEggItem(ModEntities.UMI_NYOBO, 0x1F4E5F, 0x7ED1C2, new Item.Properties())
    );
    
    // Maikubi spawn egg - dark grey/black with glowing blue (wither-like colors)
    public static final Item MAIKUBI_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "maikubi_spawn_egg"),
        new SpawnEggItem(ModEntities.MAIKUBI, 0x1A1A1A, 0x3FBAFF, new Item.Properties())
    );

    // Omukade spawn egg - centipede theme: dark red-brown with bone highlights
    public static final Item OMUKADE_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "omukade_spawn_egg"),
        new SpawnEggItem(ModEntities.OMUKADE, 0x5A2218, 0xD8C7A1, new Item.Properties())
    );
    
    public static final Item IKUCHI_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ikuchi_spawn_egg"),
        new SpawnEggItem(ModEntities.IKUCHI, 0x5A2218, 0xD8C7A1, new Item.Properties())
    );
    public static final Item UMI_BOZU_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "umi_bozu_spawn_egg"),
        new SpawnEggItem(ModEntities.UMI_BOZU, 0x5A2218, 0xD8C7A1, new Item.Properties())
    );
    
    // Kobayakawa clan spawn eggs - purple/gold theme
    public static final Item KOBAYAKAWA_ASHIGARU_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kobayakawa_ashigaru_spawn_egg"),
        new SpawnEggItem(ModEntities.KOBAYAKAWA_ASHIGARU, 0x6B4C9A, 0xFFD700, new Item.Properties())
    );
    
    public static final Item KOBAYAKAWA_SAMURAI_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kobayakawa_samurai_spawn_egg"),
        new SpawnEggItem(ModEntities.KOBAYAKAWA_SAMURAI, 0x6B4C9A, 0xFFD700, new Item.Properties())
    );
    
    public static final Item KOBAYAKAWA_SOHEI_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kobayakawa_sohei_spawn_egg"),
        new SpawnEggItem(ModEntities.KOBAYAKAWA_SOHEI, 0x6B4C9A, 0xFFD700, new Item.Properties())
    );
    
    // Takeda clan spawn eggs - red/white theme
    public static final Item TAKEDA_ASHIGARU_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "takeda_ashigaru_spawn_egg"),
        new SpawnEggItem(ModEntities.TAKEDA_ASHIGARU, 0xDC143C, 0xFFFFFF, new Item.Properties())
    );
    
    public static final Item TAKEDA_SAMURAI_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "takeda_samurai_spawn_egg"),
        new SpawnEggItem(ModEntities.TAKEDA_SAMURAI, 0xDC143C, 0xFFFFFF, new Item.Properties())
    );
    
    public static final Item TAKEDA_SOHEI_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "takeda_sohei_spawn_egg"),
        new SpawnEggItem(ModEntities.TAKEDA_SOHEI, 0xDC143C, 0xFFFFFF, new Item.Properties())
    );
    
    // Satomi clan spawn eggs - blue/white theme
    public static final Item SATOMI_ASHIGARU_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "satomi_ashigaru_spawn_egg"),
        new SpawnEggItem(ModEntities.SATOMI_ASHIGARU, 0x1E90FF, 0xF0F8FF, new Item.Properties())
    );
    
    public static final Item SATOMI_SAMURAI_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "satomi_samurai_spawn_egg"),
        new SpawnEggItem(ModEntities.SATOMI_SAMURAI, 0x1E90FF, 0xF0F8FF, new Item.Properties())
    );
    
    public static final Item SATOMI_SOHEI_SPAWN_EGG = Registry.register(
        BuiltInRegistries.ITEM,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "satomi_sohei_spawn_egg"),
        new SpawnEggItem(ModEntities.SATOMI_SOHEI, 0x1E90FF, 0xF0F8FF, new Item.Properties())
    );
    
    public static void register() {
        sengokuFabric.LOGGER.info("Registered spawn eggs");

        // Add to creative tab
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(content -> {
            content.accept(BANDIT_SPAWN_EGG);
            content.accept(RONIN_SPAWN_EGG);
            content.accept(WARLORD_SPAWN_EGG);
            content.accept(SHINOBI_SPAWN_EGG);
            content.accept(SHINOBI_LORD_SPAWN_EGG);
            content.accept(YUKI_ONNA_SPAWN_EGG);
            content.accept(ONIKUMA_SPAWN_EGG);
            content.accept(ONI_BRUTE_SPAWN_EGG);
            content.accept(GAKI_SPAWN_EGG);
            content.accept(SARUGAMI_SPAWN_EGG);
            // Goryo (ghost samurai)
            content.accept(GORYO_SPAWN_EGG);
            content.accept(SHIRYO_SPAWN_EGG);
            // Umi Nyobo (coastal swordswoman)
            content.accept(UMI_NYOBO_SPAWN_EGG);
            // Macaque (warm-water-attracted animal)
            content.accept(MACAQUE_SPAWN_EGG);
            // Crow (tameable bird similar to parrot)
            content.accept(CROW_SPAWN_EGG);
            content.accept(HITOTSUME_NYUDO_SPAWN_EGG);
            content.accept(OMUKADE_SPAWN_EGG);
            
            // Kobayakawa clan
            content.accept(KOBAYAKAWA_ASHIGARU_SPAWN_EGG);
            content.accept(KOBAYAKAWA_SAMURAI_SPAWN_EGG);
            content.accept(KOBAYAKAWA_SOHEI_SPAWN_EGG);
            
            // Takeda clan
            content.accept(TAKEDA_ASHIGARU_SPAWN_EGG);
            content.accept(TAKEDA_SAMURAI_SPAWN_EGG);
            content.accept(TAKEDA_SOHEI_SPAWN_EGG);
            
            // Satomi clan
            content.accept(SATOMI_ASHIGARU_SPAWN_EGG);
            content.accept(SATOMI_SAMURAI_SPAWN_EGG);
            content.accept(SATOMI_SOHEI_SPAWN_EGG);
            // Red-Crowned Crane spawn egg - white with red crown
            try {
                content.accept(RED_CROWNED_CRANE_SPAWN_EGG);
            } catch (Throwable ignored) {}
        });
    }
}
