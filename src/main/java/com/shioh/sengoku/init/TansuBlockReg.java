package com.shioh.sengoku.init;

import com.shioh.sengoku.sengokuFabric;
import com.shioh.sengoku.block.*;
import com.shioh.sengoku.block.entity.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TansuBlockReg {

    // --- Simple blocks ---
    public static final FishingNet FISHING_NET = new FishingNet(
            BlockBehaviour.Properties.of()
                    .strength(0.5f)
                    .noOcclusion()
                    .sound(SoundType.WOOD)
    );

    public static final Basket BASKET = new Basket(
            BlockBehaviour.Properties.of()
                    .strength(0.5f)
                    .noOcclusion()
                    .sound(SoundType.WOOD)
    );

    public static final BoilingPot BOILING_POT = new BoilingPot();

    // --- Tansu blocks ---
    public static final Tansu OAK_TANSU = new Tansu(MapColor.WOOD, "oak");
    public static final Tansu SPRUCE_TANSU = new Tansu(MapColor.COLOR_GRAY, "black_pine");
    public static final Tansu BIRCH_TANSU = new Tansu(MapColor.SAND, "birch");
    public static final Tansu JUNGLE_TANSU = new Tansu(MapColor.DIRT, "kiso");
    public static final Tansu ACACIA_TANSU = new Tansu(MapColor.COLOR_ORANGE, "keyaki");
    public static final Tansu DARK_OAK_TANSU = new Tansu(MapColor.COLOR_BROWN, "dark_cedar");
    public static final Tansu MANGROVE_TANSU = new Tansu(MapColor.COLOR_RED, "mangrove");
    public static final Tansu CHERRY_TANSU = new Tansu(MapColor.TERRACOTTA_WHITE, SoundType.CHERRY_WOOD, "sakura");
    public static final Tansu BAMBOO_TANSU = new Tansu(MapColor.COLOR_YELLOW, SoundType.BAMBOO_WOOD, "bamboo");
    public static final Tansu CRIMSON_TANSU = new Tansu(MapColor.CRIMSON_STEM, SoundType.NETHER_WOOD, "bloodgood");
    public static final Tansu WARPED_TANSU = new Tansu(MapColor.WARPED_STEM, SoundType.NETHER_WOOD, "weeping_willow");

    // --- Sake Barrel ---
    public static final SakeBarrel SAKE_BARREL = new SakeBarrel(MapColor.STONE, SoundType.WOOD);
    
                // --- Sake Brewery ---
                public static final SakeBreweryBlock SAKE_BREWERY = new SakeBreweryBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).sound(SoundType.WOOD).lightLevel(state -> state.getValue(com.shioh.sengoku.block.SakeBreweryBlock.LIT) ? 13 : 0));

    // --- Crops ---
