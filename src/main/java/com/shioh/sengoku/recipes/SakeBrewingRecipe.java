package com.shioh.sengoku.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * A lightweight custom recipe that lists multiple ingredients (order-independent)
 * and produces a single output ItemStack. JSON format used by serializer:
 * {
 *   "type":"sengoku:sake_brewing",
 *   "ingredients":[{"item":"mod:foo"}, {"item":"minecraft:bar"}],
 *   "output": {"item":"mod:result","count":1}
 * }
 */
public final class SakeBrewingRecipe {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;

    public SakeBrewingRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ItemStack result) {
        this.id = id;
        this.ingredients = ingredients;
        this.result = result;
    }

    public NonNullList<Ingredient> getIngredients() { return this.ingredients; }
    public ResourceLocation getId() { return this.id; }

    public boolean matches(Container inv, Level world) {
        // Expect first 3 slots to contain ingredients (order-independent)
        if (inv == null) return false;
        List<ItemStack> available = new ArrayList<>();
        for (int i = 0; i < Math.min(3, inv.getContainerSize()); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty()) available.add(s.copy());
        }

        // For each Ingredient in the recipe, try to consume one matching stack from available
        for (Ingredient ing : this.ingredients) {
            boolean matched = false;
            for (int i = 0; i < available.size(); i++) {
                if (ing.test(available.get(i))) {
                    // consume one from this stack copy
                    ItemStack copy = available.get(i);
                    copy.shrink(1);
                    if (copy.isEmpty()) available.remove(i);
                    else available.set(i, copy);
                    matched = true;
                    break;
                }
            }
            if (!matched) return false;
        }

        // Also require a bottle in slot 3 (water bottle / potion / whatever the BE accepts)
        if (inv.getContainerSize() > 3) {
            ItemStack bottle = inv.getItem(3);
            return !bottle.isEmpty();
        }
        return true;
    }

    public ItemStack assemble(Container inv) {
        return this.result.copy();
    }

    public static final class Parser {
        public static SakeBrewingRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray arr = json.getAsJsonArray("ingredients");
            NonNullList<Ingredient> ingredients = NonNullList.create();
            for (int i = 0; i < arr.size(); i++) {
                JsonObject o = arr.get(i).getAsJsonObject();
                if (o.has("item")) {
                    String itemStr = o.get("item").getAsString();
                    ResourceLocation idr = ResourceLocation.parse(itemStr);
                    try {
                        ingredients.add(Ingredient.of(() -> net.minecraft.core.registries.BuiltInRegistries.ITEM.get(idr)));
                    } catch (Throwable t) {
                        try { com.shioh.sengoku.sengokuFabric.LOGGER.warn("Failed to create Ingredient for {}: {}", itemStr, t.toString()); } catch (Throwable tt) {}
                    }
                }
            }

            ItemStack result = ItemStack.EMPTY;
            if (json.has("output")) {
                JsonObject out = json.getAsJsonObject("output");
                if (out.has("item")) {
                    ResourceLocation res = ResourceLocation.bySeparator(out.get("item").getAsString(), ':');
                    result = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(res));
                    if (out.has("count")) result.setCount(out.get("count").getAsInt());
                }
            }
            if (result.isEmpty()) result = new ItemStack(Items.POTION);
            return new SakeBrewingRecipe(id, ingredients, result);
        }
    }
}
