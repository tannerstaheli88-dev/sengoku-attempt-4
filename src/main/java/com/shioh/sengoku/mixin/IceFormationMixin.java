package com.shioh.sengoku.mixin;

import com.shioh.sengoku.system.WarmWaterSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class IceFormationMixin {
    
    /**
     * Prevent ice formation on warm water (water near magma blocks)
     * This intercepts the random tick that would normally freeze water
     */
    @Inject(
        method = "tickPrecipitation",
        at = @At("HEAD"),
        cancellable = true
    )
    private void preventWarmWaterFreezing(BlockPos pos, CallbackInfo ci) {
        ServerLevel level = (ServerLevel)(Object)this;
        
        // Check if this position has water
        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.isEmpty() && fluidState.is(Fluids.WATER)) {
            // Check if this water is warm (near magma blocks)
            if (WarmWaterSystem.isNearMagmaBlock(level, pos)) {
                // Don't process ice formation for warm water
                // Water near magma blocks should never freeze
                return;
            }
        }
        
        // Also melt any ice that's near magma blocks
        BlockState blockState = level.getBlockState(pos);
        if (blockState.is(Blocks.ICE) || blockState.is(Blocks.FROSTED_ICE)) {
            if (WarmWaterSystem.isNearMagmaBlock(level, pos)) {
                // Melt ice into water
                level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
            }
        }
    }
}
