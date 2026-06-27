package com.shioh.sengoku.datagen.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static com.shioh.sengoku.Constants.ID;
import static com.shioh.sengoku.registry.TagRegistry.BRONZE_INGOTS;

@SuppressWarnings("removal")
public class sengokuAdvancements implements Consumer<Consumer<AdvancementHolder>> {
  HolderGetter<Item> registryEntryLookup;

  public void accept(HolderGetter.Provider registryLookup, Consumer<AdvancementHolder> AdvancementHolderConsumer) {
    registryEntryLookup = registryLookup.lookupOrThrow(Registries.ITEM);
    accept(AdvancementHolderConsumer);
  }

  @Override
  public void accept(Consumer<AdvancementHolder> advancementConsumer) {
    AdvancementHolder wooden_weapons = Advancement.Builder.recipeAdvancement()
        .parent(ResourceLocation.withDefaultNamespace("recipes/root"))
        .addCriterion("got_sticks", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STICK))
        .rewards(AdvancementRewards.Builder
            .recipe(ID("wooden_tanto"))
            .addRecipe(ID("wooden_kanabo"))
            .addRecipe(ID("wooden_kanabo_variant"))
            .addRecipe(ID("wooden_tetsubo"))
            .addRecipe(ID("wooden_naginata"))
            .addRecipe(ID("wooden_yari"))
            .addRecipe(ID("wooden_odachi"))

        )
        .build(ID("recipes/got_sticks"));
    advancementConsumer.accept(wooden_weapons);

    AdvancementHolder stone_weapons = Advancement.Builder.recipeAdvancement()
        .parent(ResourceLocation.withDefaultNamespace("recipes/root"))
        .addCriterion("got_cobblestone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.COBBLESTONE))
        .rewards(AdvancementRewards.Builder
            .recipe(ID("stone_tanto"))
            .addRecipe(ID("stone_kanabo"))
            .addRecipe(ID("stone_kanabo_variant"))
            .addRecipe(ID("stone_tetsubo"))
            .addRecipe(ID("stone_naginata"))
            .addRecipe(ID("stone_yari"))
            .addRecipe(ID("stone_odachi"))

        )
        .build(ID("recipes/got_cobblestone"));
    advancementConsumer.accept(stone_weapons);

    AdvancementHolder iron_weapons = Advancement.Builder.recipeAdvancement()
        .parent(ResourceLocation.withDefaultNamespace("recipes/root"))
        .addCriterion("got_iron_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_INGOT))
        .rewards(AdvancementRewards.Builder
            .recipe(ID("iron_tanto"))
            .addRecipe(ID("iron_kanabo"))
            .addRecipe(ID("iron_kanabo_variant"))
            .addRecipe(ID("iron_tetsubo"))
            .addRecipe(ID("iron_naginata"))
            .addRecipe(ID("iron_yari"))
            .addRecipe(ID("iron_odachi"))

        )
        .build(ID("recipes/got_iron_ingot"));
    advancementConsumer.accept(iron_weapons);

    AdvancementHolder bronze_weapons = Advancement.Builder.recipeAdvancement()
        .parent(ResourceLocation.withDefaultNamespace("recipes/root"))
        .addCriterion("got_bronze_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(BRONZE_INGOTS)))
        .rewards(AdvancementRewards.Builder
            .recipe(ID("compat/_tanto"))
            .addRecipe(ID("compat/bronze_kanabo"))
            .addRecipe(ID("compat/bronze_kanabo_variant"))
            .addRecipe(ID("compat/bronze_tetsubo"))
            .addRecipe(ID("compat/bronze_naginata"))
            .addRecipe(ID("compat/bronze_yari"))
            .addRecipe(ID("compat/bronze_odachi"))

        )
        .build(ID("recipes/got_bronze_ingot"));
    advancementConsumer.accept(bronze_weapons);

    AdvancementHolder golden_weapons = Advancement.Builder.recipeAdvancement()
        .parent(ResourceLocation.withDefaultNamespace("recipes/root"))
        .addCriterion("got_gold_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GOLD_INGOT))
        .rewards(AdvancementRewards.Builder
            .recipe(ID("golden_tanto"))
            .addRecipe(ID("golden_kanabo"))
            .addRecipe(ID("golden_kanabo_variant"))
            .addRecipe(ID("golden_tetsubo"))
            .addRecipe(ID("golden_naginata"))
            .addRecipe(ID("golden_yari"))
            .addRecipe(ID("golden_odachi"))

        )
        .build(ID("recipes/got_gold_ingot"));
    advancementConsumer.accept(golden_weapons);

    AdvancementHolder diamond_weapons = Advancement.Builder.recipeAdvancement()
        .parent(ResourceLocation.withDefaultNamespace("recipes/root"))
        .addCriterion("got_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
        .rewards(AdvancementRewards.Builder
            .recipe(ID("diamond_tanto"))
            .addRecipe(ID("diamond_kanabo"))
            .addRecipe(ID("diamond_kanabo_variant"))
            .addRecipe(ID("diamond_tetsubo"))
            .addRecipe(ID("diamond_naginata"))
            .addRecipe(ID("diamond_yari"))
            .addRecipe(ID("diamond_odachi"))

        )
        .build(ID("recipes/got_diamond"));
    advancementConsumer.accept(diamond_weapons);
  }
}
