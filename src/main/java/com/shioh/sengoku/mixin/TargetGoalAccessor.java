package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for TargetGoal to get the mob field.
 */
@Mixin(TargetGoal.class)
public interface TargetGoalAccessor {
    @Accessor("mob")
    Mob getMob();
}
