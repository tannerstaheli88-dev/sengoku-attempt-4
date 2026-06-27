package com.shioh.sengoku.registry;

import com.shioh.sengoku.entity.*;
import com.shioh.sengoku.sengokuFabric;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Registry for custom entities.
 */
public class ModEntities {
    
    public static final EntityType<KunaiEntity> KUNAI = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kunai"),
        EntityType.Builder.<KunaiEntity>of(KunaiEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kunai").toString())
    );
    
    public static final EntityType<BanditEntity> BANDIT = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "bandit"),
        EntityType.Builder.<BanditEntity>of(BanditEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)  // Same size as illagers
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "bandit").toString())
    );
    
    public static final EntityType<RoninEntity> RONIN = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ronin"),
        EntityType.Builder.<RoninEntity>of(RoninEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)  // Same size as illagers
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ronin").toString())
    );

    public static final EntityType<GoryoEntity> GORYO = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "goryo"),
        EntityType.Builder.<GoryoEntity>of(GoryoEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)  // Same size as illagers
            .clientTrackingRange(8)
            .updateInterval(3)
            .fireImmune()  // Immune to fire (from the Nether)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "goryo").toString())
    );

    public static final EntityType<ShiryoEntity> SHIRYO = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "shiryo"),
        EntityType.Builder.<ShiryoEntity>of(ShiryoEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .fireImmune()
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "shiryo").toString())
    );

    public static final EntityType<UmiNyoboEntity> UMI_NYOBO = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "umi_nyobo"),
        EntityType.Builder.<UmiNyoboEntity>of(UmiNyoboEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "umi_nyobo").toString())
    );

    // Oni Brute - zombie-based brute with high reinforcements
    public static final EntityType<OniBruteEntity> ONI_BRUTE = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "oni_brute"),
        EntityType.Builder.<OniBruteEntity>of(OniBruteEntity::new, MobCategory.MONSTER)
            .sized(1.2F, 3.6F)  // Significantly larger than zombie: much wider and taller
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "oni_brute").toString())
    );

    // Maikubi - Flying hostile mob that resembles the Wither but shoots blaze fireballs
    public static final EntityType<MaikubiEntity> MAIKUBI = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "maikubi"),
        EntityType.Builder.<MaikubiEntity>of(MaikubiEntity::new, MobCategory.MONSTER)
            .sized(0.9F, 3.5F)  // Similar to wither but slightly smaller
            .clientTrackingRange(10)
            .updateInterval(3)
            .fireImmune()  // Immune to fire like the wither
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "maikubi").toString())
    );

    // Warlord (formerly Kensei) - a named boss based on Vindicator
    public static final EntityType<WarlordEntity> WARLORD = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "warlord"),
        EntityType.Builder.<WarlordEntity>of(WarlordEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "warlord").toString())
    );

    public static final EntityType<ShinobiLordEntity> SHINOBI_LORD = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "shinobi_lord"),
        EntityType.Builder.<ShinobiLordEntity>of(ShinobiLordEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "shinobi_lord").toString())
    );
    
    // Kobayakawa Clan
    public static final EntityType<KobayakawaAshigaruEntity> KOBAYAKAWA_ASHIGARU = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kobayakawa_ashigaru"),
        EntityType.Builder.<KobayakawaAshigaruEntity>of(KobayakawaAshigaruEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kobayakawa_ashigaru").toString())
    );
    
    public static final EntityType<KobayakawaSamuraiEntity> KOBAYAKAWA_SAMURAI = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kobayakawa_samurai"),
        EntityType.Builder.<KobayakawaSamuraiEntity>of(KobayakawaSamuraiEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kobayakawa_samurai").toString())
    );
    
    public static final EntityType<KobayakawaSoheiEntity> KOBAYAKAWA_SOHEI = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kobayakawa_sohei"),
        EntityType.Builder.<KobayakawaSoheiEntity>of(KobayakawaSoheiEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kobayakawa_sohei").toString())
    );
    
    // Takeda Clan
    public static final EntityType<TakedaAshigaruEntity> TAKEDA_ASHIGARU = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "takeda_ashigaru"),
        EntityType.Builder.<TakedaAshigaruEntity>of(TakedaAshigaruEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "takeda_ashigaru").toString())
    );
    
    public static final EntityType<TakedaSamuraiEntity> TAKEDA_SAMURAI = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "takeda_samurai"),
        EntityType.Builder.<TakedaSamuraiEntity>of(TakedaSamuraiEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "takeda_samurai").toString())
    );
    
    public static final EntityType<TakedaSoheiEntity> TAKEDA_SOHEI = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "takeda_sohei"),
        EntityType.Builder.<TakedaSoheiEntity>of(TakedaSoheiEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "takeda_sohei").toString())
    );
    
    // Satomi Clan 
    public static final EntityType<SatomiAshigaruEntity> SATOMI_ASHIGARU = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "satomi_ashigaru"),
        EntityType.Builder.<SatomiAshigaruEntity>of(SatomiAshigaruEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "satomi_ashigaru").toString())
    );
    
    public static final EntityType<SatomiSamuraiEntity> SATOMI_SAMURAI = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "satomi_samurai"),
        EntityType.Builder.<SatomiSamuraiEntity>of(SatomiSamuraiEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "satomi_samurai").toString())
    );
    
    public static final EntityType<SatomiSoheiEntity> SATOMI_SOHEI = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "satomi_sohei"),
        EntityType.Builder.<SatomiSoheiEntity>of(SatomiSoheiEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "satomi_sohei").toString())
    );
    
    // Yuki Onna - Snow Woman Yokai
    public static final EntityType<YukiOnnaEntity> YUKI_ONNA = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "yuki_onna"),
        EntityType.Builder.<YukiOnnaEntity>of(YukiOnnaEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.8F)  // Slightly shorter than illagers
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "yuki_onna").toString())
    );

    // Yokai formerly zombie function variants
    public static final EntityType<OnikumaEntity> ONIKUMA = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "onikuma"),
        EntityType.Builder.<OnikumaEntity>of(OnikumaEntity::new, MobCategory.MONSTER)
            .sized(1.2F, 2.8F) // larger bear-like brute
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "onikuma").toString())
    );

    // Gaki - Husk-like ghoul variant (shares stats/AI with vanilla Husk)
    public static final EntityType<GakiEntity> GAKI = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "gaki"),
        EntityType.Builder.<GakiEntity>of(GakiEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.95F) // Same size as Husk
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "gaki").toString())
    );

    // Kojin - Drowned-like aquatic demon variant (shares stats/AI with vanilla Drowned)
    public static final EntityType<KojinEntity> KOJIN = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kojin"),
        EntityType.Builder.<KojinEntity>of(KojinEntity::new, MobCategory.WATER_CREATURE)
            .sized(0.6F, 1.95F) // Same size as Drowned
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kojin").toString())
    );

    // Ningyo - Drowned-like aquatic demon variant (same stats/AI as vanilla Drowned)
    public static final EntityType<NingyoEntity> NINGYO = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ningyo"),
        EntityType.Builder.<NingyoEntity>of(NingyoEntity::new, MobCategory.WATER_CREATURE)
            .sized(0.6F, 1.95F) // Same size as Drowned
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ningyo").toString())
    );

    // Kamiike Hime - Guardian-like aquatic variant (shares stats/AI with vanilla Guardian)
    public static final EntityType<KamiikeHimeEntity> KAMIIKE_HIME = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kamiike_hime"),
        EntityType.Builder.<KamiikeHimeEntity>of(KamiikeHimeEntity::new, MobCategory.WATER_CREATURE)
            .sized(0.85F, 0.85F) // Same size as Guardian
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kamiike_hime").toString())
    );

    // Akugyo - Elder Guardian-like aquatic variant (shares stats/AI with vanilla Elder Guardian)
    public static final EntityType<AkugyoEntity> AKUGYO = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "akugyo"),
        EntityType.Builder.<AkugyoEntity>of(AkugyoEntity::new, MobCategory.WATER_CREATURE)
            .sized(4.2F, 4.2F) // Slightly reduced from previous scaling for better hitbox
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "akugyo").toString())
    );

    public static final EntityType<UmiInuEntity> UMI_INU = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "umi_inu"),
        EntityType.Builder.<UmiInuEntity>of(UmiInuEntity::new, MobCategory.WATER_CREATURE)
            .sized(0.85F, 0.85F) // Slightly reduced from previous scaling for better hitbox
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "umi_inu").toString())
    );
