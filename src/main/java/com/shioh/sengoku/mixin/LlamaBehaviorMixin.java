package com.shioh.sengoku.mixin;

import com.shioh.sengoku.ai.LlamaCautiousTemptGoal;
import com.shioh.sengoku.config.SengokuConfig;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

/**
 * Makes llamas skittish and afraid of players until they slowly approach with food.
 * Replaces vanilla TemptGoal with LlamaCautiousTemptGoal.
 * Does NOT apply to trader llamas.
 */
@Mixin(Llama.class)
public class LlamaBehaviorMixin {

    @Inject(method = "registerGoals", at = @At("RETURN"))
    private void modifyLlamaBehavior(CallbackInfo ci) {
        Llama self = (Llama)(Object)this;
        
        // Skip trader llamas - they should keep their vanilla behavior
        if (self instanceof TraderLlama) {
            return;
        }
        
        Mob mob = (Mob)(Object)this;
        if (!SengokuConfig.getInstance().llamasFleeEnabled) return;

        // Remove the vanilla TemptGoal
        Iterator<WrappedGoal> goalIterator = ((MobAccessor)mob).getGoalSelector().getAvailableGoals().iterator();
        while (goalIterator.hasNext()) {
            WrappedGoal wrappedGoal = goalIterator.next();
            if (wrappedGoal.getGoal() instanceof TemptGoal) {
                ((MobAccessor)mob).getGoalSelector().removeGoal(wrappedGoal.getGoal());
                break;
            }
        }

        // Add custom cautious tempt goal
        ((MobAccessor)mob).getGoalSelector().addGoal(1, new LlamaCautiousTemptGoal(self, 1.3, 0.8));
    }
}
