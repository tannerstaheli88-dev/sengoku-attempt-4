package com.shioh.sengoku.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevent wood products (planks, doors, logs, etc.) from catching or spreading fire in the Nether.
 * Leaves remain flammable.
 *
 * This injects at the head of FireBlock spread logic and cancels spread when the target
 * block is a wood product and the world is the Nether. This avoids ticking handlers and
 * keeps the change localized and performant.
 */
@Mixin(FireBlock.class)
public class FireBlockNetherMixin {

    @Inject(method = "trySpreadFire", at = @At("HEAD"), cancellable = true)
    private void onTrySpreadFire(Level world, BlockPos pos, int chance, Direction direction, BlockState state, CallbackInfo ci) {
        if (world == null) return;
        if (!isNether(world)) return;
        // Check multiple candidate targets: the passed-in state, the position itself,
        // and the relative position in case callers provide different semantics.
        try {
            if (isWoodProduct(state)) {
                ci.cancel();
                return;
            }
        } catch (Throwable ignored) {}

        try {
            BlockState direct = world.getBlockState(pos);
            if (isWoodProduct(direct)) {
                ci.cancel();
                return;
            }
        } catch (Throwable ignored) {}

        try {
            BlockPos rel = pos.relative(direction);
            BlockState relState = world.getBlockState(rel);
            if (isWoodProduct(relState)) {
                ci.cancel();
            }
        } catch (Throwable ignored) {}
    }

    @Inject(method = "tryCatchFire", at = @At("HEAD"), cancellable = true)
    private void onTryCatchFire(Level world, BlockPos pos, int chance, Direction face, BlockState state, CallbackInfo ci) {
        if (world == null) return;
        if (!isNether(world)) return;
        // Check multiple candidate targets similar to trySpreadFire.
        try {
            if (isWoodProduct(state)) {
                ci.cancel();
                return;
            }
        } catch (Throwable ignored) {}

        try {
            BlockState direct = world.getBlockState(pos);
            if (isWoodProduct(direct)) {
                ci.cancel();
                return;
            }
        } catch (Throwable ignored) {}

        try {
            BlockPos rel = pos.relative(face);
            BlockState relState = world.getBlockState(rel);
            if (isWoodProduct(relState)) {
                ci.cancel();
            }
        } catch (Throwable ignored) {}
    }

    private static boolean isNether(Level world) {
        try {
            return world.dimension().location().getPath().equals("the_nether");
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean isWoodProduct(BlockState state) {
        if (state == null) return false;
        // Leaves are explicitly allowed to burn
        if (state.is(BlockTags.LEAVES)) return false;

        // Check common wood-related tags (logs, planks). Leaves are intentionally
        // excluded above so they still burn.
        try {
            if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)) return true;
            // Fallback: check common wooden block classes (fences, slabs, stairs, doors, etc.)
            try {
                Object block = state.getBlock();
                if (block instanceof FenceBlock || block instanceof SlabBlock || block instanceof StairBlock || block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof ButtonBlock || block instanceof FenceGateBlock || block instanceof SignBlock) {
                    return true;
                }
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}

        return false;
    }

    @Inject(method = "isValidFireLocation", at = @At("HEAD"), cancellable = true)
    private void onIsValidFireLocation(BlockGetter world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (world == null) return;
            // We only care about server worlds (Level) for Nether check
            if (!(world instanceof Level)) return;
            Level lvl = (Level) world;
            if (!isNether(lvl)) return;

            BlockState state = world.getBlockState(pos);
            if (isWoodProduct(state)) {
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {}
    }
}
