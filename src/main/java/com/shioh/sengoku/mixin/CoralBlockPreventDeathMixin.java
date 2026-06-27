package com.shioh.sengoku.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.block.CoralFanBlock;
import net.minecraft.world.level.block.CoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.util.RandomSource;

@Mixin({CoralBlock.class, CoralFanBlock.class, CoralWallFanBlock.class})
public class CoralBlockPreventDeathMixin {
    @Inject(method = "randomTick(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)V", at = @At("HEAD"), cancellable = true)
    private void onRandomTick(BlockState state, Level world, BlockPos pos, RandomSource random, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        try {
            if (state != null && world != null && world.dimension() == Level.END) {
                ci.cancel();
            }
        } catch (Throwable t) {
            // defensive: don't let our mixin crash game startup
        }
    }
}