public static final EntityType<IkuchiEntity> IKUCHI = Registry.register(
    BuiltInRegistries.ENTITY_TYPE,
    ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ikuchi"),
    EntityType.Builder.<IkuchiEntity>of(IkuchiEntity::new, MobCategory.WATER_CREATURE)
        .sized(1.2F, 1.2F)
        .clientTrackingRange(10)
        .updateInterval(3)
        .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ikuchi").toString())
);

public static final EntityType<IkuchiPartEntity> IKUCHI_PART = Registry.register(
    BuiltInRegistries.ENTITY_TYPE,
    ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ikuchi_part"),
    EntityType.Builder.<IkuchiPartEntity>of(IkuchiPartEntity::new, MobCategory.WATER_CREATURE)
        .sized(1.0F, 1.0F)
        .clientTrackingRange(10)
        .updateInterval(3)
        .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ikuchi_part").toString())
);

public static final EntityType<IkuchiEndEntity> IKUCHI_END = Registry.register(
    BuiltInRegistries.ENTITY_TYPE,
    ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ikuchi_end"),
    EntityType.Builder.<IkuchiEndEntity>of(IkuchiEndEntity::new, MobCategory.WATER_CREATURE)
        .sized(0.8F, 0.8F)
        .clientTrackingRange(10)
        .updateInterval(3)
        .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "ikuchi_end").toString())
);
public static final EntityType<UmiBozuEntity> UMI_BOZU = Registry.register(
    BuiltInRegistries.ENTITY_TYPE,
    ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "umi_bozu"),
    EntityType.Builder.<UmiBozuEntity>of(UmiBozuEntity::new, MobCategory.WATER_CREATURE)
        .sized(3.5F, 8.0F)
        .clientTrackingRange(12)
        .updateInterval(3)
        .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "umi_bozu").toString())
);

    public static final EntityType<SarugamiEntity> SARUGAMI = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "sarugami"),
        EntityType.Builder.<SarugamiEntity>of(SarugamiEntity::new, MobCategory.MONSTER)
            .sized(0.65F, 2.05F) // slight height increase over base
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "sarugami").toString())
    );

    public static final EntityType<HitotsumeNyudoEntity> HITOTSUME_NYUDO = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "hitotsume_nyudo"),
        EntityType.Builder.<HitotsumeNyudoEntity>of(HitotsumeNyudoEntity::new, MobCategory.MONSTER)
            .sized(1.8F, 4.5F) // massive towering monk
            .clientTrackingRange(10)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "hitotsume_nyudo").toString())
    );
    
    // Red-Crowned Crane - peaceful flying bird
    public static final EntityType<RedCrownedCraneEntity> RED_CROWNED_CRANE = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "red_crowned_crane"),
        EntityType.Builder.<RedCrownedCraneEntity>of(RedCrownedCraneEntity::new, MobCategory.CREATURE)
            .sized(0.7F, 1.4F) // tall bird
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "red_crowned_crane").toString())
    );

    //monkey
    public static final EntityType<com.shioh.sengoku.entity.MacaqueEntity> MACAQUE = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "macaque"),
        EntityType.Builder.<com.shioh.sengoku.entity.MacaqueEntity>of(com.shioh.sengoku.entity.MacaqueEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 0.9F)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "macaque").toString())
    );

    // Crow - tameable bird like parrot but with crow sounds and texture
    public static final EntityType<CrowEntity> CROW = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "crow"),
        EntityType.Builder.<CrowEntity>of(CrowEntity::new, MobCategory.CREATURE)
            .sized(0.5F, 0.9F) // Same as parrot
            .clientTrackingRange(8)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "crow").toString())
    );
    
    // Omukade - 
    public static final EntityType<OmukadeEntity> OMUKADE = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "omukade"),
        EntityType.Builder.<OmukadeEntity>of(OmukadeEntity::new, MobCategory.MONSTER)
            .sized(2.15F, 1.3F) // Slightly larger main body
            .clientTrackingRange(10)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "omukade").toString())
    );
    
    // Omukade Part - Body segments that follow the main Omukade
    public static final EntityType<OmukadePartEntity> OMUKADE_PART = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "omukade_part"),
        EntityType.Builder.<OmukadePartEntity>of(OmukadePartEntity::new, MobCategory.MONSTER)
            .sized(1.6F, 1.0F) // Slightly smaller than main body
            .clientTrackingRange(10)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "omukade_part").toString())
    );

    // Omukade End - Dedicated final body segment for separate tail texture
    public static final EntityType<OmukadeEndEntity> OMUKADE_END = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "omukade_end"),
        EntityType.Builder.<OmukadeEndEntity>of(OmukadeEndEntity::new, MobCategory.MONSTER)
            .sized(1.6F, 1.0F)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "omukade_end").toString())
    );

    // Ender Dragon extra trailing segment for Japanese dragon effect.
    public static final EntityType<DragonPartEntity> DRAGON_PART = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_part"),
        EntityType.Builder.<DragonPartEntity>of(DragonPartEntity::new, MobCategory.MONSTER)
            .sized(2.0F, 1.0F)
            .clientTrackingRange(12)
            .updateInterval(1)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_part").toString())
    );

    // Ender Dragon dedicated front head segment for replacement chain.
    public static final EntityType<DragonHeadEntity> DRAGON_HEAD = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_head"),
        EntityType.Builder.<DragonHeadEntity>of(DragonHeadEntity::new, MobCategory.MONSTER)
            .sized(2.8F, 1.2F)
            .clientTrackingRange(12)
            .updateInterval(1)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_head").toString())
    );

    // Ender Dragon thinner rear body segment.
    public static final EntityType<DragonPartThinEntity> DRAGON_PART_THIN = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_part_thin"),
        EntityType.Builder.<DragonPartThinEntity>of(DragonPartThinEntity::new, MobCategory.MONSTER)
            .sized(1.6F, 0.8F)
            .clientTrackingRange(12)
            .updateInterval(1)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_part_thin").toString())
    );

    // Ender Dragon neck segment.
    public static final EntityType<DragonNeckEntity> DRAGON_NECK = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_neck"),
        EntityType.Builder.<DragonNeckEntity>of(DragonNeckEntity::new, MobCategory.MONSTER)
            .sized(1.6F, 0.9F)
            .clientTrackingRange(12)
            .updateInterval(1)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_neck").toString())
    );

    // Ender Dragon arms/limb segment.
    public static final EntityType<DragonArmsEntity> DRAGON_ARMS = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_arms"),
        EntityType.Builder.<DragonArmsEntity>of(DragonArmsEntity::new, MobCategory.MONSTER)
            .sized(2.4F, 1.0F)
            .clientTrackingRange(12)
            .updateInterval(1)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_arms").toString())
    );

    // Ender Dragon dedicated final trailing segment.
    public static final EntityType<DragonPartEndEntity> DRAGON_PART_END = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_part_end"),
        EntityType.Builder.<DragonPartEndEntity>of(DragonPartEndEntity::new, MobCategory.MONSTER)
            .sized(1.4F, 0.9F)
            .clientTrackingRange(12)
            .updateInterval(1)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_part_end").toString())
    );

    // Ender Dragon small extra trailing tail segment.
    public static final EntityType<DragonTailEntity> DRAGON_TAIL = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_tail"),
        EntityType.Builder.<DragonTailEntity>of(DragonTailEntity::new, MobCategory.MONSTER)
            .sized(1.1F, 0.7F)
            .clientTrackingRange(12)
            .updateInterval(1)
            .build(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "dragon_tail").toString())
    );
    
    public static void register() {
        // Force static initialization and log
        sengokuFabric.LOGGER.info("Registered Kunai entity: {}", KUNAI);
        sengokuFabric.LOGGER.info("Registered Bandit entity: {}", BANDIT);
        sengokuFabric.LOGGER.info("Registered Ronin entity: {}", RONIN);
        sengokuFabric.LOGGER.info("Registered Goryo entity: {}", GORYO);
        sengokuFabric.LOGGER.info("Registered Shiryo entity: {}", SHIRYO);
        sengokuFabric.LOGGER.info("Registered Umi Nyobo entity: {}", UMI_NYOBO);
        sengokuFabric.LOGGER.info("Registered Maikubi entity: {}", MAIKUBI);
        sengokuFabric.LOGGER.info("Registered Warlord entity: {}", WARLORD);
        sengokuFabric.LOGGER.info("Registered Shinobi Lord entity: {}", SHINOBI_LORD);
        sengokuFabric.LOGGER.info("Registered Yuki Onna entity: {}", YUKI_ONNA);
        sengokuFabric.LOGGER.info("Registered Onikuma entity: {}", ONIKUMA);
        sengokuFabric.LOGGER.info("Registered Gaki entity: {}", GAKI);
        sengokuFabric.LOGGER.info("Registered Sarugami entity: {}", SARUGAMI);
        sengokuFabric.LOGGER.info("Registered Hitotsume Nyudo entity: {}", HITOTSUME_NYUDO);
        sengokuFabric.LOGGER.info("Registered Oni Brute entity: {}", ONI_BRUTE);
        sengokuFabric.LOGGER.info("Registered Ningyo entity: {}", NINGYO);
        sengokuFabric.LOGGER.info("Registered Omukade entity: {}", OMUKADE);
        sengokuFabric.LOGGER.info("Registered Dragon Part entity: {}", DRAGON_PART);
        sengokuFabric.LOGGER.info("Registered Dragon Head entity: {}", DRAGON_HEAD);
        sengokuFabric.LOGGER.info("Registered Dragon Part Thin entity: {}", DRAGON_PART_THIN);
        sengokuFabric.LOGGER.info("Registered Dragon Neck entity: {}", DRAGON_NECK);
        sengokuFabric.LOGGER.info("Registered Dragon Arms entity: {}", DRAGON_ARMS);
        sengokuFabric.LOGGER.info("Registered Dragon Part End entity: {}", DRAGON_PART_END);
        sengokuFabric.LOGGER.info("Registered Dragon Tail entity: {}", DRAGON_TAIL);
        sengokuFabric.LOGGER.info("Registered Umi Bozu entity: {}", UMI_BOZU);
        
        // Register entity attributes
        FabricDefaultAttributeRegistry.register(BANDIT, BanditEntity.createAttributes().build());
        // Increase movement speed for vanilla Zombies only (not Husks/Drowned/Gaki)
        FabricDefaultAttributeRegistry.register(net.minecraft.world.entity.EntityType.ZOMBIE, net.minecraft.world.entity.monster.Zombie.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.25D).build());
        FabricDefaultAttributeRegistry.register(RONIN, RoninEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GORYO, GoryoEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(SHIRYO, net.minecraft.world.entity.npc.Villager.createAttributes().build());
        FabricDefaultAttributeRegistry.register(UMI_NYOBO, UmiNyoboEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(MAIKUBI, MaikubiEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(WARLORD, com.shioh.sengoku.entity.WarlordEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(SHINOBI_LORD, ShinobiLordEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(ONI_BRUTE, OniBruteEntity.createAttributes().build());
        
        // Register clan entities (they inherit attributes from vanilla entities)
        FabricDefaultAttributeRegistry.register(KOBAYAKAWA_ASHIGARU, net.minecraft.world.entity.monster.Pillager.createAttributes().build());
        // Increase samurai HP so clan samurai are tougher than vanilla vindicators
        FabricDefaultAttributeRegistry.register(KOBAYAKAWA_SAMURAI, net.minecraft.world.entity.monster.Vindicator.createAttributes().add(Attributes.MAX_HEALTH, 40.0D).build());
        FabricDefaultAttributeRegistry.register(KOBAYAKAWA_SOHEI, net.minecraft.world.entity.monster.Evoker.createAttributes().build());
        
        FabricDefaultAttributeRegistry.register(TAKEDA_ASHIGARU, net.minecraft.world.entity.monster.Pillager.createAttributes().build());
        FabricDefaultAttributeRegistry.register(TAKEDA_SAMURAI, net.minecraft.world.entity.monster.Vindicator.createAttributes().add(Attributes.MAX_HEALTH, 40.0D).build());
        FabricDefaultAttributeRegistry.register(TAKEDA_SOHEI, net.minecraft.world.entity.monster.Evoker.createAttributes().build());
        
        FabricDefaultAttributeRegistry.register(SATOMI_ASHIGARU, net.minecraft.world.entity.monster.Pillager.createAttributes().build());
        FabricDefaultAttributeRegistry.register(SATOMI_SAMURAI, net.minecraft.world.entity.monster.Vindicator.createAttributes().add(Attributes.MAX_HEALTH, 40.0D).build());
        FabricDefaultAttributeRegistry.register(SATOMI_SOHEI, net.minecraft.world.entity.monster.Evoker.createAttributes().build());
        
        // Register Yuki Onna
        FabricDefaultAttributeRegistry.register(YUKI_ONNA, YukiOnnaEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(ONIKUMA, OnikumaEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(GAKI, GakiEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(KOJIN, KojinEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(NINGYO, NingyoEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(KAMIIKE_HIME, KamiikeHimeEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(AKUGYO, AkugyoEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(UMI_INU, UmiInuEntity.createAttributes().build());
FabricDefaultAttributeRegistry.register(IKUCHI, IkuchiEntity.createAttributes().build());
FabricDefaultAttributeRegistry.register(IKUCHI_PART, IkuchiPartEntity.createAttributes().build());
FabricDefaultAttributeRegistry.register(IKUCHI_END, IkuchiEndEntity.createAttributes().build());
FabricDefaultAttributeRegistry.register(UMI_BOZU, UmiBozuEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(SARUGAMI, SarugamiEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(HITOTSUME_NYUDO, HitotsumeNyudoEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(RED_CROWNED_CRANE, RedCrownedCraneEntity.createAttributes().build());
        // Macaque attributes
        try {
            FabricDefaultAttributeRegistry.register(MACAQUE, com.shioh.sengoku.entity.MacaqueEntity.createAttributes().build());
        } catch (Throwable ignored) {}
        // Crow attributes
        FabricDefaultAttributeRegistry.register(CROW, CrowEntity.createAttributes().build());
        
        // Omukade attributes
        FabricDefaultAttributeRegistry.register(OMUKADE, OmukadeEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(OMUKADE_PART, OmukadePartEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(OMUKADE_END, OmukadePartEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(DRAGON_PART, DragonPartEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(DRAGON_HEAD, DragonPartEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(DRAGON_PART_THIN, DragonPartEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(DRAGON_NECK, DragonPartEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(DRAGON_ARMS, DragonPartEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(DRAGON_PART_END, DragonPartEntity.createAttributes().build());
        FabricDefaultAttributeRegistry.register(DRAGON_TAIL, DragonPartEntity.createAttributes().build());

        // Register spawn placement rules so these monsters spawn like vanilla hostile mobs.
        // Different mappings expose the placement enum and predicate under different names; use reflection
        // to find the correct nested enum and functional interface at runtime to avoid compile errors.
        try {
            Class<?> spClass = SpawnPlacements.class;
            // Find an enum nested class that contains an ON_GROUND constant
            Class<?> placementEnum = null;
            for (Class<?> c : spClass.getDeclaredClasses()) {
                if (c.isEnum()) {
                    try {
                        Object onGroundConst = Enum.valueOf((Class<Enum>) c, "ON_GROUND");
                        if (onGroundConst != null) {
                            placementEnum = c;
                            break;
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            // Find a suitable register(Method) with 4 parameters
            java.lang.reflect.Method registerMethod = null;
            for (java.lang.reflect.Method m : spClass.getMethods()) {
                if (!m.getName().equals("register")) continue;
                if (m.getParameterCount() == 4) {
                    registerMethod = m;
                    break;
                }
            }

            if (placementEnum != null && registerMethod != null) {
                // Create proxy predicates for each entity that delegate to the typed check methods
                Class<?> predType = registerMethod.getParameterTypes()[3];

                java.lang.Object onGroundConst = Enum.valueOf((Class<Enum>) placementEnum, "ON_GROUND");

                java.lang.Object onikumaPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    // Expected args: (EntityType, LevelAccessor, MobSpawnType, BlockPos, RandomSource)
                    return OnikumaEntity.checkOnikumaSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                java.lang.Object sarugamiPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    return SarugamiEntity.checkSarugamiSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                java.lang.Object umiNyoboPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    return UmiNyoboEntity.checkUmiNyoboSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                java.lang.Object gakiPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    return GakiEntity.checkGakiSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                java.lang.Object kojinPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    try {
                        net.minecraft.world.level.LevelAccessor la = (net.minecraft.world.level.LevelAccessor) args[1];
                        net.minecraft.core.BlockPos bp = (net.minecraft.core.BlockPos) args[3];
                        net.minecraft.world.level.material.FluidState fs = la.getFluidState(bp);
                        net.minecraft.world.level.material.FluidState fsBelow = la.getFluidState(bp.below());
                        if (!fs.is(net.minecraft.tags.FluidTags.WATER)) return false;
                        if (!fsBelow.is(net.minecraft.tags.FluidTags.WATER)) return false;
                    } catch (Throwable t) { return false; }
                    return KojinEntity.checkKojinSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                java.lang.Object ningyoPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    try {
                        net.minecraft.world.level.LevelAccessor la = (net.minecraft.world.level.LevelAccessor) args[1];
                        net.minecraft.core.BlockPos bp = (net.minecraft.core.BlockPos) args[3];
                        net.minecraft.world.level.material.FluidState fs = la.getFluidState(bp);
                        net.minecraft.world.level.material.FluidState fsBelow = la.getFluidState(bp.below());
                        if (!fs.is(net.minecraft.tags.FluidTags.WATER)) return false;
                        if (!fsBelow.is(net.minecraft.tags.FluidTags.WATER)) return false;
                    } catch (Throwable t) { return false; }
                    return NingyoEntity.checkNingyoSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                java.lang.Object kamiikeHimePred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    try {
                        net.minecraft.world.level.LevelAccessor la = (net.minecraft.world.level.LevelAccessor) args[1];
                        net.minecraft.core.BlockPos bp = (net.minecraft.core.BlockPos) args[3];
                        net.minecraft.world.level.material.FluidState fs = la.getFluidState(bp);
                        net.minecraft.world.level.material.FluidState fsBelow = la.getFluidState(bp.below());
                        if (!fs.is(net.minecraft.tags.FluidTags.WATER)) return false;
                        if (!fsBelow.is(net.minecraft.tags.FluidTags.WATER)) return false;
                    } catch (Throwable t) { return false; }
                    return KamiikeHimeEntity.checkKamiikeHimeSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                java.lang.Object akugyoPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    try {
                        net.minecraft.world.level.LevelAccessor la = (net.minecraft.world.level.LevelAccessor) args[1];
                        net.minecraft.core.BlockPos bp = (net.minecraft.core.BlockPos) args[3];
                        net.minecraft.world.level.material.FluidState fs = la.getFluidState(bp);
                        net.minecraft.world.level.material.FluidState fsBelow = la.getFluidState(bp.below());
                        if (!fs.is(net.minecraft.tags.FluidTags.WATER)) return false;
                        if (!fsBelow.is(net.minecraft.tags.FluidTags.WATER)) return false;
                    } catch (Throwable t) { return false; }
                    return AkugyoEntity.checkAkugyoSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                java.lang.Object umiinuPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    try {
                        net.minecraft.world.level.LevelAccessor la = (net.minecraft.world.level.LevelAccessor) args[1];
                        net.minecraft.core.BlockPos bp = (net.minecraft.core.BlockPos) args[3];
                        net.minecraft.world.level.material.FluidState fs = la.getFluidState(bp);
                        net.minecraft.world.level.material.FluidState fsBelow = la.getFluidState(bp.below());
                        if (!fs.is(net.minecraft.tags.FluidTags.WATER)) return false;
                        if (!fsBelow.is(net.minecraft.tags.FluidTags.WATER)) return false;
                    } catch (Throwable t) { return false; }
                    return UmiInuEntity.checkUmiInuSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });
                
java.lang.Object ikuchiPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
    try {
        net.minecraft.world.level.LevelAccessor la = (net.minecraft.world.level.LevelAccessor) args[1];
        net.minecraft.core.BlockPos bp = (net.minecraft.core.BlockPos) args[3];
        if (!la.getFluidState(bp).is(net.minecraft.tags.FluidTags.WATER)) return false;
        if (!la.getFluidState(bp.below()).is(net.minecraft.tags.FluidTags.WATER)) return false;
    } catch (Throwable t) { return false; }
    return IkuchiEntity.checkIkuchiSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
});
java.lang.Object umiBozuPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
    try {
        net.minecraft.world.level.LevelAccessor la = (net.minecraft.world.level.LevelAccessor) args[1];
        net.minecraft.core.BlockPos bp = (net.minecraft.core.BlockPos) args[3];
        if (!la.getFluidState(bp).is(net.minecraft.tags.FluidTags.WATER)) return false;
        if (!la.getFluidState(bp.below()).is(net.minecraft.tags.FluidTags.WATER)) return false;
    } catch (Throwable t) { return false; }
    return UmiBozuEntity.checkUmiBozuSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
});


                java.lang.Object hitotsumePred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    return HitotsumeNyudoEntity.checkHitotsumeNyudoSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                java.lang.Object cranePred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    return RedCrownedCraneEntity.checkRedCrownedCraneSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                java.lang.Object omukadePred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    try {
                        net.minecraft.world.level.LevelAccessor la = (net.minecraft.world.level.LevelAccessor) args[1];
                        net.minecraft.core.BlockPos bp = (net.minecraft.core.BlockPos) args[3];
                        net.minecraft.world.level.block.state.BlockState ground = la.getBlockState(bp.below());
                        if (ground.isAir()) return false;
                        if (!ground.getFluidState().isEmpty()) return false;
                        if (ground.getCollisionShape(la, bp.below()).isEmpty()) return false;
                    } catch (Throwable t) { return false; }
                    return net.minecraft.world.entity.Mob.checkMobSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                    java.lang.Object crowPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                        return com.shioh.sengoku.entity.CrowEntity.checkCrowSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                    });

                    java.lang.Object macaquePred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                        return com.shioh.sengoku.entity.MacaqueEntity.checkMacaqueSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                    });

                java.lang.Object goryoPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    return GoryoEntity.checkGoryoSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                java.lang.Object shiryoPred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    return ShiryoEntity.checkShiryoSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                java.lang.Object oniBrutePred = java.lang.reflect.Proxy.newProxyInstance(predType.getClassLoader(), new Class[]{predType}, (proxy, method, args) -> {
                    return OniBruteEntity.checkOniBruteSpawnRules((EntityType) args[0], (net.minecraft.world.level.LevelAccessor) args[1], (net.minecraft.world.entity.MobSpawnType) args[2], (net.minecraft.core.BlockPos) args[3], (net.minecraft.util.RandomSource) args[4]);
                });

                // Invoke register for each entity
                registerMethod.invoke(null, ONIKUMA, onGroundConst, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, onikumaPred);
                registerMethod.invoke(null, UMI_NYOBO, onGroundConst, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, umiNyoboPred);
                registerMethod.invoke(null, GAKI, onGroundConst, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, gakiPred);
                registerMethod.invoke(null, SARUGAMI, onGroundConst, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, sarugamiPred);
                registerMethod.invoke(null, HITOTSUME_NYUDO, onGroundConst, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, hitotsumePred);
                registerMethod.invoke(null, RED_CROWNED_CRANE, onGroundConst, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cranePred);
                registerMethod.invoke(null, OMUKADE, onGroundConst, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, omukadePred);
                    // Crow and Macaque: creature-type ground spawns
                    registerMethod.invoke(null, CROW, onGroundConst, Heightmap.Types.MOTION_BLOCKING, crowPred);
                    registerMethod.invoke(null, MACAQUE, onGroundConst, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, macaquePred);
                // Goryo: Nether-only, ground spawn
                registerMethod.invoke(null, GORYO, onGroundConst, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, goryoPred);
                registerMethod.invoke(null, SHIRYO, onGroundConst, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, shiryoPred);
                // Kojin + Ningyo: aquatic/drowned-like spawn placement (IN_WATER if available)
                java.lang.Object inWaterConst = null;
                try {
                    // Require exact IN_WATER constant. If it's not present, skip aquatic registrations
                    inWaterConst = Enum.valueOf((Class<Enum>) placementEnum, "IN_WATER");
                } catch (IllegalArgumentException e) {
                    sengokuFabric.LOGGER.warn("IN_WATER placement enum not found; skipping aquatic spawn registrations (KOJIN,NINGYO,KAMIIKE_HIME,AKUGYO)");
                    inWaterConst = null;
                }

                // Only register true aquatic spawn placements if IN_WATER is available
                if (inWaterConst != null) {
                    registerMethod.invoke(null, KOJIN, inWaterConst, Heightmap.Types.OCEAN_FLOOR, kojinPred);
                    registerMethod.invoke(null, NINGYO, inWaterConst, Heightmap.Types.OCEAN_FLOOR, ningyoPred);
                    // Kamiike Hime: aquatic/guardian-like spawn placement (IN_WATER)
                    registerMethod.invoke(null, KAMIIKE_HIME, inWaterConst, Heightmap.Types.OCEAN_FLOOR, kamiikeHimePred);
                    // Akugyo: aquatic/elder guardian-like spawn placement (IN_WATER)
                    registerMethod.invoke(null, AKUGYO, inWaterConst, Heightmap.Types.OCEAN_FLOOR, akugyoPred);
                    registerMethod.invoke(null, IKUCHI, inWaterConst, Heightmap.Types.OCEAN_FLOOR, ikuchiPred);
                    registerMethod.invoke(null, UMI_INU, inWaterConst, Heightmap.Types.OCEAN_FLOOR, umiinuPred);
                    registerMethod.invoke(null, UMI_BOZU, inWaterConst, Heightmap.Types.OCEAN_FLOOR, umiBozuPred);
                } else {
                    // If IN_WATER isn't available, avoid registering as aquatic to prevent land spawns
                    sengokuFabric.LOGGER.warn("Skipped aquatic spawn placement registration for KOJIN, NINGYO, KAMIIKE_HIME, AKUGYO due to missing IN_WATER");
                }
                // Oni Brute: standard hostile ground spawn
                registerMethod.invoke(null, ONI_BRUTE, onGroundConst, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, oniBrutePred);
            }
        } catch (Throwable t) {
            sengokuFabric.LOGGER.warn("Failed to register spawn placements reflectively", t);
        }

        sengokuFabric.LOGGER.info("Registered all clan entities");

    }
}
