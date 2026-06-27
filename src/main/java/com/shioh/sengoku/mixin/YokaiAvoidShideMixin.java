package com.shioh.sengoku.mixin;

import com.shioh.sengoku.ai.AvoidShideGoal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds shide avoidance behavior to yokai mobs (defined by minecraft:yokai entity tag).
 */
@Mixin(Mob.class)
public abstract class YokaiAvoidShideMixin {
    
    @Shadow @Final protected GoalSelector goalSelector;
    
    private static final TagKey<EntityType<?>> YOKAI_TAG = 
            TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("minecraft:yokai"));
    private static final int SHIDE_AVOID_SEARCH_RANGE = 16;
    
    private boolean shideGoalAdded = false;
    
    @Inject(method = "registerGoals", at = @At("TAIL"))
    protected void addShideAvoidance(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        
        // Only add to yokai mobs that can pathfind
        if (self instanceof PathfinderMob pathfinderMob) {
            if (self.getType().is(YOKAI_TAG)) {
                // Add avoidance goal with priority 1 (high priority, runs before most other goals)
                // Speed modifier 1.2 = 20% faster when fleeing
                // Search range 16 blocks
                this.goalSelector.addGoal(1, new AvoidShideGoal(pathfinderMob, 1.2, SHIDE_AVOID_SEARCH_RANGE));
            }
        }
    }
    
    // Also try adding when the mob is added to the world
    @Inject(method = "tick", at = @At("HEAD"))
    protected void ensureShideGoalOnTick(CallbackInfo ci) {
        if (!shideGoalAdded) {
            Mob self = (Mob) (Object) this;
            
            if (self instanceof PathfinderMob pathfinderMob && self.getType().is(YOKAI_TAG)) {
                this.goalSelector.addGoal(1, new AvoidShideGoal(pathfinderMob, 1.2, SHIDE_AVOID_SEARCH_RANGE));
                shideGoalAdded = true;
            }
        }
    }
}
