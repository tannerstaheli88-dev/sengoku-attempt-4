package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.EliteMob;
import com.shioh.sengoku.entity.ai.ShinobiAmbushGoal;
import com.shioh.sengoku.entity.ai.ShinobiCombatGoal;
import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import com.shioh.sengoku.registry.ParticleRegistry;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.shioh.sengoku.registry.WeaponRegistry;
import com.shioh.sengoku.struct.WeaponType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ServerLevelAccessor;
import java.util.List;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to reimagine the Illusioner as a shinobi:
 *  - ShinobiAmbushGoal  (priority 2): stalks player from shadows, pounces when spotted
 *  - ShinobiCombatGoal  (priority 1): switches between melee and kunai throwing in combat
 */
@Mixin(Illusioner.class)
public class IllusionerMixin implements EliteMob {

    @Unique
    private static final String SENGOKU_ELITE_TAG = "sengoku_elite";

    @Unique
    private static final double SENGOKU_ELITE_MAX_HEALTH = 64.0D;

    @Unique
    private boolean sengoku$wasCastingLastTick = false;

    @Unique
    private boolean sengoku$eliteFallback = false;

    @Unique
    private boolean sengoku$eliteInitialized = false;

    // No synched data injection - just use NBT persistence + fallback field

    @Inject(method = "finalizeSpawn", at = @At("TAIL"), require = 0)
    private void sengoku$finalizeSpawnElite(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnData, CallbackInfoReturnable<SpawnGroupData> cir) {
        // Intentionally no random elite roll for Illusioner.
        // Elite is controlled by explicit custom spawn data (e.g., tag/NBT).
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"), require = 0)
    private void sengoku$saveElite(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("Elite", this.sengoku$isElite());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"), require = 0)
    private void sengoku$readElite(CompoundTag tag, CallbackInfo ci) {
        this.sengoku$setElite(tag.getBoolean("Elite"));
    }

    @Inject(method = "getDefaultLootTable", at = @At("HEAD"), cancellable = true, require = 0)
    private void sengoku$eliteLoot(CallbackInfoReturnable<ResourceKey<LootTable>> cir) {
        if (this.sengoku$isElite()) {
            cir.setReturnValue(ResourceKey.create(
                net.minecraft.core.registries.Registries.LOOT_TABLE,
                ResourceLocation.fromNamespaceAndPath("sengoku", "entities/illusioner_elite")
            ));
        }
    }

    @Inject(method = "populateDefaultEquipmentSlots", at = @At("HEAD"), cancellable = true, require = 0)
    private void sengoku$shinobiHandWeapon(RandomSource random, DifficultyInstance difficulty, CallbackInfo ci) {
        Illusioner self = (Illusioner)(Object)this;
        // Only apply to vanilla Illusioner; ShinobiLordEntity handles its own equipment
        if (self.getClass() != Illusioner.class) return;
        List<Item> tantos = WeaponRegistry.getItemsByType(WeaponType.TANTO);
        Item weapon = tantos.size() > 2 ? tantos.get(2) : Items.IRON_SWORD;
        self.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
        Item kunai = net.minecraft.core.registries.BuiltInRegistries.ITEM
            .get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "kunai"));
        self.setItemSlot(EquipmentSlot.OFFHAND, kunai != null ? new ItemStack(kunai) : ItemStack.EMPTY);
        ci.cancel();
    }

    @Inject(method = "registerGoals", at = @At("TAIL"), require = 0)
    private void addShinobiGoals(CallbackInfo ci) {
        Illusioner self = (Illusioner)(Object)this;
        // Skip if this is actually a ShinobiLordEntity subclass (it registers its own goals)
        if (self.getClass() != Illusioner.class) return;
        // Use the same shinobi combat & ambush goals as the Shinobi Lord.
        // Also add the WeaponBlockGoal so vanilla Illusioners can parry like the Shinobi Lord.
        // Do not add WeaponBlockGoal to vanilla Illusioner — keep only shinobi combat/ambush goals
        ((MobAccessor)self).getGoalSelector().addGoal(2, new ShinobiCombatGoal(self));
        ((MobAccessor)self).getGoalSelector().addGoal(3, new ShinobiAmbushGoal(self));
    }





