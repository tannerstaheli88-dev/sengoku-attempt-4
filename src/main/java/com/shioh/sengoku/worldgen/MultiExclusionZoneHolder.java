package com.shioh.sengoku.worldgen;

import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

import java.util.List;

/**
 * Public interface for adding multiple exclusion zones to structure placements.
 * This allows external code to programmatically add exclusion zones to any structure placement.
 * 
 * Structure placements that implement this interface (via the RandomSpreadStructurePlacementMixin)
 * can have multiple exclusion zones added to them, not just the single one supported by vanilla.
 */
public interface MultiExclusionZoneHolder {
    /**
     * Adds an additional exclusion zone to this structure placement.
     * 
     * @param zone The exclusion zone to add
     */
    @SuppressWarnings("deprecation")
    void sengoku$addExclusionZone(StructurePlacement.ExclusionZone zone);
    
    /**
     * Gets all additional exclusion zones that have been added to this placement.
     * 
     * @return List of additional exclusion zones
     */
    @SuppressWarnings("deprecation")
    List<StructurePlacement.ExclusionZone> sengoku$getAdditionalExclusionZones();
}
