package com.shioh.sengoku.init;

import com.shioh.sengoku.item.DaimyoArmorItem;
import com.shioh.sengoku.item.KenseiAxeItem;
import com.shioh.sengoku.item.KenseiKanaboItem;
import com.shioh.sengoku.item.KenseiNaginataItem;
import com.shioh.sengoku.item.KenseiOdachiItem;
import com.shioh.sengoku.item.KenseiSwordItem;
import com.shioh.sengoku.item.KenseiTantoItem;
import com.shioh.sengoku.item.KenseiTetsuboItem;
import com.shioh.sengoku.item.KenseiYariItem;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import com.shioh.sengoku.registry.WeaponRegistry;
import com.shioh.sengoku.struct.WeaponType;

/**
 * Registry for Kensei-related items (Daimyo armor set and Blade of the Kensei).
 * These are custom items that replace the modified diamond equipment from the datapack.
 */
public final class KenseiItemReg {
    
    private KenseiItemReg() {}
    
    // Diamond-tier Daimyo armor set
    public static final Item DAIMYO_KABUTO = DaimyoArmorItem.createHelmet();
    public static final Item DAIMYO_DO = DaimyoArmorItem.createChestplate();
    public static final Item DAIMYO_HAIDATE = DaimyoArmorItem.createLeggings();
    public static final Item DAIMYO_KUSAZURI = DaimyoArmorItem.createBoots();
    
    // Netherite-tier Daimyo armor set
    public static final Item NETHERITE_DAIMYO_KABUTO = DaimyoArmorItem.createNetheriteHelmet();
    public static final Item NETHERITE_DAIMYO_DO = DaimyoArmorItem.createNetheriteChestplate();
    public static final Item NETHERITE_DAIMYO_HAIDATE = DaimyoArmorItem.createNetheriteLeggings();
    public static final Item NETHERITE_DAIMYO_KUSAZURI = DaimyoArmorItem.createNetheriteBoots();
    
    // Diamond-tier Blade of the Kensei
    public static final Item BLADE_OF_THE_KENSEI = KenseiSwordItem.createDiamondSword();
    
    // Netherite-tier Blade of the Kensei
    public static final Item NETHERITE_BLADE_OF_THE_KENSEI = KenseiSwordItem.createNetheriteSword();

    // Kintaro's legendary splitting axe
    public static final Item SPLITTING_AXE_OF_KINTARO = KenseiAxeItem.createDiamond();
    public static final Item NETHERITE_SPLITTING_AXE_OF_KINTARO = KenseiAxeItem.createNetherite();
    
    // Diamond-tier legendary weapons
    public static final Item NAGINATA_OF_THE_NOBUSHI = KenseiNaginataItem.createDiamond();
    public static final Item KANABO_OF_OTAKEMARU = KenseiKanaboItem.createDiamond();
    public static final Item TETSUBO_OF_THE_HATAMOTO = KenseiTetsuboItem.createDiamond();
    public static final Item ODACHI_OF_THE_SHUGODAI = KenseiOdachiItem.createDiamond();
    public static final Item YARI_OF_THE_TAISHO = KenseiYariItem.createDiamond();
    public static final Item TANTO_OF_TAMATORI_HIME = KenseiTantoItem.createDiamond();
    
    // Netherite-tier legendary weapons
    public static final Item NETHERITE_NAGINATA_OF_THE_NOBUSHI = KenseiNaginataItem.createNetherite();
    public static final Item NETHERITE_KANABO_OF_OTAKEMARU = KenseiKanaboItem.createNetherite();
    public static final Item NETHERITE_TETSUBO_OF_THE_HATAMOTO = KenseiTetsuboItem.createNetherite();
    public static final Item NETHERITE_ODACHI_OF_THE_SHUGODAI = KenseiOdachiItem.createNetherite();
    public static final Item NETHERITE_YARI_OF_THE_TAISHO = KenseiYariItem.createNetherite();
    public static final Item NETHERITE_TANTO_OF_TAMATORI_HIME = KenseiTantoItem.createNetherite();
    
