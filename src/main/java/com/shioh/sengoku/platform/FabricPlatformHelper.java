package com.shioh.sengoku.platform;

import com.shioh.sengoku.platform.services.IPlatformHelper;
import com.shioh.sengoku.registry.WeaponRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;

public class FabricPlatformHelper implements IPlatformHelper {

  private static volatile MinecraftServer currentMinecraftServer = null;

  static {
    ServerLifecycleEvents.SERVER_STARTED.register(server -> currentMinecraftServer = server);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentMinecraftServer = null);
  }

  public static void setCurrentMinecraftServer(MinecraftServer server) {
    currentMinecraftServer = server;
  }

  public static void clearCurrentMinecraftServer() {
    currentMinecraftServer = null;
  }

  @Override
  public String getPlatformName() {
    return "Fabric";
  }

  @Override
  public boolean isModLoaded(String modId) {
    return FabricLoader.getInstance().isModLoaded(modId);
  }

  @Override
  public boolean isDevelopmentEnvironment() {
    return FabricLoader.getInstance().isDevelopmentEnvironment();
  }

  @Override
  public RegistryAccess getCurrentRegistryAccess() {
    MinecraftServer server = currentMinecraftServer;
    return server != null ? server.registryAccess() : RegistryAccess.EMPTY;
  }

  @Override
  public File getWorldDatapacksDirectory() {
    MinecraftServer server = currentMinecraftServer;
    return server != null ? server.getWorldPath(LevelResource.DATAPACK_DIR).toFile() : null;
  }

  @Override
  public boolean registerFurnaceFuels() {
    for (Item weapon : WeaponRegistry.getItemsByMaterial(Tiers.WOOD)) {
      FuelRegistry.INSTANCE.add(weapon, 200); // 200 tick burn time like vanilla weapons/tools
    }
    return true;
  }
}