public static final RiceCropBlock RICE_CROP = new RiceCropBlock(
        BlockBehaviour.Properties.of() // <-- no arguments here
                .mapColor(MapColor.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
);

public static final RamieCropBlock RAMIE_CROP = new RamieCropBlock(
        BlockBehaviour.Properties.of() // <-- no arguments here
                .mapColor(MapColor.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
);

public static final TeaCropBlock TEA_CROP = new TeaCropBlock(
        BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
);


    // --- Block Entities ---
    public static BlockEntityType<TansuEntity> MORE_TANSU_BLOCK_ENTITY;
    public static BlockEntityType<SakeBarrelEntity> SAKE_BARREL_BLOCK_ENTITY;
        public static BlockEntityType<SakeBreweryEntity> SAKE_BREWERY_BLOCK_ENTITY;
    public static BlockEntityType<BoilingPotEntity> BOILING_POT_BLOCK_ENTITY;

    public static final List<Block> tansus = new ArrayList<>();

        // Small custom BlockItem for the Sake Brewery so we can add a tooltip
        public static class SakeBreweryBlockItem extends BlockItem {
                public SakeBreweryBlockItem(Block block, Properties settings) {
                        super(block, settings);
                }

                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
                        super.appendHoverText(stack, context, tooltip, flag);
                                        try {
                                                tooltip.add(net.minecraft.network.chat.Component.translatable("block.sengoku.sake_brewery.tooltip").withStyle(net.minecraft.ChatFormatting.GRAY));
                                        } catch (Throwable t) {
                                                // ignore
                        }
                }
        }

    // --- Custom BlockItem for crops to fix middle-click ---
    public static class CropBlockItem extends BlockItem {
        private final Item cropItem;

        public CropBlockItem(Block block, Item cropItem, Properties settings) {
            super(block, settings);
            this.cropItem = cropItem;
        }

        public ItemStack getPickStack(BlockGetter world, BlockPos pos, BlockState state) {
            return new ItemStack(cropItem);
        }
    }

    public static void registerBlocks() {
        // --- Register Tansus ---
        registerTansuBlocks();

        // --- Register Tansu block entities ---
        MORE_TANSU_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                sengokuFabric.asId("tansu_cabinet"),
                BlockEntityType.Builder.of(TansuEntity::new, tansus.toArray(Block[]::new)).build(null)
        );

        // --- Register Sake Barrel ---
        Registry.register(BuiltInRegistries.BLOCK, sengokuFabric.asId("sake_barrel"), SAKE_BARREL);
        SAKE_BARREL_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                sengokuFabric.asId("sake_barrel"),
                BlockEntityType.Builder.of(SakeBarrelEntity::new, SAKE_BARREL).build(null)
        );
        // Item registration moved to TansuItemReg.java to avoid duplicate registration

        // --- Register Sake Brewery ---
        Registry.register(BuiltInRegistries.BLOCK, sengokuFabric.asId("sake_brewery"), SAKE_BREWERY);
        SAKE_BREWERY_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                sengokuFabric.asId("sake_brewery"),
                BlockEntityType.Builder.of(SakeBreweryEntity::new, SAKE_BREWERY).build(null)
        );
                final Item sakeBreweryItem = new SakeBreweryBlockItem(SAKE_BREWERY, new Item.Properties());
                Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("sake_brewery"), sakeBreweryItem);
                // Place the Sake Brewery block item into the Functional Blocks creative tab
                net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents.modifyEntriesEvent(net.minecraft.world.item.CreativeModeTabs.FUNCTIONAL_BLOCKS)
                        .register(entries -> entries.addAfter(net.minecraft.world.item.Items.BREWING_STAND, sakeBreweryItem));

        // --- Register simple blocks ---
        Registry.register(BuiltInRegistries.BLOCK, sengokuFabric.asId("fishing_net"), FISHING_NET);
        // Item registration moved to TansuItemReg.java to avoid duplicate registration

        Registry.register(BuiltInRegistries.BLOCK, sengokuFabric.asId("basket"), BASKET);
        // Item registration moved to TansuItemReg.java to avoid duplicate registration

        Registry.register(BuiltInRegistries.BLOCK, sengokuFabric.asId("boiling_pot"), BOILING_POT);
        BOILING_POT_BLOCK_ENTITY = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                sengokuFabric.asId("boiling_pot"),
                BlockEntityType.Builder.of(BoilingPotEntity::new, BOILING_POT).build(null)
        );
        // Item registration moved to TansuItemReg.java to avoid duplicate registration

        // --- Register Crops with custom BlockItem using TansuItemReg ---
        Registry.register(BuiltInRegistries.BLOCK, sengokuFabric.asId("rice_crop"), RICE_CROP);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("rice_crop"),
                new CropBlockItem(RICE_CROP, TansuItemReg.RICE_I, new Item.Properties()));

        Registry.register(BuiltInRegistries.BLOCK, sengokuFabric.asId("ramie_crop"), RAMIE_CROP);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("ramie_crop"),
                new CropBlockItem(RAMIE_CROP, TansuItemReg.RAMIE_I, new Item.Properties()));

        Registry.register(BuiltInRegistries.BLOCK, sengokuFabric.asId("tea_crop"), TEA_CROP);
        Registry.register(BuiltInRegistries.ITEM, sengokuFabric.asId("tea_crop"),
                new CropBlockItem(TEA_CROP, TansuItemReg.TEA_LEAF, new Item.Properties()));
    }

    private static void registerTansuBlocks() {
        registerBlock(OAK_TANSU);
        registerBlock(SPRUCE_TANSU);
        registerBlock(BIRCH_TANSU);
        registerBlock(JUNGLE_TANSU);
        registerBlock(ACACIA_TANSU);
        registerBlock(DARK_OAK_TANSU);
        registerBlock(MANGROVE_TANSU);
        registerBlock(CHERRY_TANSU);
        registerBlock(BAMBOO_TANSU);
        registerBlock(CRIMSON_TANSU);
        registerBlock(WARPED_TANSU);
    }

    private static void registerBlock(Tansu tansu) {
        Registry.register(
                BuiltInRegistries.BLOCK,
                sengokuFabric.asId(tansu.tansuWoodType + "_tansu_cabinet"),
                tansu
        );
        tansus.add(tansu);
        // Item registration moved to TansuItemReg.java to avoid duplicate registration
    }
}
