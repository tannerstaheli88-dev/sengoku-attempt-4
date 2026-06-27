package com.shioh.sengoku.entity;

import com.shioh.sengoku.entity.ai.AdvancedMeleeAttackGoal;
import com.shioh.sengoku.entity.ai.CircleStrafeGoal;
import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import net.minecraft.core.registries.BuiltInRegistries;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;

/**
 * Satomi Samurai - Exact copy of Vindicator but attacks other clans and carries iron sword or yari.
 */
public class SatomiSamuraiEntity extends Vindicator {
    
    private static final float SENGOKU_ELITE_SPAWN_CHANCE = 0.08F;
    private static final double SENGOKU_ELITE_MAX_HEALTH = 48.0D;
    private static final String SENGOKU_ELITE_TAG = "sengoku_elite";
    private boolean sengoku$eliteWeaponInitialized = false;

    public SatomiSamuraiEntity(EntityType<? extends Vindicator> type, Level level) {
        super(type, level);
    }
    
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        
        // 50% chance for iron sword, 50% for iron yari
        if (this.random.nextBoolean()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        } else {
            // Try to get iron yari, fall back to iron sword if not found
            Item yari = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sengoku", "iron_yari"));
            if (yari != null && yari != Items.AIR) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(yari));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }
        }
        
        this.setDropChance(EquipmentSlot.MAINHAND, 0.05F);

        boolean makeElite = this.getTags().contains(SENGOKU_ELITE_TAG);
        if (makeElite) {
            var maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(SENGOKU_ELITE_MAX_HEALTH);
                this.setHealth(this.getMaxHealth());
            }
            this.sengoku$applyEliteWeaponEnchantments();
        }
        return data;
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
                this.setItemSlot(EquipmentSlot.MAINHAND, sengoku$createPrimaryWeapon());
            }
            this.sengoku$applyEliteWeaponEnchantments();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && !this.sengoku$eliteWeaponInitialized
                && this.getAttributeValue(Attributes.MAX_HEALTH) >= (SENGOKU_ELITE_MAX_HEALTH - 0.1D)) {
            this.sengoku$eliteWeaponInitialized = true;
            if (this.getMainHandItem().isEmpty()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, sengoku$createPrimaryWeapon());
            }
            this.sengoku$applyEliteWeaponEnchantments();
        }
    }

    private ItemStack sengoku$createPrimaryWeapon() {
        if (this.random.nextBoolean()) return new ItemStack(Items.IRON_SWORD);
        Item yari = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sengoku", "iron_yari"));
        if (yari != null && yari != Items.AIR) return new ItemStack(yari);
        return new ItemStack(Items.IRON_SWORD);
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

    @Override
    public boolean isAlliedTo(Entity other) {
        if (other == null) return false;
        // Treat iron golems as allies so they won't attack clan mobs
        if (other instanceof IronGolem) return true;
        if (other instanceof SatomiSamuraiEntity || other instanceof SatomiAshigaruEntity || other instanceof SatomiSoheiEntity) {
            return true;
        }
        if (other instanceof AbstractIllager) {
            return false;
        }
        return super.isAlliedTo(other);
    }

    @Override
    protected void registerGoals() {
        // DON'T call super - override vanilla targeting completely
        this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        
        // Advanced combat AI - Ghost of Tsushima/Sekiro style
        this.goalSelector.addGoal(1, new WeaponBlockGoal(this)); // Block incoming attacks
        this.goalSelector.addGoal(2, new AdvancedMeleeAttackGoal(this, 1.0, false)); // Tactical melee attacks with timing
        this.goalSelector.addGoal(3, new CircleStrafeGoal(this)); // Circle around target like vexes
        // Fallback vanilla melee attack while mounted or when advanced goals are disabled
        this.goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.0D, true));
        
        this.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.Mob.class, 15.0F));
        
        // HurtByTargetGoal - retaliate when attacked
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
        // Assist nearby allies of same clan when they are attacked
        this.targetSelector.addGoal(1, new com.shioh.sengoku.ai.DefendAlliesTargetGoal<>(this, 16));
        // Proactively target monsters that are targeting this mob
        this.targetSelector.addGoal(2, new com.shioh.sengoku.ai.DefendFromTargetedMonsterGoal(this, 16.0));
        
        // NEW: Clan hostility check - become hostile if player is in clan guard structure or killed clan member
        this.targetSelector.addGoal(2, new com.shioh.sengoku.ai.ClanHostileTargetGoal(this, "satomi"));
        
        // Target other clans and outlaws using entity type tags - LONG DETECTION RANGE (64 blocks)
        NearestAttackableTargetGoal<net.minecraft.world.entity.Mob> rivalClansGoal = new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.Mob.class, 10, true, false, 
            (entity) -> {
                EntityType<?> type = entity.getType();
                // Target Takeda clan
                if (type.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("sengoku", "takeda_clan")))) {
                    return true;
                }
                // Target Kobayakawa clan
                if (type.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("sengoku", "kobayakawa_clan")))) {
                    return true;
                }
                // Target outlaws (vanilla illagers, ronin, bandits)
                if (type.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("sengoku", "outlaws")))) {
                    return true;
                }
                return false;
            }) {
            @Override
            protected double getFollowDistance() {
                return 64.0; // 64 block detection range for rival clans and outlaws
            }
        };
        this.targetSelector.addGoal(4, rivalClansGoal);
        
        // Explicitly target vanilla illagers to override faction behavior - LONG RANGE (64 blocks)
        NearestAttackableTargetGoal<Pillager> pillagerGoal = new NearestAttackableTargetGoal<>(this, Pillager.class, 10, true, false, (target) -> !(target instanceof SatomiAshigaruEntity)) {
            @Override
            protected double getFollowDistance() {
                return 64.0;
            }
        };
        this.targetSelector.addGoal(4, pillagerGoal);
        
        NearestAttackableTargetGoal<net.minecraft.world.entity.monster.Vindicator> vindicatorGoal = new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Vindicator.class, 10, true, false, (target) -> !(target instanceof SatomiSamuraiEntity)) {
            @Override
            protected double getFollowDistance() {
                return 64.0;
            }
        };
        this.targetSelector.addGoal(4, vindicatorGoal);
        
        NearestAttackableTargetGoal<net.minecraft.world.entity.monster.Evoker> evokerGoal = new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Evoker.class, 10, true, false, (target) -> !(target instanceof SatomiSoheiEntity)) {
            @Override
            protected double getFollowDistance() {
                return 64.0;
            }
        };
        this.targetSelector.addGoal(4, evokerGoal);
    }
    
    @Override
    public IllagerArmPose getArmPose() {
        try {
            if (com.shioh.sengoku.entity.ai.WeaponBlockGoal.isCurrentlyBlocking(this)) {
                return IllagerArmPose.ATTACKING; // one-handed block pose
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
            
            // Play custom parry sound
            this.playSound(amount >= 6.0F ? SoundRegistry.PARTIAL_PARRY : SoundRegistry.WEAPON_PARRY, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            
            // Spawn posture spark particle (same as player weapon block)
            WeaponBlockGoal.spawnBlockFeedbackParticles(this, amount);
            
            // Push the blocker (this) slightly away from the attacker instead
            Entity attacker = source.getEntity();
            if (!this.level().isClientSide && attacker instanceof LivingEntity livingAttacker) {
                Vec3 dir = this.position().subtract(livingAttacker.position()).normalize();
                this.setDeltaMovement(this.getDeltaMovement().add(dir.scale(0.25)));
                this.hurtMarked = true;
            }
            // Return false to completely block the damage
            // After successful block, target attacker and briefly drop block to counterattack
            attacker = source.getEntity();
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
        this.swing(InteractionHand.MAIN_HAND);
        boolean result = super.doHurtTarget(target);
        if (!this.level().isClientSide && result && target instanceof LivingEntity living) {
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
    public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
        // Override the vanilla illager faction check completely
        if (target instanceof SatomiSamuraiEntity || target instanceof SatomiAshigaruEntity || target instanceof SatomiSoheiEntity) {
            return false;
        }
        if (target instanceof net.minecraft.world.entity.player.Player player) {
            if (player.isCreative() || player.isSpectator()) {
                return false;
            }
        }
        return target.isAlive();
    }
}
