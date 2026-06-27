package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.entity.YukiOnnaEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Flying charge attack goal for Yuki Onna - flies towards target like a vex
 */
public class YukiOnnaChargeAttackGoal extends Goal {
    private final YukiOnnaEntity yukiOnna;
    private int rechargeCooldown = 0;
    private int teleportTimer = 0;
    private int orbitTimer = 0;
    private Vec3 orbitOffset = Vec3.ZERO;
    
    public YukiOnnaChargeAttackGoal(YukiOnnaEntity yukiOnna) {
        this.yukiOnna = yukiOnna;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }
    
    @Override
    public boolean canUse() {
        LivingEntity target = this.yukiOnna.getTarget();
        return target != null && target.isAlive();
    }
    
    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.yukiOnna.getTarget();
        return target != null && target.isAlive();
    }
    
    @Override
    public void start() {
        LivingEntity target = this.yukiOnna.getTarget();
        if (target != null) {
            // Raise arms like evoker
            this.yukiOnna.setAggressive(true);
            this.pickNewOrbit(target);
        }
        
        this.rechargeCooldown = 0;
        this.teleportTimer = 100 + this.yukiOnna.getRandom().nextInt(100); // Teleport less often to be less annoying
    }
    
    @Override
    public void stop() {
        this.yukiOnna.setAggressive(false);
        this.rechargeCooldown = 20; // 1 second cooldown
        this.orbitOffset = Vec3.ZERO;
    }
    
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
    
    @Override
    public void tick() {
        LivingEntity target = this.yukiOnna.getTarget();
        if (target == null) {
            return;
        }
        
        // Teleport occasionally during combat for unpredictability
        if (--this.teleportTimer <= 0) {
            double distance = this.yukiOnna.distanceToSqr(target);
            if (distance > 100.0D || this.yukiOnna.getRandom().nextFloat() < 0.3F) {
                this.yukiOnna.teleportNearTarget(target);
                this.pickNewOrbit(target);
            }
            this.teleportTimer = 120 + this.yukiOnna.getRandom().nextInt(120);
        }
        
        if (--this.orbitTimer <= 0) {
            this.pickNewOrbit(target);
        }

        double distanceSq = this.yukiOnna.distanceToSqr(target);
        double speed = 1.6D + Math.min(distanceSq, 144.0D) / 144.0D * 0.6D;

        Vec3 targetPos = target.position();
        Vec3 wave = new Vec3(
            Math.sin((this.yukiOnna.tickCount + 20) * 0.35F) * 0.8D,
            Math.sin((this.yukiOnna.tickCount + 5) * 0.25F) * 0.35D,
            Math.cos((this.yukiOnna.tickCount + 20) * 0.35F) * 0.8D
        );

        Vec3 moveTarget = targetPos.add(this.orbitOffset).add(wave);
        this.yukiOnna.getMoveControl().setWantedPosition(
            moveTarget.x,
            moveTarget.y + target.getEyeHeight() * 0.25D,
            moveTarget.z,
            speed
        );
        
        // Look at target
        this.yukiOnna.getLookControl().setLookAt(target, 30.0F, 30.0F);
        
        // If close enough, attack with short cooldown to avoid constant hits
        if (distanceSq < 6.25D && this.rechargeCooldown <= 0) {
            this.yukiOnna.doHurtTarget(target);
            this.rechargeCooldown = 10;
        }
        
        if (this.rechargeCooldown > 0) {
            --this.rechargeCooldown;
        }
    }

    private void pickNewOrbit(LivingEntity target) {
        Vec3 look = target.getViewVector(1.0F).normalize();
    Vec3 right = new Vec3(-look.z, 0.0D, look.x).normalize();
    if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }

        double radius = 1.5D + this.yukiOnna.getRandom().nextDouble() * 2.0D;
        double side = (this.yukiOnna.getRandom().nextBoolean() ? 1 : -1) * radius;
        double behind = -1.2D - this.yukiOnna.getRandom().nextDouble() * 2.0D;
        Vec3 lateral = right.scale(side);
        Vec3 backward = look.scale(behind);
        double vertical = 0.4D + this.yukiOnna.getRandom().nextDouble() * 1.2D;

        this.orbitOffset = lateral.add(backward).add(0.0D, vertical, 0.0D);
        this.orbitTimer = 15 + this.yukiOnna.getRandom().nextInt(25);
    }
}
