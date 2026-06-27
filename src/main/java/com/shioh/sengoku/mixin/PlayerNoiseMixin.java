package com.shioh.sengoku.mixin;

import com.shioh.sengoku.util.PlayerNoiseTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tracks player noise from sprinting, jumping, and taking damage.
 * Noisy actions alert nearby mobs even if player is behind them.
 */
@Mixin(Player.class)
public abstract class PlayerNoiseMixin {
    
    @Unique
    private boolean sengoku$wasSprinting = false;
    
    
    /**
     * Track sprinting and consumption (eating/drinking) each tick.
     */
@Unique
private int sengoku$useItemTicks = 0;

@Inject(method = "tick", at = @At("TAIL"))
private void trackHornNoise(CallbackInfo ci) {
    Player player = (Player)(Object)this;
    if (player.level().isClientSide) return;

    int currentTicks = player.getUseItemRemainingTicks();
    // Detect when item use finishes (ticks drop to 0 from a positive value)
    if (sengoku$useItemTicks > 0 && currentTicks == 0) {
        net.minecraft.world.item.ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof net.minecraft.world.item.InstrumentItem) {
            PlayerNoiseTracker.getInstance().addNoise(player, PlayerNoiseTracker.HORN_NOISE);
        }
    }
    sengoku$useItemTicks = currentTicks;
}
    @Inject(method = "tick", at = @At("HEAD"))
    private void trackNoise(CallbackInfo ci) {
        Player player = (Player)(Object)this;
        
        if (player.level().isClientSide) return;
        
        // Track sprinting noise
        boolean isSprinting = player.isSprinting();
        if (isSprinting && !sengoku$wasSprinting) {
            // Started sprinting
            PlayerNoiseTracker.getInstance().addNoise(player, PlayerNoiseTracker.SPRINT_NOISE);
        }
        sengoku$wasSprinting = isSprinting;
        
        // Continue making noise while sprinting (smaller amount)
        if (isSprinting && player.onGround() && player.getDeltaMovement().horizontalDistanceSqr() > 0.001) {
            PlayerNoiseTracker.getInstance().addNoise(player, PlayerNoiseTracker.SPRINT_NOISE * 0.1F);
        }
        
        // Jumping no longer produces noise (stealth improvement)
    }
    
    /**
     * Track damage noise
     */
    @Inject(method = "hurt", at = @At("HEAD"))
    private void trackDamageNoise(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player)(Object)this;
        
        if (player.level().isClientSide) return;
        
        // Taking damage makes noise (grunts, armor clanking, etc.)
        if (amount > 0.0F) {
            PlayerNoiseTracker.getInstance().addNoise(player, PlayerNoiseTracker.DAMAGE_NOISE);
        }
    }
}
