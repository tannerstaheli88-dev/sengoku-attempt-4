package com.shioh.sengoku.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Vindicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to make vanilla Vindicators target clan mobs.
 */
@Mixin(Vindicator.class)
public class VindicatorTargetingMixin {
    
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addClanTargeting(CallbackInfo ci) {
        Vindicator self = (Vindicator)(Object)this;
        
        // Target all clan mobs using entity type tags
        ((MobAccessor)self).getTargetSelector().addGoal(3, new NearestAttackableTargetGoal<>(self, net.minecraft.world.entity.Mob.class, 10, true, false, 
            (entity) -> {
                EntityType<?> type = entity.getType();
                // Target any clan
                return type.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("sengoku", "takeda_clan"))) ||
                       type.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("sengoku", "satomi_clan"))) ||
                       type.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("sengoku", "kobayakawa_clan")));
            }));
    }
}
