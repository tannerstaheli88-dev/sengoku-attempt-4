package com.shioh.sengoku.mixin;

import com.shioh.sengoku.mixin.MobAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/**
 * Make axolotls behave more like ambush predators: remain mostly dormant
 * while in water and only move when they have a target or are out of water.
 * This is intentionally simple: it adds a high-priority "dormant" goal
 * that stops navigation and watches for targets.
 */
@Mixin(Axolotl.class)
public class AxolotlBehaviorMixin {

    // Track previous water state so we only update the attribute when it changes
    private boolean sengoku$wasInWater = false;
    private long sengoku$enteredWaterAt = -1L; // game time when axolotl entered water

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sengoku$addAmbushDormantGoal(CallbackInfo ci) {
        Axolotl axolotl = (Axolotl) (Object) this;

        try {
            // Add a high-priority dormant/ambush goal (priority 0)
            ((MobAccessor)axolotl).getGoalSelector().addGoal(0, new AmbushDormantGoal(axolotl));
        } catch (Throwable ignored) {}
    }

    /**
     * Goal that keeps the axolotl mostly stationary while it has no target
     * and is in water. If it leaves water or gets a target, normal goals resume.
     */
    private class AmbushDormantGoal extends Goal {
        private final Axolotl axolotl;

        public AmbushDormantGoal(Axolotl axolotl) {
            this.axolotl = axolotl;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            // Only be dormant when alive, in water, and not currently targeting someone
            try {
                if (!this.axolotl.isAlive()) return false;

                // Manage water-entry timing so they don't go dormant immediately upon touching water
                boolean inWater = this.axolotl.isInWater();
                long gameTime = 0L;
                try { gameTime = this.axolotl.level().getGameTime(); } catch (Throwable ignored) {}

                if (inWater) {
                    if (!sengoku$wasInWater) {
                        // Just entered water
                        sengoku$enteredWaterAt = gameTime;
                        sengoku$wasInWater = true;
                    }
                } else {
                    sengoku$wasInWater = false;
                    sengoku$enteredWaterAt = -1L;
                    return false; // not in water
                }

                // Require been in water for at least 6 seconds (120 ticks) before choosing a spot
                if (sengoku$enteredWaterAt >= 0L && gameTime - sengoku$enteredWaterAt < 120L) return false;

                // Don't go dormant if currently targeting someone
                if (this.axolotl.getTarget() != null) return false;

                // If recently hurt, don't stay dormant
                try { if (this.axolotl.hurtTime > 0) return false; } catch (Throwable ignored) {}

                // Seclusion checks: prefer seabed (block below should not be air) and not near players/other axolotls
                try {
                    BlockPos pos = this.axolotl.blockPosition();
                    // check block below is not air (i.e., seabed exists)
                    if (this.axolotl.level().getBlockState(pos.below()).isAir()) return false;

                    // avoid players nearby (12 blocks)
                    Player p = this.axolotl.level().getNearestPlayer(this.axolotl, 12.0D);
                    if (p != null && !p.isSpectator()) return false;

                    // avoid clustering with other axolotls (require <=1 nearby within 8 blocks)
                    int nearby = this.axolotl.level().getEntitiesOfClass(Axolotl.class, this.axolotl.getBoundingBox().inflate(8.0)).size();
                    if (nearby > 1) return false;
                } catch (Throwable ignored) {}

                return true;
            } catch (Throwable ignored) {
                return false;
            }
        }

        @Override
        public void start() {
            try {
                this.axolotl.getNavigation().stop();
                // When entering dormant ambush, reduce movement speed
                try {
                    AttributeInstance inst = this.axolotl.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (inst != null) inst.setBaseValue(0.0D); // fully idle while ambushing
                    // zero current motion immediately so they appear relaxed
                    try { this.axolotl.setDeltaMovement(0.0D, 0.0D, 0.0D); } catch (Throwable ignored) {}
                } catch (Throwable ignored) {}
            } catch (Throwable ignored) {}
        }

        @Override
        public void tick() {
            try {
                // Keep stationary and occasionally look around (don't repeatedly stop navigation)

                // Once in a while, twitch/look to simulate scanning for prey
                if (this.axolotl.getRandom().nextInt(200) == 0) {
                    float yaw = this.axolotl.getYRot() + (this.axolotl.getRandom().nextFloat() - 0.5F) * 40.0F;
                    double dx = Math.cos(Math.toRadians(yaw));
                    double dz = Math.sin(Math.toRadians(yaw));
                    // Look towards a horizontal offset at eye height to avoid extreme up/down pitch
                    this.axolotl.getLookControl().setLookAt(this.axolotl.getX() + dx, this.axolotl.getEyeY(), this.axolotl.getZ() + dz, 10.0F, 10.0F);
                }
            } catch (Throwable ignored) {}
        }

        @Override
        public void stop() {
            try {
                // Restore land movement speed when the goal stops
                try {
                    AttributeInstance inst = this.axolotl.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (inst != null) inst.setBaseValue(0.6D); // restore reasonable land speed
                } catch (Throwable ignored) {}
                this.axolotl.getNavigation().stop();
            } catch (Throwable ignored) {}
        }
    }

    // Movement speed is handled when the ambush goal starts/stops to avoid risky method injections.
}
