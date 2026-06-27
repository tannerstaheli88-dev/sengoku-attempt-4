package com.shioh.sengoku.mixin;

import com.shioh.sengoku.ai.DefendAlliesTargetGoal;
import com.shioh.sengoku.ai.PigMeleeAttackGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;

/**
 * Injects defensive/retaliation targeting into vanilla pigs so they behave like wild boar:
 * - retaliate when attacked (HurtByTargetGoal)
 * - alert nearby allied pigs to assist (DefendAlliesTargetGoal)
 */
@Mixin(Pig.class)
public class PigBehaviorMixin {

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void sengoku$addBoarLikeGoals(CallbackInfo ci) {
        Pig self = (Pig) (Object) this;
        Mob mob = (Mob) (Object) this;
        try {
            com.shioh.sengoku.config.SengokuConfig cfg = com.shioh.sengoku.config.SengokuConfig.getInstance();
            boolean enabled = cfg == null || cfg.pigBoarBehaviorEnabled;
            if (enabled) {
                // Removed verbose info log to reduce noisy console output

                // Remove panic goals to prevent fleeing behavior
                Iterator<WrappedGoal> goalIterator = ((MobAccessor)mob).getGoalSelector().getAvailableGoals().iterator();
                while (goalIterator.hasNext()) {
                    WrappedGoal wrappedGoal = goalIterator.next();
                    if (wrappedGoal.getGoal() instanceof PanicGoal) {
                        goalIterator.remove();
                        ((MobAccessor)mob).getGoalSelector().removeGoal(wrappedGoal.getGoal());
                    }
                }

                // Make pigs neutral - they retaliate when attacked and alert nearby allied pigs to assist
                ((MobAccessor)mob).getTargetSelector().addGoal(1, new HurtByTargetGoal((net.minecraft.world.entity.PathfinderMob)mob).setAlertOthers());
                ((MobAccessor)mob).getTargetSelector().addGoal(2, new DefendAlliesTargetGoal<>(mob, cfg != null ? cfg.pigCallForHelpRadius : 12.0D));

                // Add custom melee attack goal that doesn't require attack_damage attribute
                try {
                    ((MobAccessor)mob).getGoalSelector().addGoal(3, new PigMeleeAttackGoal((net.minecraft.world.entity.PathfinderMob)mob, 1.2D, false));
                } catch (Throwable t2) {
                    com.shioh.sengoku.sengokuFabric.LOGGER.warn("[PigBehaviorMixin] Failed to add PigMeleeAttackGoal: {}", t2.getMessage());
                }
            }
        } catch (Throwable t) {
            com.shioh.sengoku.sengokuFabric.LOGGER.warn("Failed to add neutral mob behavior to Pig: {}", t.getMessage());
        }
    }

}
