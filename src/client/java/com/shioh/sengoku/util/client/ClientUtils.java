package com.shioh.sengoku.util.client;

import net.minecraft.client.Minecraft;

import java.util.Collection;

import static com.shioh.sengoku.sengokuFabric.LOGGER;
import static com.shioh.sengoku.util.BedShapeState.*;

public class ClientUtils {
    public static void checkActiveResourcePacks() {
        Collection<String> activePackList = Minecraft.getInstance().getResourcePackRepository().getSelectedIds();
        isPillowedPackActive = activePackList.contains("sengoku:more-pillowed-bed-variants");
        isPillowedConnectedPackActive = activePackList.contains("sengoku:more-pillowed-connected-bed-variants");
        needsToBeChecked = false;
        if (isPillowedPackActive && isPillowedConnectedPackActive) {
            LOGGER.info("Found both regular and connected 'Pillowed' resource packs active. Attempting to disable redundant non-connected pack.");
            boolean success = Minecraft.getInstance().getResourcePackRepository().removePack("sengoku:more-pillowed-bed-variants");
            if (success)
            {
                LOGGER.info("Removed \"More 'Pillowed' Bed Variants\" from active resource packs.");
                activePackList.remove("more-pillowed-bed-variants");
            } else {LOGGER.warn("Failed to remove \"More 'Pillowed' Bed Variants\" from active resource packs.");}
        }
    }
}
