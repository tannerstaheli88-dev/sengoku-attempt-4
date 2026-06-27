package com.shioh.sengoku;

import com.shioh.sengoku.commands.SpawnPatrolCommand;
import com.shioh.sengoku.commands.SpawnNightZombiePatrolCommand;
import com.shioh.sengoku.commands.SpawnKuchisakeOnnaCommand;
import com.shioh.sengoku.entity.ai.BanditPatrolSpawner;
import com.shioh.sengoku.entity.ai.NightZombiePatrolSpawner;
import com.shioh.sengoku.entity.ai.ShinobiPatrolSpawner;
import com.shioh.sengoku.fabric.TickHandler;
import com.shioh.sengoku.fabric.FabricEventManager;
import com.shioh.sengoku.fabric.HorsePatrolManager;
import com.shioh.sengoku.system.BloodSprayManager;
import com.shioh.sengoku.registry.CreativeModeTabHandler;
import com.shioh.sengoku.registry.FabricLootTableModifier;
import com.shioh.sengoku.registry.ModBlockEntities;
import com.shioh.sengoku.registry.SoundRegistry;
import com.shioh.sengoku.init.POIReplacer;
import com.shioh.sengoku.init.TansuBlockReg;
import com.shioh.sengoku.init.TansuItemReg;
import com.shioh.sengoku.init.SakeItemReg;
import com.shioh.sengoku.init.ThrowableArrowReg;
import com.shioh.sengoku.init.KenseiItemReg;
import com.shioh.sengoku.init.DaimyoArmorMaterialReg;
import com.shioh.sengoku.init.ShinobiItemReg;
import com.shioh.sengoku.init.HatItemReg;
import com.shioh.sengoku.init.HatArmorMaterialReg;
import com.shioh.sengoku.init.HatItemReg;
import com.shioh.sengoku.init.ShinobiArmorMaterialReg;
import com.shioh.sengoku.init.IronSandReg;
import com.shioh.sengoku.init.TamahaganeItemReg;
import com.shioh.sengoku.init.ZenGardenReg;
// import com.shioh.sengoku.init.KunaiItemReg;
import com.shioh.sengoku.registry.ModMenuTypes;
import com.shioh.sengoku.registry.ModEntities;
import com.shioh.sengoku.worldgen.StructureExclusionZoneExample;
import com.shioh.sengoku.registry.ModSpawnEggs;
import com.shioh.sengoku.init.GrassBedBlockReg;
import com.shioh.sengoku.init.GrassBedItemInit;
import com.shioh.sengoku.materialpack.MaterialPackLoader;
import com.shioh.sengoku.registry.EnchantmentRegistry;
import com.shioh.sengoku.registry.WeaponRegistry;
import com.shioh.sengoku.registry.SengokuBlocks;
import com.shioh.sengoku.registry.helper.Reggie;
import com.shioh.sengoku.registry.helper.Reginald;
import com.shioh.sengoku.util.BedShapeState;
import com.shioh.sengoku.platform.FabricPlatformHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.sounds.SoundEvent;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class sengokuFabric implements ModInitializer {

    public static final String MODID = "sengoku";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    // Mod detection
    public static final boolean bettercombat_mod_loaded = FabricLoader.getInstance().isModLoaded("bettercombat");
    public static final boolean bronze_mod_loaded = FabricLoader.getInstance().isModLoaded("bronze");

    // Toggle to disable the chunk-load crane spawner for debugging/world-load hangs
    public static final boolean DISABLE_CHUNK_CRANE_SPAWNER = true;
    // Registrars
    public static final Reginald REGISTRARS = new Reginald();
    public static final Reggie<Item> ITEM_REGISTRAR = REGISTRARS.get(Registries.ITEM);

    @Override
    public void onInitialize() {
        LOGGER.info("=== SENGOKU MOD INITIALIZING ===");
        // Register networking payload codecs (S2C)
        try {
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.VillageMusicPayload.TYPE, com.shioh.sengoku.network.VillageMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.RaidMusicPayload.TYPE, com.shioh.sengoku.network.RaidMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.YukiOnnaMusicPayload.TYPE, com.shioh.sengoku.network.YukiOnnaMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.CastleMusicPayload.TYPE, com.shioh.sengoku.network.CastleMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.CastleCombatMusicPayload.TYPE, com.shioh.sengoku.network.CastleCombatMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.ShinobiMusicPayload.TYPE, com.shioh.sengoku.network.ShinobiMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.ShinobiLordMusicPayload.TYPE, com.shioh.sengoku.network.ShinobiLordMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.WarlordMusicPayload.TYPE, com.shioh.sengoku.network.WarlordMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.PatrolMusicPayload.TYPE, com.shioh.sengoku.network.PatrolMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.CombatBasicMusicPayload.TYPE, com.shioh.sengoku.network.CombatBasicMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.CombatYomiMusicPayload.TYPE, com.shioh.sengoku.network.CombatYomiMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.CombatRyuguMusicPayload.TYPE, com.shioh.sengoku.network.CombatRyuguMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.TatarigamiMusicPayload.TYPE, com.shioh.sengoku.network.TatarigamiMusicPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.WarmWaterConfigPayload.TYPE, com.shioh.sengoku.network.WarmWaterConfigPayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.PlayerPoisePayload.TYPE, com.shioh.sengoku.network.PlayerPoisePayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.DebugTogglePayload.TYPE, com.shioh.sengoku.network.DebugTogglePayload.CODEC);
            net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry.playS2C()
                .register(com.shioh.sengoku.network.DebugDataPayload.TYPE, com.shioh.sengoku.network.DebugDataPayload.CODEC);
            
        } catch (Throwable ignored) {}
        
        // --- Custom Effects ---
        com.shioh.sengoku.registry.ModEffects.initialize();
        
        // --- Custom Particles ---
        com.shioh.sengoku.particle.ModParticles.register();
        // Ensure custom particle types from ParticleRegistry are initialized
        try {
            com.shioh.sengoku.registry.ParticleRegistry.init();
        } catch (Throwable ignored) {}
        
        // --- Kunai Entity ---
        ModEntities.register();

        // Confirm that sengoku startup and spawn logging are enabled (helps verify mixins/load order)
        LOGGER.info("[sengoku] Startup complete — spawn debug logging enabled");
        // Make vanilla axolotls behave more like ambush predators by lowering
        // their base movement speed and follow range. This is a simple, safe
        // tweak: it doesn't alter goals, only the attribute defaults so axolotls
        // are less likely to chase and will move much slower.
        try {
            net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(
                net.minecraft.world.entity.EntityType.AXOLOTL,
                net.minecraft.world.entity.animal.axolotl.Axolotl.createAttributes()
                    .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.12D) // slower base speed (water)
                    .add(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE, 6.0D)
                    .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 4.0D)
                    .build()
            );
            LOGGER.info("Overrode Axolotl attributes: movement speed lowered for ambush behavior");
        } catch (Throwable ignored) {}
        ModSpawnEggs.register();
        // Register biome-based parrot/bird spawns using biome tags (similar to frogs/foxes/rabbits)
        try {
            // Use biome tags defined in resources (e.g. data/minecraft/tags/worldgen/biome/spawns_yellow_birds.json)
                net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
                    net.fabricmc.fabric.api.biome.v1.BiomeSelectors.tag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "spawns_yellow_birds"))),
                net.minecraft.world.entity.MobCategory.CREATURE,
                net.minecraft.world.entity.EntityType.PARROT,
                10, // weight
                1,  // min
                2   // max
            );

                net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
                    net.fabricmc.fabric.api.biome.v1.BiomeSelectors.tag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "spawns_red_blue_birds"))),
                net.minecraft.world.entity.MobCategory.CREATURE,
                net.minecraft.world.entity.EntityType.PARROT,
                8,
                1,
                2
            );

                net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
                    net.fabricmc.fabric.api.biome.v1.BiomeSelectors.tag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "spawns_grey_birds"))),
                net.minecraft.world.entity.MobCategory.CREATURE,
                net.minecraft.world.entity.EntityType.PARROT,
                6,
                1,
                1
            );

                net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
                    net.fabricmc.fabric.api.biome.v1.BiomeSelectors.tag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "spawns_green_birds"))),
                net.minecraft.world.entity.MobCategory.CREATURE,
                net.minecraft.world.entity.EntityType.PARROT,
                10,
                1,
                2
            );

                net.fabricmc.fabric.api.biome.v1.BiomeModifications.addSpawn(
                    net.fabricmc.fabric.api.biome.v1.BiomeSelectors.tag(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, ResourceLocation.fromNamespaceAndPath("minecraft", "spawns_blue_birds"))),
                net.minecraft.world.entity.MobCategory.CREATURE,
                net.minecraft.world.entity.EntityType.PARROT,
                7,
                1,
                2
            );
            LOGGER.info("Registered biome-tag-based parrot spawns (color-split)");
        } catch (Throwable ignored) {}
        // Register Goryo server-side spawner (code-driven, respects goryo_spawnable_on tag)
        try { com.shioh.sengoku.spawn.GoryoSpawner.register(); } catch (Throwable ignored) {}
        // Register Gaki server-side spawner (mirror of Goryo spawner)
        try { com.shioh.sengoku.spawn.GakiSpawner.register(); } catch (Throwable ignored) {}
        // Register Omukade patrol spawner (mostly underground, occasional surface patrols)
        try { com.shioh.sengoku.spawn.OmukadePatrolSpawner.register(); } catch (Throwable ignored) {}
        try { com.shioh.sengoku.spawn.IkuchiSpawner.register(); } catch (Throwable ignored) {}
        try { com.shioh.sengoku.spawn.UmiBozuSpawner.register(); } catch (Throwable ignored) {}
        try { com.shioh.sengoku.spawn.UmiInuSpawner.register(); } catch (Throwable ignored) {}
        // Akugyo spawner removed — no registration (file deleted)
        // Giant daytime despawn + Yuki Onna clear-weather despawn (respects PersistenceRequired)
        try { com.shioh.sengoku.event.SengokuDespawnHandler.register(); } catch (Throwable ignored) {}
        
        LOGGER.info("About to call registerRaidMembers()...");
        // Register entities for raids
        registerRaidMembers();
        LOGGER.info("Finished registerRaidMembers(), about to call registerRaidHorseSpawner()...");
        registerRaidHorseSpawner();
        LOGGER.info("Finished registerRaidHorseSpawner().");

        // Ensure Warlord spawns always have Daimyo armor and Kensei blade equipped (no drop — drops custom loot instead)
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            try {
                if (entity.getType() == com.shioh.sengoku.registry.ModEntities.WARLORD && entity instanceof net.minecraft.world.entity.Mob mob) {
                    // Equip diamond-tier set with 0% drop chance (custom loot drops via dropCustomDeathLoot)
                    mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(com.shioh.sengoku.init.KenseiItemReg.DAIMYO_KABUTO));
                    mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 0.0F);
                    mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(com.shioh.sengoku.init.KenseiItemReg.DAIMYO_DO));
                    mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.CHEST, 0.0F);
                    mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, new net.minecraft.world.item.ItemStack(com.shioh.sengoku.init.KenseiItemReg.DAIMYO_HAIDATE));
                    mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.LEGS, 0.0F);
                    mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, new net.minecraft.world.item.ItemStack(com.shioh.sengoku.init.KenseiItemReg.DAIMYO_KUSAZURI));
                    mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.FEET, 0.0F);
                    mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(com.shioh.sengoku.init.KenseiItemReg.BLADE_OF_THE_KENSEI));
                    mob.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0F);
                }
                // Ensure OniBrute sometimes spawns with a stone kanabo and is never a baby
                if (entity.getType() == com.shioh.sengoku.registry.ModEntities.ONI_BRUTE && entity instanceof net.minecraft.world.entity.Mob oniMob) {
                    try {
                        // Force adult
                        oniMob.setBaby(false);
                    } catch (Throwable ignored) {}
                    try {
                        java.util.Random r = new java.util.Random();
                        if (r.nextDouble() < 0.25D) {
                            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(com.shioh.sengoku.sengokuFabric.asId("stone_kanabo"));
                            if (item != null) {
                                oniMob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(item));
                                oniMob.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.5F);
                            }
                        }
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
        });
        
        // --- Throwable Kunai ---
        ThrowableArrowReg.registerItems();
        // Ensure warm water features are enabled on startup (restore after debugging)
        try {
            com.shioh.sengoku.config.SengokuConfig cfg = com.shioh.sengoku.config.SengokuConfig.getInstance();
            cfg.warmWaterEnabled = true;
            cfg.save();
            LOGGER.info("warmWaterEnabled set to true on startup");
        } catch (Throwable ignored) {}
        
        // --- Kensei Items (Daimyo armor & Blade of the Kensei) ---
        // Register the custom armor materials first so the items use the correct textures
        DaimyoArmorMaterialReg.register();
        com.shioh.sengoku.init.DaimyoNetheriteArmorMaterialReg.register();
        KenseiItemReg.registerItems();
        
        // --- Shinobi Items (Shinobi armor for Illusioners) ---
        ShinobiArmorMaterialReg.register();
        ShinobiItemReg.registerItems();
        // Hats (straw, sando, tengai) - register armor material first for custom texture layer
        HatArmorMaterialReg.register();
        HatItemReg.registerItems();
        
        // --- Iron Sand & Tamahagane ---
        IronSandReg.registerBlocks();
        IronSandReg.registerItems();
        ZenGardenReg.registerBlocks();
        ZenGardenReg.registerItems();
        TamahaganeItemReg.registerItems();
        
        // --- Seigun Ingredient Items ---
        com.shioh.sengoku.init.SeigunItemReg.registerItems();
        
        // --- Grass Bed ---
        GrassBedBlockReg.registerBedBlocks();
        GrassBedItemInit.registerBedItems();
        ModBlockEntities.register();
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> {
            BedShapeState.needsToBeChecked = true;
            BedShapeState.silent = false;
        });
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            FabricPlatformHelper.setCurrentMinecraftServer(server);
            if (server.isDedicatedServer()) {
                BedShapeState.ServerConfig.checkServerConfig();
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            FabricPlatformHelper.setCurrentMinecraftServer(server);
            BedShapeState.needsToBeChecked = true;
            BedShapeState.silent = false;
            // Register horse patrol manager on server start so it runs on the server thread
            com.shioh.sengoku.fabric.HorsePatrolManager.register();
            // Register the blood spray manager to handle multi-tick death sprays
            // TEMPORARILY DISABLED: suspected cause of /kill freeze
            // BloodSprayManager.register();
        });
        
        // Register bandit patrol spawner
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    BanditPatrolSpawner.INSTANCE.tick(level, true, false);
                }
            });
        });

        // Register one-time chance crane spawn when a chunk loads.
        if (!DISABLE_CHUNK_CRANE_SPAWNER) {
            try {
                net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents.CHUNK_LOAD.register((serverLevel, chunk) -> {
                try {
                    if (!serverLevel.dimensionType().natural()) return;
                    net.minecraft.util.RandomSource rand = serverLevel.getRandom();
                    // 12.5% chance per chunk load to attempt a spawn
                    if (rand.nextDouble() > 0.125) return;

                    // Pick a random position inside the chunk at Y=62
                    int baseX = chunk.getPos().getMinBlockX();
                    int baseZ = chunk.getPos().getMinBlockZ();
                    int x = baseX + rand.nextInt(16);
                    int z = baseZ + rand.nextInt(16);
                    int y = 62;
                    net.minecraft.core.BlockPos spawnPos = new net.minecraft.core.BlockPos(x, y, z);

                    if (!serverLevel.isLoaded(spawnPos)) return;

                    // Check block tag
                    try {
                        net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.ResourceLocation.parse("sengoku:red_crowned_crane_spawnable_on"));
                        net.minecraft.world.level.block.state.BlockState ground = serverLevel.getBlockState(spawnPos);
                        if (!ground.is(tag)) return;
                    } catch (Throwable ignored) { return; }

                    // Require air above
                    if (!serverLevel.getBlockState(spawnPos.above()).isAir()) return;

                    // Spawn the crane
                    var ent = com.shioh.sengoku.registry.ModEntities.RED_CROWNED_CRANE.create(serverLevel);
                    if (!(ent instanceof net.minecraft.world.entity.Mob mob)) return;
                    mob.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, (float)(rand.nextDouble() * 360.0), 0.0F);
                    mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), net.minecraft.world.entity.MobSpawnType.NATURAL, null);
                    serverLevel.addFreshEntity(mob);
                } catch (Throwable ignored) {}
            });
            } catch (Throwable ignored) {}
        }
        
        // Register ravager surface spawner
        com.shioh.sengoku.event.RavagerSurfaceSpawnHandler.register();

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            FabricPlatformHelper.clearCurrentMinecraftServer();
        });

    // Register rare Yuki Onna patrol spawner (snowy biomes)
    net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
        server.getAllLevels().forEach(level -> {
            if (level.dimensionType().natural()) {
                com.shioh.sengoku.entity.ai.YukiOnnaPatrolSpawner.INSTANCE.tick(level, true, false);
            }
        });
    });

    // Despawn Yuki Onna immediately when the weather clears (not raining/thundering)
    net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
        server.getAllLevels().forEach(level -> {
            try {
                if (!level.dimensionType().natural()) return;
                if (level.isRaining() || level.isThundering()) return;
                // Remove any existing Yuki Onna instances when weather is clear
                // Scan around each player and clear nearby Yuki Onna within 256 blocks
                level.players().forEach(p -> {
                    try {
                        var box = new net.minecraft.world.phys.AABB(p.blockPosition()).inflate(256.0D);
                        var list = level.getEntities((net.minecraft.world.entity.Entity) null, box,
                            ent -> ent.getType() == com.shioh.sengoku.registry.ModEntities.YUKI_ONNA
                                && (!(ent instanceof net.minecraft.world.entity.Mob mob) || !mob.isPersistenceRequired()));
                        for (var ent : list) {
                            try { ent.discard(); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}
                });
            } catch (Throwable ignored) {}
        });
    });
        
        // Register clan patrol spawner
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    com.shioh.sengoku.entity.ai.ClanPatrolSpawner.INSTANCE.tick(level, true, false);
                }
            });
        });

        // Register night zombie patrol spawner
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    NightZombiePatrolSpawner.INSTANCE.tick(level, true, false);
                }
            });
        });

        // Register kuchisaka onna patrol spawner (rarer than night oni patrol)
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    com.shioh.sengoku.entity.ai.KuchisakaOnnaPatrolSpawner.INSTANCE.tick(level, true, false);
                }
            });
        });

        // Register shinobi patrol spawner
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    ShinobiPatrolSpawner.INSTANCE.tick(level, true, false);
                }
            });
        });
        
        // Register mist weather system
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    com.shioh.sengoku.system.MistWeatherSystem.serverTick(level);
                }
            });
        });

        // Ensure mist state never leaks between world/server sessions.
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register(
            server -> com.shioh.sengoku.system.MistWeatherSystem.clearAll()
        );
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(
            server -> com.shioh.sengoku.system.MistWeatherSystem.clearAll()
        );

        // --- Village music structure sync (server -> client) ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    com.shioh.sengoku.network.VillageMusicSync.serverTick(level);
                    com.shioh.sengoku.network.WarmWaterConfigSync.serverTick(level);
                }
            });
        });

        // --- Castle music structure sync (server -> client) ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    com.shioh.sengoku.network.CastleMusicSync.serverTick(level);
                }
            });
        });

        // --- Castle combat music sync (server -> client) ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    com.shioh.sengoku.network.CastleCombatMusicSync.serverTick(level);
                }
            });
        });

        // --- Raid activity sync (server -> client) ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    com.shioh.sengoku.network.RaidMusicSync.serverTick(level);
                }
            });
        });

        // --- Yuki Onna aggro/stalk sync (server -> client) ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    com.shioh.sengoku.network.YukiOnnaMusicSync.serverTick(level);
                }
            });
        });

        // --- Shinobi (Illusioner aggro) music sync (server -> client) ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    com.shioh.sengoku.network.ShinobiMusicSync.serverTick(level);
                }
            });
        });

        // --- Warlord (boss) aggro music sync (server -> client) ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    com.shioh.sengoku.network.WarlordMusicSync.serverTick(level);
                }
            });
        });

        // --- Tatarigami (Wither) boss music sync (server -> client) ---
        // Run in all dimensions so Wither fights in the Nether/End are detected as well.
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                try {
                    com.shioh.sengoku.network.TatarigamiMusicSync.serverTick(level);
                } catch (Throwable ignored) {}
            });
        });

        // Ender Dragon music is driven by client-side boss-bar detection; no server scanner needed

        // --- Patrol (bandit/clan) aggro music sync (server -> client) ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    com.shioh.sengoku.network.PatrolMusicSync.serverTick(level);
                }
            });
        });

        // --- Basic combat music sync (server -> client) ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    com.shioh.sengoku.network.CombatBasicMusicSync.serverTick(level);
                }
            });
        });

        // --- Yomi (Nether) combat music sync (server -> client) ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimension() == net.minecraft.world.level.Level.NETHER) {
                    com.shioh.sengoku.network.CombatYomiMusicSync.serverTick(level);
                }
            });
        });

        // --- Ryugu (End) combat music sync (server -> client) ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimension() == net.minecraft.world.level.Level.END) {
                    com.shioh.sengoku.network.CombatRyuguMusicSync.serverTick(level);
                }
            });
        });

        

        // --- Pig retaliation manager: handle transient pig retaliation + ally alerts ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                if (level.dimensionType().natural()) {
                    try { com.shioh.sengoku.network.PigRetaliationManager.serverTick(level); } catch (Throwable ignored) {}
                }
            });
        });

        // --- Coral protection in the End: periodically revive dead coral near players ---
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            server.getAllLevels().forEach(level -> {
                try {
                    if (level.dimension() == net.minecraft.world.level.Level.END) {
                        com.shioh.sengoku.system.CoralProtectionSystem.serverTick((net.minecraft.server.level.ServerLevel) level);
                    }
                } catch (Throwable ignored) {}
            });
        });

        // --- Commands ---
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });

        // --- Common Initialization merged here ---
        MaterialPackLoader.loadPacks();

        SengokuBlocks.init();
        // Register custom tree decorators that rely on mod blocks
        try { com.shioh.sengoku.registry.SengokuTreeDecorators.register(); } catch (Throwable ignored) {}
        SoundRegistry.init();
        WeaponRegistry.init();
        EnchantmentRegistry.init();

        REGISTRARS.registerAll();

        // Optionally log furnace fuel registration warning if needed
        // if (!Fabric-specific logic for furnace fuels) { LOGGER.info(...); }

        if (FabricLoader.getInstance().isModLoaded(MODID)) {
            LOGGER.info("- Basic Weapons Loaded -");
        }

        // --- Tansu Blocks & Items ---
        TansuBlockReg.registerBlocks();
    TansuItemReg.registerItems();
    // Register Sake-related items and menu types
    SakeItemReg.registerItems();
    ModMenuTypes.register();
    
    // --- Kunai Items & Entities (DISABLED - using throwable arrows instead) ---
    // ModEntities.register();
    // KunaiItemReg.registerItems();
        // Load mod-packaged sake recipes (simple JSON loader)
        com.shioh.sengoku.recipes.ModSakeBrewingRecipes.load();



        // --- POI Replacement ---
        POIReplacer.replaceFishermanPOI(TansuBlockReg.FISHING_NET);

        // --- Event Handlers & Misc ---
        FabricEventManager.init();
        
        // --- Villager Trading System ---
        try {
            com.shioh.sengoku.villager.VillagerTradeLoader.init();
            LOGGER.info("Villager trading system initialized successfully");
        } catch (Throwable e) {
            LOGGER.warn("Failed to initialize villager trading system", e);
        }

        // Intercept right-clicks on beehives to give paper instead of honey and disable bottle use.
        try {
            net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
                try {
                    if (world == null || hitResult == null) return net.minecraft.world.InteractionResult.PASS;
                    net.minecraft.core.BlockPos pos = hitResult.getBlockPos();
                    net.minecraft.world.level.block.state.BlockState state = world.getBlockState(pos);
                    if (!(state.getBlock() instanceof net.minecraft.world.level.block.BeehiveBlock)) return net.minecraft.world.InteractionResult.PASS;

                    net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
                    if (stack == null) return net.minecraft.world.InteractionResult.PASS;

                    // Block glass bottle -> honey bottle behavior
                    if (stack.getItem() == net.minecraft.world.item.Items.GLASS_BOTTLE) {
                        return net.minecraft.world.InteractionResult.FAIL;
                    }

                    if (stack.getItem() == net.minecraft.world.item.Items.SHEARS) {
                        int honey = 0;
                        try {
                            honey = state.getValue(net.minecraft.world.level.block.BeehiveBlock.HONEY_LEVEL);
                        } catch (Exception e) {
                            return net.minecraft.world.InteractionResult.PASS;
                        }

                        if (honey >= 5) {
                            if (!world.isClientSide) {
                                net.minecraft.world.item.ItemStack drop = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PAPER, 3);
                                net.minecraft.world.entity.item.ItemEntity ent = new net.minecraft.world.entity.item.ItemEntity(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, drop);
                                world.addFreshEntity(ent);
                                stack.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));
                                world.setBlock(pos, state.setValue(net.minecraft.world.level.block.BeehiveBlock.HONEY_LEVEL, 0), 3);
                            }

                            return net.minecraft.world.InteractionResult.sidedSuccess(world.isClientSide);
                        }
                    }
                } catch (Throwable ignored) {}

                return net.minecraft.world.InteractionResult.PASS;
            });
        } catch (Throwable ignored) {}
        FabricLootTableModifier.init();
        CreativeModeTabHandler.buildContents();
        TickHandler.register();
        // HorsePatrolManager will be registered when the dedicated server starts
        
        // === DATAPACK PERFORMANCE OPTIMIZATIONS ===
        LOGGER.info("Registering datapack performance optimizations...");
        
        // Optimize mob effect applications (replaces tick.mcfunction lines 148-154)
        com.shioh.sengoku.system.MobEffectOptimizer.register();
        LOGGER.info("✓ Mob effect optimizer registered (expected +5-10 FPS)");
        
        // Particle spawning reverted to datapack (remove_flame_particle.mcfunction)
        // com.shioh.sengoku.system.ParticleOptimizer.register();
        // LOGGER.info("✓ Particle optimizer registered (expected +15-25 FPS)");
        
        // Optimize baby zombie mounting (replaces tick2.mcfunction lines 1-14)
        com.shioh.sengoku.system.BabyZombieMountOptimizer.register();
        LOGGER.info("✓ Baby zombie mount optimizer registered (expected +5-10 FPS)");

        // --- Custom Structure Types ---
        com.shioh.sengoku.worldgen.ModStructureTypes.init();

        // --- Custom Structure Pool Elements ---
        com.shioh.sengoku.worldgen.ModStructurePoolElements.init();

        // --- Multiple Exclusion Zones for Structures ---
        StructureExclusionZoneExample.register();

        // --- Composter & Villager Configuration ---
        registerComposterItems();
        registerVillagerPlantableItems();
        // Register mappings so custom saplings can be potted
        registerPottedPlants();

        // Report DoorAI debug toggle at startup for easier troubleshooting
        boolean doorAiDebug = false;
        try {
            String v = System.getProperty("sengoku.debug.doorai", "");
            if (!v.isEmpty()) {
                doorAiDebug = v.equalsIgnoreCase("true") || v.equals("1");
            } else {
                doorAiDebug = FabricLoader.getInstance().isDevelopmentEnvironment();
            }
        } catch (Throwable ignored) {}
        LOGGER.info("[Sengoku] DoorAI debug enabled: {}", doorAiDebug);
    }

    // Helper method for resource IDs
    public static ResourceLocation asId(String path) {
        return ResourceLocation.parse(MODID + ":" + path);
    }

    // Composter and Villager Configuration
    private void registerComposterItems() {
        // Register tea and ramie crops/seeds as compostable
        // Composter values: 0.3 = 30% chance, 0.5 = 50% chance, 0.65 = 65% chance, 0.85 = 85% chance, 1.0 = 100% chance
        
        net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE.add(TansuItemReg.TEA_LEAF, 0.3f);
        net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE.add(TansuItemReg.TEA_SEEDS, 0.3f);
        net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE.add(TansuItemReg.RAMIE_I, 0.3f);
        net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE.add(TansuItemReg.RAMIE_FIBER, 0.5f);
        
        // Rice is already compostable since RiceItem extends ItemNameBlockItem
        // But we can ensure it's registered properly
        net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE.add(TansuItemReg.RICE_I, 0.5f);
        
        LOGGER.info("Registered tea, ramie, and rice items for composters");
        // Allow vanilla honeycomb to be composted (30% base chance)
        net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE.add(net.minecraft.world.item.Items.HONEYCOMB, 0.3f);
        LOGGER.info("Registered vanilla honeycomb for composter (30% chance)");
        // Allow Bloodgood and Weeping Willow leaves to be composted (30% base chance)
        try {
            net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE.add(SengokuBlocks.BLOODGOOD_LEAVES.asItem(), 0.3f);
            net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE.add(SengokuBlocks.WEEPING_WILLOW_LEAVES.asItem(), 0.3f);
            LOGGER.info("Registered custom leaves for composter (Bloodgood & Weeping Willow)");
        } catch (Throwable ignored) {}
        // Register custom saplings as compostable (30% chance)
        try {
            net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE.add(SengokuBlocks.MAPLE_SAPLING.asItem(), 0.3f);
            net.fabricmc.fabric.api.registry.CompostingChanceRegistry.INSTANCE.add(SengokuBlocks.GINKGO_SAPLING.asItem(), 0.3f);
            LOGGER.info("Registered custom saplings for composter (Maple & Ginkgo)");
        } catch (Throwable ignored) {}
        
    }

    private void registerVillagerPlantableItems() {
        // Register tea and ramie crops for farmer villagers
        // Note: Rice cannot be planted by villagers because it requires water/mud, not farmland
        
        // Make items collectable by farmer villagers (they'll pick them up)
        net.fabricmc.fabric.api.registry.VillagerInteractionRegistries.registerCollectable(TansuItemReg.TEA_LEAF);
        net.fabricmc.fabric.api.registry.VillagerInteractionRegistries.registerCollectable(TansuItemReg.TEA_SEEDS);
        net.fabricmc.fabric.api.registry.VillagerInteractionRegistries.registerCollectable(TansuItemReg.RAMIE_I);
        net.fabricmc.fabric.api.registry.VillagerInteractionRegistries.registerCollectable(TansuItemReg.RAMIE_FIBER);
        
        // Register as food so villagers know these are valuable crop items
        // The value determines how much villagers "want" the item (higher = more desirable)
        net.fabricmc.fabric.api.registry.VillagerInteractionRegistries.registerFood(TansuItemReg.TEA_SEEDS, 4);
        net.fabricmc.fabric.api.registry.VillagerInteractionRegistries.registerFood(TansuItemReg.RAMIE_I, 4);
        
        // Rice is waterlogged and planted on mud/dirt, not farmland
        // Villagers cannot plant rice due to its special planting requirements
        // But they can still pick it up and compost it
        net.fabricmc.fabric.api.registry.VillagerInteractionRegistries.registerCollectable(TansuItemReg.RICE_I);
        net.fabricmc.fabric.api.registry.VillagerInteractionRegistries.registerFood(TansuItemReg.RICE_I, 4);
        
        LOGGER.info("Registered tea and ramie for villager farming (rice requires manual planting)");
    }

    private void registerPottedPlants() {
        try {
            // Compostable saplings already handled elsewhere; ensure saplings can be placed in flower pots
            // Attempt reflective registration to handle different mappings/versions
            try {
                Class<?> fpClass = net.minecraft.world.level.block.FlowerPotBlock.class;
                java.lang.reflect.Method[] methods = fpClass.getDeclaredMethods();
                for (java.lang.reflect.Method m : methods) {
                    if (!m.getName().equals("registerPlant")) continue;
                    m.setAccessible(true);
                    try {
                        // try (Block, Block)
                        m.invoke(null, com.shioh.sengoku.registry.SengokuBlocks.MAPLE_SAPLING, com.shioh.sengoku.registry.SengokuBlocks.POTTED_MAPLE_SAPLING);
                        m.invoke(null, com.shioh.sengoku.registry.SengokuBlocks.GINKGO_SAPLING, com.shioh.sengoku.registry.SengokuBlocks.POTTED_GINKGO_SAPLING);
                        LOGGER.info("Registered potted saplings for maple and ginkgo via reflection (Block,Block)");
                        break;
                    } catch (Throwable ignore1) {}
                    try {
                        // try (ResourceLocation, Block)
                        m.invoke(null, sengokuFabric.asId("maple_sapling"), com.shioh.sengoku.registry.SengokuBlocks.POTTED_MAPLE_SAPLING);
                        m.invoke(null, sengokuFabric.asId("ginkgo_sapling"), com.shioh.sengoku.registry.SengokuBlocks.POTTED_GINKGO_SAPLING);
                        LOGGER.info("Registered potted saplings for maple and ginkgo via reflection (ResourceLocation,Block)");
                        break;
                    } catch (Throwable ignore2) {}
                }
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }

    // Commands
    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Spawn patrol commands
        SpawnPatrolCommand.register(dispatcher);
        // Sengoku debug overlay command
        com.shioh.sengoku.commands.SengokuDebugCommand.register(dispatcher);
        // Night zombie patrol debug command
        SpawnNightZombiePatrolCommand.register(dispatcher);
        com.shioh.sengoku.commands.SpawnKuchisakeOnnaCommand.register(dispatcher);
        com.shioh.sengoku.commands.SpawnClanPatrolCommand.register(dispatcher);
        
        // Mist weather command
        com.shioh.sengoku.commands.MistWeatherCommand.register(dispatcher);
        
        // Yuki Onna debug spawn command
        com.shioh.sengoku.commands.SpawnYukiOnnaCommand.register(dispatcher);
        // Yuki Onna timer reset command
        com.shioh.sengoku.commands.ResetYukiOnnaCommand.register(dispatcher);
        
        // Bed shape command
        dispatcher.register(
            Commands.literal("setbedshape")
                .requires(source -> source.hasPermission(2) && source.getServer().isDedicatedServer())
                .executes(context -> {
                    context.getSource().sendFailure(Component.translatable("commands.setbedshape.warn"));
                    return -1;
                })
                .then(Commands.literal("vanilla")
                    .executes(context -> {
                        BedShapeState.isPillowedPackActive = false;
                        BedShapeState.isPillowedConnectedPackActive = false;
                        BedShapeState.ServerConfig.writeServerConfig();
                        BedShapeState.needsToBeChecked = false;
                        context.getSource().sendSuccess(() -> Component.translatable("commands.setbedshape.vanilla"), true);
                        return 0;
                    })
                )
                .then(Commands.literal("pillowed")
                    .executes(context -> {
                        context.getSource().sendFailure(Component.translatable("commands.setbedshape.pillowed.warn"));
                        return -1;
                    })
                    .then(Commands.literal("connected")
                        .executes(context -> {
                            BedShapeState.isPillowedConnectedPackActive = true;
                            BedShapeState.isPillowedPackActive = false;
                            BedShapeState.ServerConfig.writeServerConfig();
                            BedShapeState.needsToBeChecked = false;
                            context.getSource().sendSuccess(() -> Component.translatable("commands.setbedshape.pillowed.connected"), true);
                            return 2;
                        })
                    )
                    .then(Commands.literal("unconnected")
                        .executes(context -> {
                            BedShapeState.isPillowedConnectedPackActive = false;
                            BedShapeState.isPillowedPackActive = true;
                            BedShapeState.ServerConfig.writeServerConfig();
                            BedShapeState.needsToBeChecked = false;
                            context.getSource().sendSuccess(() -> Component.translatable("commands.setbedshape.pillowed.unconnected"), true);
                            return 1;
                        })
                    )
                )
        );
    }
    
    private void registerRaidMembers() {
        // NOTE: In Minecraft 1.21.1, the raid system uses hardcoded RaiderType enums.
        // There is NO WAY to add custom raiders to official raids without complex Mixin/ASM bytecode manipulation.
        // 
        // The raid wave counts are stored INSIDE the RaiderType enum values themselves, not in a Map.
        // Since we can't extend or modify enums at runtime, we cannot integrate our entities into the vanilla raid system.
        //
        // ALTERNATIVE APPROACHES:
        // 1. Create custom "bandit raid" events separate from village raids
        // 2. Spawn bandits/ronin naturally near villages (already implemented via patrols)
        // 3. Use commands to manually spawn raid-like scenarios
        //
        // The horse spawning system below WILL work for any bandits/ronin that do spawn during raids
        // (if spawned manually via commands or other means), but they won't spawn automatically.
        
        LOGGER.info("Skipping raid registration - Minecraft 1.21.1 uses hardcoded RaiderType enum");
        LOGGER.info("Bandits/Ronin can be spawned manually during raids for testing");
    }
    

    private void registerRaidHorseSpawner() {
        // Listen for entity join world events to spawn horses for bandits/ronin during raids
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            // Check if this is a bandit or ronin that just spawned
            if (entity instanceof com.shioh.sengoku.entity.BanditEntity || 
                entity instanceof com.shioh.sengoku.entity.RoninEntity) {
                
                net.minecraft.world.entity.Mob raider = (net.minecraft.world.entity.Mob) entity;
                
                // Check if this entity is part of a raid
                if (raider instanceof net.minecraft.world.entity.raid.Raider) {
                    net.minecraft.world.entity.raid.Raider raiderEntity = (net.minecraft.world.entity.raid.Raider) raider;
                    if (raiderEntity.hasActiveRaid()) {
                        // 60% chance to spawn on a horse during raids
                        if (world.getRandom().nextFloat() < 0.6f) {
                            net.minecraft.world.entity.animal.horse.Horse horse = 
                                net.minecraft.world.entity.EntityType.HORSE.create(world.getLevel());
                            
                            if (horse != null) {
                                // Position the horse at the raider's location
                                horse.moveTo(raider.getX(), raider.getY(), raider.getZ(), 
                                    raider.getYRot(), 0.0F);
                                
                                // Make the horse faster for raids
                                horse.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)
                                    .setBaseValue(0.35);
                                
                                // Make horse tamed so the raider can ride it
                                horse.setTamed(true);
                                
                                // Add saddle using equipSaddle method (requires ItemStack + SoundSource)
                                horse.equipSaddle(
                                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SADDLE),
                                    net.minecraft.sounds.SoundSource.NEUTRAL
                                );
                                
                                // Add armor (70% iron, 30% leather) using body armor slot
                                if (world.getRandom().nextFloat() < 0.7f) {
                                    horse.setBodyArmorItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_HORSE_ARMOR));
                                } else {
                                    horse.setBodyArmorItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LEATHER_HORSE_ARMOR));
                                }
                                
                                // Spawn the horse
                                world.getLevel().addFreshEntity(horse);
                                
                                // Mount the raider on the horse
                                raider.startRiding(horse, true);
                                
                                LOGGER.debug("Spawned raid horse for {}", raider.getClass().getSimpleName());
                            }
                        }
                    }
                }
            }
        });
    }

    // Optional: access giant footsteps sound directly
    public static SoundEvent getGiantFootsteps() {
        return SoundRegistry.GIANT_FOOTSTEPS;
    }
}
