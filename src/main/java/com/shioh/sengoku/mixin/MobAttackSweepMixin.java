package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.ai.AdvancedMeleeAttackGoal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(LivingEntity.class)
public class MobAttackSweepMixin {

    private static final TagKey<EntityType<?>> BITING_MOBS = TagKey.create(
        Registries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath("sengoku", "biting_mobs")
    );

    private static final TagKey<EntityType<?>> HEAVY_MOBS = TagKey.create(
        Registries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath("sengoku", "heavy_mobs")
    );

    private static boolean usesAdvancedMeleeGoal(Mob mob) {
        try {
            for (var wrappedGoal : ((MobAccessor) mob).getGoalSelector().getAvailableGoals()) {
                if (wrappedGoal != null && wrappedGoal.getGoal() instanceof AdvancedMeleeAttackGoal) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At("HEAD"), require = 0)
    private void sengoku$sweepTelegraphOnSwing_one(InteractionHand hand, CallbackInfo ci) {
        try { playSweepIfApplicable(); } catch (Throwable ignored) {}
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", at = @At("HEAD"), require = 0)
    private void sengoku$sweepTelegraphOnSwing_two(InteractionHand hand, boolean animate, CallbackInfo ci) {
        try { playSweepIfApplicable(); } catch (Throwable ignored) {}
    }

    private void playSweepIfApplicable() {
        Entity ent = (Entity) (Object) this;
        if (!(ent instanceof Mob self)) return;
        if (self.level().isClientSide()) return;
        if (usesAdvancedMeleeGoal(self)) return;
        if (!(self.level() instanceof ServerLevel sl)) return;
        if (!(self.getTarget() instanceof Player)) return;

        double x = self.getX();
        double y = self.getY() + self.getBbHeight() * 0.5;
        double z = self.getZ();

        SoundEvent sound;
        float volume;
        float pitch;

        if (self.getType().is(BITING_MOBS)) {
            // Biting mobs: use bite-like sound
            sound = SoundEvents.FOX_BITE;
            volume = 0.7F;
            pitch = 0.9F + self.getRandom().nextFloat() * 0.2F;
        } else if (self.getType().is(HEAVY_MOBS)) {
            // Heavy mobs: use a weighty impact sound
            sound = SoundEvents.MACE_SMASH_AIR;
            volume = 0.8F;
            pitch = 0.85F + self.getRandom().nextFloat() * 0.15F;
        } else {
            // Default: existing sweep sound
            sound = SoundEvents.PLAYER_ATTACK_SWEEP;
            volume = 0.6F;
            pitch = 1.05F + self.getRandom().nextFloat() * 0.15F;
        }

        sl.sendParticles(ParticleTypes.SWEEP_ATTACK, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        sl.playSound(null, x, y, z, sound, SoundSource.HOSTILE, volume, pitch);
    }
}