package com.shioh.sengoku.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import com.shioh.sengoku.Constants;

import com.shioh.sengoku.screen.SakeBreweryMenu;

public class SakeBreweryScreen extends AbstractContainerScreen<SakeBreweryMenu> {
    private static final ResourceLocation BACKGROUND = Constants.ID("textures/gui/sake_brewery.png");
    // We'll use the mod BACKGROUND for the fuel progression bar so your own texture is used
    public SakeBreweryScreen(SakeBreweryMenu menu, Inventory inv, Component c) {
        super(menu, inv, c);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        gui.blit(BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Draw fuel and progress indicators from the menu data
        SakeBreweryMenu menu = this.menu;
        if (menu != null) {
            // Brew progress: render as a vertical bar on the far right that goes down
            int brewTime = menu.getBrewTime();
            int brewMax = 400; // server brew time threshold
            int maxBrewHeight = 24; // height of the vertical progress bar
            int brewHeight = (int)((brewTime / (float)brewMax) * maxBrewHeight);
            if (brewHeight > 0) {
                // Recipe progress bar: top-left at (112,16) in-game, texture area 16x54. Animate top-down.
                int barX = this.leftPos + 112;
                int barTopY = this.topPos + 16;
                int fullHeight = 54; // texture height
                int drawHeight = (int)((brewTime / (float)brewMax) * fullHeight);
                // draw top-down: draw the top portion that represents current progress
                int srcY = 0 + (fullHeight - drawHeight); // assuming brew texture starts at v=0 in BACKGROUND
                int destY = barTopY + (fullHeight - drawHeight);
                gui.blit(BACKGROUND, barX, destY, 176, srcY, 16, drawHeight);
            }

            // Fuel indicator: draw a 16x16 fuel icon at (17,35) using the BACKGROUND texture (u=192)
            int fuelUses = menu.getFuelUses();
            int fuelMax = menu.getFuelMax();
            if (fuelMax > 0 && fuelUses > 0) {
                int barX = this.leftPos + 17;
                int barTopY = this.topPos + 35;
                int fullFuelH = 16; // icon is 16px tall
                int drawH = (int)((fuelUses / (float)fuelMax) * fullFuelH);
                if (drawH > 0) {
                    int srcY = 0 + (fullFuelH - drawH);
                    int destY = barTopY + (fullFuelH - drawH);
                    // fuel texture located at u=192 per user instruction, width 16
                    gui.blit(BACKGROUND, barX, destY, 192, srcY, 16, drawH);
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
