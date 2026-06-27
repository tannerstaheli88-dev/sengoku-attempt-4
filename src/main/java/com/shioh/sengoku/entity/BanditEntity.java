package com.shioh.sengoku.entity;

import com.shioh.sengoku.entity.ai.BanditMeleeAttackGoal;
import com.shioh.sengoku.entity.ai.BanditBowAttackGoal;
import com.shioh.sengoku.entity.ai.PatrolCautiousEngagementGoal;
import com.shioh.sengoku.registry.WeaponRegistry;
import com.shioh.sengoku.registry.SoundRegistry;
import com.shioh.sengoku.struct.WeaponType;
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
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Bandit entity that can use either melee or ranged weapons (but not both).
 * Like piglins - they spawn with either a bow OR a melee weapon.
 */
public class BanditEntity extends AbstractIllager implements RangedAttackMob {
    
    public BanditEntity(EntityType<? extends BanditEntity> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }
    
    @Override
    protected void registerGoals() {
        super.registerGoals();
        
        // Basic AI goals
        this.goalSelector.addGoal(0, new FloatGoal(this));
        
        // Patrol gating goal - makes patrols cautious and watch from distance
        this.goalSelector.addGoal(1, new PatrolCautiousEngagementGoal(this));
        
        // Combat AI goals - only one will be active based on equipment
        this.goalSelector.addGoal(2, new BanditBowAttackGoal(this, 1.0, 20, 15.0F));
        this.goalSelector.addGoal(3, new BanditMeleeAttackGoal(this, 1.0, false));
        
        // Movement and patrol
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
        
        // Targeting
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());  // Removed AbstractIllager.class so it retaliates against ALL attackers
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        
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
            .add(Attributes.MAX_HEALTH, 12.0)  // Reduced from 16
            .add(Attributes.ATTACK_DAMAGE, 1.0)  // Reduced from 3.0
            .add(Attributes.ARMOR, 2.0);
    }
    
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // 50/50 chance of being ranged or melee
        if (random.nextBoolean()) {
            // Ranged bandit - bow only
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        } else {
            // Melee bandit - custom weapon only
            equipMeleeWeapon(random);
        }
        
        // Random light armor - bandits wear leather armor
        if (random.nextFloat() < 0.3f) {
            this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
        }
        if (random.nextFloat() < 0.5f) {
            this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.LEATHER_CHESTPLATE));
        }
    }
    
    private void equipMeleeWeapon(RandomSource random) {
        // Pick a random weapon type that bandits would use
        WeaponType[] banditWeapons = {
            WeaponType.YARI, WeaponType.NAGINATA, WeaponType.KANABO, WeaponType.TANTO
        };
        
        WeaponType chosenType = banditWeapons[random.nextInt(banditWeapons.length)];
        List<net.minecraft.world.item.Item> weapons = WeaponRegistry.getItemsByType(chosenType);
        
        if (!weapons.isEmpty()) {
            // Bandits only use WOOD tier weapons
            var woodWeapons = WeaponRegistry.getItemsByMaterial(Tiers.WOOD);
            var availableWeapon = weapons.stream()
                .filter(weapon -> woodWeapons.contains(weapon))
                .findFirst()
                .orElse(null);
            
            if (availableWeapon != null) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(availableWeapon));
            }
        }
    }
    
    public boolean hasRangedWeapon() {
        return this.getMainHandItem().getItem() == Items.BOW;
    }
    
    public boolean hasMeleeWeapon() {
        return this.isMeleeWeapon(this.getMainHandItem());
    }
    
    public boolean isMeleeWeapon(ItemStack stack) {
        // Check if it's one of our custom weapons
        for (WeaponType type : WeaponType.values()) {
            if (WeaponRegistry.getItemsByType(type).stream()
                .anyMatch(weapon -> weapon == stack.getItem())) {
                return true;
            }
        }
        return false;
    }
    
    // RangedAttackMob implementation
    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack bow = this.getMainHandItem();
        if (bow.getItem() == Items.BOW) {
            // Create arrow like other mobs do
            net.minecraft.world.entity.projectile.Arrow arrow = new net.minecraft.world.entity.projectile.Arrow(this.level(), this, Items.ARROW.getDefaultInstance(), null);
            double deltaX = target.getX() - this.getX();
            double deltaY = target.getY(0.3333333333333333D) - arrow.getY();
            double deltaZ = target.getZ() - this.getZ();
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
            
            arrow.shoot(deltaX, deltaY + distance * 0.20000000298023224D, deltaZ, 1.6F, 14 - this.level().getDifficulty().getId() * 4);
            this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level().addFreshEntity(arrow);
        }
    }
    
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PILLAGER_HURT;
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
            try {
                this.playSound(SoundRegistry.BANDIT_AMBIENT_AGGRO, 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
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
        if (this.isAggressive() && this.hasMeleeWeapon()) {
            return IllagerArmPose.ATTACKING;
        }
        return IllagerArmPose.NEUTRAL;
    }
    
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, 
                                       MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), difficulty);
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }
    
    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem weapon) {
        return weapon == Items.BOW;
    }
    
    // Required Raider abstract methods
    @Override
    public void applyRaidBuffs(ServerLevel level, int wave, boolean unusedFalse) {
        // Apply enchantments based on difficulty
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), level.getCurrentDifficultyAt(this.blockPosition()));
    }
    
    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }
}