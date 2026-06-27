package com.shioh.sengoku.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import com.shioh.sengoku.Constants;

/**
 * Dedicated GUI for the Boiling Pot. Texture expected at assets/sengoku/textures/gui/boiling_pot.png.
 * Fuel flame (u=176,v=0 size 14x14) shrinks upward; progress arrow (u=176,v=14 size 24x17) fills left->right.
 */
public class BoilingPotScreen extends AbstractContainerScreen<BoilingPotMenu> {
    private static final ResourceLocation BACKGROUND = Constants.ID("textures/gui/boiling_pot.png");

    public BoilingPotScreen(BoilingPotMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F,1F,1F,1F);
        gui.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        BoilingPotMenu m = this.menu;
        if (m != null) {
            int litTime = m.getLitTime();
            int litDuration = m.getLitDuration();
            if (litTime > 0 && litDuration > 0) {
                int full = 14;
                int remaining = (int)((litTime/(float)litDuration)*full);
                if (remaining > 0) {
                    // flame position matches smoker: x=56, y=36
                    gui.blit(BACKGROUND, this.leftPos + 56, this.topPos + 36 + (full - remaining), 176, (full - remaining), 14, remaining);
                }
            }
            int cook = m.getCookTime();
            int total = m.getCookTimeTotal();
            if (cook > 0 && total > 0) {
                int width = (int)(cook/(float)total * 24);
                if (width > 0) {
                    // progress arrow position to match smoker: x=79, y=35
                    gui.blit(BACKGROUND, this.leftPos + 79, this.topPos + 35, 176, 14, width, 17);
                }
            }
        }
    }

    @Override
    public void render(net.minecraft.client.gui.GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui, mouseX, mouseY, partialTicks);
        super.render(gui, mouseX, mouseY, partialTicks);
        this.renderTooltip(gui, mouseX, mouseY);
    }
}
