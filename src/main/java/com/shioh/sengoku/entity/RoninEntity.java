package com.shioh.sengoku.entity;

import com.shioh.sengoku.entity.ai.AdvancedMeleeAttackGoal;
import com.shioh.sengoku.entity.ai.CircleStrafeGoal;
import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import com.shioh.sengoku.entity.ai.PatrolCautiousEngagementGoal;
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

/**
 * Ronin entity - a masterless samurai that fights with an iron sword.
 * Similar to vindicator but themed as a wandering samurai.
 */
public class RoninEntity extends AbstractIllager {
    private static final EntityDataAccessor<Boolean> DATA_WINDING_UP = SynchedEntityData.defineId(RoninEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_WINDUP_TICKS = SynchedEntityData.defineId(RoninEntity.class, EntityDataSerializers.INT);
    private static final double SENGOKU_ELITE_MAX_HEALTH = 60.0D;
    private static final String SENGOKU_ELITE_TAG = "sengoku_elite";
    private boolean sengoku$eliteWeaponInitialized = false;
    private int aggroAmbientCooldown = 0;
    
    public RoninEntity(EntityType<? extends RoninEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WINDING_UP, false);
        builder.define(DATA_WINDUP_TICKS, 0);
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
        
        // Patrol gating goal - makes patrols cautious and watch from distance
        this.goalSelector.addGoal(1, new PatrolCautiousEngagementGoal(this));
        
        // Advanced combat AI - Ghost of Tsushima/Sekiro style
        this.goalSelector.addGoal(2, new WeaponBlockGoal(this)); // Block incoming attacks
        this.goalSelector.addGoal(3, new AdvancedMeleeAttackGoal(this, 1.0, false)); // Tactical melee attacks with timing
        this.goalSelector.addGoal(4, new CircleStrafeGoal(this)); // Circle around target like vexes
        // Fallback vanilla melee attack while mounted or when advanced goals are disabled
        this.goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.0D, true));
        
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
            .add(Attributes.MAX_HEALTH, 30.0)  // Increased Ronin HP from 16 to 30
            .add(Attributes.ATTACK_DAMAGE, 1.5)  // Nerfed further from 2.0
            .add(Attributes.ARMOR, 2.0);
    }
    
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // Ronin carry stone swords
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
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
                        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                        return;
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
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
        return SoundRegistry.RONIN_AMBIENT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.RONIN_DEATH;
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.RONIN_HURT;
    }

    @Override
    public void playAmbientSound() {
        try {
            if (this.random.nextInt(1200) == 0) {
                super.playAmbientSound();
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void setAggressive(boolean aggressive) {
        boolean wasAggressive = this.isAggressive();
        super.setAggressive(aggressive);
        if (!this.level().isClientSide && aggressive && !wasAggressive) {
            if (this.aggroAmbientCooldown <= 0) {
                try {
                    this.playSound(SoundRegistry.RONIN_AMBIENT_AGGRO, 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
                } catch (Throwable ignored) {}
                this.aggroAmbientCooldown = 100;
            }
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
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Check if currently blocking
        if (WeaponBlockGoal.isCurrentlyBlocking(this)) {
            // Completely block the damage
            
            // Play parry sound
            this.playSound(amount >= 6.0F ? SoundRegistry.PARTIAL_PARRY : SoundRegistry.WEAPON_PARRY, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            
            // Spawn posture spark particle (same as player weapon block)
            WeaponBlockGoal.spawnBlockFeedbackParticles(this, amount);

            // Apply slight knockback to attacker for visual feedback
            Entity attacker = source.getEntity();
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
                    this.setTarget((LivingEntity) attacker);
                    this.setAggressive(true);
                }
            } catch (Throwable ignored) {}
            try { WeaponBlockGoal.onSuccessfulBlock(this, attacker instanceof LivingEntity ? (LivingEntity) attacker : null); } catch (Throwable ignored) {}
            return false;
        }
        
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // Play swing animation on attack
        this.swing(InteractionHand.MAIN_HAND);
        boolean result = super.doHurtTarget(target);
        if (!this.level().isClientSide && result && target instanceof LivingEntity living) {
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
    
    @Override
    public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, DifficultyInstance difficulty, 
                                       MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.applyAdvancementWeaponProgression(level);
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), difficulty);
        boolean makeElite = this.getTags().contains(SENGOKU_ELITE_TAG);
        if (makeElite) {
            var maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(SENGOKU_ELITE_MAX_HEALTH);
                this.setHealth(this.getMaxHealth());
            }
            this.sengoku$applyEliteWeaponEnchantments();
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Elite", this.getAttributeValue(Attributes.MAX_HEALTH) >= (SENGOKU_ELITE_MAX_HEALTH - 0.1D));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("Elite")) {
            var maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(SENGOKU_ELITE_MAX_HEALTH);
                this.setHealth(this.getMaxHealth());
            }
            if (this.getMainHandItem().isEmpty()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }
            this.sengoku$applyEliteWeaponEnchantments();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.aggroAmbientCooldown > 0) {
            this.aggroAmbientCooldown--;
        }
        if (!this.level().isClientSide && !this.sengoku$eliteWeaponInitialized
                && this.getAttributeValue(Attributes.MAX_HEALTH) >= (SENGOKU_ELITE_MAX_HEALTH - 0.1D)) {
            this.sengoku$eliteWeaponInitialized = true;
            if (this.getMainHandItem().isEmpty()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }
            this.sengoku$applyEliteWeaponEnchantments();
        }
    }

    private void sengoku$applyEliteWeaponEnchantments() {
        try {
            ItemStack weapon = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (weapon.isEmpty()) return;
            ItemEnchantments current = weapon.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);
            var enchantments = this.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            enchantments.getHolder(Enchantments.SHARPNESS).ifPresent(h -> mutable.set(h, 3));
            enchantments.getHolder(Enchantments.UNBREAKING).ifPresent(h -> mutable.set(h, 2));
            weapon.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        } catch (Throwable ignored) {}
    }
    
    // Required Raider abstract methods
    @Override
    public void applyRaidBuffs(ServerLevel level, int wave, boolean unusedFalse) {
        // Apply buffs based on wave - similar to vindicator
        ItemStack weapon = this.getItemBySlot(EquipmentSlot.MAINHAND);
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), level.getCurrentDifficultyAt(this.blockPosition()));
    }
    
    @Override
    public SoundEvent getCelebrateSound() {
        return SoundRegistry.RONIN_CELEBRATE;
    }
}
