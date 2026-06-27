package com.shioh.sengoku.entity;

import com.shioh.sengoku.entity.ai.AdvancedMeleeAttackGoal;
import com.shioh.sengoku.init.SeigunItemReg;
import com.shioh.sengoku.entity.ai.CircleStrafeGoal;
import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import com.shioh.sengoku.init.KenseiItemReg;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerLevel;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

/**
 * Warlord - a custom boss type based on the Vindicator.
 * Uses the same model and attributes as vanilla Vindicator by default.
 */
public class WarlordEntity extends Vindicator {
    public static final float PHASE_TWO_HEALTH_FRACTION = 0.5F;
    private static final double PHASE_TWO_KNOCKBACK_RADIUS = 3.0D;
    private static final double PHASE_TWO_KNOCKBACK_STRENGTH = 3.15D;
    private static final double PHASE_TWO_SCALE = 1.3D;

    // Dormant/active state: when spawned the Warlord is dormant (won't wander)
    private boolean active = false;
    private boolean phaseTwoBurstTriggered = false;
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE = SynchedEntityData.defineId(WarlordEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_MUSIC_ACTIVE = SynchedEntityData.defineId(WarlordEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double ACTIVATION_RANGE = 5.0D; // blocks to trigger combat
    // When in combat, give up when the player gets this far away (20-30 blocks requested)
    private static final double DEACTIVATION_RANGE = 30.0D; // distance to give up combat
    private static final double DEFENSE_RANGE = 3.0D; // instantly kill hostile mobs within this range (only in idle)
    // (music state is synced to clients by the server-side scanner `WarlordMusicSync`)

    public WarlordEntity(EntityType<? extends WarlordEntity> type, Level level) {
        super(type, level);
        // Equip default items immediately so held item renders correctly
        try {
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(com.shioh.sengoku.init.KenseiItemReg.DAIMYO_KABUTO));
            this.setDropChance(net.minecraft.world.entity.EquipmentSlot.HEAD, 0.0F);
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(com.shioh.sengoku.init.KenseiItemReg.DAIMYO_DO));
            this.setDropChance(net.minecraft.world.entity.EquipmentSlot.CHEST, 0.0F);
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, new net.minecraft.world.item.ItemStack(com.shioh.sengoku.init.KenseiItemReg.DAIMYO_HAIDATE));
            this.setDropChance(net.minecraft.world.entity.EquipmentSlot.LEGS, 0.0F);
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, new net.minecraft.world.item.ItemStack(com.shioh.sengoku.init.KenseiItemReg.DAIMYO_KUSAZURI));
            this.setDropChance(net.minecraft.world.entity.EquipmentSlot.FEET, 0.0F);
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(com.shioh.sengoku.init.KenseiItemReg.ODACHI_OF_THE_SHUGODAI));
            this.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0F);
        } catch (Throwable ignored) {}
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACTIVE, false);
        builder.define(DATA_MUSIC_ACTIVE, false);
    }

    @Override
    protected void registerGoals() {
        // Keep vanilla Vindicator goals but override with advanced combat AI
        super.registerGoals();
        
        // Remove the vanilla melee attack goal and replace with advanced combat
        this.goalSelector.getAvailableGoals().removeIf(goal -> 
            goal.getGoal() instanceof net.minecraft.world.entity.ai.goal.MeleeAttackGoal
        );
        
        // Add dormant goal as highest priority
        this.goalSelector.addGoal(0, new DormantGoal());
        
        // Advanced combat AI for the Warlord boss - Ghost of Tsushima/Sekiro style
        this.goalSelector.addGoal(1, new WeaponBlockGoal(this)); // Block incoming attacks (boss blocks more often)
        this.goalSelector.addGoal(2, new AdvancedMeleeAttackGoal(this, 0.8, false)); // Faster, more aggressive attacks
        this.goalSelector.addGoal(3, new CircleStrafeGoal(this)); // Circle around target like a master swordsman
    }

    public static AttributeSupplier.Builder createAttributes() {
        // Base on Vindicator attributes but give the Warlord a much larger health pool
        return Vindicator.createAttributes()
            .add(Attributes.MAX_HEALTH, 100.0D)
            .add(Attributes.ATTACK_DAMAGE, 12.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.65D)
            .add(Attributes.SCALE, 1.0D);
    }

    private LivingEntity resolveCombatTarget() {
        LivingEntity currentTarget = this.getTarget();
        if (currentTarget != null && currentTarget.isAlive()) {
            return currentTarget;
        }

        LivingEntity lastAttacker = this.getLastHurtByMob();
        if (lastAttacker != null && lastAttacker.isAlive()) {
            double distanceSq = this.distanceToSqr(lastAttacker);
            if (distanceSq <= DEACTIVATION_RANGE * DEACTIVATION_RANGE) {
                return lastAttacker;
            }
        }

        Player nearestPlayer = this.level().getNearestPlayer(this, DEACTIVATION_RANGE);
        if (nearestPlayer != null && nearestPlayer.isAlive() && !nearestPlayer.isCreative() && !nearestPlayer.isSpectator()) {
            return nearestPlayer;
        }

        return null;
    }

    private void triggerPhaseTwoShockwave() {
        if (this.level().isClientSide) {
            return;
        }

        try {
            java.util.List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(PHASE_TWO_KNOCKBACK_RADIUS),
                entity -> entity != null && entity.isAlive() && entity != this
            );

            for (LivingEntity entity : nearbyEntities) {
                try {
                    Vec3 offset = entity.position().subtract(this.position());
                    double horizontalDistanceSq = offset.x * offset.x + offset.z * offset.z;
                    if (horizontalDistanceSq <= 0.0001D) {
                        double angle = this.getRandom().nextDouble() * (Math.PI * 2.0D);
                        offset = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
                        horizontalDistanceSq = 1.0D;
                    }

                    if (horizontalDistanceSq > PHASE_TWO_KNOCKBACK_RADIUS * PHASE_TWO_KNOCKBACK_RADIUS) {
                        continue;
                    }

                    Vec3 horizontalDirection = new Vec3(offset.x, 0.0D, offset.z).normalize();
                    double falloff = 1.0D - (Math.sqrt(horizontalDistanceSq) / PHASE_TWO_KNOCKBACK_RADIUS);
                    double strength = Math.max(0.45D, PHASE_TWO_KNOCKBACK_STRENGTH * falloff);

                    entity.push(horizontalDirection.x * strength, 0.32D, horizontalDirection.z * strength);
                    entity.hurtMarked = true;
                } catch (Throwable ignored) {}
            }

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.CLOUD,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    20, 0.8D, 0.35D, 0.8D, 0.02D
                );
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    2, 0.0D, 0.0D, 0.0D, 0.0D
                );
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    14, 0.9D, 0.45D, 0.9D, 0.02D
                );
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FLAME,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    30, 0.8D, 0.35D, 0.8D, 0.02D
                );
                serverLevel.playSound(
                    null,
                    this.blockPosition(),
                    net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                    net.minecraft.sounds.SoundSource.HOSTILE,
                    1.1F,
                    0.7F
                );
                serverLevel.playSound(
                    null,
                    this.getX(), this.getY(), this.getZ(),
                    net.minecraft.sounds.SoundEvents.TRIDENT_THUNDER,
                    net.minecraft.sounds.SoundSource.HOSTILE,
                    1.0F,
                    1.0F
                );
                serverLevel.playSound(
                    null,
                    this.getX(), this.getY(), this.getZ(),
                    SoundRegistry.KENSEI_FINALE,
                    net.minecraft.sounds.SoundSource.HOSTILE,
                    1.0F,
                    1.0F
                );
                try {
                    if (this.getAttribute(Attributes.SCALE) != null) {
                        this.getAttribute(Attributes.SCALE).setBaseValue(PHASE_TWO_SCALE);
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    private boolean isWarlordWeaponGuarding() {
        if (WeaponBlockGoal.isCurrentlyBlocking(this)) {
            return true;
        }

        if (WeaponBlockGoal.isPoiseBroken(this) || WeaponBlockGoal.isStunned(this) || WeaponBlockGoal.isRecentPoiseBreak(this)) {
            return false;
        }

        if (!this.isUsingItem()) {
            return false;
        }

        ItemStack stack = this.getMainHandItem();
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        boolean validGuardWeapon = stack.getItem() instanceof net.minecraft.world.item.SwordItem
            || stack.getItem() instanceof net.minecraft.world.item.TridentItem
            || stack.getItem().toString().toLowerCase().contains("sword")
            || stack.getItem().toString().toLowerCase().contains("yari")
            || stack.getItem().toString().toLowerCase().contains("katana")
            || stack.getItem().toString().toLowerCase().contains("odachi")
            || stack.getItem().toString().toLowerCase().contains("blade");
        if (!validGuardWeapon) {
            return false;
        }

        try {
            java.lang.reflect.Method method = this.getClass().getMethod("sengoku$isWeaponBlocking");
            method.setAccessible(true);
            Object result = method.invoke(this);
            if (result instanceof Boolean) {
                return ((Boolean) result).booleanValue();
            }
        } catch (Throwable ignored) {}

        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            boolean phaseTwoNow = this.isPhaseTwo();
            if (phaseTwoNow && !this.phaseTwoBurstTriggered) {
                this.phaseTwoBurstTriggered = true;
                triggerPhaseTwoShockwave();
            }

            // If active, monitor target to potentially deactivate when very far or dead
            if (this.active) {
                net.minecraft.world.entity.LivingEntity t = resolveCombatTarget();
                boolean poiseInterrupted = WeaponBlockGoal.isPoiseBroken(this)
                    || WeaponBlockGoal.isStunned(this)
                    || WeaponBlockGoal.isRecentPoiseBreak(this);

                if (t == null) {
                    if (poiseInterrupted) {
                        // Keep boss combat state and music alive during guard-break stun windows.
                        try { setMusicActive(true); } catch (Throwable ignored) {}
                    } else {
                        setActive(false);
                    }
                } else {
                    if (this.getTarget() != t) {
                        this.setTarget(t);
                    }
                    double distSq = this.distanceToSqr(t);
                    if (distSq > DEACTIVATION_RANGE * DEACTIVATION_RANGE) {
                        // Target ran very far away - stop combat
                        this.setTarget(null);
                        setActive(false);
                    } else {
                        try { setMusicActive(true); } catch (Throwable ignored) {}
                    }
                }
            }
            // Defensive behavior (idle mode only): instantly remove hostile mobs (Monster) that come very close
            if (!this.active) {
                try {
                    java.util.List<net.minecraft.world.entity.monster.Monster> hostiles = this.level().getEntitiesOfClass(
                            net.minecraft.world.entity.monster.Monster.class,
                            this.getBoundingBox().inflate(DEFENSE_RANGE),
                            (m) -> m != null && m.isAlive() && m != this
                    );
                    boolean spawnedParticle = false;
                    for (net.minecraft.world.entity.monster.Monster mob : hostiles) {
                        try {
                            // Skip allies (pillagers/vindicators/evokers etc.)
                            if (mob.isAlliedTo(WarlordEntity.this)) continue;

                            // Only instantly kill mobs that are explicitly hostile toward the Warlord
                            boolean hostileToWarlord = false;
                            // If the mob currently has the Warlord as its target
                            if (mob.getTarget() == WarlordEntity.this) hostileToWarlord = true;
                            // Or if the mob was last hurt by the Warlord
                            if (!hostileToWarlord && mob.getLastHurtByMob() == WarlordEntity.this) hostileToWarlord = true;

                            if (!hostileToWarlord) continue;

                            mob.hurt(WarlordEntity.this.damageSources().mobAttack(WarlordEntity.this), Float.MAX_VALUE);
                            if (!spawnedParticle && WarlordEntity.this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                                // Spawn the sweep particle at the slain mob's position (centered)
                                double px = mob.getX();
                                double py = mob.getY() + (mob.getBbHeight() > 0.0D ? mob.getBbHeight() / 2.0D : 0.5D);
                                double pz = mob.getZ();
                                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK,
                                        px, py, pz,
                                        1, 0.0D, 0.0D, 0.0D, 0.0D);
                                spawnedParticle = true;
                            }
                        } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {}
            }
        }
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // Always equip the Warlord with the Blade of the Kensei and Daimyo armor (diamond tier)
        try {
            this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(KenseiItemReg.DAIMYO_KABUTO));
            this.setDropChance(EquipmentSlot.HEAD, 0.0F);
            this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(KenseiItemReg.DAIMYO_DO));
            this.setDropChance(EquipmentSlot.CHEST, 0.0F);
            this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(KenseiItemReg.DAIMYO_HAIDATE));
            this.setDropChance(EquipmentSlot.LEGS, 0.0F);
            this.setItemSlot(EquipmentSlot.FEET, new ItemStack(KenseiItemReg.DAIMYO_KUSAZURI));
            this.setDropChance(EquipmentSlot.FEET, 0.0F);
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(KenseiItemReg.ODACHI_OF_THE_SHUGODAI));
            this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        } catch (Throwable ignored) {}
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        // Drop 1-2 Seigun upgrade templates on death.
        try {
            this.spawnAtLocation(new ItemStack(SeigunItemReg.SEIGUN_UPGRADE_SMITHING_TEMPLATE, 1 + this.getRandom().nextInt(2)));
        } catch (Throwable ignored) {}
    }

    @Override
    public IllagerArmPose getArmPose() {
        try {
            if (com.shioh.sengoku.entity.ai.WeaponBlockGoal.isCurrentlyBlocking(this)) {
                return IllagerArmPose.ATTACKING;
            }
        } catch (Throwable ignored) {}

        if (this.isUsingItem() && this.hasRangedWeapon()) {
            return IllagerArmPose.BOW_AND_ARROW;
        }
        if (this.hasRangedWeapon()) {
            return IllagerArmPose.CROSSBOW_HOLD;
        }
        if (this.isAggressive()) {
            return IllagerArmPose.ATTACKING;
        }
        return IllagerArmPose.NEUTRAL;
    }

    public boolean hasRangedWeapon() {
        return this.getMainHandItem().getItem() == Items.BOW;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        // Suppress vanilla Vindicator ambient/idle sound for the Warlord entirely.
        return null;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData) {
        // Equipment already populated in constructor to avoid accessing thread-bound world random
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        try {
            if (attacker instanceof net.minecraft.world.entity.projectile.Projectile projectile) {
                Entity owner = projectile.getOwner();
                if (owner != null) {
                    attacker = owner;
                }
            }
        } catch (Throwable ignored) {}

        if (isWarlordWeaponGuarding()) {
            this.playSound(amount >= 6.0F ? SoundRegistry.PARTIAL_PARRY : SoundRegistry.WEAPON_PARRY, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);

            WeaponBlockGoal.spawnBlockFeedbackParticles(this);

            if (!this.level().isClientSide && attacker instanceof LivingEntity livingAttacker) {
                Vec3 dir = this.position().subtract(livingAttacker.position()).normalize();
                this.setDeltaMovement(this.getDeltaMovement().add(dir.scale(0.28)));
                this.hurtMarked = true;
            }

            try {
                setActive(true);
                try { setMusicActive(true); } catch (Throwable ignored) {}
                if (attacker instanceof LivingEntity) {
                    this.setTarget((LivingEntity) attacker);
                    this.setAggressive(true);
                }
            } catch (Throwable ignored) {}
            try { WeaponBlockGoal.onSuccessfulBlock(this, attacker instanceof LivingEntity ? (LivingEntity) attacker : null); } catch (Throwable ignored) {}
            return false;
        }

        boolean hurt = super.hurt(source, amount);
        if (!hurt) {
            return false;
        }

        try {
            setActive(true);
            try { setMusicActive(true); } catch (Throwable ignored) {}
            if (attacker instanceof LivingEntity livingAttacker) {
                this.setTarget(livingAttacker);
                try { this.setLastHurtByMob(livingAttacker); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}

        return true;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        this.swing(InteractionHand.MAIN_HAND);
        if (!this.level().isClientSide && hit) {
            // Activate boss music on successful hit
            try { setMusicActive(true); } catch (Throwable ignored) {}
            // Record last attacker reference if target is living
            if (target instanceof LivingEntity lt) {
                try { this.setLastHurtByMob(lt); } catch (Throwable ignored) {}
            }
            if (this.isPhaseTwo() && target instanceof Player player) {
                try {
                    ItemStack staggerItem = player.isUsingItem() ? player.getUseItem() : player.getMainHandItem();
                    if (!staggerItem.isEmpty()) {
                        player.getCooldowns().addCooldown(staggerItem.getItem(), 18);
                    }
                } catch (Throwable ignored) {}
                try {
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 14, 2, false, false, true));
                } catch (Throwable ignored) {}
            }
            // Apply knockback to self if target is actively blocking with weapon
            if (target instanceof LivingEntity living && living.isUsingItem()) {
                ItemStack stack = living.getUseItem();
                if (stack.getItem() instanceof net.minecraft.world.item.SwordItem || stack.getItem() instanceof net.minecraft.world.item.TridentItem) {
                    Vec3 dir = this.position().subtract(living.position()).normalize();
                    this.setDeltaMovement(this.getDeltaMovement().add(dir.scale(0.32)));
                    this.hurtMarked = true;
                    try { com.shioh.sengoku.entity.ai.WeaponBlockGoal.resetConsecutiveBlocks(this); } catch (Throwable ignored) {}
                }
            }
        }
        return hit;
    }

    private void setActive(boolean flag) {
        if (this.active == flag) return;
        this.active = flag;
        try { this.entityData.set(DATA_ACTIVE, flag); } catch (Throwable ignored) {}
        if (flag) {
            // Activating: enter combat mode and enable boss music immediately
            try { setMusicActive(true); } catch (Throwable ignored) {}
            try {
                this.playSound(SoundRegistry.WARLORD_COMBAT_ENTER, 1.0F, 0.95F + this.getRandom().nextFloat() * 0.15F);
            } catch (Throwable ignored) {}
        } else {
            // If deactivating, stop navigation and clear target
            try { setMusicActive(false); } catch (Throwable ignored) {}
            try { this.getNavigation().stop(); } catch (Throwable ignored) {}
            try { this.setTarget(null); } catch (Throwable ignored) {}
        }
    }

    /**
     * Explicitly control whether the warlord music should play for nearby clients.
     */
    public void setMusicActive(boolean flag) {
        try {
            if (!this.level().isClientSide) {
                try { this.entityData.set(DATA_MUSIC_ACTIVE, flag); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    public boolean isMusicActive() {
        try {
            return this.entityData.get(DATA_MUSIC_ACTIVE);
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean isPhaseTwo() {
        try {
            return this.getHealth() <= this.getMaxHealth() * PHASE_TWO_HEALTH_FRACTION;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public boolean hasEnteredPhaseTwo() {
        return this.phaseTwoBurstTriggered || this.isPhaseTwo();
    }

    public void playPhaseTwoFinisherSound() {
        if (!this.hasEnteredPhaseTwo()) {
            return;
        }

        try {
            this.playSound(SoundRegistry.WARLORD_PHASE_TWO_ATTACK, 1.1F, 0.9F + this.getRandom().nextFloat() * 0.2F);
        } catch (Throwable ignored) {}
    }

    public int getMusicPhase() {
        if (!isMusicActive()) {
            return 0;
        }
        return isPhaseTwo() ? 2 : 1;
    }


    /**
     * Public accessor to check whether the Warlord is currently active (in combat).
     */
    public boolean isActive() {
        try {
            return this.level() != null && this.level().isClientSide ? this.entityData.get(DATA_ACTIVE) : this.active;
        } catch (Throwable ignored) {
            return this.active;
        }
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource damageSource) {
        if (this.hasEnteredPhaseTwo()) {
            return SoundRegistry.WARLORD_PHASE_TWO_HURT;
        }
        return super.getHurtSound(damageSource);
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        if (this.hasEnteredPhaseTwo()) {
            return SoundRegistry.WARLORD_PHASE_TWO_DEATH;
        }
        return super.getDeathSound();
    }

    /**
     * Goal that keeps the Warlord stationary until a player gets close enough
     * or the mob is attacked. Prevents wandering in its boss room.
     */
    private class DormantGoal extends Goal {
        public DormantGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !WarlordEntity.this.active && WarlordEntity.this.isAlive();
        }

        @Override
        public void start() {
            try { WarlordEntity.this.getNavigation().stop(); } catch (Throwable ignored) {}
        }

        @Override
        public void tick() {
            // Ensure the mob stays still and look for nearby players to activate
            try {
                WarlordEntity.this.getNavigation().stop();
                if (!WarlordEntity.this.level().isClientSide) {
                    Player p = WarlordEntity.this.level().getNearestPlayer(WarlordEntity.this, ACTIVATION_RANGE);
                    if (p != null && p.isAlive() && !p.isCreative() && !p.isSpectator()) {
                        // Player entered activation range
                        WarlordEntity.this.setTarget(p);
                        setActive(true);
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    // finalizeSpawn removed; equipment is applied in constructor to ensure renderer picks up held item immediately.
}
