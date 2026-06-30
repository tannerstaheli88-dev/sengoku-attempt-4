package com.shioh.sengoku.registry;

import com.shioh.sengoku.init.KenseiItemReg;
import com.shioh.sengoku.struct.WeaponType;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import com.shioh.sengoku.registry.SengokuBlocks;

import java.util.List;

public class CreativeModeTabHandler {

    public static void buildContents() {
        // === Move Dark Oak family (vanilla + custom blocks) to the top ===
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            // Prepend vanilla dark oak blocks in reverse order
            entries.prepend(Items.DARK_OAK_BUTTON);
            entries.prepend(Items.DARK_OAK_PRESSURE_PLATE);
            entries.prepend(Items.DARK_OAK_TRAPDOOR);
            
            // Prepend dark_cedar shoji frames in order (reverse for prepend): covered -> checkered -> paly -> regular
            if (SengokuBlocks.SHOJI_FRAMES.containsKey("dark_cedar_covered_shoji_frame"))
                entries.prepend(SengokuBlocks.SHOJI_FRAMES.get("dark_cedar_covered_shoji_frame").asItem());
            if (SengokuBlocks.SHOJI_FRAMES.containsKey("dark_cedar_checkered_shoji_frame"))
                entries.prepend(SengokuBlocks.SHOJI_FRAMES.get("dark_cedar_checkered_shoji_frame").asItem());
            if (SengokuBlocks.SHOJI_FRAMES.containsKey("dark_cedar_paly_shoji_frame"))
                entries.prepend(SengokuBlocks.SHOJI_FRAMES.get("dark_cedar_paly_shoji_frame").asItem());
            if (SengokuBlocks.SHOJI_FRAMES.containsKey("dark_cedar_shoji_frame"))
                entries.prepend(SengokuBlocks.SHOJI_FRAMES.get("dark_cedar_shoji_frame").asItem());
            
            // Prepend dark_cedar shoji panels in order (reverse for prepend)
            if (SengokuBlocks.SHOJI_PANELS.containsKey("dark_cedar_checkered_shoji_panel"))
                entries.prepend(SengokuBlocks.SHOJI_PANELS.get("dark_cedar_checkered_shoji_panel").asItem());
            if (SengokuBlocks.SHOJI_PANELS.containsKey("dark_cedar_paly_shoji_panel"))
                entries.prepend(SengokuBlocks.SHOJI_PANELS.get("dark_cedar_paly_shoji_panel").asItem());
            if (SengokuBlocks.SHOJI_PANELS.containsKey("dark_cedar_shoji_panel"))
                entries.prepend(SengokuBlocks.SHOJI_PANELS.get("dark_cedar_shoji_panel").asItem());
            
            // Prepend dark_cedar shoji doors in order (reverse for prepend): triple -> double -> single, checkered -> paly -> base
            if (SengokuBlocks.SHOJI_DOORS.containsKey("dark_cedar_checkered_triple_shoji_door"))
                entries.prepend(SengokuBlocks.SHOJI_DOORS.get("dark_cedar_checkered_triple_shoji_door").asItem());
            if (SengokuBlocks.SHOJI_DOORS.containsKey("dark_cedar_paly_triple_shoji_door"))
                entries.prepend(SengokuBlocks.SHOJI_DOORS.get("dark_cedar_paly_triple_shoji_door").asItem());
            if (SengokuBlocks.SHOJI_DOORS.containsKey("dark_cedar_triple_shoji_door"))
                entries.prepend(SengokuBlocks.SHOJI_DOORS.get("dark_cedar_triple_shoji_door").asItem());
            
            if (SengokuBlocks.SHOJI_DOORS.containsKey("dark_cedar_checkered_double_shoji_door"))
                entries.prepend(SengokuBlocks.SHOJI_DOORS.get("dark_cedar_checkered_double_shoji_door").asItem());
            if (SengokuBlocks.SHOJI_DOORS.containsKey("dark_cedar_paly_double_shoji_door"))
                entries.prepend(SengokuBlocks.SHOJI_DOORS.get("dark_cedar_paly_double_shoji_door").asItem());
            if (SengokuBlocks.SHOJI_DOORS.containsKey("dark_cedar_double_shoji_door"))
                entries.prepend(SengokuBlocks.SHOJI_DOORS.get("dark_cedar_double_shoji_door").asItem());
            
            if (SengokuBlocks.SHOJI_DOORS.containsKey("dark_cedar_checkered_shoji_door"))
                entries.prepend(SengokuBlocks.SHOJI_DOORS.get("dark_cedar_checkered_shoji_door").asItem());
            if (SengokuBlocks.SHOJI_DOORS.containsKey("dark_cedar_paly_shoji_door"))
                entries.prepend(SengokuBlocks.SHOJI_DOORS.get("dark_cedar_paly_shoji_door").asItem());
            if (SengokuBlocks.SHOJI_DOORS.containsKey("dark_cedar_shoji_door"))
                entries.prepend(SengokuBlocks.SHOJI_DOORS.get("dark_cedar_shoji_door").asItem());
            
            entries.prepend(Items.DARK_OAK_DOOR);
            entries.prepend(Items.DARK_OAK_FENCE_GATE);
            entries.prepend(Items.DARK_OAK_FENCE);
            
            // Prepend dark_cedar shirasu kabe (yellow and white)
            for (var entry : SengokuBlocks.SHIRASU_YELLOWS.entrySet()) {
                if (entry.getKey().startsWith("dark_cedar")) {
                    entries.prepend(entry.getValue().asItem());
                }
            }
            for (var entry : SengokuBlocks.SHIRASU_WALLS.entrySet()) {
                if (entry.getKey().startsWith("dark_cedar")) {
                    entries.prepend(entry.getValue().asItem());
                }
            }
            
            // Prepend dark_cedar tatami (in reverse so they appear after slab)
            for (var entry : SengokuBlocks.TATAMI_MATS.entrySet()) {
                if (entry.getKey().startsWith("dark_cedar")) {
                    entries.prepend(entry.getValue().asItem());
                }
            }
            for (var entry : SengokuBlocks.TATAMI_BLOCKS.entrySet()) {
                if (entry.getKey().startsWith("dark_cedar")) {
                    entries.prepend(entry.getValue().asItem());
                }
            }
            
            entries.prepend(Items.DARK_OAK_SLAB);
            entries.prepend(Items.DARK_OAK_STAIRS);
            entries.prepend(Items.DARK_OAK_PLANKS);
            
            // Prepend dark_cedar lacquered wood and log
            for (var entry : SengokuBlocks.LACQUERED_WOODS.entrySet()) {
                if (entry.getKey().contains("dark_cedar")) {
                    entries.prepend(entry.getValue().asItem());
                }
            }
            for (var entry : SengokuBlocks.LACQUERED_LOGS.entrySet()) {
                if (entry.getKey().contains("dark_cedar")) {
                    entries.prepend(entry.getValue().asItem());
                }
            }
            
            entries.prepend(Items.STRIPPED_DARK_OAK_WOOD);
            entries.prepend(Items.STRIPPED_DARK_OAK_LOG);
            entries.prepend(Items.DARK_OAK_WOOD);
            entries.prepend(Items.DARK_OAK_LOG);
        });
        
        // === Tatami after slabs of their wood type (other woods) ===
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            addTatamiAfter(entries, Items.OAK_SLAB, "oak");
            addShirasuAfter(entries, Items.OAK_FENCE, "oak");
            addLacqueredAfter(entries, Items.OAK_PLANKS, "oak");
            addShikkuiAfter(entries, Items.OAK_PLANKS, "oak");
            addTatamiAfter(entries, Items.BIRCH_SLAB, "birch");
            addShirasuAfter(entries, Items.BIRCH_FENCE, "birch");
            addLacqueredAfter(entries, Items.BIRCH_PLANKS, "birch");
            addShikkuiAfter(entries, Items.BIRCH_PLANKS, "birch");
            addTatamiAfter(entries, Items.SPRUCE_SLAB, "black_pine");
            addShirasuAfter(entries, Items.SPRUCE_FENCE, "black_pine");
            addLacqueredAfter(entries, Items.SPRUCE_PLANKS, "black_pine");
            addShikkuiAfter(entries, Items.SPRUCE_PLANKS, "black_pine");
            addTatamiAfter(entries, Items.ACACIA_SLAB, "keyaki");
            addShirasuAfter(entries, Items.ACACIA_FENCE, "keyaki");
            addLacqueredAfter(entries, Items.ACACIA_PLANKS, "keyaki");
            addShikkuiAfter(entries, Items.ACACIA_PLANKS, "keyaki");
            addTatamiAfter(entries, Items.JUNGLE_SLAB, "kiso");
            addShirasuAfter(entries, Items.JUNGLE_FENCE, "kiso");
            addLacqueredAfter(entries, Items.JUNGLE_PLANKS, "kiso");
            addShikkuiAfter(entries, Items.JUNGLE_PLANKS, "kiso");
            addTatamiAfter(entries, Items.MANGROVE_SLAB, "mangrove");
            addShirasuAfter(entries, Items.MANGROVE_FENCE, "mangrove");
            addLacqueredAfter(entries, Items.MANGROVE_PLANKS, "mangrove");
            addShikkuiAfter(entries, Items.MANGROVE_PLANKS, "mangrove");
            addTatamiAfter(entries, Items.BAMBOO_SLAB, "bamboo");
            addShirasuAfter(entries, Items.BAMBOO_FENCE, "bamboo");
            addLacqueredAfter(entries, Items.BAMBOO_PLANKS, "bamboo");
            addShikkuiAfter(entries, Items.BAMBOO_PLANKS, "bamboo");
            addTatamiAfter(entries, Items.CHERRY_SLAB, "sakura");
            addShirasuAfter(entries, Items.CHERRY_FENCE, "sakura");
            addLacqueredAfter(entries, Items.CHERRY_PLANKS, "sakura");
            addShikkuiAfter(entries, Items.CHERRY_PLANKS, "sakura");
            addTatamiAfter(entries, Items.CRIMSON_SLAB, "bloodgood");
            addShirasuAfter(entries, Items.CRIMSON_FENCE, "bloodgood");
            addLacqueredAfter(entries, Items.CRIMSON_PLANKS, "bloodgood");
            addShikkuiAfter(entries, Items.CRIMSON_PLANKS, "bloodgood");
            addTatamiAfter(entries, Items.WARPED_SLAB, "weeping_willow");
            addShirasuAfter(entries, Items.WARPED_FENCE, "weeping_willow");
            addLacqueredAfter(entries, Items.WARPED_PLANKS, "weeping_willow");
            addShikkuiAfter(entries, Items.WARPED_PLANKS, "weeping_willow");
            
            // Dark Cedar Shikkui (shifted to vanilla Block of Quartz)
            addShikkuiAfter(entries, Items.DARK_OAK_PLANKS, "dark_cedar");
        });

        // === Shoji after their corresponding vanilla doors (other woods) ===
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.BUILDING_BLOCKS).register(entries -> {
            addShojiAfter(entries, Items.OAK_DOOR, "oak");
            addShojiAfter(entries, Items.BIRCH_DOOR, "birch");
            addShojiAfter(entries, Items.SPRUCE_DOOR, "black_pine");
            addShojiAfter(entries, Items.ACACIA_DOOR, "keyaki");
            addShojiAfter(entries, Items.JUNGLE_DOOR, "kiso");
            addShojiAfter(entries, Items.MANGROVE_DOOR, "mangrove");
            addShojiAfter(entries, Items.BAMBOO_DOOR, "bamboo");
            addShojiAfter(entries, Items.CHERRY_DOOR, "sakura");
            addShojiAfter(entries, Items.CRIMSON_DOOR, "bloodgood");
            addShojiAfter(entries, Items.WARPED_DOOR, "weeping_willow");
        });

        // === Limestone ore variants in building blocks ===
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            if (SengokuBlocks.LIMESTONE_COAL_ORE != null)
                entries.addAfter(Items.COAL_ORE, SengokuBlocks.LIMESTONE_COAL_ORE.asItem());
            if (SengokuBlocks.LIMESTONE_IRON_ORE != null)
                entries.addAfter(Items.IRON_ORE, SengokuBlocks.LIMESTONE_IRON_ORE.asItem());
            if (SengokuBlocks.LIMESTONE_COPPER_ORE != null)
                entries.addAfter(Items.COPPER_ORE, SengokuBlocks.LIMESTONE_COPPER_ORE.asItem());
            if (SengokuBlocks.LIMESTONE_GOLD_ORE != null)
                entries.addAfter(Items.GOLD_ORE, SengokuBlocks.LIMESTONE_GOLD_ORE.asItem());
            if (SengokuBlocks.LIMESTONE_DIAMOND_ORE != null)
                entries.addAfter(Items.DIAMOND_ORE, SengokuBlocks.LIMESTONE_DIAMOND_ORE.asItem());
            if (SengokuBlocks.LIMESTONE_JADE_ORE != null)
                entries.addAfter(Items.EMERALD_ORE, SengokuBlocks.LIMESTONE_JADE_ORE.asItem());
            if (SengokuBlocks.LIMESTONE_LAPIS_ORE != null)
                entries.addAfter(Items.LAPIS_ORE, SengokuBlocks.LIMESTONE_LAPIS_ORE.asItem());
            if (SengokuBlocks.LIMESTONE_REDSTONE_ORE != null)
                entries.addAfter(Items.REDSTONE_ORE, SengokuBlocks.LIMESTONE_REDSTONE_ORE.asItem());
        });

        // === Natural tab (leaves + reeds) ===
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.NATURAL_BLOCKS).register(entries -> {
            entries.addAfter(Items.OAK_LEAVES, SengokuBlocks.BLOODGOOD_LEAVES.asItem());
            entries.addAfter(SengokuBlocks.BLOODGOOD_LEAVES.asItem(), SengokuBlocks.WEEPING_WILLOW_LEAVES.asItem());
            entries.addAfter(SengokuBlocks.WEEPING_WILLOW_LEAVES.asItem(), SengokuBlocks.GINKGO_LEAVES.asItem());
            entries.addAfter(SengokuBlocks.GINKGO_LEAVES.asItem(), SengokuBlocks.MAPLE_LEAVES.asItem());
            // Saplings after oak sapling
            entries.addAfter(Items.OAK_SAPLING, SengokuBlocks.GINKGO_SAPLING.asItem());
            entries.addAfter(SengokuBlocks.GINKGO_SAPLING.asItem(), SengokuBlocks.MAPLE_SAPLING.asItem());
            // Place mod's weeping willow vines next to vanilla vines
            if (SengokuBlocks.WEEPING_WILLOW_VINES != null)
                entries.addAfter(Items.VINE, SengokuBlocks.WEEPING_WILLOW_VINES.asItem());
            if (SengokuBlocks.MANGROVE_VINES != null)
                entries.addAfter(Items.VINE, SengokuBlocks.MANGROVE_VINES.asItem());
            if (SengokuBlocks.SAKURA_VINES != null)
                entries.addAfter(Items.VINE, SengokuBlocks.SAKURA_VINES.asItem());
            if (SengokuBlocks.COARSE_FARMLAND != null)
                entries.addAfter(Items.COARSE_DIRT, SengokuBlocks.COARSE_FARMLAND.asItem());
            entries.addAfter(Items.HAY_BLOCK, SengokuBlocks.PALE_REEDS_BLOCK.asItem());
            entries.addAfter(SengokuBlocks.PALE_REEDS_BLOCK.asItem(), SengokuBlocks.DARK_REEDS_BLOCK.asItem());
        });

        // === Weapons in combat tab ===
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            entries.addAfter(Items.NETHERITE_AXE, KenseiItemReg.SPLITTING_AXE_OF_KINTARO);
            entries.addAfter(KenseiItemReg.SPLITTING_AXE_OF_KINTARO, KenseiItemReg.NETHERITE_SPLITTING_AXE_OF_KINTARO);

            Item lastItem = KenseiItemReg.NETHERITE_SPLITTING_AXE_OF_KINTARO;
            for (WeaponType type : WeaponType.values()) {
                List<Item> items = WeaponRegistry.getItemsByType(type);
                for (Item item : items) {
                    if (item != null) {
                        entries.addAfter(lastItem, item);
                        lastItem = item; // update so next weapon goes after this one
                    }
                }
            }
        });
        
        // === Reorder spawn eggs ===
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            // Prepend in reverse order so they appear in the correct order
            entries.prepend(ModSpawnEggs.YUKI_ONNA_SPAWN_EGG);
            entries.prepend(Items.WITCH_SPAWN_EGG);
            entries.prepend(Items.OCELOT_SPAWN_EGG);
            entries.prepend(Items.WOLF_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.WARLORD_SPAWN_EGG);
            entries.prepend(Items.WANDERING_TRADER_SPAWN_EGG);
            entries.prepend(Items.ZOGLIN_SPAWN_EGG);
            entries.prepend(Items.STRIDER_SPAWN_EGG);
            entries.prepend(Items.VILLAGER_SPAWN_EGG); 
            entries.prepend(Items.RAVAGER_SPAWN_EGG);
            entries.prepend(Items.ELDER_GUARDIAN_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.UMI_NYOBO_SPAWN_EGG);
            entries.prepend(Items.GUARDIAN_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.UMI_INU_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.UMI_BOZU_SPAWN_EGG);
            entries.prepend(Items.BLAZE_SPAWN_EGG);
            entries.prepend(Items.TURTLE_SPAWN_EGG);
            entries.prepend(Items.SLIME_SPAWN_EGG);
            entries.prepend(Items.MAGMA_CUBE_SPAWN_EGG);
            entries.prepend(Items.STRAY_SPAWN_EGG);
            entries.prepend(Items.SILVERFISH_SPAWN_EGG);
            entries.prepend(Items.ENDERMITE_SPAWN_EGG);
            entries.prepend(Items.CAVE_SPIDER_SPAWN_EGG);
            entries.prepend(Items.VEX_SPAWN_EGG);
            entries.prepend(Items.WITHER_SPAWN_EGG);
            entries.prepend(Items.PANDA_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.TAKEDA_SOHEI_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.TAKEDA_SAMURAI_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.TAKEDA_ASHIGARU_SPAWN_EGG);
            entries.prepend(Items.TADPOLE_SPAWN_EGG);
            entries.prepend(Items.SQUID_SPAWN_EGG);
            entries.prepend(Items.SKELETON_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.SHIRYO_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.SHINOBI_LORD_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.SHINOBI_SPAWN_EGG);
            entries.prepend(Items.WITHER_SKELETON_SPAWN_EGG);
            entries.prepend(Items.ZOMBIFIED_PIGLIN_SPAWN_EGG);
            entries.prepend(Items.SHEEP_SPAWN_EGG);
            entries.prepend(Items.GOAT_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.SATOMI_SOHEI_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.SATOMI_SAMURAI_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.SATOMI_ASHIGARU_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.SARUGAMI_SPAWN_EGG);
            entries.prepend(Items.ZOMBIE_HORSE_SPAWN_EGG);
            entries.prepend(Items.ENDER_DRAGON_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.RONIN_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.RED_CROWNED_CRANE_SPAWN_EGG);
            entries.prepend(Items.RABBIT_SPAWN_EGG);
            entries.prepend(Items.PUFFERFISH_SPAWN_EGG);
            entries.prepend(Items.WARDEN_SPAWN_EGG);
            entries.prepend(Items.COW_SPAWN_EGG);
            entries.prepend(Items.EVOKER_SPAWN_EGG);
            entries.prepend(Items.VINDICATOR_SPAWN_EGG);
            entries.prepend(Items.PILLAGER_SPAWN_EGG);
            entries.prepend(Items.HOGLIN_SPAWN_EGG);
            entries.prepend(Items.SNOW_GOLEM_SPAWN_EGG);
            entries.prepend(Items.AXOLOTL_SPAWN_EGG);
            entries.prepend(Items.GHAST_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.ONIKUMA_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.ONI_BRUTE_SPAWN_EGG);
            entries.prepend(Items.ZOMBIE_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.OMUKADE_SPAWN_EGG);
            entries.prepend(Items.HUSK_SPAWN_EGG);
            entries.prepend(Items.BOGGED_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.NINGYO_SPAWN_EGG);
            entries.prepend(Items.MULE_SPAWN_EGG);
            entries.prepend(Items.TRADER_LLAMA_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.MAIKUBI_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.MACAQUE_SPAWN_EGG);
            entries.prepend(Items.MOOSHROOM_SPAWN_EGG);
            entries.prepend(Items.ENDERMAN_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.KOJIN_SPAWN_EGG);
            entries.prepend(Items.TROPICAL_FISH_SPAWN_EGG);
            entries.prepend(Items.CREEPER_SPAWN_EGG);
            entries.prepend(Items.ALLAY_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.KOBAYAKAWA_SOHEI_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.KOBAYAKAWA_SAMURAI_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.KOBAYAKAWA_ASHIGARU_SPAWN_EGG);
            entries.prepend(Items.PIGLIN_SPAWN_EGG);
            entries.prepend(Items.BREEZE_SPAWN_EGG);
            entries.prepend(Items.DROWNED_SPAWN_EGG);
            entries.prepend(Items.PHANTOM_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.KAMIIKE_HIME_SPAWN_EGG);
            entries.prepend(Items.SHULKER_SPAWN_EGG);
            entries.prepend(Items.SPIDER_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.IKUCHI_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.HITOTSUME_NYUDO_SPAWN_EGG);
            entries.prepend(Items.HORSE_SPAWN_EGG);
            entries.prepend(Items.BEE_SPAWN_EGG);
            entries.prepend(Items.ARMADILLO_SPAWN_EGG);
            entries.prepend(Items.CAMEL_SPAWN_EGG);
            entries.prepend(Items.SNIFFER_SPAWN_EGG);
            entries.prepend(Items.PIGLIN_BRUTE_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.GORYO_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.GASHADOKU_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.GAKI_SPAWN_EGG);
            entries.prepend(Items.FROG_SPAWN_EGG);
            entries.prepend(Items.FOX_SPAWN_EGG);
            entries.prepend(Items.GLOW_SQUID_SPAWN_EGG);
            entries.prepend(Items.ZOMBIE_VILLAGER_SPAWN_EGG);
            entries.prepend(Items.DONKEY_SPAWN_EGG);
            entries.prepend(Items.DOLPHIN_SPAWN_EGG);
            entries.prepend(Items.LLAMA_SPAWN_EGG);
            entries.prepend(Items.IRON_GOLEM_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.CROW_SPAWN_EGG);
            entries.prepend(Items.COD_SPAWN_EGG);
            entries.prepend(Items.CHICKEN_SPAWN_EGG);
            entries.prepend(Items.CAT_SPAWN_EGG);
            entries.prepend(Items.SALMON_SPAWN_EGG);
            entries.prepend(Items.PIG_SPAWN_EGG);
            entries.prepend(Items.POLAR_BEAR_SPAWN_EGG);
            entries.prepend(Items.PARROT_SPAWN_EGG);
            entries.prepend(Items.BAT_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.BANDIT_SPAWN_EGG);
            entries.prepend(Items.SKELETON_HORSE_SPAWN_EGG);
            entries.prepend(ModSpawnEggs.AKUGYO_SPAWN_EGG);
            entries.prepend(Items.TRIAL_SPAWNER);
            entries.prepend(Items.SPAWNER);
        });

        // === Functional Blocks: paper and lanterns after torches ===
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            // Order: torches > paper lanterns > stone lanterns > lanterns
            // Insert after soul torch (last torch variant)
            if (SengokuBlocks.PAPER_LANTERN != null) 
                entries.addAfter(Items.REDSTONE_TORCH, SengokuBlocks.PAPER_LANTERN.asItem());
            if (SengokuBlocks.SOUL_PAPER_LANTERN != null) 
                entries.addAfter(Items.REDSTONE_TORCH, SengokuBlocks.SOUL_PAPER_LANTERN.asItem());
            if (SengokuBlocks.STONE_LANTERN != null) 
                entries.addAfter(Items.RESPAWN_ANCHOR, SengokuBlocks.STONE_LANTERN.asItem());
            if (SengokuBlocks.SOUL_STONE_LANTERN != null) 
                entries.addAfter(Items.RESPAWN_ANCHOR, SengokuBlocks.SOUL_STONE_LANTERN.asItem());
            if (SengokuBlocks.COBBLESTONE_LANTERN != null) 
                entries.addAfter(Items.RESPAWN_ANCHOR, SengokuBlocks.COBBLESTONE_LANTERN.asItem());
            if (SengokuBlocks.MOSSY_COBBLESTONE_LANTERN != null) 
                entries.addAfter(Items.RESPAWN_ANCHOR, SengokuBlocks.MOSSY_COBBLESTONE_LANTERN.asItem());
            if (SengokuBlocks.COBBLED_DEEPSLATE_LANTERN != null) 
                entries.addAfter(Items.RESPAWN_ANCHOR, SengokuBlocks.COBBLED_DEEPSLATE_LANTERN.asItem());
            if (SengokuBlocks.STONE_BRICK_LANTERN != null) 
                entries.addAfter(Items.RESPAWN_ANCHOR, SengokuBlocks.STONE_BRICK_LANTERN.asItem());
            if (SengokuBlocks.MOSSY_STONE_BRICK_LANTERN != null) 
                entries.addAfter(Items.RESPAWN_ANCHOR, SengokuBlocks.MOSSY_STONE_BRICK_LANTERN.asItem());
            if (SengokuBlocks.ANDESITE_LANTERN != null)
                entries.addAfter(Items.RESPAWN_ANCHOR, SengokuBlocks.ANDESITE_LANTERN.asItem());
            if (SengokuBlocks.DIORITE_LANTERN != null)
                entries.addAfter(Items.RESPAWN_ANCHOR, SengokuBlocks.DIORITE_LANTERN.asItem());
            if (SengokuBlocks.TUFF_LANTERN != null)
                entries.addAfter(Items.RESPAWN_ANCHOR, SengokuBlocks.TUFF_LANTERN.asItem());
        });
    }

    // Helper: insert tatami (block + mat) after matching slab
    private static void addTatamiAfter(FabricItemGroupEntries entries, Item vanillaSlab, String woodName) {
        for (var entry : SengokuBlocks.TATAMI_BLOCKS.entrySet()) {
            if (entry.getKey().startsWith(woodName)) {
                entries.addAfter(vanillaSlab, entry.getValue().asItem());
            }
        }
        for (var entry : SengokuBlocks.TATAMI_MATS.entrySet()) {
            if (entry.getKey().startsWith(woodName)) {
                entries.addAfter(vanillaSlab, entry.getValue().asItem());
            }
        }
    }

    // Helper: insert shoji doors & panels after vanilla door
    private static void addShojiAfter(FabricItemGroupEntries entries, Item vanillaDoor, String woodName) {
        // Add shoji frames in order: covered -> checkered -> paly -> regular (reverse for addAfter positioning)
        if (SengokuBlocks.SHOJI_FRAMES.containsKey(woodName + "_covered_shoji_frame"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_FRAMES.get(woodName + "_covered_shoji_frame").asItem());
        if (SengokuBlocks.SHOJI_FRAMES.containsKey(woodName + "_checkered_shoji_frame"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_FRAMES.get(woodName + "_checkered_shoji_frame").asItem());
        if (SengokuBlocks.SHOJI_FRAMES.containsKey(woodName + "_paly_shoji_frame"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_FRAMES.get(woodName + "_paly_shoji_frame").asItem());
        if (SengokuBlocks.SHOJI_FRAMES.containsKey(woodName + "_shoji_frame"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_FRAMES.get(woodName + "_shoji_frame").asItem());
        
        // Add shoji panels in order: checkered -> paly -> base (reverse order for proper positioning)
        if (SengokuBlocks.SHOJI_PANELS.containsKey(woodName + "_checkered_shoji_panel"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_PANELS.get(woodName + "_checkered_shoji_panel").asItem());
        if (SengokuBlocks.SHOJI_PANELS.containsKey(woodName + "_paly_shoji_panel"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_PANELS.get(woodName + "_paly_shoji_panel").asItem());
        if (SengokuBlocks.SHOJI_PANELS.containsKey(woodName + "_shoji_panel"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_PANELS.get(woodName + "_shoji_panel").asItem());
        
        // Add shoji doors in order: triple checkered -> triple paly -> triple base -> double checkered -> double paly -> double base -> checkered -> paly -> base
        if (SengokuBlocks.SHOJI_DOORS.containsKey(woodName + "_checkered_triple_shoji_door"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_DOORS.get(woodName + "_checkered_triple_shoji_door").asItem());
        if (SengokuBlocks.SHOJI_DOORS.containsKey(woodName + "_paly_triple_shoji_door"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_DOORS.get(woodName + "_paly_triple_shoji_door").asItem());
        if (SengokuBlocks.SHOJI_DOORS.containsKey(woodName + "_triple_shoji_door"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_DOORS.get(woodName + "_triple_shoji_door").asItem());
        
        if (SengokuBlocks.SHOJI_DOORS.containsKey(woodName + "_checkered_double_shoji_door"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_DOORS.get(woodName + "_checkered_double_shoji_door").asItem());
        if (SengokuBlocks.SHOJI_DOORS.containsKey(woodName + "_paly_double_shoji_door"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_DOORS.get(woodName + "_paly_double_shoji_door").asItem());
        if (SengokuBlocks.SHOJI_DOORS.containsKey(woodName + "_double_shoji_door"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_DOORS.get(woodName + "_double_shoji_door").asItem());
        
        if (SengokuBlocks.SHOJI_DOORS.containsKey(woodName + "_checkered_shoji_door"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_DOORS.get(woodName + "_checkered_shoji_door").asItem());
        if (SengokuBlocks.SHOJI_DOORS.containsKey(woodName + "_paly_shoji_door"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_DOORS.get(woodName + "_paly_shoji_door").asItem());
        if (SengokuBlocks.SHOJI_DOORS.containsKey(woodName + "_shoji_door"))
            entries.addAfter(vanillaDoor, SengokuBlocks.SHOJI_DOORS.get(woodName + "_shoji_door").asItem());
    }
    
    // Helper: insert shirasu kabe (walls and yellows) after vanilla fence
    private static void addShirasuAfter(FabricItemGroupEntries entries, Item vanilleFence, String woodName) {
        // Add shirasu yellows first (any order from HashMap is fine)
        for (var entry : SengokuBlocks.SHIRASU_YELLOWS.entrySet()) {
            if (entry.getKey().startsWith(woodName)) {
                entries.addAfter(vanilleFence, entry.getValue().asItem());
            }
        }
        // Add shirasu walls
        for (var entry : SengokuBlocks.SHIRASU_WALLS.entrySet()) {
            if (entry.getKey().startsWith(woodName)) {
                entries.addAfter(vanilleFence, entry.getValue().asItem());
            }
        }
    }
    
    // Helper: insert lacquered logs and woods after vanilla planks
    private static void addLacqueredAfter(FabricItemGroupEntries entries, Item vanillaPlanks, String woodName) {
        // Add lacquered woods first (any order from HashMap is fine)
        for (var entry : SengokuBlocks.LACQUERED_WOODS.entrySet()) {
            if (entry.getKey().contains(woodName)) {
                entries.addAfter(vanillaPlanks, entry.getValue().asItem());
            }
        }
        // Add lacquered logs
        for (var entry : SengokuBlocks.LACQUERED_LOGS.entrySet()) {
            if (entry.getKey().contains(woodName)) {
                entries.addAfter(vanillaPlanks, entry.getValue().asItem());
            }
        }
    }

    // Helper: insert shikkui blocks after yellow shirasu kabe if present,
    // otherwise after white shirasu kabe, otherwise fall back to the provided vanilla item.
    private static void addShikkuiAfter(FabricItemGroupEntries entries, Item vanillaItem, String woodName) {
        // Try to find a yellow shirasu entry for this wood
        Item anchor = null;
        for (var entry : SengokuBlocks.SHIRASU_YELLOWS.entrySet()) {
            if (entry.getKey().startsWith(woodName)) {
                anchor = entry.getValue().asItem();
                break;
            }
        }

        // If no yellow shirasu, fall back to the white shirasu wall
        if (anchor == null) {
            for (var entry : SengokuBlocks.SHIRASU_WALLS.entrySet()) {
                if (entry.getKey().startsWith(woodName)) {
                    anchor = entry.getValue().asItem();
                    break;
                }
            }
        }

        // Final fallback to the vanilla item passed in
        if (anchor == null) anchor = vanillaItem;

        // Place Dark Cedar mapping to vanilla quartz block at the anchor position
        if ("dark_cedar".equals(woodName)) {
            entries.addAfter(anchor, Items.QUARTZ_BLOCK);
            return;
        }

        // Place the mod's shikkui block after the anchor
        String shikkuiKey = woodName + "_shikkui";
        if (SengokuBlocks.SHIKKUI_BLOCKS.containsKey(shikkuiKey)) {
            entries.addAfter(anchor, SengokuBlocks.SHIKKUI_BLOCKS.get(shikkuiKey).asItem());
        }
    }
}