    public static void registerItems() {
        // Register diamond-tier Daimyo armor pieces
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("daimyo_kabuto"), DAIMYO_KABUTO);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("daimyo_do"), DAIMYO_DO);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("daimyo_haidate"), DAIMYO_HAIDATE);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("daimyo_kusazuri"), DAIMYO_KUSAZURI);
        
        // Register netherite-tier Daimyo armor pieces
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("netherite_daimyo_kabuto"), NETHERITE_DAIMYO_KABUTO);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("netherite_daimyo_do"), NETHERITE_DAIMYO_DO);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("netherite_daimyo_haidate"), NETHERITE_DAIMYO_HAIDATE);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("netherite_daimyo_kusazuri"), NETHERITE_DAIMYO_KUSAZURI);
        
        // Register diamond-tier Blade of the Kensei
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("blade_of_the_kensei"), BLADE_OF_THE_KENSEI);
        
        // Register netherite-tier Blade of the Kensei
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("netherite_blade_of_the_kensei"), NETHERITE_BLADE_OF_THE_KENSEI);

        // Register Kintaro's splitting axe variants
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("splitting_axe_of_kintaro"), SPLITTING_AXE_OF_KINTARO);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("netherite_splitting_axe_of_kintaro"), NETHERITE_SPLITTING_AXE_OF_KINTARO);
        
        // Register diamond-tier legendary weapons
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("naginata_of_the_nobushi"), NAGINATA_OF_THE_NOBUSHI);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("kanabo_of_otakemaru"), KANABO_OF_OTAKEMARU);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("tetsubo_of_the_hatamoto"), TETSUBO_OF_THE_HATAMOTO);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("odachi_of_the_shugodai"), ODACHI_OF_THE_SHUGODAI);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("yari_of_the_taisho"), YARI_OF_THE_TAISHO);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("tanto_of_tamatori_hime"), TANTO_OF_TAMATORI_HIME);
        
        // Register netherite-tier legendary weapons
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("netherite_naginata_of_the_nobushi"), NETHERITE_NAGINATA_OF_THE_NOBUSHI);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("netherite_kanabo_of_otakemaru"), NETHERITE_KANABO_OF_OTAKEMARU);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("netherite_tetsubo_of_the_hatamoto"), NETHERITE_TETSUBO_OF_THE_HATAMOTO);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("netherite_odachi_of_the_shugodai"), NETHERITE_ODACHI_OF_THE_SHUGODAI);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("netherite_yari_of_the_taisho"), NETHERITE_YARI_OF_THE_TAISHO);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("netherite_tanto_of_tamatori_hime"), NETHERITE_TANTO_OF_TAMATORI_HIME);

            // Add Kensei legendary weapons into the WeaponRegistry so they appear with the normal material variants
            WeaponRegistry.registerExternalItem("naginata_of_the_nobushi", WeaponType.NAGINATA, () -> NAGINATA_OF_THE_NOBUSHI);
            WeaponRegistry.registerExternalItem("kanabo_of_otakemaru", WeaponType.KANABO, () -> KANABO_OF_OTAKEMARU);
            WeaponRegistry.registerExternalItem("tetsubo_of_the_hatamoto", WeaponType.TETSUBO, () -> TETSUBO_OF_THE_HATAMOTO);
            WeaponRegistry.registerExternalItem("odachi_of_the_shugodai", WeaponType.ODACHI, () -> ODACHI_OF_THE_SHUGODAI);
            WeaponRegistry.registerExternalItem("yari_of_the_taisho", WeaponType.YARI, () -> YARI_OF_THE_TAISHO);
            WeaponRegistry.registerExternalItem("tanto_of_tamatori_hime", WeaponType.TANTO, () -> TANTO_OF_TAMATORI_HIME);

            WeaponRegistry.registerExternalItem("netherite_naginata_of_the_nobushi", WeaponType.NAGINATA, () -> NETHERITE_NAGINATA_OF_THE_NOBUSHI);
            WeaponRegistry.registerExternalItem("netherite_kanabo_of_otakemaru", WeaponType.KANABO, () -> NETHERITE_KANABO_OF_OTAKEMARU);
            WeaponRegistry.registerExternalItem("netherite_tetsubo_of_the_hatamoto", WeaponType.TETSUBO, () -> NETHERITE_TETSUBO_OF_THE_HATAMOTO);
            WeaponRegistry.registerExternalItem("netherite_odachi_of_the_shugodai", WeaponType.ODACHI, () -> NETHERITE_ODACHI_OF_THE_SHUGODAI);
            WeaponRegistry.registerExternalItem("netherite_yari_of_the_taisho", WeaponType.YARI, () -> NETHERITE_YARI_OF_THE_TAISHO);
            WeaponRegistry.registerExternalItem("netherite_tanto_of_tamatori_hime", WeaponType.TANTO, () -> NETHERITE_TANTO_OF_TAMATORI_HIME);

            // Blade of the Kensei (sword) — place the diamond Kensei blade after the vanilla netherite sword
            // and then place the netherite Kensei blade after that, so they appear in-between vanilla netherite
            // sword and the netherite Kensei blade.
            ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
                entries.addAfter(Items.DIAMOND_BOOTS, DAIMYO_KABUTO);
                entries.addAfter(DAIMYO_KABUTO, DAIMYO_DO);
                entries.addAfter(DAIMYO_DO, DAIMYO_HAIDATE);
                entries.addAfter(DAIMYO_HAIDATE, DAIMYO_KUSAZURI);

                entries.addAfter(Items.NETHERITE_BOOTS, NETHERITE_DAIMYO_KABUTO);
                entries.addAfter(NETHERITE_DAIMYO_KABUTO, NETHERITE_DAIMYO_DO);
                entries.addAfter(NETHERITE_DAIMYO_DO, NETHERITE_DAIMYO_HAIDATE);
                entries.addAfter(NETHERITE_DAIMYO_HAIDATE, NETHERITE_DAIMYO_KUSAZURI);

                entries.addAfter(Items.NETHERITE_SWORD, BLADE_OF_THE_KENSEI);
                entries.addAfter(BLADE_OF_THE_KENSEI, NETHERITE_BLADE_OF_THE_KENSEI);
            });
    }
}
