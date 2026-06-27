package com.shioh.sengoku.mixin;

import com.shioh.sengoku.ai.ParrotCautiousTemptGoal;
import com.shioh.sengoku.config.SengokuConfig;
import com.shioh.sengoku.entity.CrowEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Parrot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Makes parrots skittish and afraid of players until they slowly approach with seeds.
 * Similar to cat AI - they'll flee unless you're patient and have seeds.
 */
@Mixin(Parrot.class)
public class ParrotBehaviorMixin {
    
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addCautiousTemptGoal(CallbackInfo ci) {
        Parrot self = (Parrot)(Object)this;
        Mob mob = (Mob)(Object)this;
        SengokuConfig cfg = SengokuConfig.getInstance();
        // Check per-entity config: crows vs parrots
        if (self instanceof CrowEntity) {
            if (!cfg.crowsFleeEnabled) return;
        } else {
            if (!cfg.parrotsFleeEnabled) return;
        }
        
        // Remove vanilla tempt goal to replace it with our cautious version
        Set<WrappedGoal> goalsToRemove = ((MobAccessor)mob).getGoalSelector()
            .getAvailableGoals()
            .stream()
            .filter(goal -> goal.getGoal() instanceof TemptGoal)
            .collect(Collectors.toSet());
        
        for (WrappedGoal goal : goalsToRemove) {
            ((MobAccessor)mob).getGoalSelector().removeGoal(goal.getGoal());
        }
        
        // Add the cautious tempt goal with very high priority
        // Priority 1 means it will override almost everything except panic
        // fleeSpeed: 3.0 (parrots are EXTREMELY quick and flighty like real birds!)
        // approachSpeed: 0.6 (slow and cautious when being tempted to approach)
        ((MobAccessor)mob).getGoalSelector().addGoal(1, new ParrotCautiousTemptGoal(self, 3.0, 0.6));
    }
}
