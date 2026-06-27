package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.ai.GiantChasePlayerGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.shioh.sengoku.mixin.MobAccessor;

@Mixin(Giant.class)
public class GiantAIMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void addBuffedAI(EntityType<? extends Giant> entityType, net.minecraft.world.level.Level world, CallbackInfo ci) {
        Giant giant = (Giant) (Object) this;
        MobAccessor accessor = (MobAccessor) giant;

        // Clear default AI goals
        accessor.getGoalSelector().getAvailableGoals().clear();
        accessor.getTargetSelector().getAvailableGoals().clear();

        // Combat + chasing AI
        accessor.getGoalSelector().addGoal(1, new MeleeAttackGoal(giant, 1.2D, false));
        accessor.getGoalSelector().addGoal(2, new GiantChasePlayerGoal(giant)); // custom long-range chase
        accessor.getGoalSelector().addGoal(3, new WaterAvoidingRandomStrollGoal(giant, 1.0D));
        accessor.getGoalSelector().addGoal(4, new LookAtPlayerGoal(giant, Player.class, 8.0F));
        accessor.getGoalSelector().addGoal(5, new RandomLookAroundGoal(giant));

        // Targeting
        // Only target players who are in survival/adventure (not creative/spectator)
        accessor.getTargetSelector().addGoal(1, new NearestAttackableTargetGoal<>(giant, Player.class, 10, true, false,
            (entity) -> {
                if (!(entity instanceof Player)) return false;
                Player p = (Player) entity;
                return !p.isSpectator() && !p.isCreative();
            }
        ));
        accessor.getTargetSelector().addGoal(2, new HurtByTargetGoal(giant));

        // Buff attributes
        giant.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(16.0D);
        giant.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D);
        giant.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(60.0D); // extended aggro range

    }
}
