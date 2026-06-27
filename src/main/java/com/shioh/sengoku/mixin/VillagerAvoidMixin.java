package com.shioh.sengoku.mixin;
 
import com.shioh.sengoku.ai.FleeToHomeGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
 
@Mixin(Villager.class)
public class VillagerAvoidMixin {
 
    @Inject(method = "<init>", at = @At("RETURN"))
    private void addFleeToHomeGoal(CallbackInfo ci) {
        Villager self = (Villager)(Object)this;
        Mob mob = (Mob)(Object)this;
 
        // Priority 1 — runs before vanilla's own flee/avoid goals (which are at 3+)
        // FleeToHomeGoal handles all threat detection internally,
        // so you no longer need the individual AvoidEntityGoal calls.
        ((MobAccessor)mob).getGoalSelector().addGoal(1, new FleeToHomeGoal(self));
    }
}
 
