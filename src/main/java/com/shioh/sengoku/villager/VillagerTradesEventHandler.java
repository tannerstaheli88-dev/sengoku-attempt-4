package com.shioh.sengoku.villager;

import com.shioh.sengoku.init.TansuItemReg;
import com.shioh.sengoku.init.SakeItemReg;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.npc.VillagerProfession;

/**
 * Example/Reference event handler for Villager Profession-specific trades.
 * 
 * This file is provided as OPTIONAL reference code showing how to implement
 * profession-specific villager trades if you want to use Fabric's merchant trading system
 * or if you add NeoForge compatibility.
 * 
 * IMPORTANT: This is NOT CURRENTLY USED. The main villager system is in VillagerTradeLoader.java
 * which uses VillagerInteractionRegistries for item recognition and collecting.
 * 
 * To activate this handler:
 * 1. Uncomment the @SubscribeEvent method
 * 2. Ensure your tracking event bus registration
 * 3. Add event listener pattern to sengokuFabric.java if using event bus
 * 
 * EXAMPLE TRADES COMMENTED OUT BELOW - uncomment and adapt as needed
 */
public class VillagerTradesEventHandler {
    
    /**
     * EXAMPLE: Add custom trades to specific villager professions
     * 
     * This example shows how to add trades if using NeoForge's VillagerTradesEvent
     * or Fabric's equivalent merchant event.
     * 
     * Profession Trade Level Reference (vanilla):
     * - Level 1 (novice): Generally 1 trade pair
     * - Level 2 (apprentice): 1-2 trade pairs
     * - Level 3 (journeyman): 2-3 trade pairs
     * - Level 4 (expert): 2-3 trade pairs
     * - Level 5 (master): 2-3 trade pairs
     * 
     * Note: Each level corresponds to a different trade list index (0-4)
     */
    public void exampleTradeSetupMethods() {
        /*
        EXAMPLE 1: BUTCHER - Buy Rotten Flesh
        
        public static void setupButcherTrades(VillagerTradesEvent event) {
            if (event.getType() == VillagerProfession.BUTCHER) {
                // Add to Journeyman level (level 3, index 2)
                var trades = event.getTrades().get(2);
                
                // Villager buys 1 rotten flesh for 1 emerald
                trades.add(new VillagerTradeLoader.EmeraldsForItemsTrade(
                    Items.ROTTEN_FLESH,
                    1,      // emeralds per flesh
                    12,     // max uses
                    2       // xp reward
                ));
                
                event.getTrades().put(2, trades);
            }
        }
        
        
        EXAMPLE 2: FARMER - Sell Rice/Tea
        
        public static void setupFarmerTrades(VillagerTradesEvent event) {
            if (event.getType() == VillagerProfession.FARMER) {
                // Add to Expert level (level 4, index 3)
                var trades = event.getTrades().get(3);
                
                // Villager sells rice for 2 emeralds
                trades.add(new VillagerTradeLoader.ItemsForEmeraldsTrade(
                    new ItemStack(TansuItemReg.RICE_I),
                    2,      // emeralds required
                    8,      // count of items given
                    8,      // max uses
                    3,      // xp reward
                    0.05F   // price multiplier
                ));
                
                // Villager buys tea seeds for 1 emerald
                trades.add(new VillagerTradeLoader.EmeraldsForItemsTrade(
                    TansuItemReg.TEA_SEEDS,
                    1,      // emeralds given
                    12,     // max uses
                    2       // xp reward
                ));
                
                event.getTrades().put(3, trades);
            }
        }
        
        
        EXAMPLE 3: CLERIC - Fermented Sake Trading
        
        public static void setupClericTrades(VillagerTradesEvent event) {
            if (event.getType() == VillagerProfession.CLERIC) {
                // Add to Master level (level 5, index 4)
                var trades = event.getTrades().get(4);
                
                // Villager buys fermented sake for emeralds
                trades.add(new VillagerTradeLoader.EmeraldsForItemsTrade(
                    SakeItemReg.FERMENTED_SAKE_BOTTLE,
                    3,      // emeralds per bottle
                    8,      // max uses
                    5       // xp reward (high for rare item)
                ));
                
                // Villager sells healing potion variant
                trades.add(new VillagerTradeLoader.ItemsForEmeraldsTrade(
                    new ItemStack(SakeItemReg.SAKE_HEALING),
                    4,      // emeralds required
                    1,      // count given
                    4,      // max uses
                    3,      // xp reward
                    0.05F   // price multiplier
                ));
                
                event.getTrades().put(4, trades);
            }
        }
        
        
        EXAMPLE 4: LIBRARIAN - Rare Item Trading
        
        public static void setupLibrarianTrades(VillagerTradesEvent event) {
            if (event.getType() == VillagerProfession.LIBRARIAN) {
                // Add to Journeyman level
                var trades = event.getTrades().get(2);
                
                // Sell tea leaves
                trades.add(new VillagerTradeLoader.ItemsForEmeraldsTrade(
                    new ItemStack(TansuItemReg.TEA_LEAF),
                    1,
                    4,
                    8,
                    2,
                    0.05F
                ));
                
                event.getTrades().put(2, trades);
            }
        }
        
        
        EXAMPLE 5: FISHERMAN - Tea & Sake Trading
        
        public static void setupFishermanTrades(VillagerTradesEvent event) {
            if (event.getType() == VillagerProfession.FISHERMAN) {
                var trades = event.getTrades().get(2);
                
                // Buy tea for emeralds
                trades.add(new VillagerTradeLoader.EmeraldsForItemsTrade(
                    TansuItemReg.TEA_LEAF,
                    2,
                    10,
                    2
                ));
                
                // Sell sake bottle
                trades.add(new VillagerTradeLoader.ItemsForEmeraldsTrade(
                    new ItemStack(SakeItemReg.SAKE_BOTTLE),
                    3,
                    1,
                    6,
                    4,
                    0.05F
                ));
                
                event.getTrades().put(2, trades);
            }
        }
        */
    }
    
    /**
     * ACTIVATION INSTRUCTIONS:
     * 
     * If you want to use specific profession trades, you have two options:
     * 
     * Option A: Use Fabric API (if available)
     * - Implement ServerLoadCallback or similar
     * - Hook into merchant/villager events
     * 
     * Option B: Use NeoForge API (requires NeoForge compatibility)
     * - Add @SubscribeEvent annotation to methods
     * - Listen to VillagerTradesEvent from neoforged.neoforge.event.village
     * - Register this class to event bus
     * 
     * Option C: Extend VillagerTradeLoader.java
     * - Add methods directly to VillagerTradeLoader
     * - Wire them to appropriate Fabric events
     * - No need for separate event handler
     * 
     * RECOMMENDED: Option C - Keep everything in VillagerTradeLoader for Fabric consistency
     */
}