    @Inject(method = "aiStep", at = @At("TAIL"), require = 0)
    private void sengoku$shinobiSpellSmoke(CallbackInfo ci) {
        Illusioner self = (Illusioner)(Object)this;

        // Some spawn/save hooks are not stable across mappings for Illusioner.
        // Initialize elite status on first server tick as a reliable fallback.
if (!self.level().isClientSide && !this.sengoku$eliteInitialized) {
    this.sengoku$eliteInitialized = true;

    // Boost follow range so the illusioner doesn't lose track when player moves rooms
    try {
        var followRange = self.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange != null) followRange.setBaseValue(64.0D);
    } catch (Throwable ignored) {}

    if (self.getAttributeValue(Attributes.MAX_HEALTH) >= (SENGOKU_ELITE_MAX_HEALTH - 0.1D)) {
        this.sengoku$setElite(true);
    } else if (!this.sengoku$isElite() && self.getTags().contains(SENGOKU_ELITE_TAG)) {
        this.sengoku$setElite(true);
    }
}

        // Trigger once per cast start, not every tick while channeling.
        boolean castingNow = self.isCastingSpell();
        if (castingNow && !this.sengoku$wasCastingLastTick) {
            sengoku$applyAwkwardSplashCloak(self);
        }
        this.sengoku$wasCastingLastTick = castingNow;
    }

    @Inject(method = "getAmbientSound", at = @At("HEAD"), cancellable = true, require = 0)
    private void sengoku$ambientOnlyInCombat(CallbackInfoReturnable<SoundEvent> cir) {
        Illusioner self = (Illusioner)(Object)this;
        var target = self.getTarget();
        if (target == null || !target.isAlive()) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true, require = 0)
    private void sengoku$onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Illusioner self = (Illusioner)(Object)this;

        // If attacked, try to set the attacker as target (resolve projectile owners)
        try {
            Entity attacker = source.getEntity();
            try {
                if (attacker instanceof net.minecraft.world.entity.projectile.Projectile ppe) {
                    Entity owner = ppe.getOwner();
                    if (owner != null) attacker = owner;
                }
            } catch (Throwable ignored) {}
            if (attacker instanceof LivingEntity la) {
                self.setTarget(la);
            }
        } catch (Throwable ignored) {}

        // If currently blocking, perform a parry: play sound/particles, knockback attacker, cancel damage
        if (WeaponBlockGoal.isCurrentlyBlocking(self)) {
            try { self.playSound(amount >= 6.0F ? SoundRegistry.PARTIAL_PARRY : SoundRegistry.WEAPON_PARRY, 1.0F, 0.8F + self.getRandom().nextFloat() * 0.4F); } catch (Throwable ignored) {}
            WeaponBlockGoal.spawnBlockFeedbackParticles(self);
            Entity blockAttacker = source.getEntity();
            if (!self.level().isClientSide && blockAttacker instanceof LivingEntity livingAttacker) {
                Vec3 dir = self.position().subtract(livingAttacker.position()).normalize();
                self.setDeltaMovement(self.getDeltaMovement().add(dir.scale(0.25)));
                self.hurtMarked = true;
            }
            try { WeaponBlockGoal.onSuccessfulBlock(self, blockAttacker instanceof LivingEntity ? (LivingEntity) blockAttacker : null); } catch (Throwable ignored) {}
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static void sengoku$applyAwkwardSplashCloak(Illusioner self) {
        // Short ninja vanish window after the spell cast starts.
        self.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 80, 0, false, true, true));

        // Awkward splash self-use feedback.
        self.level().playSound(
            null,
            self.getX(), self.getY(), self.getZ(),
            SoundEvents.SPLASH_POTION_THROW,
            self.getSoundSource(),
            0.9F,
            0.95F + self.getRandom().nextFloat() * 0.15F
        );
        self.level().playSound(
            null,
            self.getX(), self.getY(), self.getZ(),
            SoundEvents.GENERIC_SPLASH,
            self.getSoundSource(),
            0.8F,
            1.05F
        );

        if (self.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.WITCH,
                self.getX(),
                self.getY() + 1.0D,
                self.getZ(),
                16,
                0.35D,
                0.45D,
                0.35D,
                0.0D
            );
            serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                self.getX(),
                self.getY() + 0.3D,
                self.getZ(),
                10,
                0.25D,
                0.1D,
                0.25D,
                0.02D
            );
        }
    }

    @Override
    public boolean sengoku$isElite() {
        return this.sengoku$eliteFallback;
    }

    @Override
    public void sengoku$setElite(boolean elite) {
        this.sengoku$eliteFallback = elite;
        if (elite) {
            this.sengoku$applyEliteStats();
        }
    }

    @Unique
    private void sengoku$applyEliteStats() {
        Illusioner self = (Illusioner) (Object) this;
        try {
            var maxHealth = self.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(SENGOKU_ELITE_MAX_HEALTH);
                self.setHealth(self.getMaxHealth());
            }
        } catch (Throwable ignored) {
        }
    }
}
