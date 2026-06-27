package com.shioh.sengoku.registry;

import com.shioh.sengoku.material.ConditionalToolMaterials;
import com.shioh.sengoku.materialpack.MaterialPackLoader;
import com.shioh.sengoku.struct.WeaponType;
import com.shioh.sengoku.config.PostureValues;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.shioh.sengoku.sengokuFabric.ITEM_REGISTRAR;
import static com.shioh.sengoku.sengokuFabric.bronze_mod_loaded;
import static com.shioh.sengoku.struct.WeaponType.*;

public class WeaponRegistry {
  private static final Map<String, Supplier<Item>> ITEMS = new LinkedHashMap<>();
  private static final Map<WeaponType, List<Supplier<Item>>> ITEMS_BY_TYPE = new EnumMap<>(WeaponType.class);
  private static final Map<Tier, List<Supplier<Item>>> ITEMS_BY_MATERIAL = new HashMap<>();
  private static final List<PendingExternal> PENDING_EXTERNALS = new ArrayList<>();
  private static boolean initialized = false;

  public static final List<MaterialEntry> VANILLA_MATERIALS = Arrays.asList(
      new MaterialEntry(Tiers.WOOD, "wooden"),
      new MaterialEntry(Tiers.STONE, "stone"),
      new MaterialEntry(Tiers.IRON, "iron"),
      new MaterialEntry(Tiers.GOLD, "golden"),
      new MaterialEntry(Tiers.DIAMOND, "diamond"),
      new MaterialEntry(Tiers.NETHERITE, "netherite", Item.Properties::fireResistant)
  );
  public static final MaterialEntry BRONZE_MATERIAL_ENTRY = new MaterialEntry(ConditionalToolMaterials.BRONZE, "bronze");

  public static void init() {
    for (MaterialEntry material : VANILLA_MATERIALS) {
      registerAllWeaponsForMaterial(material);
    }
    if (bronze_mod_loaded) registerAllWeaponsForMaterial(BRONZE_MATERIAL_ENTRY);
    // Apply any external items that were registered before init()
    for (PendingExternal ext : PENDING_EXTERNALS) {
      ITEMS_BY_TYPE.computeIfAbsent(ext.type(), k -> new ArrayList<>()).add(ext.supplier());
    }
    PENDING_EXTERNALS.clear();
    initialized = true;
  }

  /* Register weapons from MaterialEntry */
  public static void registerAllWeaponsForMaterial(MaterialEntry material) {
    for (WeaponType type : WeaponType.values()) {
      String itemId = material.prefix() + "_" + type.getId();
      Item.Properties baseSettings = material.settingsModifier().apply(new Item.Properties());
      
        // Add posture damage as a data component — scales with material tier
        Item.Properties itemSettings = baseSettings.component(ModDataComponents.WEAPON_POSTURE_DAMAGE,
          PostureValues.getScaledPosture(type.getBasePoise(), material.material()));

      float damageModifier = getDamageModifier(type, material.material());
      float speedModifier = getSpeedModifier(type, material.material());
      float reachModifier = getReachModifier(type, material.material());

      Supplier<Item> itemSupplier = ITEM_REGISTRAR.register(itemId, () -> type.create(material.material, damageModifier, speedModifier, reachModifier, itemSettings));

      ITEMS.put(itemId, itemSupplier);
      ITEMS_BY_TYPE.computeIfAbsent(type, k -> new ArrayList<>()).add(itemSupplier);
      ITEMS_BY_MATERIAL.computeIfAbsent(material.material(), k -> new ArrayList<>()).add(itemSupplier);
    }
  }

  /* Register weapons from string of material name (used for material packs) */
  public static void registerAllWeaponsForMaterial(String materialName) {
    Tier material = MaterialPackLoader.getMaterial(materialName);
    registerAllWeaponsForMaterial(new MaterialEntry(material, materialName));
  }

  public static List<Item> getItemsByType(WeaponType type) {
    return ITEMS_BY_TYPE.getOrDefault(type, Collections.emptyList()).stream()
        .map(Supplier::get)
        .filter(Objects::nonNull)
        .toList();
  }

  public static List<Item> getItemsByMaterial(Tier material) {
    return ITEMS_BY_MATERIAL.getOrDefault(material, Collections.emptyList()).stream()
        .map(Supplier::get)
        .filter(Objects::nonNull)
        .toList();
  }

  /**
   * Register an externally-created item so it appears in the weapon lists (e.g. creative tab).
   * The supplier should return the already-registered Item instance.
   */
  public static void registerExternalItem(String itemId, WeaponType type, Supplier<Item> supplier) {
    ITEMS.put(itemId, supplier);
    if (initialized) {
      ITEMS_BY_TYPE.computeIfAbsent(type, k -> new ArrayList<>()).add(supplier);
    } else {
      PENDING_EXTERNALS.add(new PendingExternal(itemId, type, supplier));
    }
  }

  private record PendingExternal(String id, WeaponType type, Supplier<Item> supplier) {
    public WeaponType type() { return type; }
    public Supplier<Item> supplier() { return supplier; }
  }

  /**
   * Record for defining a material variant with its properties
   */
  public record MaterialEntry(Tier material, String prefix,
                              Function<Item.Properties, Item.Properties> settingsModifier) {
    public MaterialEntry(Tier material, String prefix) {
      this(material, prefix, settings -> settings);
    }
  }
}
