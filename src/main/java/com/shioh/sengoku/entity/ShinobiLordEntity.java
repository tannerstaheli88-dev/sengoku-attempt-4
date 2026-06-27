package com.shioh.sengoku.entity;

import com.shioh.sengoku.entity.ai.ShinobiLordCombatGoal;
import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import com.shioh.sengoku.init.SeigunItemReg;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import java.util.EnumSet;

/**
 * Ronin entity - a masterless samurai that fights with an iron sword.
 * Similar to vindicator but themed as a wandering samurai.
 */
public class ShinobiLordEntity extends AbstractIllager {
    private static final EntityDataAccessor<Boolean> DATA_WINDING_UP = SynchedEntityData.defineId(ShinobiLordEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_WINDUP_TICKS = SynchedEntityData.defineId(ShinobiLordEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ACTIVE = SynchedEntityData.defineId(ShinobiLordEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int AMBIENT_SOUND_INTERVAL = 3600;
    private static final int AGGRO_VOICE_COOLDOWN_TICKS = 200;
    private static final double ACTIVATION_RANGE = 5.0D;
    private int lastAggroVoiceTick = -AGGRO_VOICE_COOLDOWN_TICKS;
    private boolean active = false;
    private boolean phaseTwoSummonTriggered = false;
    
    public ShinobiLordEntity(EntityType<? extends ShinobiLordEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WINDING_UP, false);
        builder.define(DATA_WINDUP_TICKS, 0);
        builder.define(DATA_ACTIVE, false);
    }

    // Windup animation / state helpers (used by AI to synchronize client-side animation playback)
    public void sengoku$setElite(boolean elite) {
        if (!elite) return;
        var maxHealth = this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(60.0D);
            this.setHealth(this.getMaxHealth());
        }
    }

    public void setWindingUp(boolean flag) {
        try { this.entityData.set(DATA_WINDING_UP, flag); } catch (Throwable ignored) {}
    }

    public boolean isWindingUp() {
        try { return this.entityData.get(DATA_WINDING_UP); } catch (Throwable ignored) { return false; }
    }

    public void setWindupTicks(int ticks) {
        try { this.entityData.set(DATA_WINDUP_TICKS, ticks); } catch (Throwable ignored) {}
    }

    public int getWindupTicks() {
        try { return this.entityData.get(DATA_WINDUP_TICKS); } catch (Throwable ignored) { return 0; }
    }

    // Playing-animation API removed: client visuals driven by synched windup/blocks and client mixins
    
    @Override
    protected void registerGoals() {
        super.registerGoals();
        
        // Basic AI goals
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new DormantGoal());
        
        // Shinobi Lord exclusive combat AI: melee, kunai pressure, and smoke-bomb repositioning
        this.goalSelector.addGoal(2, new WeaponBlockGoal(this)); // Block incoming attacks
        this.goalSelector.addGoal(3, new ShinobiLordCombatGoal(this));
        // Fallback vanilla melee attack while mounted or when advanced goals are disabled
        this.goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.0D, true));
        
        // Movement and patrol
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
        
        // Targeting
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());  // Removed AbstractIllager.class so it retaliates against ALL attackers
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        
        // Target all clan samurai/ashigaru/sohei using entity type tags
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.Mob.class, 10, true, false, 
            (entity) -> {
                EntityType<?> type = entity.getType();
                // Target any clan
                return type.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "takeda_clan"))) ||
                       type.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "satomi_clan"))) ||
                       type.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "kobayakawa_clan")));
            }));
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.35)
            .add(Attributes.FOLLOW_RANGE, 32.0)
            .add(Attributes.MAX_HEALTH, 100.0)  // Increased Ronin HP from 16 to 30
            .add(Attributes.ATTACK_DAMAGE, 1.5)  // Nerfed further from 2.0
            .add(Attributes.ARMOR, 2.0);
    }
    
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {

        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
    }

    private void applyAdvancementWeaponProgression(ServerLevelAccessor level) {
        try {
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }

            BlockPos pos = this.blockPosition();
            for (ServerPlayer player : serverLevel.players()) {
                try {
                    if (player.distanceToSqr(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) <= 64.0 * 64.0 && hasMineDiamondAdvancement(player)) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
                        return;
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void applyRaidBuffs(ServerLevel level, int wave, boolean unusedFalse) {
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), level.getCurrentDifficultyAt(this.blockPosition()));
    }

    private boolean hasMineDiamondAdvancement(ServerPlayer serverPlayer) {
        try {
            ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath("minecraft", "story/mine_diamond");
            var advancements = serverPlayer.getAdvancements();
            var progress = advancements.getOrStartProgress(
                serverPlayer.server.getAdvancements().get(advancementId)
            );
            return progress != null && progress.isDone();
        } catch (Throwable ignored) {
            return false;
        }
    }
    
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ILLUSIONER_DEATH;
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ILLUSIONER_HURT;
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundRegistry.RONIN_CELEBRATE;
    }

    @Override
    public void playAmbientSound() {
        try {
            if (this.random.nextInt(AMBIENT_SOUND_INTERVAL) == 0) {
                super.playAmbientSound();
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void setAggressive(boolean aggressive) {
        boolean wasAggressive = this.isAggressive();
        super.setAggressive(aggressive);
        if (!this.level().isClientSide && aggressive && !wasAggressive && this.tickCount - this.lastAggroVoiceTick >= AGGRO_VOICE_COOLDOWN_TICKS) {
            try {
                this.playSound(SoundEvents.ILLUSIONER_AMBIENT, 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
                this.lastAggroVoiceTick = this.tickCount;
            } catch (Throwable ignored) {}
        }
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

    public boolean isPhaseTwo() {
        try {
            return this.getHealth() <= this.getMaxHealth() * 0.5F;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void summonPhaseTwoPhantoms() {
        if (this.level().isClientSide || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LivingEntity target = this.getTarget();
        for (int i = 0; i < 2; i++) {
            try {
                Phantom phantom = EntityType.PHANTOM.create(serverLevel);
                if (phantom == null) {
                    continue;
                }

                double angle = i * Math.PI;
                double spawnX = this.getX() + Math.cos(angle) * 2.5D;
                double spawnY = this.getY() + 4.0D;
                double spawnZ = this.getZ() + Math.sin(angle) * 2.5D;

                phantom.moveTo(spawnX, spawnY, spawnZ, this.getYRot(), 0.0F);
                if (target != null && target.isAlive()) {
                    phantom.setTarget(target);
                }
                serverLevel.addFreshEntity(phantom);
            } catch (Throwable ignored) {}
        }

        try {
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.CLOUD,
                this.getX(), this.getY() + 1.1D, this.getZ(),
                45, 1.2D, 0.6D, 1.2D, 0.03D
            );
            serverLevel.playSound(
                null,
                this.getX(), this.getY(), this.getZ(),
                SoundEvents.PHANTOM_SWOOP,
                net.minecraft.sounds.SoundSource.HOSTILE,
                1.2F,
                0.8F
            );
        } catch (Throwable ignored) {}
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (this.isPhaseTwo() && !this.phaseTwoSummonTriggered) {
            this.phaseTwoSummonTriggered = true;
            this.summonPhaseTwoPhantoms();
        }

        if (this.active) {
            LivingEntity target = this.getTarget();
            if (target == null || !target.isAlive()) {
                LivingEntity lastAttacker = this.getLastHurtByMob();
                if (lastAttacker != null && lastAttacker.isAlive()) {
                    this.setTarget(lastAttacker);
                }
            }
            return;
        }

        try {
            this.setAggressive(false);
            this.getNavigation().stop();
            if (this.getTarget() != null) {
                this.setTarget(null);
            }
        } catch (Throwable ignored) {}

        try {
            Player player = this.level().getNearestPlayer(this, ACTIVATION_RANGE);
            if (player != null && player.isAlive() && !player.isCreative() && !player.isSpectator()) {
                this.setTarget(player);
                setActive(true);
            }
        } catch (Throwable ignored) {}
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        try {
            if (attacker instanceof net.minecraft.world.entity.projectile.Projectile projectile) {
                Entity owner = projectile.getOwner();
                if (owner != null) {
                    attacker = owner;
                }
            }
        } catch (Throwable ignored) {}

        // Check if currently blocking
        if (WeaponBlockGoal.isCurrentlyBlocking(this)) {
            // Completely block the damage
            
            // Play parry sound
            this.playSound(amount >= 6.0F ? SoundRegistry.PARTIAL_PARRY : SoundRegistry.WEAPON_PARRY, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            
            // Spawn posture spark particle (same as player weapon block)
            WeaponBlockGoal.spawnBlockFeedbackParticles(this, amount);

            // Apply slight knockback to attacker for visual feedback
            if (!this.level().isClientSide && attacker instanceof LivingEntity livingAttacker) {
                // Push the blocker (this) slightly away from the attacker instead
                Vec3 dir = this.position().subtract(livingAttacker.position()).normalize();
                this.setDeltaMovement(this.getDeltaMovement().add(dir.scale(0.25)));
                this.hurtMarked = true; // ensure motion sync
            }
            
            // Return false to completely block the damage
            // After a successful block, briefly drop blocking and auto-counter: target attacker and allow attack AI
            try {
                if (attacker instanceof LivingEntity) {
                    setActive(true);
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
            if (attacker instanceof LivingEntity livingAttacker) {
                this.setTarget(livingAttacker);
                this.setLastHurtByMob(livingAttacker);
            }
        } catch (Throwable ignored) {}

        return true;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // Play swing animation on attack
        this.swing(InteractionHand.MAIN_HAND);
        boolean result = super.doHurtTarget(target);
        if (!this.level().isClientSide && result && target instanceof LivingEntity living) {
            try { setActive(true); } catch (Throwable ignored) {}
            // If target is weapon-blocking (using item that can block), knock this entity back slightly
            if (living.isUsingItem()) {
                ItemStack stack = living.getUseItem();
                if (stack.getItem() instanceof net.minecraft.world.item.SwordItem || stack.getItem() instanceof net.minecraft.world.item.TridentItem) {
                    Vec3 dir = this.position().subtract(living.position()).normalize();
                    this.setDeltaMovement(this.getDeltaMovement().add(dir.scale(0.30)));
                    this.hurtMarked = true;
                    try { com.shioh.sengoku.entity.ai.WeaponBlockGoal.resetConsecutiveBlocks(this); } catch (Throwable ignored) {}
                }
            }
        }
        return result;
    }

    private void setActive(boolean flag) {
        if (this.active == flag) {
            return;
        }

        this.active = flag;
        try { this.entityData.set(DATA_ACTIVE, flag); } catch (Throwable ignored) {}

        if (!flag) {
            try { this.getNavigation().stop(); } catch (Throwable ignored) {}
            try { this.setTarget(null); } catch (Throwable ignored) {}
            try { this.setAggressive(false); } catch (Throwable ignored) {}
        }
    }

    public boolean isActive() {
        try {
            return this.level() != null && this.level().isClientSide ? this.entityData.get(DATA_ACTIVE) : this.active;
        } catch (Throwable ignored) {
            return this.active;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("SengokuActive", this.active);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setActive(compound.getBoolean("SengokuActive"));
    }

    private class DormantGoal extends Goal {
        public DormantGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !ShinobiLordEntity.this.active && ShinobiLordEntity.this.isAlive();
        }

        @Override
        public void start() {
            try { ShinobiLordEntity.this.getNavigation().stop(); } catch (Throwable ignored) {}
        }

        @Override
        public void tick() {
            try {
                ShinobiLordEntity.this.getNavigation().stop();
                if (!ShinobiLordEntity.this.level().isClientSide) {
                    Player player = ShinobiLordEntity.this.level().getNearestPlayer(ShinobiLordEntity.this, ACTIVATION_RANGE);
                    if (player != null && player.isAlive() && !player.isCreative() && !player.isSpectator()) {
                        ShinobiLordEntity.this.setTarget(player);
                        setActive(true);
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        // Shinobi Lord-specific template drop.
        try {
            this.spawnAtLocation(new ItemStack(SeigunItemReg.RYOSHIN_UPGRADE_SMITHING_TEMPLATE));
        } catch (Throwable ignored) {}
    }
}
