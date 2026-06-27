package com.shioh.sengoku.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.Optional;

/**
 * Utility class for loading multiple exclusion zones from JSON data so structures dont spawn in eachother.
 */
public class MultiExclusionZoneLoader {
    
    /**
     * Parses and applies additional exclusion zones from a structure set JSON.
     * This should be called during structure set registration or loading.
     * 
     * @param placement The structure placement to add exclusion zones to
     * @param placementJson The JSON object containing the placement configuration
     * @param registryAccess The registry access for looking up structure sets
     */
    @SuppressWarnings("deprecation")
    public static void applyAdditionalExclusionZones(
            StructurePlacement placement,
            JsonObject placementJson,
            RegistryAccess registryAccess) {
        
        if (!(placement instanceof MultiExclusionZoneHolder holder)) {
            return; // Placement doesn't support multiple exclusion zones
        }
        
        if (!placementJson.has("additional_exclusion_zones")) {
            return; // No additional exclusion zones specified
        }
        
        JsonElement additionalZonesElement = placementJson.get("additional_exclusion_zones");
        if (!additionalZonesElement.isJsonArray()) {
            return;
        }
        
        JsonArray additionalZones = additionalZonesElement.getAsJsonArray();
        
        for (JsonElement zoneElement : additionalZones) {
            if (!zoneElement.isJsonObject()) {
                continue;
            }
            
            JsonObject zoneObj = zoneElement.getAsJsonObject();
            
            // Parse "other_set" field
            if (!zoneObj.has("other_set")) {
                continue;
            }
            
            String otherSetId = zoneObj.get("other_set").getAsString();
            ResourceLocation otherSetLocation = ResourceLocation.parse(otherSetId);
            
            // Parse "chunk_count" field
            int chunkCount = 1;
            if (zoneObj.has("chunk_count")) {
                chunkCount = zoneObj.get("chunk_count").getAsInt();
            }
            
            // Look up the structure set from registry
            ResourceKey<StructureSet> key = ResourceKey.create(Registries.STRUCTURE_SET, otherSetLocation);
            
            try {
                Optional<Holder.Reference<StructureSet>> structureSetHolder = 
                    registryAccess.lookupOrThrow(Registries.STRUCTURE_SET).get(key);
                
                if (structureSetHolder.isPresent()) {
                    // Create and add the exclusion zone
                    StructurePlacement.ExclusionZone zone = 
                        new StructurePlacement.ExclusionZone(structureSetHolder.get(), chunkCount);
                    holder.sengoku$addExclusionZone(zone);
                }
            } catch (Exception e) {
                // Registry lookup failed, skip this exclusion zone
                continue;
            }
        }
    }
    
    /**
     * Alternative method that accepts a structure set holder instead of a ResourceLocation.
     * Useful for programmatic creation of exclusion zones.
     * 
     * @param placement The structure placement to add exclusion zones to
     * @param otherSet The structure set to exclude from
     * @param chunkCount The number of chunks to exclude
     */
    @SuppressWarnings("deprecation")
    public static void addExclusionZone(
            StructurePlacement placement,
            Holder<StructureSet> otherSet,
            int chunkCount) {
        
        if (placement instanceof MultiExclusionZoneHolder holder) {
            StructurePlacement.ExclusionZone zone = 
                new StructurePlacement.ExclusionZone(otherSet, chunkCount);
            holder.sengoku$addExclusionZone(zone);
        }
    }
}
