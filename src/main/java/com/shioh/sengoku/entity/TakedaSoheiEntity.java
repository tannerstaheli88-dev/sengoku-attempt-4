package com.shioh.sengoku.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Takeda Sohei - Exact copy of Evoker but attacks other clans.
 */
public class TakedaSoheiEntity extends Evoker {
    
    public TakedaSoheiEntity(EntityType<? extends Evoker> type, Level level) {
        super(type, level);
    }
    
    @Override
    public boolean isAlliedTo(Entity other) {
        if (other == null) return false;
        // Treat iron golems as allies so they won't attack clan mobs
        if (other instanceof IronGolem) return true;
        if (other instanceof TakedaSamuraiEntity || other instanceof TakedaAshigaruEntity || other instanceof TakedaSoheiEntity) {
            return true;
        }
        if (other instanceof AbstractIllager) {
            return false;
        }
        return super.isAlliedTo(other);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals(); // Keep spell-casting goals
        
        // Clear vanilla targeting goals and add our own
        this.targetSelector.getAvailableGoals().clear();
        
        // HurtByTargetGoal - retaliate when attacked
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
        // Assist nearby allies of same clan when they are attacked
        this.targetSelector.addGoal(1, new com.shioh.sengoku.ai.DefendAlliesTargetGoal<>(this, 16));
        // Proactively target monsters that are targeting this mob
        this.targetSelector.addGoal(1, new com.shioh.sengoku.ai.DefendFromTargetedMonsterGoal(this, 16.0));
        
        // NEW: Clan hostility check - become hostile if player is in clan guard structure or killed clan member
        this.targetSelector.addGoal(2, new com.shioh.sengoku.ai.ClanHostileTargetGoal(this, "takeda"));
        
        // Target other clans and outlaws using entity type tags
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.Mob.class, 10, true, false, 
            (entity) -> {
                EntityType<?> type = entity.getType();
                // Target Satomi clan
                if (type.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "satomi_clan")))) {
                    return true;
                }
                // Target Kobayakawa clan
                if (type.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "kobayakawa_clan")))) {
                    return true;
                }
                // Target outlaws (vanilla illagers, ronin, bandits)
                if (type.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "outlaws")))) {
                    return true;
                }
                return false;
            }));
        
        // Explicitly target vanilla illagers to override faction behavior
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Pillager.class, 10, true, false, (target) -> !(target instanceof TakedaAshigaruEntity)));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Vindicator.class, 10, true, false, (target) -> !(target instanceof TakedaSamuraiEntity)));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Evoker.class, 10, true, false, (target) -> !(target instanceof TakedaSoheiEntity)));
    }
    
    @Override
    public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
        // Override the vanilla illager faction check completely
        if (target instanceof TakedaSamuraiEntity || target instanceof TakedaAshigaruEntity || target instanceof TakedaSoheiEntity) {
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
