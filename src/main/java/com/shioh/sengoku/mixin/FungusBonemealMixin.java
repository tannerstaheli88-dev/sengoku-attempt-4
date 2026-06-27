package com.shioh.sengoku.mixin;

import com.shioh.sengoku.sengokuFabric;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FungusBlock.class)
public class FungusBonemealMixin {

    @Inject(method = "performBonemeal", at = @At("HEAD"), cancellable = true)
    private void onPerformBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state, CallbackInfo ci) {
        try {
            // Handle crimson and warped fungus
            boolean isCrimson = state.is(Blocks.CRIMSON_FUNGUS);
            boolean isWarped = state.is(Blocks.WARPED_FUNGUS);
            if (!isCrimson && !isWarped) return;

            // Allow growth when the block below is nylium OR common soil blocks
            BlockState below = world.getBlockState(pos.below());
            boolean validBase = below.is(net.minecraft.tags.BlockTags.NYLIUM)
                || below.is(Blocks.DIRT)
                || below.is(Blocks.COARSE_DIRT)
                || below.is(Blocks.PODZOL)
                || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.ROOTED_DIRT);
            if (!validBase) return;

            // Lookup our configured feature (minecraft namespace)
            net.minecraft.resources.ResourceLocation lookup = net.minecraft.resources.ResourceLocation.parse(isCrimson ? "minecraft:bloodgood_tree" : "minecraft:willow_tree");
            ConfiguredFeature<?, ?> cfg = world.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).get(lookup);
            if (cfg == null) return;

            // Remove the fungus block and place the feature
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            cfg.place(world, world.getChunkSource().getGenerator(), random, pos);
            ci.cancel();
        } catch (Throwable t) {
            // Swallow errors to avoid breaking bonemeal behavior
        }
    }

    @Inject(method = "isValidBonemealTarget(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    private void onIsValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (!state.is(Blocks.CRIMSON_FUNGUS) && !state.is(Blocks.WARPED_FUNGUS)) return;
            BlockState below = world.getBlockState(pos.below());
            boolean validBase = below.is(net.minecraft.tags.BlockTags.NYLIUM)
                || below.is(Blocks.DIRT)
                || below.is(Blocks.COARSE_DIRT)
                || below.is(Blocks.PODZOL)
                || below.is(Blocks.GRASS_BLOCK)
                || below.is(Blocks.ROOTED_DIRT);
            if (validBase) cir.setReturnValue(true);
        } catch (Throwable t) {
            // ignore
        }
    }
}
