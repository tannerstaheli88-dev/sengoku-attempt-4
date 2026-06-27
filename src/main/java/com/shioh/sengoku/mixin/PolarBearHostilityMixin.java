package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.player.Player;
import com.shioh.sengoku.entity.BanditEntity;
import com.shioh.sengoku.entity.RoninEntity;
import com.shioh.sengoku.entity.TakedaSamuraiEntity;
import com.shioh.sengoku.entity.SatomiSamuraiEntity;
import com.shioh.sengoku.entity.KobayakawaSamuraiEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds a proper target goal to polar bears so they will target nearby players
 * using the vanilla goal system instead of being forced each tick.
 */
@Mixin(PolarBear.class)
public class PolarBearHostilityMixin {

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addPlayerTargetGoal(CallbackInfo ci) {
        PolarBear self = (PolarBear) (Object) this;
        // Prefer targeting mod hostile mobs first
        ((MobAccessor) self).getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(self, BanditEntity.class, true));
        ((MobAccessor) self).getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(self, RoninEntity.class, true));
        ((MobAccessor) self).getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(self, TakedaSamuraiEntity.class, true));
        ((MobAccessor) self).getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(self, SatomiSamuraiEntity.class, true));
        ((MobAccessor) self).getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(self, KobayakawaSamuraiEntity.class, true));

        // React when hurt
        ((MobAccessor) self).getTargetSelector().addGoal(2, new HurtByTargetGoal(self));

        // Add a nearest-player target goal. Priority 3 matches other mixins in this mod.
        ((MobAccessor) self).getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(self, Player.class, 10, true, false,
            (entity) -> {
                if (!(entity instanceof Player)) return false;
                Player p = (Player) entity;
                // Only target players who are not spectator/creative
                return !p.isSpectator() && !p.isCreative();
            }
        ));
    }
}
