package com.shioh.sengoku.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

/**
 * Simple mod-side loader for sake brewery recipe JSONs packaged with the mod.
 * This reads a small, known set of files under data/sengoku/recipes/*.json
 * and converts them into SakeBrewingRecipe instances using the recipe
 * serializer already implemented in SakeBrewingRecipe.
 */
public final class ModSakeBrewingRecipes {
    private ModSakeBrewingRecipes() {}

    private static final List<SakeBrewingRecipe> RECIPES = new ArrayList<>();
    private static final String[] FILES = new String[] {
        "fermented_sake.json",
        "fermented_sake_healing.json",
        "fermented_sake_regen.json",
        "fermented_sake_swiftness.json",
        "fermented_sake_leaping.json",
        "fermented_sake_fire_resistance.json",
        "fermented_sake_invisibility.json",
        "fermented_sake_slow_falling.json",
        "fermented_sake_absorption.json",
        "fermented_sake_water_breathing.json",
        "fermented_sake_luck.json"
    };

    public static void load() {
        RECIPES.clear();
        for (String file : FILES) {
            String path = "data/sengoku/recipe/" + file;
            try (InputStream is = ModSakeBrewingRecipes.class.getClassLoader().getResourceAsStream(path)) {
                if (is == null) {
                    // Not all environments will include the dev-time recipe files; log at debug to avoid spamming INFO
                    sengokuFabric.LOGGER.debug("Sake recipe resource not found: {}", path);
                    continue;
                }
                String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                ResourceLocation id = ResourceLocation.bySeparator("sengoku:" + file.replaceFirst("\\.json$", ""), ':');
                SakeBrewingRecipe recipe = SakeBrewingRecipe.Parser.fromJson(id, obj);
                RECIPES.add(recipe);
                sengokuFabric.LOGGER.info("Loaded sake recipe {} from {}", id, path);
            } catch (Exception e) {
                sengokuFabric.LOGGER.warn("Failed to load sake recipe {}: {}", path, e.toString());
            }
        }
        // Summary log of loaded recipes
        try {
            // Sort recipes by specificity: more ingredients first so specific recipes are matched before generic ones
            RECIPES.sort(Comparator.comparingInt(r -> -r.getIngredients().size()));
            sengokuFabric.LOGGER.info("Sake recipes loaded (ordered): {}", RECIPES.stream().map(r -> r.getId().toString()).toList());
        } catch (Throwable t) {}
    }

    public static List<SakeBrewingRecipe> getRecipes() {
        return Collections.unmodifiableList(RECIPES);
    }
}
