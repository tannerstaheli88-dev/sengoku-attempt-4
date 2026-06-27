package com.shioh.sengoku.mixin;

import com.shioh.sengoku.util.HurtTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.entity.LivingEntity.class)
public abstract class LivingEntityHurtMixin {

    @Inject(method = "hurt", at = @At("HEAD"))
    private void sengoku$recordHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        try {
            // Record at HEAD so the attacker is captured before the mob internally calls setTarget()
            if (!((Object)this instanceof Mob)) return;
            net.minecraft.world.entity.Entity attacker = source.getEntity();
            if (attacker instanceof Player) {
                Player p = (Player) attacker;
                Mob mob = (Mob) (Object) this;
                try {
                    if (mob.level().isClientSide()) return; // only track on server
                    HurtTracker.record(mob.getUUID(), p.getUUID(), mob.level().getGameTime());
                    try { mob.addTag("sengoku_recently_hurt"); } catch (Throwable ignored) {}
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }
    
}

