package com.shioh.sengoku;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import com.shioh.sengoku.registry.ModMenuTypes;
import com.shioh.sengoku.screen.SakeBreweryScreen;

/**
 * Client initializer: registers screens for custom menus.
 * Uses SakeBreweryScreen as a temporary GUI for Boiling Pot to remove runtime warnings.
 */
public class sengokuFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        try {
            if (ModMenuTypes.SAKE_BREWERY != null) {
                MenuScreens.register(ModMenuTypes.SAKE_BREWERY, com.shioh.sengoku.screen.SakeBreweryScreen::new);
            }
            // Boiling Pot screen registration moved to `sengokuClient` to ensure
            // a defensive, anonymous-class-based constructor is used and to
            // avoid lambda/method-ref bytecode issues on some JVM/mixin setups.
        } catch (Throwable t) {
            sengokuFabric.LOGGER.warn("Failed to register menu screens: {}", t.toString());
        }
    }
}