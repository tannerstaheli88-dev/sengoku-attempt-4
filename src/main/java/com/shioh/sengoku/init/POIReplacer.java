package com.shioh.sengoku.init;

import com.shioh.sengoku.block.FishingNet;
import com.shioh.sengoku.mixin.PoiTypesAccessor;
import com.shioh.sengoku.mixin.PoiTypeAccessor;
import com.shioh.sengoku.sengokuFabric; // <-- add this import

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class POIReplacer {

    public static void replaceFishermanPOI(FishingNet fishingNetBlock) {
        try {
            Holder<PoiType> fishermanHolder =
                    BuiltInRegistries.POINT_OF_INTEREST_TYPE.getHolderOrThrow(PoiTypes.FISHERMAN);
            PoiType fishermanPoi = fishermanHolder.value();

            // Remove barrels from TYPE_BY_STATE
            Map<BlockState, Holder<PoiType>> poiMap = PoiTypesAccessor.getPoiMap();
            Iterator<Map.Entry<BlockState, Holder<PoiType>>> it = poiMap.entrySet().iterator();
            int removedFromMap = 0;
            while (it.hasNext()) {
                Map.Entry<BlockState, Holder<PoiType>> entry = it.next();
                if (entry.getKey().getBlock() instanceof BarrelBlock &&
                        entry.getValue().equals(fishermanHolder)) {
                    it.remove();
                    removedFromMap++;
                }
            }

            // Add fishing nets to TYPE_BY_STATE
            int addedToMap = 0;
            for (BlockState state : fishingNetBlock.getStateDefinition().getPossibleStates()) {
                poiMap.put(state, fishermanHolder);
                addedToMap++;
            }
            sengokuFabric.LOGGER.info("POIReplacer: removed {} barrel states from TYPE_BY_STATE and added {} fishing net states.",
                    removedFromMap, addedToMap);

            // Fix matchingStates
            Set<BlockState> matchingStates = ((PoiTypeAccessor) (Object) fishermanPoi).getMatchingStates();
            Set<BlockState> desired = new HashSet<>(matchingStates);
            desired.removeIf(s -> s.getBlock() instanceof BarrelBlock);
            for (BlockState state : fishingNetBlock.getStateDefinition().getPossibleStates()) {
                desired.add(state);
            }

            try {
                matchingStates.clear();
                matchingStates.addAll(desired);
                sengokuFabric.LOGGER.info("POIReplacer: updated fisherman PoiType.matchingStates in-place.");
            } catch (UnsupportedOperationException ex) {
                try {
                    Field f = PoiType.class.getDeclaredField("matchingStates");
                    f.setAccessible(true);
                    f.set(fishermanPoi, desired);
                    sengokuFabric.LOGGER.info("POIReplacer: replaced fisherman PoiType.matchingStates via reflection.");
                } catch (Exception reflectEx) {
                    sengokuFabric.LOGGER.error("POIReplacer: failed to replace matchingStates via reflection.", reflectEx);
                }
            }

        } catch (Exception ex) {
            sengokuFabric.LOGGER.error("POIReplacer: fatal error while replacing fisherman POI.", ex);
        }
    }
}
