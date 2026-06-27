package com.shioh.sengoku.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.level.Level;

/**
 * Satomi Ashigaru - Exact copy of Pillager but attacks other clans and shoots spectral arrows.
 */
public class SatomiAshigaruEntity extends Pillager {
    
    public SatomiAshigaruEntity(EntityType<? extends Pillager> type, Level level) {
        super(type, level);
        // Give full stack of spectral arrows in offhand for crossbow projectile
        this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SPECTRAL_ARROW, 1));
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return com.shioh.sengoku.registry.SoundRegistry.ASHIGARU_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource damageSource) {
        return com.shioh.sengoku.registry.SoundRegistry.ASHIGARU_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return com.shioh.sengoku.registry.SoundRegistry.ASHIGARU_DEATH;
    }

    @Override
    public void setAggressive(boolean aggressive) {
        boolean was = this.isAggressive();
        super.setAggressive(aggressive);
        if (!was && aggressive && !this.level().isClientSide) {
            this.playSound(com.shioh.sengoku.registry.SoundRegistry.ASHIGARU_AMBIENT_AGGRO, 1.0F, 1.0F);
        }
    }

    @Override
    public void tick() {
        super.tick();
        // Refill spectral arrows if they run out
        net.minecraft.world.item.ItemStack offhand = this.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        if (offhand.isEmpty() || offhand.getCount() < 1 || offhand.getItem() != net.minecraft.world.item.Items.SPECTRAL_ARROW) {
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.SPECTRAL_ARROW, 1));
        }
    }
    
    // Note: We DON'T override performRangedAttack - vanilla Pillager/CrossbowItem handles it
    // CrossbowItemMixin forces spectral arrows and SpectralArrowVelocityMixin makes them fast
    
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
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal<>(this, 1.0, 8.0F));
        this.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.Mob.class, 15.0F));
        
        // HurtByTargetGoal - retaliate when attacked
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
        // Assist nearby allies of same clan when they are attacked
        this.targetSelector.addGoal(1, new com.shioh.sengoku.ai.DefendAlliesTargetGoal<>(this, 16));
        // Proactively target monsters that are targeting this mob
        this.targetSelector.addGoal(1, new com.shioh.sengoku.ai.DefendFromTargetedMonsterGoal(this, 16.0));
        
        // NEW: Clan hostility check - become hostile if player is in clan guard structure or killed clan member
        this.targetSelector.addGoal(2, new com.shioh.sengoku.ai.ClanHostileTargetGoal(this, "satomi"));
        
        // Target other clans and outlaws using entity type tags
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.Mob.class, 10, true, false, 
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
            }));
        
        // Explicitly target vanilla illagers to override faction behavior
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Pillager.class, 10, true, false, (target) -> !(target instanceof SatomiAshigaruEntity)));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Vindicator.class, 10, true, false, (target) -> !(target instanceof SatomiSamuraiEntity)));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Evoker.class, 10, true, false, (target) -> !(target instanceof SatomiSoheiEntity)));
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
