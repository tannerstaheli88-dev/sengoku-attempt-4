package com.shioh.sengoku.registry;

import com.shioh.sengoku.Constants;
import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.block.ShojiDoorBlock;
import com.shioh.sengoku.block.ShojiDoubleDoorBlock;
import com.shioh.sengoku.block.ShojiTrapdoorBlock;
import com.shioh.sengoku.block.TatamiBlock;
import com.shioh.sengoku.block.TatamiMatBlock;
import com.shioh.sengoku.block.ShojiTripleDoorBlock;
import com.shioh.sengoku.block.VoidAirBlock;
import com.shioh.sengoku.block.CoarseFarmlandBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import com.shioh.sengoku.block.ShojiFrameBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;

import java.util.HashMap;
import java.util.Map;

public class SengokuBlocks {

    // Shoji blocks
    public static final Map<String, Block> SHOJI_DOORS = new HashMap<>();
    public static final Map<String, Block> SHOJI_PANELS = new HashMap<>();
    // Shoji frames (pane-like blocks)
    public static final Map<String, Block> SHOJI_FRAMES = new HashMap<>();

    // Tatami blocks
    public static final Map<String, Block> TATAMI_BLOCKS = new HashMap<>();
    public static final Map<String, Block> TATAMI_MATS = new HashMap<>();

    // White Shirasu-kabe (wall-like terracotta variants)
    public static final Map<String, Block> SHIRASU_WALLS = new HashMap<>();
    // Yellow Shirasu-kabe (subset of woods)
    public static final Map<String, Block> SHIRASU_YELLOWS = new HashMap<>();
    
    // Shikkui blocks (white plaster-like blocks per wood type)
    public static final Map<String, Block> SHIKKUI_BLOCKS = new HashMap<>();

    // Lacquered logs/wood variants per wood type
    public static final Map<String, Block> LACQUERED_LOGS = new HashMap<>();
    public static final Map<String, Block> LACQUERED_WOODS = new HashMap<>();

    // Leaves
    public static Block BLOODGOOD_LEAVES;
    public static Block WEEPING_WILLOW_LEAVES;
    public static Block GINKGO_LEAVES;
    public static Block MAPLE_LEAVES;
    // Vines
    public static Block WEEPING_WILLOW_VINES;
    public static Block MANGROVE_VINES;
    public static Block SAKURA_VINES;
    
    // Saplings
    public static Block GINKGO_SAPLING;
    public static Block MAPLE_SAPLING;
    // Potted saplings (flower pot variants)
    public static Block POTTED_GINKGO_SAPLING;
    public static Block POTTED_MAPLE_SAPLING;

    // Reeds (simple cube blocks, no axis)
    public static Block PALE_REEDS_BLOCK;
    public static Block DARK_REEDS_BLOCK;
    public static Block COARSE_FARMLAND;
    public static Block PAPER_LANTERN;
    public static Block SOUL_PAPER_LANTERN;
    
    // Stone Lanterns (Japanese ishidōrō - must be lit with flint and steel)
    public static Block STONE_LANTERN;
    public static Block SOUL_STONE_LANTERN;
    public static Block COBBLESTONE_LANTERN;
    public static Block MOSSY_COBBLESTONE_LANTERN;
    public static Block COBBLED_DEEPSLATE_LANTERN;
    public static Block ANDESITE_LANTERN;
    public static Block DIORITE_LANTERN;
    public static Block TUFF_LANTERN;
    public static Block STONE_BRICK_LANTERN;
    public static Block MOSSY_STONE_BRICK_LANTERN;

    // Limestone ore variants
    public static Block LIMESTONE_COAL_ORE;
    public static Block LIMESTONE_IRON_ORE;
    public static Block LIMESTONE_COPPER_ORE;
    public static Block LIMESTONE_GOLD_ORE;
    public static Block LIMESTONE_DIAMOND_ORE;
    public static Block LIMESTONE_JADE_ORE;
    public static Block LIMESTONE_LAPIS_ORE;
    public static Block LIMESTONE_REDSTONE_ORE;
    
    // Shide (wall-mounted decoration)
    public static Block SHIDE;
    // Straw ornament (wall-mounted decoration placed by wheat)
    public static Block STRAW_ORNAMENT;
    
    // Void Air (behaves like air but treated as structure void in world gen)
    public static Block VOID_AIR;

