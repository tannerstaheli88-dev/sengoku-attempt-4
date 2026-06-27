package com.shioh.sengoku.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Maikubi - A flying hostile mob that resembles the Wither but without boss mechanics bc maikubi used to be the wither.
 * Shoots blaze fireballs instead of wither skulls and is a standard enemy.
 */
public class MaikubiEntity extends Monster {
    
    private static final int SHOOT_COOLDOWN = 80; // 2 seconds between shots
    private int shootCooldown = 0;
    
    public MaikubiEntity(EntityType<? extends MaikubiEntity> type, Level level) {
        super(type, level);
        // Use flying move control similar to Ghast so the mob truly flies
        this.moveControl = new net.minecraft.world.entity.ai.control.FlyingMoveControl(this, 20, true);
        // Maikubi is a flying mob with no gravity
        this.setNoGravity(true);
        this.xpReward = 10; // Good XP for a flying ranged enemy
    }
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MaikubiAttackGoal(this));
        this.goalSelector.addGoal(2, new MaikubiRandomFloatGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.FOLLOW_RANGE, 40.0)
            .add(Attributes.MOVEMENT_SPEED, 0.25)
            .add(Attributes.FLYING_SPEED, 0.25)
            .add(Attributes.ARMOR, 2.0);
    }
    
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_HURT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }
    
    @Override
    public void aiStep() {
        if (!this.level().isClientSide) {
            if (shootCooldown > 0) {
                shootCooldown--;
            }
        }
        
        // Add ambient smoke particles similar to wither
        if (this.level().isClientSide) {
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(
                    ParticleTypes.SMOKE,
                    this.getRandomX(0.5),
                    this.getRandomY(),
                    this.getRandomZ(0.5),
                    0.0, 0.0, 0.0
                );
            }
        }
        
        super.aiStep();
    }
    
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
    }
    
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false; // Flying mob doesn't take fall damage
    }
    
    @Override
    protected void checkFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos) {
        // No fall damage
    }
    
    /**
     * Shoots a blaze fireball at the target
     */
    private void performRangedAttack(LivingEntity target) {
        if (shootCooldown > 0) {
            return;
        }
        
        // Calculate direction to target
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5) - this.getY(0.5);
        double dz = target.getZ() - this.getZ();
        
        // Create and shoot fireball using position + velocity constructor
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double vx = dx / dist + this.getRandom().nextGaussian() * 0.05;
        double vy = dy / dist + this.getRandom().nextGaussian() * 0.05;
        double vz = dz / dist + this.getRandom().nextGaussian() * 0.05;

        Vec3 motion = new Vec3(vx, vy, vz);
        SmallFireball fireball = new SmallFireball(
            this.level(),
            this.getX(), this.getY(0.5), this.getZ(),
            motion
        );

        this.level().addFreshEntity(fireball);
        
        // Play shoot sound (using blaze shoot sound)
        this.playSound(net.minecraft.sounds.SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F);
        
        shootCooldown = SHOOT_COOLDOWN;
    }
    
    /**
     * Custom move control for floating/flying behavior
     */
    static class MaikubiMoveControl extends MoveControl {
        private final MaikubiEntity maikubi;
        private int floatDuration;
        
        public MaikubiMoveControl(MaikubiEntity maikubi) {
            super(maikubi);
            this.maikubi = maikubi;
        }
        
        @Override
        public void tick() {
            if (this.operation == Operation.MOVE_TO) {
                if (this.floatDuration-- <= 0) {
                    this.floatDuration += this.maikubi.getRandom().nextInt(5) + 2;
                    Vec3 vec3 = new Vec3(
                        this.wantedX - this.maikubi.getX(),
                        this.wantedY - this.maikubi.getY(),
                        this.wantedZ - this.maikubi.getZ()
                    );
                    double length = vec3.length();
                    vec3 = vec3.normalize();
                    
                    if (this.canReach(vec3, Mth.ceil(length))) {
                        this.maikubi.setDeltaMovement(
                            this.maikubi.getDeltaMovement().add(vec3.scale(0.1))
                        );
                    } else {
                        this.operation = Operation.WAIT;
                    }
                }
            }
        }
        
        private boolean canReach(Vec3 direction, int distance) {
            net.minecraft.world.phys.AABB aabb = this.maikubi.getBoundingBox();
            for (int i = 1; i < distance; ++i) {
                aabb = aabb.move(direction);
                if (!this.maikubi.level().noCollision(this.maikubi, aabb)) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) {
        // Use flying navigation so the pathfinder can move through air and float
        net.minecraft.world.entity.ai.navigation.FlyingPathNavigation nav = new net.minecraft.world.entity.ai.navigation.FlyingPathNavigation(this, level);
        nav.setCanOpenDoors(false);
        nav.setCanFloat(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state) {
        // Flying mob makes no footsteps
    }
    
    /**
     * Attack goal - fly towards target and shoot fireballs
     */
    static class MaikubiAttackGoal extends Goal {
        private final MaikubiEntity maikubi;
        private int attackTime = -1;
        
        public MaikubiAttackGoal(MaikubiEntity maikubi) {
            this.maikubi = maikubi;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }
        
        @Override
        public boolean canUse() {
            LivingEntity target = this.maikubi.getTarget();
            return target != null && target.isAlive() && this.maikubi.canAttack(target);
        }
        
        @Override
        public void start() {
            this.attackTime = 0;
        }
        
        @Override
        public void stop() {
            this.attackTime = -1;
        }
        
        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
        
        @Override
        public void tick() {
            LivingEntity target = this.maikubi.getTarget();
            if (target == null) {
                return;
            }
            
            double distanceSq = this.maikubi.distanceToSqr(target);
            boolean canSee = this.maikubi.getSensing().hasLineOfSight(target);
            
            // Look at target
            this.maikubi.getLookControl().setLookAt(target, 10.0F, 10.0F);
            
            // Move towards target if too far, away if too close
            if (distanceSq > 256.0) { // More than 16 blocks
                Vec3 targetPos = new Vec3(target.getX(), target.getY() + 3.0, target.getZ());
                this.maikubi.getMoveControl().setWantedPosition(
                    targetPos.x, targetPos.y, targetPos.z, 1.0
                );
            } else if (distanceSq < 64.0) { // Less than 8 blocks - back away
                Vec3 awayDir = this.maikubi.position().subtract(target.position()).normalize();
                Vec3 awayPos = this.maikubi.position().add(awayDir.scale(3.0));
                this.maikubi.getMoveControl().setWantedPosition(
                    awayPos.x, awayPos.y + 2.0, awayPos.z, 1.0
                );
            }
            
            // Attack if in range and can see
            if (canSee && distanceSq < 400.0 && this.maikubi.shootCooldown <= 0) {
                this.maikubi.performRangedAttack(target);
            }
        }
    }
    
    /**
     * Random floating goal - makes the Maikubi float around when not attacking
     */
    static class MaikubiRandomFloatGoal extends Goal {
        private final MaikubiEntity maikubi;
        
        public MaikubiRandomFloatGoal(MaikubiEntity maikubi) {
            this.maikubi = maikubi;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }
        
        @Override
        public boolean canUse() {
            MoveControl moveControl = this.maikubi.getMoveControl();
            if (!moveControl.hasWanted()) {
                return true;
            } else {
                double dx = moveControl.getWantedX() - this.maikubi.getX();
                double dy = moveControl.getWantedY() - this.maikubi.getY();
                double dz = moveControl.getWantedZ() - this.maikubi.getZ();
                double distSq = dx * dx + dy * dy + dz * dz;
                return distSq < 1.0 || distSq > 3600.0;
            }
        }
        
        @Override
        public boolean canContinueToUse() {
            return false;
        }
        
        @Override
        public void start() {
            net.minecraft.util.RandomSource random = this.maikubi.getRandom();
            double x = this.maikubi.getX() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            double y = this.maikubi.getY() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            double z = this.maikubi.getZ() + (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
            
            // Clamp Y to reasonable values
            y = Mth.clamp(y, this.maikubi.level().getMinBuildHeight() + 5, this.maikubi.level().getMaxBuildHeight() - 5);
            
            this.maikubi.getMoveControl().setWantedPosition(x, y, z, 1.0);
        }
    }
}
