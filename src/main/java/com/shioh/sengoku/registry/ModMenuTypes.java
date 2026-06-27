package com.shioh.sengoku.registry;

/**
 * Placeholder registrar. MenuType registration was causing mapping-specific issues.
 * We rely on a client mixin to relax client-side slot validation instead.
 */
public class ModMenuTypes {
    public static net.minecraft.world.inventory.MenuType<com.shioh.sengoku.screen.SakeBreweryMenu> SAKE_BREWERY;
    public static net.minecraft.world.inventory.MenuType<com.shioh.sengoku.screen.BoilingPotMenu> BOILING_POT;

    public static void register() {
        // Use the MenuType constructor that accepts a MenuSupplier and FeatureFlagSet
        net.minecraft.world.inventory.MenuType<com.shioh.sengoku.screen.SakeBreweryMenu> type =
            new net.minecraft.world.inventory.MenuType<>((i, playerInv) -> new com.shioh.sengoku.screen.SakeBreweryMenu(i, playerInv), net.minecraft.world.flag.FeatureFlagSet.of());
        SAKE_BREWERY = type;
        net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.MENU, com.shioh.sengoku.sengokuFabric.asId("sake_brewery"), SAKE_BREWERY);

        // Boiling Pot menu type
        net.minecraft.world.inventory.MenuType<com.shioh.sengoku.screen.BoilingPotMenu> boilingType =
            new net.minecraft.world.inventory.MenuType<>((i, playerInv) -> {
                // Placeholder block entity-less menu (client side will sync real stacks)
                return new com.shioh.sengoku.screen.BoilingPotMenu(i, playerInv);
            }, net.minecraft.world.flag.FeatureFlagSet.of());
        BOILING_POT = boilingType;
        net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.MENU, com.shioh.sengoku.sengokuFabric.asId("boiling_pot"), BOILING_POT);
    }
}
