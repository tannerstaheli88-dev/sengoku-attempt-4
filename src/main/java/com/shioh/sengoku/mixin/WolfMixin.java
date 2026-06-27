package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Wolf;
import com.shioh.sengoku.entity.MacaqueEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes wild wolves hunt macaques like they hunt sheep in vanilla.
 */
@Mixin(Wolf.class)
public class WolfMixin {

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addMacaqueTargetGoal(CallbackInfo ci) {
        Wolf self = (Wolf) (Object) this;
        
        // Add macaques to wolf hunting behavior, similar to sheep
        // Priority 7 matches vanilla wolf targeting goals (sheep, rabbits, foxes, etc.)
        ((MobAccessor) self).getTargetSelector().addGoal(7, new NearestAttackableTargetGoal<>(
            self, 
            MacaqueEntity.class, 
            10, 
            true, 
            false, 
            (entity) -> {
                // Only hunt if wolf is not tamed (wild wolves only)
                return !self.isTame();
            }
        ));
    }
}
