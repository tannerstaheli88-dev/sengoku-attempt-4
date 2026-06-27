package com.shioh.sengoku.mixin;

import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.item.ItemStack;

/**
 * Prevent trapdoors marked as `stiff` from being opened by wind charges / explosion hits.
 */
@Mixin(TrapDoorBlock.class)
public class TrapDoorExplosionStiffMixin {

    @Inject(method = "onExplosionHit", at = @At("HEAD"), cancellable = true)
    private void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer, CallbackInfo ci) {
        try {
            // If the trapdoor has the STIFF property and it's true, cancel the vanilla explosion handling
            // so wind charges / breezes (which use Explosion.canTriggerBlocks()) don't toggle it.
            if (state.getProperties().contains(com.shioh.sengoku.util.TrapdoorStiffProperties.STIFF)) {
                Boolean stiff = state.getValue(com.shioh.sengoku.util.TrapdoorStiffProperties.STIFF);
                if (stiff != null && stiff.booleanValue()) {
                    ci.cancel();
                }
            }
        } catch (Throwable ignored) {}
    }
}
