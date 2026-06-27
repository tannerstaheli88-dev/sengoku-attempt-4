package com.shioh.sengoku.screen;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * Minimal fallback screen returned if the real BoilingPotScreen fails to construct.
 */
public class BoilingPotScreenFallback extends AbstractContainerScreen<BoilingPotMenu> {
    public BoilingPotScreenFallback(BoilingPotMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        // Intentionally blank - minimal safe fallback UI
    }
}
