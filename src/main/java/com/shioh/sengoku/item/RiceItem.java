package com.shioh.sengoku.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class RiceItem extends ItemNameBlockItem {
    public RiceItem(Block cropBlock, Item.Properties properties) {
        super(cropBlock, properties.food(new FoodProperties.Builder()
                .nutrition(1)            // hunger restored
                .saturationModifier(0.6f) // use this instead of saturationMod
                .build()));
    }
}
