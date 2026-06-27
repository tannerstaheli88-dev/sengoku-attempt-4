package com.shioh.sengoku.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import com.shioh.sengoku.util.TrapdoorStiffProperties;

/**
 * Cancel neighbor-driven block updates for trapdoors that are marked `stiff`.
 * This prevents redstone and other neighbor mechanisms from opening/closing them.
 */
@Mixin(Block.class)
public class BlockNeighborChangedStiffMixin {

    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void onNeighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston, CallbackInfo ci) {
        try {
            if (state.getBlock() instanceof TrapDoorBlock) {
                if (state.getProperties().contains(TrapdoorStiffProperties.STIFF)) {
                    Boolean stiff = state.getValue(TrapdoorStiffProperties.STIFF);
                    if (stiff != null && stiff.booleanValue()) {
                        ci.cancel();
                    }
                }
            }
        } catch (Throwable ignored) {}
    }
}
