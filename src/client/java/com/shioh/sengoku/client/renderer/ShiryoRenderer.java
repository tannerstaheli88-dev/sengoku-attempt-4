package com.shioh.sengoku.client.renderer;

import com.shioh.sengoku.entity.ShiryoEntity;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ShiryoRenderer extends MobRenderer<ShiryoEntity, VillagerModel<ShiryoEntity>> {
    private static final ResourceLocation TEXTURE = sengokuFabric.asId("textures/entity/shiryo.png");

    public ShiryoRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModelLayers.VILLAGER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(ShiryoEntity entity) {
        return TEXTURE;
    }

    @Override
    protected RenderType getRenderType(ShiryoEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(TEXTURE);
    }
}