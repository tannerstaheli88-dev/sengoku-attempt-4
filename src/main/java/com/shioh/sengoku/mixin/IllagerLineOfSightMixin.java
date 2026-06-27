package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TargetGoal.class)
public abstract class IllagerLineOfSightMixin {

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void sengoku$preventBlindAggro(CallbackInfo ci) {
        TargetGoalAccessor accessor = (TargetGoalAccessor) this;
        Mob mob = accessor.getMob();

        if (!(mob instanceof AbstractIllager)) return;

        // Boost vanilla vindicator follow range on first target acquisition
        if (mob instanceof Vindicator && mob.getClass() == Vindicator.class) {
            var followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
            if (followRange != null && followRange.getBaseValue() < 32.0D) {
                followRange.setBaseValue(32.0D);
            }
        }

        LivingEntity target = mob.getTarget();
        if (target == null) return;

        if (isSeparatedByFloor(mob, target)) {
            mob.setTarget(null);
            ci.cancel();
        }
    }

    private static boolean isSeparatedByFloor(Mob mob, LivingEntity target) {
        int mobY = mob.blockPosition().getY();
        int targetY = target.blockPosition().getY();
        int minY = Math.min(mobY, targetY);
        int maxY = Math.max(mobY, targetY);

        if (maxY - minY < 2) return false;

        double midX = (mob.getX() + target.getX()) * 0.5D;
        double midZ = (mob.getZ() + target.getZ()) * 0.5D;
        double[] sampleX = {mob.getX(), target.getX(), midX};
        double[] sampleZ = {mob.getZ(), target.getZ(), midZ};

        for (int i = 0; i < sampleX.length; i++) {
            for (int y = minY + 1; y < maxY; y++) {
                BlockPos pos = new BlockPos(
                    (int) Math.floor(sampleX[i]),
                    y,
                    (int) Math.floor(sampleZ[i])
                );
                BlockState state = mob.level().getBlockState(pos);
                if (!state.isAir()) return true;
            }
        }

        return false;
    }
}