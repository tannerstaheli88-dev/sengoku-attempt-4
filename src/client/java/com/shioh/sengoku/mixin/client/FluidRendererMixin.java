package com.shioh.sengoku.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.shioh.sengoku.sengokuClient;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LiquidBlockRenderer.class)
public class FluidRendererMixin {
    
    @ModifyVariable(
        method = "tesselate",
        at = @At("STORE"),
        ordinal = 0
    )
    private int modifyWaterColor(int originalColor, BlockAndTintGetter level, BlockPos pos, VertexConsumer buffer, BlockState blockState, FluidState fluidState) {
        // Check if this is warm water - cast to Level for the method call
        if (level instanceof net.minecraft.world.level.Level realLevel && sengokuClient.isWarmWaterAt(realLevel, pos)) {
            // Extract alpha channel from original color
            int alpha = (originalColor >> 24) & 0xFF;
            
            // Change to a warm teal/turquoise color like hot spring water
            // Slightly brightened to compensate for Minecraft's lighting system
            // Color: #8DE6E6 (bright light teal/turquoise)
            int r = 0x8D;  // 141
            int g = 0xE6;  // 230
            int b = 0xE6;  // 230
            
            // Keep the original alpha (transparency) - just change the color
            return (alpha << 24) | (r << 16) | (g << 8) | b;
        }
        
        return originalColor;
    }
}
