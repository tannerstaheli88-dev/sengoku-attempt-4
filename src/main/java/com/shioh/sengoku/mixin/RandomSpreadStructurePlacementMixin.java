package com.shioh.sengoku.mixin;

import com.shioh.sengoku.worldgen.MultiExclusionZoneHolder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Mixin to extend RandomSpreadStructurePlacement to support multiple exclusion zones.
 * 
 * Vanilla only supports a single exclusion_zone with one "other_set".
 * This mixin allows you to specify multiple exclusion zones by programmatically adding them.
 * 
 * Usage example in code:
 * if (placement instanceof MultiExclusionZoneHolder holder) {
 *     holder.sengoku$addExclusionZone(new StructurePlacement.ExclusionZone(villagesSet, 8));
 *     holder.sengoku$addExclusionZone(new StructurePlacement.ExclusionZone(outpostsSet, 5));
 * }
 */
@Mixin(RandomSpreadStructurePlacement.class)
public abstract class RandomSpreadStructurePlacementMixin implements MultiExclusionZoneHolder {
    
    @Shadow
    protected abstract boolean isPlacementChunk(ChunkGeneratorStructureState state, int x, int z);
    
    @Unique
    private final List<StructurePlacement.ExclusionZone> sengoku$additionalExclusionZones = new ArrayList<>();
    
    @Override
    public void sengoku$addExclusionZone(StructurePlacement.ExclusionZone zone) {
        sengoku$additionalExclusionZones.add(zone);
    }
    
    @Override
    public List<StructurePlacement.ExclusionZone> sengoku$getAdditionalExclusionZones() {
        return sengoku$additionalExclusionZones;
    }
    
    /**
     * Injects into the isPlacementChunk method to check against additional exclusion zones.
     * This runs after the vanilla exclusion zone check passes.
     */
    @Inject(method = "isPlacementChunk", at = @At("RETURN"), cancellable = true)
    private void checkAdditionalExclusionZones(ChunkGeneratorStructureState state, int x, int z, CallbackInfoReturnable<Boolean> cir) {
        // Only check if the vanilla check passed
        if (!cir.getReturnValue()) {
            return;
        }
        
        // Check all additional exclusion zones
        for (StructurePlacement.ExclusionZone zone : sengoku$additionalExclusionZones) {
            Holder<StructureSet> otherSet = zone.otherSet();
            int chunkCount = zone.chunkCount();
            
            // Get the structure set and its placement
            StructureSet structureSet = otherSet.value();
            StructurePlacement otherPlacement = structureSet.placement();
            
            // Check surrounding chunks within the exclusion distance
            // If the other structure set would place in any nearby chunk, deny this placement
            if (otherPlacement instanceof RandomSpreadStructurePlacement) {
                for (int dx = -chunkCount; dx <= chunkCount; dx++) {
                    for (int dz = -chunkCount; dz <= chunkCount; dz++) {
                        // Cast to our mixin to access the shadowed protected method
                        RandomSpreadStructurePlacementMixin otherMixin = 
                            (RandomSpreadStructurePlacementMixin) (Object) otherPlacement;
                        
                        if (otherMixin.isPlacementChunk(state, x + dx, z + dz)) {
                            cir.setReturnValue(false);
                            return;
                        }
                    }
                }
            }
        }
    }
}


