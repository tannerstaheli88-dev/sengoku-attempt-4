package com.shioh.sengoku.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Block;
import com.shioh.sengoku.util.TrapdoorStiffProperties;

/**
 * Prevent redstone (and other non-entity sources) from toggling trapdoors
 * that have the `stiff` property set to true.
 */
@Mixin(TrapDoorBlock.class)
public class TrapDoorRedstoneStiffMixin {

    @Inject(method = "neighborChanged", at = @At("HEAD"), cancellable = true)
    private void onNeighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston, CallbackInfo ci) {
        try {
            if (state.getProperties().contains(TrapdoorStiffProperties.STIFF)) {
                Boolean stiff = state.getValue(TrapdoorStiffProperties.STIFF);
                if (stiff != null && stiff) {
                    ci.cancel();
                }
            }
        } catch (Throwable ignored) {}
    }
}
