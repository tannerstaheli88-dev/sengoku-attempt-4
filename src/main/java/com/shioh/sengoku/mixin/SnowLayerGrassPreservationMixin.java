package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevents snow layers from converting grass blocks to dirt blocks.
 * In vanilla, 2+ layers of snow convert grass to dirt, but this mixin preserves grass blocks.
 */
@Mixin(SpreadingSnowyDirtBlock.class)
public class SnowLayerGrassPreservationMixin {
    
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void preventSnowGrassToDirtConversion(
            BlockState state, 
            ServerLevel level, 
            BlockPos pos, 
            RandomSource random, 
            CallbackInfo ci) {
        
        // Check if there's a snow layer above
        BlockState aboveState = level.getBlockState(pos.above());
        
        // If snow layer is above, prevent the vanilla dirt conversion
        if (aboveState.getBlock() instanceof SnowLayerBlock) {
            // Cancel the vanilla randomTick, preventing grass-to-dirt conversion
            ci.cancel();
        }
    }
}
