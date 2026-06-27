package com.shioh.sengoku.mixin;

import com.shioh.sengoku.registry.SengokuBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrowingPlantBlock.class)
public class WeepingVinesPlacementMixin {
    
    @Shadow @Final protected Direction growthDirection;
    
    /**
     * Allows weeping vines to be placed under bloodgood leaves.
     * Injects at the HEAD of canSurvive to check if the block above is bloodgood leaves
     * when the plant is growing downward (weeping vines).
     */
    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void allowBloodgoodLeavesSupport(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        // Only apply to downward-growing plants (weeping vines grow DOWN)
        if (this.growthDirection == Direction.DOWN) {
            BlockPos abovePos = pos.relative(Direction.UP);
            BlockState aboveState = level.getBlockState(abovePos);
            
            // If the block above is bloodgood leaves, allow placement
            if (aboveState.is(SengokuBlocks.BLOODGOOD_LEAVES)) {
                cir.setReturnValue(true);
            }
        }
    }
}
