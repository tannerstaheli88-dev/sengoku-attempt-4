package com.shioh.sengoku.mixin;

import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds custom loud musket fire sound on top of vanilla crossbow sound
 */
@Mixin(CrossbowItem.class)
public class CrossbowLouderSoundMixin {
    
    @Inject(
        method = "shootProjectile",
        at = @At("TAIL")
    )
    private static void playMusketFireSound(
            LivingEntity shooter, 
            Projectile projectile, 
            int index, 
            float velocity, 
            float inaccuracy, 
            float angle, 
            LivingEntity target, 
            CallbackInfo ci) {
        
        if (!shooter.level().isClientSide) {
            // Play custom loud musket fire sound
            // Volume: 5.0 - very loud, can be heard from ~80 blocks away
            shooter.level().playSound(
                null,
                shooter.getX(),
                shooter.getY(),
                shooter.getZ(),
                SoundRegistry.MUSKET_FIRE,
                SoundSource.PLAYERS,
                5.0F, // Very loud volume
                1.0F  // Normal pitch
            );
        }
    }
}
