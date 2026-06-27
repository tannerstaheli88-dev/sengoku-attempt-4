package com.shioh.sengoku.materialpack;

import com.shioh.sengoku.sengokuFabric;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Exposes datapacks shipped inside the mod jar/folder under resources/datapacks
 * to the Create World datapack selection screen.
 */
public class BuiltinModDatapackLoader implements RepositorySource {
  private static final PackSource SOURCE = PackSource.create(Component::copy, false);

  @Override
  public void loadPacks(Consumer<Pack> packConsumer) {
    Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(sengokuFabric.MODID);
    if (modContainer.isEmpty()) return;

    Set<String> seenPackIds = new HashSet<>();
    for (Path rootPath : modContainer.get().getRootPaths()) {
      Path datapacksRoot = rootPath.resolve("datapacks");
      if (!Files.isDirectory(datapacksRoot)) continue;

      try (Stream<Path> children = Files.list(datapacksRoot)) {
        children
            .filter(BuiltinModDatapackLoader::isValidDatapackDirectory)
            .forEach(packPath -> addPack(packPath, seenPackIds, packConsumer));
      } catch (IOException e) {
        sengokuFabric.LOGGER.warn("Failed to scan built-in datapacks at {}", datapacksRoot, e);
      }
    }
  }

  private static boolean isValidDatapackDirectory(Path path) {
    return Files.isDirectory(path)
        && Files.isRegularFile(path.resolve("pack.mcmeta"))
        && Files.isDirectory(path.resolve("data"));
  }

  private static void addPack(Path packPath, Set<String> seenPackIds, Consumer<Pack> packConsumer) {
    String folderName = packPath.getFileName().toString();
    String packIdPath = sanitizePath(folderName);
    String packId = sengokuFabric.MODID + ":builtin_datapacks/" + packIdPath;
    if (!seenPackIds.add(packId)) return;

    PackLocationInfo location = new PackLocationInfo(packId, Component.literal(folderName), SOURCE, Optional.empty());
    Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
      @Override
      public PackResources openPrimary(PackLocationInfo info) {
        return new PathPackResources(info, packPath);
      }

      @Override
      public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
        return new PathPackResources(info, packPath);
      }
    };

    PackSelectionConfig selectionConfig = new PackSelectionConfig(false, Pack.Position.TOP, false);
    Pack pack = Pack.readMetaAndCreate(location, resources, PackType.SERVER_DATA, selectionConfig);
    if (pack != null) {
      packConsumer.accept(pack);
    }
  }

  private static String sanitizePath(String input) {
    String lower = input.toLowerCase(java.util.Locale.ROOT).trim();
    String normalized = lower.replaceAll("[^a-z0-9._/\\-]+", "_");
    return normalized.replaceAll("_+", "_");
  }
}