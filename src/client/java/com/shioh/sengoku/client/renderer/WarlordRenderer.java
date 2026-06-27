package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.WarlordEntity;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import com.shioh.sengoku.client.renderer.TwoHandBlockLayer;

/**
 * Renderer for the Warlord boss. Switches to an enraged texture when health
 * drops to the phase-2 threshold (<= 25 HP).
 */
public class WarlordRenderer extends IllagerRenderer<WarlordEntity> {

    private static final ResourceLocation WARLORD_BASE = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/warlord.png");
    private static final ResourceLocation WARLORD_ENRAGED = ResourceLocation.fromNamespaceAndPath("sengoku", "textures/entity/warlord_enraged.png");

    public WarlordRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.VINDICATOR)), 0.5F);
        // Use a custom item layer that hides the held weapon while the Warlord is dormant
        this.addLayer(new com.shioh.sengoku.client.renderer.WarlordItemLayer(this, context.getItemInHandRenderer()));
        // Two-handed blocking handled by client mixin
    }

    @Override
    public ResourceLocation getTextureLocation(WarlordEntity entity) {
        // Match the server-side phase logic: switch texture at 50% HP.
        try {
            if (entity.isPhaseTwo()) {
                return WARLORD_ENRAGED;
            }
        } catch (Throwable ignored) {}
        return WARLORD_BASE;
    }
}
