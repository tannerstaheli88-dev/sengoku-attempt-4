package com.shioh.sengoku.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.shioh.sengoku.entity.WarlordEntity;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;

/**
 * Custom item-in-hand layer for the Warlord.
 * Hides the held item while the Warlord is dormant to simulate a sheathed weapon.
 */
public class WarlordItemLayer extends ItemInHandLayer<WarlordEntity, IllagerModel<WarlordEntity>> {

    public WarlordItemLayer(RenderLayerParent<WarlordEntity, IllagerModel<WarlordEntity>> parent, ItemInHandRenderer itemRenderer) {
        super(parent, itemRenderer);
    }

    @Override
    public void render(PoseStack matrices, MultiBufferSource vertexConsumers, int light, WarlordEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // When dormant, do not render the held item so the sword appears sheathed
        try {
            if (!entity.isActive()) return;
        } catch (Throwable ignored) {}
        super.render(matrices, vertexConsumers, light, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
    }
}
