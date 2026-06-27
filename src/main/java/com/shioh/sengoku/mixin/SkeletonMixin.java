package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.BanditEntity;
import com.shioh.sengoku.entity.RoninEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSkeleton.class)
public class SkeletonMixin {
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addIllagerTargets(CallbackInfo ci) {
        AbstractSkeleton self = (AbstractSkeleton)(Object)this;
        Mob mob = (Mob)(Object)this;

        ((MobAccessor)mob).getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(self, Vindicator.class, true));
        ((MobAccessor)mob).getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(self, Pillager.class, true));
        ((MobAccessor)mob).getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(self, Evoker.class, true));
        ((MobAccessor)mob).getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(self, RoninEntity.class, true));
        ((MobAccessor)mob).getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(self, BanditEntity.class, true));
        ((MobAccessor)mob).getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(self, Villager.class, true));
    }
}
