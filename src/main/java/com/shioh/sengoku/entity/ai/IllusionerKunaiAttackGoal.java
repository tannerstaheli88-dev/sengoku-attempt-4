package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.entity.KunaiEntity;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.EnumSet;

/**
 * Custom kunai throwing attack goal for shinobi-style mobs.
 */
public class IllusionerKunaiAttackGoal extends Goal {
     private final Mob attacker;
    private final double speedModifier;
    private int attackIntervalMin;
    private final float attackRadiusSqr;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;

    public IllusionerKunaiAttackGoal(Mob attacker, double speedModifier, int attackInterval, float attackRadius) {
        this.attacker = attacker;
        this.speedModifier = speedModifier;
        this.attackIntervalMin = attackInterval;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    /** Forces attackTime to 1 so a kunai is thrown on the very next tick. */
    public void forceImmediateAttack() {
        this.attackTime = 1;
    }

    @Override
    public boolean canUse() {
        return this.attacker.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse() || !this.attacker.getNavigation().isDone();
    }

    @Override
    public void start() {
        super.start();
        this.attacker.setAggressive(true);
    }

    @Override
    public void stop() {
        super.stop();
        this.attacker.setAggressive(false);
        this.seeTime = 0;
        this.attackTime = -1;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.attacker.getTarget();
        if (target == null) {
            return;
        }

        double distanceToTarget = this.attacker.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean canSeeTarget = this.attacker.getSensing().hasLineOfSight(target);

        if (canSeeTarget) {
            ++this.seeTime;
        } else {
            this.seeTime = 0;
        }

        // Move towards or strafe around target
        if (distanceToTarget > (double)this.attackRadiusSqr && canSeeTarget) {
            this.attacker.getNavigation().moveTo(target, this.speedModifier);
            this.strafingTime = -1;
        } else if (distanceToTarget < (double)(this.attackRadiusSqr * 0.75F)) {
            this.attacker.getNavigation().stop();
            ++this.strafingTime;
        } else {
            this.attacker.getNavigation().stop();
            this.strafingTime = -1;
        }

        // Strafe movement
        if (this.strafingTime >= 20) {
            if ((double)this.attacker.getRandom().nextFloat() < 0.3D) {
                this.strafingClockwise = !this.strafingClockwise;
            }

            if ((double)this.attacker.getRandom().nextFloat() < 0.3D) {
                this.strafingBackwards = !this.strafingBackwards;
            }

            this.strafingTime = 0;
        }

        if (this.strafingTime > -1) {
            if (distanceToTarget > (double)(this.attackRadiusSqr * 0.75F)) {
                this.strafingBackwards = false;
            } else if (distanceToTarget < (double)(this.attackRadiusSqr * 0.25F)) {
                this.strafingBackwards = true;
            }

            this.attacker.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.attacker.lookAt(target, 30.0F, 30.0F);
        } else {
            this.attacker.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // Handle kunai throwing timing
        --this.attackTime;
        if (this.attackTime == 0) {
            if (!canSeeTarget) {
                return;
            }

            // Throw kunai
            this.throwKunai(target);
            this.attackTime = this.attackIntervalMin;
        } else if (this.attackTime < 0) {
            this.attackTime = this.attackIntervalMin;
        }
    }

    private void throwKunai(LivingEntity target) {
        // Get the kunai item from the registry instead of the static field to avoid initialization issues
        Item kunaiItem = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(sengokuFabric.MODID, "kunai"));
        if (kunaiItem == null) {
            return; // Kunai not registered, shouldn't happen but safety check
        }
        
        // Create kunai entity
        ItemStack kunaiStack = new ItemStack(kunaiItem);
        KunaiEntity kunai = new KunaiEntity(this.attacker.level(), this.attacker, kunaiStack);
        
        // Calculate trajectory
        double deltaX = target.getX() - this.attacker.getX();
        double deltaY = target.getY(0.3333333333333333D) - kunai.getY();
        double deltaZ = target.getZ() - this.attacker.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        
        // Set velocity - faster than normal kunai throws
        float velocity = 2.5F;
        kunai.shoot(deltaX, deltaY + horizontalDistance * 0.20000000298023224D, deltaZ, velocity, 1.0F);
        
        // Set damage
        kunai.setBaseDamage(3.0D);
        
        // Spawn the kunai
        this.attacker.level().addFreshEntity(kunai);
        
        // Play throw sound using level().playSound instead of entity.playSound
        this.attacker.level().playSound(
            null,
            this.attacker.getX(),
            this.attacker.getY(),
            this.attacker.getZ(),
            SoundEvents.PLAYER_ATTACK_SWEEP,
            this.attacker.getSoundSource(),
            1.0F,
            1.0F
        );
    }
}
