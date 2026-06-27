package com.shioh.sengoku.villager;

import com.shioh.sengoku.init.TansuItemReg;
import com.shioh.sengoku.villager.VillagerTradeLoader.ItemsForEmeraldsTrade;
import com.shioh.sengoku.init.TamahaganeItemReg;
import com.shioh.sengoku.init.HatItemReg;
import com.shioh.sengoku.init.SakeItemReg;
import com.shioh.sengoku.registry.SengokuBlocks;
import com.shioh.sengoku.item.TantoItem;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.core.registries.BuiltInRegistries;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.entity.Entity;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;

/**
 * Villager trading system - Exact carbon copy of vanilla Minecraft trades.
 * Based on Minecraft Wiki trading data (Java Edition 1.21.4+)
 * Provides a searchable, modifiable framework for all villager profession trades.
 */
public class VillagerTradeLoader {
    
    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            try {
                // Register trades for each profession - vanilla exact copies
                addFarmerTrades();
                addClericTrades();
                addArmorerTrades();
                addButcherTrades();
                addLeatherworkerTrades();
                
                System.out.println("[Sengoku] Vanilla villager trades registered successfully");
            } catch (Exception e) {
                System.err.println("[Sengoku] Failed to register villager trades: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * FARMER TRADES - Composter job site block
     * Buys crops, sells food
     */
    @SuppressWarnings("unchecked")
    private static void addFarmerTrades() {
        try {
            var map = VillagerTrades.TRADES.computeIfAbsent(VillagerProfession.FARMER, 
                k -> new it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap<>());
            map.clear();
            
            // Level 1 - Novice
            map.put(1, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.WHEAT, 15, 1, 16, 2),
                new EmeraldsForItemsTrade(Items.POTATO, 15, 1, 16, 2),
                new EmeraldsForItemsTrade(Items.CARROT, 15, 1, 16, 2),
                new EmeraldsForItemsTrade(Items.BEETROOT, 15, 1, 16, 2),
                new EmeraldsForItemsTrade(TansuItemReg.RICE_I, 25, 1, 16, 2),
                new ItemsForEmeraldsTrade(new ItemStack(Items.BREAD), 1, 6, 16, 1, 0.05F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.BAKED_POTATO), 1, 6, 16, 1, 0.05F),

            });
            
            // Level 2 - Apprentice
            map.put(2, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.PUMPKIN, 6, 1, 12, 10),
                new ItemsForEmeraldsTrade(new ItemStack(Items.RABBIT_STEW), 1, 1, 12, 1, 0.05F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.PUMPKIN_PIE), 1, 4, 12, 5, 0.05F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.APPLE), 1, 4, 16, 5, 0.05F)
            });
            
            // Level 3 - Journeyman
            map.put(3, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(HatItemReg.STRAW_HAT, 1, 1, 2, 4),
                new ItemsForEmeraldsTrade(new ItemStack(Items.COOKIE), 3, 18, 12, 10, 0.05F)
            });
            
            // Level 4 - Expert
            map.put(4, new VillagerTrades.ItemListing[] {
                new ItemsForEmeraldsTrade(new ItemStack(SakeItemReg.SAKE_BOTTLE), 1, 1, 12, 15, 0.05F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.CAKE), 1, 1, 12, 15, 0.05F)
            });
            
            // Level 5 - Master
            map.put(5, new VillagerTrades.ItemListing[] {
                new ItemsForEmeraldsTrade(new ItemStack(Items.GOLDEN_CARROT), 3, 3, 12, 30, 0.05F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.GLISTERING_MELON_SLICE), 4, 3, 12, 30, 0.05F)
            });
            
        } catch (Exception e) {
            System.err.println("Failed to add farmer trades: " + e.getMessage());
        }
    }
    
    /**
     * CLERIC TRADES - Brewing Stand job site block
     * Buys nether items, sells magic items
     */
    @SuppressWarnings("unchecked")
    private static void addClericTrades() {
        try {
            var map = VillagerTrades.TRADES.computeIfAbsent(VillagerProfession.CLERIC, 
                k -> new it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap<>());
            map.clear();
            
            // Level 1 - Novice
            map.put(1, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.WHEAT, 15, 1, 16, 2),
                new ItemsForEmeraldsTrade(new ItemStack(Items.REDSTONE), 1, 2, 12, 1, 0.05F)
            });
            
            // Level 2 - Apprentice
            map.put(2, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.GOLD_INGOT, 3, 1, 12, 10),
                new ItemsForEmeraldsTrade(new ItemStack(Items.LAPIS_LAZULI), 1, 1, 12, 5, 0.05F)
            });
            
            // Level 3 - Journeyman
            map.put(3, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.RABBIT_FOOT, 10, 1, 12, 20),
                new EmeraldsForItemsTrade(HatItemReg.TENGAI_HAT, 1, 1, 12, 20),
                new ItemsForEmeraldsTrade(new ItemStack(Items.WHITE_WOOL), 4, 10, 12, 10, 0.05F)
            });
            
            // Level 4 - Expert
            map.put(4, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(BuiltInRegistries.ITEM.get(sengokuFabric.asId("diorite_lantern")), 1, 1, 12, 30),
                new ItemsForEmeraldsTrade(new ItemStack(Items.ENDER_PEARL), 5, 1, 12, 15, 0.05F)
            });
            
            // Level 5 - Master
            map.put(5, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.NETHER_WART, 22, 1, 12, 30),
                new ItemsForEmeraldsTrade(new ItemStack(Items.BLAZE_POWDER), 1, 2, 12, 15, 0.05F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.EXPERIENCE_BOTTLE), 3, 1, 12, 30, 0.05F)
            });
            
        } catch (Exception e) {
            System.err.println("Failed to add cleric trades: " + e.getMessage());
        }
    }
    
    /**
     * ARMORER TRADES - Blast Furnace job site block
     * Buys coal/ingots/diamonds, sells armor
     */
    @SuppressWarnings("unchecked")
    private static void addArmorerTrades() {
        try {
            var map = VillagerTrades.TRADES.computeIfAbsent(VillagerProfession.ARMORER, 
                k -> new it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap<>());
            map.clear();
            
            // Level 1 - Novice - Iron armor
            map.put(1, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.COAL, 15, 1, 16, 2),
                new ItemsForEmeraldsTrade(new ItemStack(Items.CHAINMAIL_HELMET), 1, 1, 12, 5, 0.2F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 1, 1, 12, 5, 0.2F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.CHAINMAIL_LEGGINGS), 1, 1, 12, 5, 0.2F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.CHAINMAIL_BOOTS), 1, 1, 12, 5, 0.2F)
            });
            
            // Level 2 - Apprentice - Bell and Chainmail
            map.put(2, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.IRON_INGOT, 5, 1, 12, 10),
                new EmeraldsForItemsTrade(Items.COPPER_INGOT, 10, 1, 12, 10),
                new ItemsForEmeraldsTrade(new ItemStack(Items.IRON_LEGGINGS), 3, 1, 12, 7, 0.2F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.IRON_BOOTS), 2, 1, 12, 7, 0.2F)
            });
            
            // Level 3 - Journeyman - More Chainmail and Shield
            map.put(3, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.LAVA_BUCKET, 1, 1, 12, 20),
                new EmeraldsForItemsTrade(TamahaganeItemReg.TAMAHAGANE, 1, 1, 12, 20),
                new ItemsForEmeraldsTrade(new ItemStack(Items.IRON_HELMET), 2, 1, 12, 10, 0.2F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.IRON_CHESTPLATE), 4, 1, 12, 10, 0.2F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.SHIELD), 5, 1, 12, 10, 0.2F)
            });
            
            // Level 4 - Expert - Enchanted Diamond Armor
            map.put(4, new VillagerTrades.ItemListing[] {
                new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_HELMET, 17, 3, 15, 0.2F),
                new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_LEGGINGS, 16, 3, 15, 0.2F)
            });
            
            // Level 5 - Master
            map.put(5, new VillagerTrades.ItemListing[] {
                new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_CHESTPLATE, 21, 3, 15, 0.2F),
                new VillagerTrades.EnchantedItemForEmeralds(Items.DIAMOND_BOOTS, 13, 3, 30, 0.2F)
            });
            
        } catch (Exception e) {
            System.err.println("Failed to add armorer trades: " + e.getMessage());
        }
    }
        @SuppressWarnings("unchecked")
    private static void addLeatherworkerTrades() {
        try {
            var map = VillagerTrades.TRADES.computeIfAbsent(VillagerProfession.LEATHERWORKER, 
                k -> new it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap<>());
            map.clear();
            
            // Level 1 - Novice
            map.put(1, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.STRING, 14, 1, 16, 3),
                new ItemsForEmeraldsTrade(new ItemStack(Items.LEATHER_BOOTS), 1, 1, 12, 5, 0.2F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.LEATHER), 1, 2, 12, 3, 0.05F),
            });
            
            // Level 2 - Apprentice
            map.put(2, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.FEATHER, 15, 1, 16, 6),
                new EmeraldsForItemsTrade(Items.RABBIT_HIDE, 14, 1, 16, 3),
            });
            
            // Level 3 - Journeyman
            map.put(3, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.BONE, 15, 1, 16, 2),
                new ItemsForEmeraldsTrade(new ItemStack(Items.BUNDLE), 1, 1, 3, 5, 0.2F),
            });
            
            // Level 4 - Expert
            map.put(4, new VillagerTrades.ItemListing[] {
                new ItemsForEmeraldsTrade(new ItemStack(BuiltInRegistries.ITEM.get(sengokuFabric.asId("diamond_tanto"))), 1, 1, 12, 30, 0.2F),
                new ItemsForEmeraldsTrade(new ItemStack(BuiltInRegistries.ITEM.get(sengokuFabric.asId("tattered_shinobi_cloth"))), 1, 1, 12, 20, 0.2F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.TURTLE_SCUTE), 5, 1, 16, 7, 0.05F),
            });
            
            // Level 5 - Master
            map.put(5, new VillagerTrades.ItemListing[] {
                new ItemsForEmeraldsTrade(new ItemStack(Items.SHULKER_SHELL), 10, 1, 12, 30, 0.2F)
            });
            
        } catch (Exception e) {
            System.err.println("Failed to add leatherworker trades: " + e.getMessage());
        }
    }
    
    /**
     * BUTCHER TRADES - Smoker job site block
     * Buys raw meat, sells cooked meat
     */
    @SuppressWarnings("unchecked")
    private static void addButcherTrades() {
        try {
            var map = VillagerTrades.TRADES.computeIfAbsent(VillagerProfession.BUTCHER, 
                k -> new it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap<>());
            map.clear();
            
            // Level 1 - Novice
            map.put(1, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.ROTTEN_FLESH, 14, 1, 16, 2),
                new EmeraldsForItemsTrade(Items.SPIDER_EYE, 4, 1, 16, 2),
                new EmeraldsForItemsTrade(Items.PORKCHOP, 7, 1, 16, 2)
            });
            
            // Level 2 - Apprentice
            map.put(2, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.COAL, 15, 1, 16, 2),
                new ItemsForEmeraldsTrade(new ItemStack(Items.COOKED_CHICKEN), 1, 8, 16, 5, 0.05F),
                new ItemsForEmeraldsTrade(new ItemStack(Items.COOKED_PORKCHOP), 1, 5, 16, 5, 0.05F)
            });
            
            // Level 3 - Journeyman
            map.put(3, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.BEEF, 1, 10, 16, 20),
                new EmeraldsForItemsTrade(Items.MUTTON, 1, 7, 16, 20)
            });
            
            // Level 4 - Expert
            map.put(4, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.DRIED_KELP_BLOCK, 10, 1, 12, 30)
            });
            
            // Level 5 - Master
            map.put(5, new VillagerTrades.ItemListing[] {
                new EmeraldsForItemsTrade(Items.SWEET_BERRIES, 10, 1, 12, 30)
            });
            
        } catch (Exception e) {
            System.err.println("Failed to add butcher trades: " + e.getMessage());
        }
    }
    
    // ============================================
    // Trade Helper Classes
    // ============================================
    
    /**
     * Trade: Villager sells items for emeralds
     * Example: ItemsForEmeraldsTrade(new ItemStack(Items.IRON_HELMET), 5, 1, 12, 1, 0.2F)
     *   - Requires 5 emeralds
     *   - Returns 1 iron helmet
     *   - Can be used 12 times
     *   - Gives 1 XP to villager
     *   - Price multiplier 0.2F
     */
    public static class ItemsForEmeraldsTrade implements VillagerTrades.ItemListing {
        private final ItemStack sellingItem;
        private final int emeraldCount;
        private final int sellingItemCount;
        private final int maxUses;
        private final int xpValue;
        private final float priceMultiplier;

        public ItemsForEmeraldsTrade(ItemStack sellingItem, int emeraldCount, int sellingItemCount, 
                                     int maxUses, int xpValue, float priceMultiplier) {
            this.sellingItem = sellingItem;
            this.emeraldCount = emeraldCount;
            this.sellingItemCount = sellingItemCount;
            this.maxUses = maxUses;
            this.xpValue = xpValue;
            this.priceMultiplier = priceMultiplier;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource rand) {
            return new MerchantOffer(
                new ItemCost(Items.EMERALD, this.emeraldCount),
                new ItemStack(this.sellingItem.getItem(), this.sellingItemCount),
                this.maxUses,
                this.xpValue,
                this.priceMultiplier
            );
        }
    }
    
    /**
     * Trade: Villager buys items for emeralds
     * Example: EmeraldsForItemsTrade(Items.WHEAT, 1, 20, 16, 2)
     *   - Buys 20 wheat items from player
     *   - Gives 1 emerald
     *   - Can be used 16 times
     *   - Gives 2 XP to villager
     */
    public static class EmeraldsForItemsTrade implements VillagerTrades.ItemListing {
        private final net.minecraft.world.item.Item tradeItem;
        private final int itemQuantity;
        private final int emeraldCount;
        private final int maxUses;
        private final int xpValue;
        private final float priceMultiplier;

        public EmeraldsForItemsTrade(net.minecraft.world.item.Item tradeItem, int itemQuantity,
                                    int emeraldCount, int maxUses, int xpValue) {
            this.tradeItem = tradeItem;
            this.itemQuantity = itemQuantity;
            this.emeraldCount = emeraldCount;
            this.maxUses = maxUses;
            this.xpValue = xpValue;
            this.priceMultiplier = 0.05F;
        }

        @Override
        public MerchantOffer getOffer(Entity trader, RandomSource rand) {
            return new MerchantOffer(
                new ItemCost(this.tradeItem, this.itemQuantity),
                new ItemStack(Items.EMERALD, this.emeraldCount),
                this.maxUses,
                this.xpValue,
                this.priceMultiplier
            );
        }
    }
    
}
