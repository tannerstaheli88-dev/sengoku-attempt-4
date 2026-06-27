package com.shioh.sengoku.mixin;

import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class GiantFootstepMixin {

    private BlockPos lastStepPos = null;
    private int tickCooldown = 0; // Cooldown in ticks (20 ticks = 1 second)

    @Inject(method = "tick", at = @At("TAIL"))
    private void playGiantFootstep(CallbackInfo ci) {
        if (!((Object)this instanceof Giant)) return;

        Giant giant = (Giant) (Object) this;
        Level world = giant.level();

        if (world.isClientSide) return;

        // Decrease cooldown
        if (tickCooldown > 0) tickCooldown--;

        BlockPos currentPos = giant.blockPosition();
        if ((lastStepPos == null || !currentPos.equals(lastStepPos)) && tickCooldown <= 0) {
            // Giant has moved to a new block AND cooldown is over
            world.playSound(
                    null,
                    giant.getX(),
                    giant.getY(),
                    giant.getZ(),
                    SoundRegistry.GIANT_FOOTSTEPS,
                    SoundSource.HOSTILE,
                    3.0f,
                    1.0f
            );
            lastStepPos = currentPos;
            tickCooldown = 10; // 10 ticks cooldown (~0.5 seconds) between footsteps
        }
    }
}
