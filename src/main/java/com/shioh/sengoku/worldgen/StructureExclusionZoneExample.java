package com.shioh.sengoku.worldgen;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.Optional;

/**
 * Example class demonstrating how to add multiple exclusion zones to structure sets.
 * 
 * This class registers a server start callback that adds additional exclusion zones
 * to your structure sets programmatically.
 * 
 * To use this, call StructureExclusionZoneExample.register() from your mod initializer.
 */
@SuppressWarnings("deprecation")
public class StructureExclusionZoneExample {
    
    /**
     * Call this from your mod's onInitialize() method to register the exclusion zones.
     */
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(StructureExclusionZoneExample::addExclusionZones);
    }
    
    /**
     * Adds exclusion zones when the server starts.
     * This is called automatically if you call register() from your mod initializer.
     */
    private static void addExclusionZones(MinecraftServer server) {
        Registry<StructureSet> structureSetRegistry = server.registryAccess()
            .registryOrThrow(Registries.STRUCTURE_SET);
        
        // Make "tree" structures avoid ALL structures EXCEPT lanterns and clearing
        // Note: The JSON file already has exclusion_zone for minecraft:villages with chunk_count 8
        addMultipleExclusionZones(
            structureSetRegistry,
            "shioh:tree",
            // modded structures
            new ExclusionZoneConfig("shioh:outcast_villager", 3),
            // Vanilla structures
            new ExclusionZoneConfig("minecraft:villages", 8),
            new ExclusionZoneConfig("minecraft:desert_pyramids", 3),
            new ExclusionZoneConfig("minecraft:pillager_outposts", 5),
            new ExclusionZoneConfig("minecraft:jungle_temples", 2),
            new ExclusionZoneConfig("minecraft:swamp_huts", 2),
            new ExclusionZoneConfig("minecraft:igloos", 2)
            
        );

        // Make outcast villager avoid pillager outposts by 5 chunks
        addMultipleExclusionZones(
            structureSetRegistry,
            "shioh:outcast_villager",
            new ExclusionZoneConfig("minecraft:pillager_outposts", 5)
        );
        // Make woodland mansions avoid pillager outposts by 5 chunks
        addMultipleExclusionZones(
            structureSetRegistry,
            "minecraft:woodland_mansions",
            new ExclusionZoneConfig("minecraft:pillager_outposts", 5),
            new ExclusionZoneConfig("minecraft:villages", 8)
        );
        addMultipleExclusionZones(
            structureSetRegistry,
            "shioh:yomilanterns",
            new ExclusionZoneConfig("shioh:yomi_house", 2)
        );
    }
    
    /**
     * Helper method to add multiple exclusion zones to a structure set.
     * 
     * @param registry The structure set registry
     * @param structureSetId The ID of the structure set to modify (e.g. "shioh:tree")
     * @param exclusionZones The exclusion zones to add
     */
    private static void addMultipleExclusionZones(
            Registry<StructureSet> registry,
            String structureSetId,
            ExclusionZoneConfig... exclusionZones) {
        
        ResourceLocation structureSetLocation = ResourceLocation.parse(structureSetId);
        ResourceKey<StructureSet> key = ResourceKey.create(Registries.STRUCTURE_SET, structureSetLocation);
        
        Optional<Holder.Reference<StructureSet>> structureSetHolder = registry.getHolder(key);
        
        if (structureSetHolder.isEmpty()) {
            // Structure set not found - might not be loaded yet or doesn't exist
            return;
        }
        
        StructureSet structureSet = structureSetHolder.get().value();
        StructurePlacement placement = structureSet.placement();
        
        if (!(placement instanceof MultiExclusionZoneHolder holder)) {
            // Placement doesn't support multiple exclusion zones (shouldn't happen with our mixin)
            return;
        }
        
        // Add each exclusion zone
        for (ExclusionZoneConfig config : exclusionZones) {
            ResourceLocation otherSetLocation = ResourceLocation.parse(config.otherSet);
            ResourceKey<StructureSet> otherKey = ResourceKey.create(Registries.STRUCTURE_SET, otherSetLocation);
            
            Optional<Holder.Reference<StructureSet>> otherSetHolder = registry.getHolder(otherKey);
            
            if (otherSetHolder.isPresent()) {
                StructurePlacement.ExclusionZone zone = 
                    new StructurePlacement.ExclusionZone(otherSetHolder.get(), config.chunkCount);
                holder.sengoku$addExclusionZone(zone);
            }
        }
    }
    
    /**
     * Simple record to hold exclusion zone configuration.
     */
    private record ExclusionZoneConfig(String otherSet, int chunkCount) {}
}