    public static void init() {
        // Register Shoji sets per wood in the order: panels -> single doors -> double doors
        registerShojiTrapdoorSet("oak", BlockSetType.OAK, Blocks.OAK_TRAPDOOR);
    // shoji frames (pane copies)
    registerShojiFrameSet("oak", BlockSetType.OAK, Blocks.GLASS_PANE);
    registerCoveredShojiFrameSet("oak", BlockSetType.OAK, Blocks.GLASS_PANE);
        registerShojiSet("oak", BlockSetType.OAK, Blocks.OAK_DOOR);
        registerShojiDoubleSet("oak", BlockSetType.OAK, Blocks.OAK_DOOR);

        // Triple shoji doors (three visual variants per wood)
        registerShojiTripleSet("oak", BlockSetType.OAK, Blocks.OAK_DOOR);

        registerShojiTrapdoorSet("birch", BlockSetType.BIRCH, Blocks.BIRCH_TRAPDOOR);
    registerShojiFrameSet("birch", BlockSetType.BIRCH, Blocks.GLASS_PANE);
    registerCoveredShojiFrameSet("birch", BlockSetType.BIRCH, Blocks.GLASS_PANE);
        registerShojiSet("birch", BlockSetType.BIRCH, Blocks.BIRCH_DOOR);
        registerShojiDoubleSet("birch", BlockSetType.BIRCH, Blocks.BIRCH_DOOR);
    registerShojiTripleSet("birch", BlockSetType.BIRCH, Blocks.BIRCH_DOOR);

        registerShojiTrapdoorSet("black_pine", BlockSetType.SPRUCE, Blocks.SPRUCE_TRAPDOOR);
    registerShojiFrameSet("black_pine", BlockSetType.SPRUCE, Blocks.GLASS_PANE);
    registerCoveredShojiFrameSet("black_pine", BlockSetType.SPRUCE, Blocks.GLASS_PANE);
        registerShojiSet("black_pine", BlockSetType.SPRUCE, Blocks.SPRUCE_DOOR);
        registerShojiDoubleSet("black_pine", BlockSetType.SPRUCE, Blocks.SPRUCE_DOOR);
    registerShojiTripleSet("black_pine", BlockSetType.SPRUCE, Blocks.SPRUCE_DOOR);

        registerShojiTrapdoorSet("dark_cedar", BlockSetType.DARK_OAK, Blocks.DARK_OAK_TRAPDOOR);
    registerShojiFrameSet("dark_cedar", BlockSetType.DARK_OAK, Blocks.GLASS_PANE);
    registerCoveredShojiFrameSet("dark_cedar", BlockSetType.DARK_OAK, Blocks.GLASS_PANE);
        registerShojiSet("dark_cedar", BlockSetType.DARK_OAK, Blocks.DARK_OAK_DOOR);
        registerShojiDoubleSet("dark_cedar", BlockSetType.DARK_OAK, Blocks.DARK_OAK_DOOR);
    registerShojiTripleSet("dark_cedar", BlockSetType.DARK_OAK, Blocks.DARK_OAK_DOOR);

        registerShojiTrapdoorSet("keyaki", BlockSetType.ACACIA, Blocks.ACACIA_TRAPDOOR);
    registerShojiFrameSet("keyaki", BlockSetType.ACACIA, Blocks.GLASS_PANE);
    registerCoveredShojiFrameSet("keyaki", BlockSetType.ACACIA, Blocks.GLASS_PANE);
        registerShojiSet("keyaki", BlockSetType.ACACIA, Blocks.ACACIA_DOOR);
        registerShojiDoubleSet("keyaki", BlockSetType.ACACIA, Blocks.ACACIA_DOOR);
    registerShojiTripleSet("keyaki", BlockSetType.ACACIA, Blocks.ACACIA_DOOR);

        registerShojiTrapdoorSet("kiso", BlockSetType.JUNGLE, Blocks.JUNGLE_TRAPDOOR);
    registerShojiFrameSet("kiso", BlockSetType.JUNGLE, Blocks.GLASS_PANE);
    registerCoveredShojiFrameSet("kiso", BlockSetType.JUNGLE, Blocks.GLASS_PANE);
        registerShojiSet("kiso", BlockSetType.JUNGLE, Blocks.JUNGLE_DOOR);
        registerShojiDoubleSet("kiso", BlockSetType.JUNGLE, Blocks.JUNGLE_DOOR);
    registerShojiTripleSet("kiso", BlockSetType.JUNGLE, Blocks.JUNGLE_DOOR);

        registerShojiTrapdoorSet("mangrove", BlockSetType.MANGROVE, Blocks.MANGROVE_TRAPDOOR);
    registerShojiFrameSet("mangrove", BlockSetType.MANGROVE, Blocks.GLASS_PANE);
    registerCoveredShojiFrameSet("mangrove", BlockSetType.MANGROVE, Blocks.GLASS_PANE);
        registerShojiSet("mangrove", BlockSetType.MANGROVE, Blocks.MANGROVE_DOOR);
        registerShojiDoubleSet("mangrove", BlockSetType.MANGROVE, Blocks.MANGROVE_DOOR);
    registerShojiTripleSet("mangrove", BlockSetType.MANGROVE, Blocks.MANGROVE_DOOR);

        registerShojiTrapdoorSet("bamboo", BlockSetType.BAMBOO, Blocks.BAMBOO_TRAPDOOR);
    registerShojiFrameSet("bamboo", BlockSetType.BAMBOO, Blocks.GLASS_PANE);
    registerCoveredShojiFrameSet("bamboo", BlockSetType.BAMBOO, Blocks.GLASS_PANE);
        registerShojiSet("bamboo", BlockSetType.BAMBOO, Blocks.BAMBOO_DOOR);
        registerShojiDoubleSet("bamboo", BlockSetType.BAMBOO, Blocks.BAMBOO_DOOR);
    registerShojiTripleSet("bamboo", BlockSetType.BAMBOO, Blocks.BAMBOO_DOOR);

        registerShojiTrapdoorSet("sakura", BlockSetType.CHERRY, Blocks.CHERRY_TRAPDOOR);
    registerShojiFrameSet("sakura", BlockSetType.CHERRY, Blocks.GLASS_PANE);
    registerCoveredShojiFrameSet("sakura", BlockSetType.CHERRY, Blocks.GLASS_PANE);
        registerShojiSet("sakura", BlockSetType.CHERRY, Blocks.CHERRY_DOOR);
        registerShojiDoubleSet("sakura", BlockSetType.CHERRY, Blocks.CHERRY_DOOR);
    registerShojiTripleSet("sakura", BlockSetType.CHERRY, Blocks.CHERRY_DOOR);

        registerShojiTrapdoorSet("bloodgood", BlockSetType.CRIMSON, Blocks.CRIMSON_TRAPDOOR);
    registerShojiFrameSet("bloodgood", BlockSetType.CRIMSON, Blocks.GLASS_PANE);
    registerCoveredShojiFrameSet("bloodgood", BlockSetType.CRIMSON, Blocks.GLASS_PANE);
        registerShojiSet("bloodgood", BlockSetType.CRIMSON, Blocks.CRIMSON_DOOR);
        registerShojiDoubleSet("bloodgood", BlockSetType.CRIMSON, Blocks.CRIMSON_DOOR);
    registerShojiTripleSet("bloodgood", BlockSetType.CRIMSON, Blocks.CRIMSON_DOOR);

        registerShojiTrapdoorSet("weeping_willow", BlockSetType.WARPED, Blocks.WARPED_TRAPDOOR);
    registerShojiFrameSet("weeping_willow", BlockSetType.WARPED, Blocks.GLASS_PANE);
    registerCoveredShojiFrameSet("weeping_willow", BlockSetType.WARPED, Blocks.GLASS_PANE);
        registerShojiSet("weeping_willow", BlockSetType.WARPED, Blocks.WARPED_DOOR);
        registerShojiDoubleSet("weeping_willow", BlockSetType.WARPED, Blocks.WARPED_DOOR);
        registerShojiTripleSet("weeping_willow", BlockSetType.WARPED, Blocks.WARPED_DOOR);

    // Lacquered log/wood variants per wood
    registerLacqueredSet("oak", Blocks.OAK_LOG, Blocks.OAK_WOOD);
    registerLacqueredSet("birch", Blocks.BIRCH_LOG, Blocks.BIRCH_WOOD);
    registerLacqueredSet("black_pine", Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD);
    registerLacqueredSet("dark_cedar", Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD);
    registerLacqueredSet("keyaki", Blocks.ACACIA_LOG, Blocks.ACACIA_WOOD);
    registerLacqueredSet("kiso", Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD);
    registerLacqueredSet("mangrove", Blocks.MANGROVE_LOG, Blocks.MANGROVE_WOOD);
    // Bamboo uses the bamboo block (rotated pillar) for both variants
    registerLacqueredSet("bamboo", Blocks.BAMBOO_BLOCK, Blocks.BAMBOO_BLOCK);
    registerLacqueredSet("sakura", Blocks.CHERRY_LOG, Blocks.CHERRY_WOOD);
    // Nether woods map to stems/hyphae
    registerLacqueredSet("bloodgood", Blocks.CRIMSON_STEM, Blocks.CRIMSON_HYPHAE);
    registerLacqueredSet("weeping_willow", Blocks.WARPED_STEM, Blocks.WARPED_HYPHAE);

        // White Shirasu-kabe (terracotta-like wall blocks) per wood
    registerShirasuSet("oak");
    registerShirasuSet("birch");
    registerShirasuSet("black_pine");
    registerShirasuSet("dark_cedar");
    registerShirasuSet("keyaki");
    registerShirasuSet("kiso");
    registerShirasuSet("mangrove");
    registerShirasuSet("bamboo");
    registerShirasuSet("sakura");
    registerShirasuSet("bloodgood");
    registerShirasuSet("weeping_willow");

    // Yellow variants for select woods only
    // skipped some wood types bc that shit ugly as hell
    registerShirasuYellowSet("oak");
    registerShirasuYellowSet("black_pine");
    registerShirasuYellowSet("dark_cedar");
    registerShirasuYellowSet("kiso");
    registerShirasuYellowSet("bamboo");
    registerShirasuYellowSet("mangrove");

    // Shikkui blocks (per wood type, excluding dark_cedar which uses vanilla Block of Quartz)
    registerShikkuiSet("oak");
    registerShikkuiSet("birch");
    registerShikkuiSet("black_pine");
    registerShikkuiSet("keyaki");
    registerShikkuiSet("kiso");
    registerShikkuiSet("mangrove");
    registerShikkuiSet("bamboo");
    registerShikkuiSet("sakura");
    registerShikkuiSet("bloodgood");
    registerShikkuiSet("weeping_willow");

        // Tatami Blocks (full, axis like logs)
        registerTatamiSet("oak");
        registerTatamiSet("birch");
        registerTatamiSet("black_pine");
        registerTatamiSet("dark_cedar");
        registerTatamiSet("keyaki");
        registerTatamiSet("kiso");
        registerTatamiSet("mangrove");
        registerTatamiSet("bamboo");
        registerTatamiSet("sakura");
        registerTatamiSet("bloodgood");
        registerTatamiSet("weeping_willow");

        // Tatami Mats (slabs with axis behavior)
        registerTatamiMatSet("oak");
        registerTatamiMatSet("birch");
        registerTatamiMatSet("black_pine");
        registerTatamiMatSet("dark_cedar");
        registerTatamiMatSet("keyaki");
        registerTatamiMatSet("kiso");
        registerTatamiMatSet("mangrove");
        registerTatamiMatSet("bamboo");
        registerTatamiMatSet("sakura");
        registerTatamiMatSet("bloodgood");
        registerTatamiMatSet("weeping_willow");

        // Leaves (decay & shears behavior via LeavesBlock)
        BLOODGOOD_LEAVES = register("bloodgood_leaves",
            new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).mapColor(MapColor.CRIMSON_STEM)));
        WEEPING_WILLOW_LEAVES = register("weeping_willow_leaves",
            new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).mapColor(MapColor.WARPED_WART_BLOCK)));

        // Ginkgo (yellow) and Maple (red) leaves
        GINKGO_LEAVES = register("ginkgo_leaves",
            new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).mapColor(MapColor.GOLD)));
        MAPLE_LEAVES = register("maple_leaves",
            new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).mapColor(MapColor.COLOR_RED)));

        // Weeping Willow Vines (behaves like vanilla vines)
        WEEPING_WILLOW_VINES = register("weeping_willow_vines",
            new net.minecraft.world.level.block.VineBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.VINE)));

        // Mangrove Vines (behaves like vanilla vines)
        MANGROVE_VINES = register("mangrove_vines",
            new net.minecraft.world.level.block.VineBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.VINE)));

        // Sakura Vines (behaves like vanilla vines)
        SAKURA_VINES = register("sakura_vines",
            new net.minecraft.world.level.block.VineBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.VINE)));

        // Saplings
        GINKGO_SAPLING = register("ginkgo_sapling",
            new net.minecraft.world.level.block.SaplingBlock(
                com.shioh.sengoku.worldgen.GinkgoTreeGrower.GINKGO,
                BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));
        MAPLE_SAPLING = register("maple_sapling",
            new net.minecraft.world.level.block.SaplingBlock(
                com.shioh.sengoku.worldgen.MapleTreeGrower.MAPLE,
                BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));

        // Potted variants so these saplings can be placed in flower pots
        POTTED_GINKGO_SAPLING = registerBlockOnly("potted_ginkgo_sapling",
            new net.minecraft.world.level.block.FlowerPotBlock(GINKGO_SAPLING, BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT)));
        POTTED_MAPLE_SAPLING = registerBlockOnly("potted_maple_sapling",
            new net.minecraft.world.level.block.FlowerPotBlock(MAPLE_SAPLING, BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT)));

        // Reeds (plain blocks; no axis)
        PALE_REEDS_BLOCK = register("pale_reeds_block",
                new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.HAY_BLOCK).mapColor(MapColor.STONE)));
        DARK_REEDS_BLOCK = register("dark_reeds_block",
                new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.HAY_BLOCK).mapColor(MapColor.PODZOL)));

        // Coarse Farmland (full dirt-like block; can be tilled into farmland via hoe)
        COARSE_FARMLAND = register("coarse_farmland",
            new CoarseFarmlandBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COARSE_DIRT)));

    // Paper Lantern (like vanilla lantern but easier to break and uses painting place/break sounds)
    net.minecraft.sounds.SoundEvent paintPlace = net.minecraft.sounds.SoundEvents.PAINTING_PLACE;
    net.minecraft.sounds.SoundEvent paintBreak = net.minecraft.sounds.SoundEvents.PAINTING_BREAK;
    net.minecraft.world.level.block.SoundType paperSound = new net.minecraft.world.level.block.SoundType(
            1.0F, 1.0F,
            paintBreak,
            net.minecraft.sounds.SoundEvents.WOOL_STEP,
            paintPlace,
            net.minecraft.sounds.SoundEvents.WOOL_HIT,
            net.minecraft.sounds.SoundEvents.WOOL_FALL
    );

    PAPER_LANTERN = register("paper_lantern",
        new com.shioh.sengoku.block.PaperLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)
            .strength(0.2F)
            .sound(paperSound)));

    // Soul Paper Lantern (uses soul lantern base properties but same painting sounds)
    SOUL_PAPER_LANTERN = register("soul_paper_lantern",
        new com.shioh.sengoku.block.PaperLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_LANTERN)
            .strength(0.2F)
            .sound(paperSound)));

    // Stone Lantern (Japanese ishidōrō - ground only, must be lit with flint and steel)
    // Light level controlled by blockstate, starts unlit
    STONE_LANTERN = register("stone_lantern",
        new com.shioh.sengoku.block.StoneLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
            .strength(1.5F, 6.0F)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .lightLevel(state -> state.getValue(com.shioh.sengoku.block.StoneLanternBlock.LIT) ? 15 : 0)
            .sound(SoundType.STONE)));

    // Soul Stone Lantern (blue light variant)
    SOUL_STONE_LANTERN = register("soul_stone_lantern",
        new com.shioh.sengoku.block.StoneLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
            .strength(1.5F, 6.0F)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .lightLevel(state -> state.getValue(com.shioh.sengoku.block.StoneLanternBlock.LIT) ? 10 : 0)
            .sound(SoundType.STONE)));

    // Cobblestone Lantern
    COBBLESTONE_LANTERN = register("cobblestone_lantern",
        new com.shioh.sengoku.block.StoneLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE)
            .strength(1.5F, 6.0F)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .lightLevel(state -> state.getValue(com.shioh.sengoku.block.StoneLanternBlock.LIT) ? 15 : 0)
            .sound(SoundType.STONE)));

    // Mossy Cobblestone Lantern
    MOSSY_COBBLESTONE_LANTERN = register("mossy_cobblestone_lantern",
        new com.shioh.sengoku.block.StoneLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSSY_COBBLESTONE)
            .strength(1.5F, 6.0F)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .lightLevel(state -> state.getValue(com.shioh.sengoku.block.StoneLanternBlock.LIT) ? 15 : 0)
            .sound(SoundType.STONE)));

    // Limestone ore variants
    LIMESTONE_COAL_ORE = register("limestone_coal_ore",
        new DropExperienceBlock(UniformInt.of(0, 2),
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_RED_SANDSTONE)
                .requiresCorrectToolForDrops()));
    LIMESTONE_IRON_ORE = register("limestone_iron_ore",
        new DropExperienceBlock(UniformInt.of(0, 0),
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_RED_SANDSTONE)
                .requiresCorrectToolForDrops()));
    LIMESTONE_COPPER_ORE = register("limestone_copper_ore",
        new DropExperienceBlock(UniformInt.of(0, 2),
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_RED_SANDSTONE)
                .requiresCorrectToolForDrops()));
    LIMESTONE_GOLD_ORE = register("limestone_gold_ore",
        new DropExperienceBlock(UniformInt.of(0, 0),
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_RED_SANDSTONE)
                .requiresCorrectToolForDrops()));
    LIMESTONE_DIAMOND_ORE = register("limestone_diamond_ore",
        new DropExperienceBlock(UniformInt.of(3, 7),
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_RED_SANDSTONE)
                .requiresCorrectToolForDrops()));
    LIMESTONE_JADE_ORE = register("limestone_jade_ore",
        new DropExperienceBlock(UniformInt.of(3, 7),
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_RED_SANDSTONE)
                .requiresCorrectToolForDrops()));
    LIMESTONE_LAPIS_ORE = register("limestone_lapis_ore",
        new DropExperienceBlock(UniformInt.of(2, 5),
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_RED_SANDSTONE)
                .requiresCorrectToolForDrops()));
    LIMESTONE_REDSTONE_ORE = register("limestone_redstone_ore",
        new DropExperienceBlock(UniformInt.of(1, 5),
            BlockBehaviour.Properties.ofFullCopy(Blocks.SMOOTH_RED_SANDSTONE)
                .requiresCorrectToolForDrops()));

    // Cobbled Deepslate Lantern
    COBBLED_DEEPSLATE_LANTERN = register("cobbled_deepslate_lantern",
        new com.shioh.sengoku.block.StoneLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLED_DEEPSLATE)
            .strength(3.0F, 6.0F)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .lightLevel(state -> state.getValue(com.shioh.sengoku.block.StoneLanternBlock.LIT) ? 15 : 0)
            .sound(SoundType.DEEPSLATE)));

    // Stone Brick Lantern
    STONE_BRICK_LANTERN = register("stone_brick_lantern",
        new com.shioh.sengoku.block.StoneLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)
            .strength(2.0F, 6.0F)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .lightLevel(state -> state.getValue(com.shioh.sengoku.block.StoneLanternBlock.LIT) ? 15 : 0)
            .sound(SoundType.STONE)));

    // Mossy Stone Brick Lantern
    MOSSY_STONE_BRICK_LANTERN = register("mossy_stone_brick_lantern",
        new com.shioh.sengoku.block.StoneLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSSY_STONE_BRICKS)
            .strength(2.0F, 6.0F)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .lightLevel(state -> state.getValue(com.shioh.sengoku.block.StoneLanternBlock.LIT) ? 15 : 0)
            .sound(SoundType.STONE)));

    // Andesite Lantern
    ANDESITE_LANTERN = register("andesite_lantern",
        new com.shioh.sengoku.block.StoneLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE)
            .strength(1.5F, 6.0F)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .lightLevel(state -> state.getValue(com.shioh.sengoku.block.StoneLanternBlock.LIT) ? 15 : 0)
            .sound(SoundType.STONE)));

    // Diorite Lantern
    DIORITE_LANTERN = register("diorite_lantern",
        new com.shioh.sengoku.block.StoneLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DIORITE)
            .strength(1.5F, 6.0F)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .lightLevel(state -> state.getValue(com.shioh.sengoku.block.StoneLanternBlock.LIT) ? 15 : 0)
            .sound(SoundType.STONE)));

    // Tuff Lantern
    TUFF_LANTERN = register("tuff_lantern",
        new com.shioh.sengoku.block.StoneLanternBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TUFF)
            .strength(1.5F, 6.0F)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .lightLevel(state -> state.getValue(com.shioh.sengoku.block.StoneLanternBlock.LIT) ? 15 : 0)
            .sound(SoundType.STONE)));

    // Shide (wall-mounted paper streamer decoration, placed by rabbit's foot)
    SHIDE = registerBlockOnly("shide",
        new com.shioh.sengoku.block.ShideBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_BANNER)
            .strength(0.1F)
            .noCollission()
            .sound(paperSound)));

    // Straw ornament (placed by using wheat on a wall) - no item, consumed on placement
    STRAW_ORNAMENT = registerBlockOnly("straw_ornament",
        new com.shioh.sengoku.block.ShideBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HAY_BLOCK)
            .strength(0.1F)
            .noCollission()
            .sound(paperSound)));

    // Void Air (behaves like air but treated as structure void during world generation)
    // No item - cannot be obtained or placed by players, intended for WorldEdit use
    VOID_AIR = registerBlockOnly("void_air",
        new com.shioh.sengoku.block.VoidAirBlock(BlockBehaviour.Properties.of()
            .noCollission()
            .noLootTable()
            .air()));

                Constants.LOG.info(
                        "Registered " + SHOJI_DOORS.size() + " Shoji doors, "
                            + SHOJI_PANELS.size() + " Shoji panels, "
                            + SHOJI_FRAMES.size() + " Shoji frames, "
                                + TATAMI_BLOCKS.size() + " Tatami blocks, "
                    + TATAMI_MATS.size() + " Tatami mats, "
                    + SHIRASU_WALLS.size() + " White Shirasu-kabe blocks, "
                    + SHIRASU_YELLOWS.size() + " Yellow Shirasu-kabe blocks, "
                    + SHIKKUI_BLOCKS.size() + " Shikkui blocks, "
                    + LACQUERED_LOGS.size() + " Lacquered logs, "
                    + LACQUERED_WOODS.size() + " Lacquered woods, "
                    + "plus custom leaves, reeds, and furniture!"
                );
    }

    // === Shoji Double Door Helpers ===
    private static void registerShojiDoubleSet(String woodName, BlockSetType setType, Block baseDoor) {
        // plain double door
        String name = woodName + "_double_shoji_door";
        Block block = new ShojiDoubleDoorBlock(setType, BlockBehaviour.Properties.ofFullCopy(baseDoor));
        register(name, block);
        SHOJI_DOORS.put(name, block);

        // checkered double door
        String checkName = woodName + "_checkered_double_shoji_door";
        Block checkBlock = new ShojiDoubleDoorBlock(setType, BlockBehaviour.Properties.ofFullCopy(baseDoor));
        register(checkName, checkBlock);
        SHOJI_DOORS.put(checkName, checkBlock);

        // paly double door
        String palyName = woodName + "_paly_double_shoji_door";
        Block palyBlock = new ShojiDoubleDoorBlock(setType, BlockBehaviour.Properties.ofFullCopy(baseDoor));
        register(palyName, palyBlock);
        SHOJI_DOORS.put(palyName, palyBlock);
    }

    // === Shoji Triple Door Helpers ===
    private static void registerShojiTripleSet(String woodName, BlockSetType setType, Block baseDoor) {
        registerShojiTriple(woodName + "_triple_shoji_door", setType, baseDoor);
        registerShojiTriple(woodName + "_checkered_triple_shoji_door", setType, baseDoor);
        registerShojiTriple(woodName + "_paly_triple_shoji_door", setType, baseDoor);
    }

    private static void registerShojiTriple(String name, BlockSetType setType, Block baseDoor) {
        Block block = new ShojiTripleDoorBlock(setType, BlockBehaviour.Properties.ofFullCopy(baseDoor));
        register(name, block);
        SHOJI_DOORS.put(name, block);
    }

    // === Shoji Door Helpers ===
    private static void registerShojiSet(String woodName, BlockSetType setType, Block baseDoor) {
        registerShoji(woodName + "_shoji_door", setType, baseDoor);
        registerShoji(woodName + "_checkered_shoji_door", setType, baseDoor);
        registerShoji(woodName + "_paly_shoji_door", setType, baseDoor);
    }

    private static void registerShoji(String name, BlockSetType setType, Block baseDoor) {
        Block block = new ShojiDoorBlock(setType, BlockBehaviour.Properties.ofFullCopy(baseDoor));
        register(name, block);
        SHOJI_DOORS.put(name, block);
    }

    // === Shoji Panel Helpers ===
    private static void registerShojiTrapdoorSet(String woodName, BlockSetType setType, Block baseTrapdoor) {
        registerShojiTrapdoor(woodName + "_shoji_panel", setType, baseTrapdoor);
        registerShojiTrapdoor(woodName + "_checkered_shoji_panel", setType, baseTrapdoor);
        registerShojiTrapdoor(woodName + "_paly_shoji_panel", setType, baseTrapdoor);
    }

    private static void registerShojiTrapdoor(String name, BlockSetType setType, Block baseTrapdoor) {
        Block block = new ShojiTrapdoorBlock(setType,
                BlockBehaviour.Properties.ofFullCopy(baseTrapdoor).noOcclusion());
        register(name, block);
        SHOJI_PANELS.put(name, block);
    }

    // === Shoji Frame (pane) Helpers ===
    private static void registerShojiFrameSet(String woodName, BlockSetType setType, Block basePane) {
        registerShojiFrame(woodName + "_shoji_frame", setType, basePane);
        registerShojiFrame(woodName + "_checkered_shoji_frame", setType, basePane);
        registerShojiFrame(woodName + "_paly_shoji_frame", setType, basePane);
    }

    private static void registerShojiFrame(String name, BlockSetType setType, Block basePane) {
        // For normal/checkered/paly frames use the variant with DAMAGED and AGED properties
        Block block = new com.shioh.sengoku.block.ShojiFrameVariantBlock(
                BlockBehaviour.Properties.ofFullCopy(basePane).noOcclusion(),
                shouldPlaceAsMaxDamaged(name));
        register(name, block);
        SHOJI_FRAMES.put(name, block);
    }

    private static boolean shouldPlaceAsMaxDamaged(String frameName) {
        return switch (frameName) {
            case "bamboo_shoji_frame",
                    "bamboo_checkered_shoji_frame",
                    "bamboo_paly_shoji_frame",
                    "mangrove_checkered_shoji_frame",
                    "mangrove_paly_shoji_frame",
                    "dark_cedar_paly_shoji_frame",
                    "kiso_paly_shoji_frame",
                    "oak_paly_shoji_frame",
                    "weeping_willow_shoji_frame",
                    "weeping_willow_paly_shoji_frame",
                    "black_pine_checkered_shoji_frame",
                    "bloodgood_checkered_shoji_frame",
                    "keyaki_checkered_shoji_frame" -> true;
            default -> false;
        };
    }

    // === Covered Shoji Frame Helpers ===
    // Register exactly one covered shoji frame per wood (no additional covered variants)
    private static void registerCoveredShojiFrameSet(String woodName, BlockSetType setType, Block basePane) {
        registerCoveredShojiFrame(woodName + "_covered_shoji_frame", setType, basePane);
    }

    private static void registerCoveredShojiFrame(String name, BlockSetType setType, Block basePane) {
        Block block = new ShojiFrameBlock(BlockBehaviour.Properties.ofFullCopy(basePane).noOcclusion().strength(0.2F));
        register(name, block);
        // covered frames are stored in the main SHOJI_FRAMES map
        SHOJI_FRAMES.put(name, block);
    }

    // === Tatami Helpers ===
    private static void registerTatamiSet(String woodName) {
        String name = woodName + "_tatami_block";
        Block block = new TatamiBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));
        register(name, block);
        TATAMI_BLOCKS.put(name, block);
    }

    private static void registerTatamiMatSet(String woodName) {
        String name = woodName + "_tatami_mat";
        Block block = new TatamiMatBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB));
        register(name, block);
        TATAMI_MATS.put(name, block);
    }

    // === White Shirasu-kabe Helpers ===
    // These are terracotta-like solid blocks named <wood>_white_shirasu_kabe
    private static void registerShirasuSet(String woodName) {
        String name = woodName + "_white_shirasu_kabe";
        Block block = new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.TERRACOTTA));
        register(name, block);
        SHIRASU_WALLS.put(name, block);
    }

    // Register a yellow variant for a subset of woods. Uses yellow terracotta base.
    private static void registerShirasuYellowSet(String woodName) {
        String name = woodName + "_yellow_shirasu_kabe";
        Block block = new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.YELLOW_TERRACOTTA));
        register(name, block);
        SHIRASU_YELLOWS.put(name, block);
    }

    // === Shikkui Block Helpers ===
    // These are white plaster-like solid blocks. Uses block of quartz texture but applies to wood types.
    private static void registerShikkuiSet(String woodName) {
        String name = woodName + "_shikkui";
        Block block = new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK));
        register(name, block);
        SHIKKUI_BLOCKS.put(name, block);
    }

    // === Shared Registry Helper ===
    private static Block register(String name, Block block) {
        sengokuFabric.REGISTRARS.get(Registries.BLOCK).register(name, () -> block);
        sengokuFabric.ITEM_REGISTRAR.register(name, () -> new BlockItem(block, new Item.Properties()));
        return block;
    }

    // Register a block without an item (used for shide, which is placed by rabbit's foot)
    private static Block registerBlockOnly(String name, Block block) {
        sengokuFabric.REGISTRARS.get(Registries.BLOCK).register(name, () -> block);
        return block;
    }

    // === Lacquered Helpers ===
    private static void registerLacqueredSet(String woodName, Block baseLog, Block baseWood) {
        // lacquered_<wood>_log
        String logName = "lacquered_" + woodName + "_log";
        Block lacqueredLog = new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(baseLog));
        register(logName, lacqueredLog);
        LACQUERED_LOGS.put(logName, lacqueredLog);

        // lacquered_<wood>_wood
        String woodNameFull = "lacquered_" + woodName + "_wood";
        Block lacqueredWood = new net.minecraft.world.level.block.RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(baseWood));
        register(woodNameFull, lacqueredWood);
        LACQUERED_WOODS.put(woodNameFull, lacqueredWood);
    }
}
