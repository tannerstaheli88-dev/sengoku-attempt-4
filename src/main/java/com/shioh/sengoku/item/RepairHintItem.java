package com.shioh.sengoku.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import java.util.List;

public class RepairHintItem extends Item {
    private final String appliesTo;
    private final String ingredients;
    private final String title;
    private final String tooltipKey;
    private final boolean isSmithingTemplate;

    public RepairHintItem(Properties props, String appliesTo, String ingredients, String title) {
        super(props);
        this.appliesTo = appliesTo;
        this.ingredients = ingredients;
        this.title = title;
        this.tooltipKey = null;
        this.isSmithingTemplate = true;
    }

    public RepairHintItem(Properties props, String tooltipKey) {
        super(props);
        this.tooltipKey = tooltipKey;
        this.appliesTo = null;
        this.ingredients = null;
        this.title = null;
        this.isSmithingTemplate = false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        if (isSmithingTemplate) {
            tooltip.add(Component.translatable(this.title).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("  ").append(Component.literal("Applies to: ").withStyle(ChatFormatting.GRAY)).append(Component.translatable(this.appliesTo).withStyle(ChatFormatting.BLUE)));
            tooltip.add(Component.literal("  ").append(Component.literal("Ingredients: ").withStyle(ChatFormatting.GRAY)).append(Component.translatable(this.ingredients).withStyle(ChatFormatting.BLUE)));
        } else {
            try {
                tooltip.add(Component.translatable(this.tooltipKey).withStyle(ChatFormatting.GRAY));
            } catch (Throwable ignored) {}
        }
    }
}
