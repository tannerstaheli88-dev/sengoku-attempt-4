package com.shioh.sengoku.mixin;

import com.shioh.sengoku.util.PlayerNoiseTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds noise when a player successfully breaks a block (tool impacts, debris).
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {
    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void sengoku$addBlockBreakNoise(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return; // Only if block actually broken
        try {
            ServerPlayerGameMode self = (ServerPlayerGameMode)(Object)this;
            ServerPlayer player = null;
            try {
                java.lang.reflect.Field f = self.getClass().getDeclaredField("player");
                f.setAccessible(true);
                player = (ServerPlayer) f.get(self);
            } catch (Throwable ignored) {}
            if (player != null && !player.level().isClientSide) {
                PlayerNoiseTracker.getInstance().addNoise(player, PlayerNoiseTracker.BLOCK_BREAK_NOISE);
            }
        } catch (Throwable ignored) {}
    }
}
