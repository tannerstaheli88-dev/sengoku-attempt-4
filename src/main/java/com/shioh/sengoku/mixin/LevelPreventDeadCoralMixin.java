package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelPreventDeadCoralMixin {
    /**
     * Prevent vanilla or other mods from setting dead coral blocks in the End dimension.
     * This stops the root cause (blocks being converted to dead variants) rather than
     * trying to replace them later via a server tick.
     */
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", at = @At("HEAD"), cancellable = true)
    private void onSetBlock(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level)(Object)this;
        try {
                if (level != null && level.dimension() == Level.END && newState != null) {
                if (newState.is(Blocks.DEAD_BRAIN_CORAL_BLOCK)
                    || newState.is(Blocks.DEAD_TUBE_CORAL_BLOCK)
                    || newState.is(Blocks.DEAD_BUBBLE_CORAL_BLOCK)
                    || newState.is(Blocks.DEAD_FIRE_CORAL_BLOCK)
                    || newState.is(Blocks.DEAD_HORN_CORAL_BLOCK)
                    || newState.is(Blocks.DEAD_BRAIN_CORAL)
                    || newState.is(Blocks.DEAD_TUBE_CORAL)
                    || newState.is(Blocks.DEAD_BUBBLE_CORAL)
                    || newState.is(Blocks.DEAD_FIRE_CORAL)
                    || newState.is(Blocks.DEAD_HORN_CORAL)
                    || newState.is(Blocks.DEAD_BRAIN_CORAL_FAN)
                    || newState.is(Blocks.DEAD_TUBE_CORAL_FAN)
                    || newState.is(Blocks.DEAD_BUBBLE_CORAL_FAN)
                    || newState.is(Blocks.DEAD_FIRE_CORAL_FAN)
                    || newState.is(Blocks.DEAD_HORN_CORAL_FAN)
                    || newState.is(Blocks.DEAD_BRAIN_CORAL_WALL_FAN)
                    || newState.is(Blocks.DEAD_TUBE_CORAL_WALL_FAN)
                    || newState.is(Blocks.DEAD_BUBBLE_CORAL_WALL_FAN)
                    || newState.is(Blocks.DEAD_FIRE_CORAL_WALL_FAN)
                    || newState.is(Blocks.DEAD_HORN_CORAL_WALL_FAN)) {
                    // Cancel setting dead coral in the End.
                    cir.setReturnValue(false);
                    cir.cancel();
                }
            }
        } catch (Throwable ignored) {
            // defensive: don't crash game if mixin has an issue
        }
    }
}
