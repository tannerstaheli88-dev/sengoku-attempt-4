package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import com.shioh.sengoku.sengokuFabric;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Inject into the common `Mob` tick so we don't rely on `IronGolem` declaring
 * a `tick` method (which varies between mappings). Only clear targets when
 * the mob is an `IronGolem` and the target belongs to clan tags.
 */
@Mixin(Mob.class)
public class IronGolemTargetClearMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void sengoku$clearClanTarget(CallbackInfo ci) {
        try {
            Mob self = (Mob) (Object) this;
            if (!(self instanceof IronGolem)) return;
            IronGolem golem = (IronGolem) self;
            LivingEntity t = golem.getTarget();
            if (t == null) return;
            EntityType<?> et = t.getType();
            TagKey<EntityType<?>> takeda = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("sengoku:takeda_clan"));
            TagKey<EntityType<?>> satomi = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("sengoku:satomi_clan"));
            TagKey<EntityType<?>> koba = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("sengoku:kobayakawa_clan"));
            if (et.is(takeda) || et.is(satomi) || et.is(koba)) {
                try { sengokuFabric.LOGGER.info("IronGolem: clearing clan target {}", t.getType()); } catch (Throwable ignored) {}
                try { golem.setTarget(null); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }
}
