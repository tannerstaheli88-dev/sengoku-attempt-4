package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import com.shioh.sengoku.registry.ParticleRegistry;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import com.shioh.sengoku.system.BloodSprayManager;
import com.shioh.sengoku.system.StealthCritCooldownManager;
import com.shioh.sengoku.util.PlayerNoiseTracker;
import com.shioh.sengoku.item.TantoItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to add blood particles and melee weapon hit sounds when entities are damaged by melee weapons.
 */
@Mixin(LivingEntity.class)
public class MeleeWeaponHitMixin {
    @org.spongepowered.asm.mixin.Unique
    private static final net.minecraft.resources.ResourceLocation TANTO_ONESHOT_ADVANCEMENT =
        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "tanto_stealth_oneshot");
    @org.spongepowered.asm.mixin.Unique
    private static final String TANTO_ONESHOT_CRITERION = "one_shot_with_tanto_stealth";

    @org.spongepowered.asm.mixin.Unique
    private static final ThreadLocal<Boolean> sengoku$hurtReentrant = ThreadLocal.withInitial(() -> false);
    @org.spongepowered.asm.mixin.Unique
    private static final ThreadLocal<Integer> sengoku$skipReturnEffectsCount = ThreadLocal.withInitial(() -> 0);

    @org.spongepowered.asm.mixin.Unique
    private static boolean sengoku$isWeaponBlockItem(ItemStack stack) {
        return !stack.isEmpty() && (
            stack.getItem() instanceof SwordItem
            || stack.getItem() instanceof AxeItem
            || stack.getItem() instanceof com.shioh.sengoku.item.SweeplessItem
        );
    }

    @org.spongepowered.asm.mixin.Unique
    private static boolean sengoku$isPlayerWeaponGuarding(Player player) {
        if (player == null) {
            return false;
        }

        ItemStack useItem = player.getUseItem();
        if (!useItem.isEmpty() && sengoku$isWeaponBlockItem(useItem)) {
            return true;
        }

        return player.isUsingItem() && sengoku$isWeaponBlockItem(player.getMainHandItem());
    }

    @org.spongepowered.asm.mixin.Unique
    private static void sengoku$awardAdvancement(ServerPlayer player, net.minecraft.resources.ResourceLocation advancementId, String criterion) {
        net.minecraft.advancements.AdvancementHolder holder = player.server.getAdvancements().get(advancementId);
        if (holder != null) {
            player.getAdvancements().award(holder, criterion);
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void onEntityHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        // Only run on server side
        if (self.level().isClientSide()) {
            return;
        }

        // Determine attacker (may be null for environmental damage)
        Entity attacker = source.getDirectEntity();
        Entity trueAttacker = source.getEntity(); // shooter/source (may be same as direct entity)

        // Resolve an actual Player attacker whether direct or true
        Player playerAttacker = null;
        if (trueAttacker instanceof Player) playerAttacker = (Player) trueAttacker;
        else if (attacker instanceof Player) playerAttacker = (Player) attacker;

        boolean playerWasNoisyBeforeAttack = false;
        if (playerAttacker != null) {
            try {
                playerWasNoisyBeforeAttack = PlayerNoiseTracker.getInstance().isNoisy(playerAttacker);
            } catch (Throwable ignored) {}
            try {
                PlayerNoiseTracker.getInstance().addNoise(playerAttacker, PlayerNoiseTracker.ATTACK_NOISE);
            } catch (Throwable ignored) {}
            try {
                if (playerAttacker.isInvisible()) {
                    try { playerAttacker.removeEffect(net.minecraft.world.effect.MobEffects.INVISIBILITY); } catch (Throwable ignored) {}
                    try { playerAttacker.setInvisible(false); } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
        }

        if (sengoku$hurtReentrant.get()) {
            return;
        }

        ServerLevel level = (ServerLevel) self.level();
        Vec3 hitPos = self.position().add(0, self.getBbHeight() * 0.5, 0);

        Vec3 attackDirection;
        if (attacker instanceof LivingEntity) {
            LivingEntity livingAttacker = (LivingEntity) attacker;
            attackDirection = livingAttacker.getLookAngle();
        } else {
            double rx = level.random.nextDouble() - 0.5;
            double ry = 0.2 + level.random.nextDouble() * 0.6;
            double rz = level.random.nextDouble() - 0.5;
            attackDirection = new Vec3(rx, ry, rz).normalize();
        }

        boolean allowBlood = true;
        if (self instanceof Skeleton || self instanceof WitherSkeleton || self instanceof SkeletonHorse || self instanceof IronGolem) {
            allowBlood = false;
        }
        try {
            if (self.getClass().getName().equals("com.shioh.sengoku.entity.GoryoEntity") || self instanceof com.shioh.sengoku.entity.GoryoEntity) {
                allowBlood = false;
            }
            if (self.getClass().getName().equals("com.shioh.sengoku.entity.KojinEntity") || self instanceof com.shioh.sengoku.entity.KojinEntity) {
                allowBlood = false;
            }
            if (self.getClass().getName().equals("com.shioh.sengoku.entity.NingyoEntity") || self instanceof com.shioh.sengoku.entity.NingyoEntity) {
                allowBlood = false;
            }
            if (self.getClass().getName().equals("com.shioh.sengoku.entity.KamiikeHimeEntity") || self instanceof com.shioh.sengoku.entity.KamiikeHimeEntity) {
                allowBlood = false;
            }
            if (self.getClass().getName().equals("com.shioh.sengoku.entity.AkugyoEntity") || self instanceof com.shioh.sengoku.entity.AkugyoEntity) {
                allowBlood = false;
            }
        } catch (Throwable ignored) {}

        if (self instanceof Player) {
            Player tp = (Player) self;
            if (tp.isBlocking() || sengoku$isPlayerWeaponGuarding(tp)) allowBlood = false;
        }

        boolean mobWeaponGuarding = self instanceof Mob mobTarget && WeaponBlockGoal.isCurrentlyBlocking(mobTarget);
        if (mobWeaponGuarding) {
            allowBlood = false;
        }

        boolean appliedCustom = false;
        try {
            if (self instanceof Mob mobTarget && attacker instanceof LivingEntity livingAttacker && attacker == source.getDirectEntity()) {
                ItemStack punishWeapon = livingAttacker.getMainHandItem();
                if ((livingAttacker instanceof Player || isMeleeWeapon(punishWeapon)) && WeaponBlockGoal.consumeBrokenPoisePunish(mobTarget)) {
                    float criticalDamage = amount * 1.75F;
                    try {
                        level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, hitPos.x, hitPos.y + 0.8D, hitPos.z, 24, 0.45D, 0.35D, 0.45D, 0.06D);
                        level.playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 1.0F, 0.95F + level.random.nextFloat() * 0.1F);
                    } catch (Throwable ignored) {}

                    if (allowBlood) {
                        try {
                            spawnBloodSpray(level, hitPos, attackDirection, criticalDamage);
                        } catch (Throwable ignored) {}
                    }

                    sengoku$skipUpcomingReturnEffects(2);
                    sengoku$hurtReentrant.set(true);
                    try {
                        boolean result = ((LivingEntity)(Object)this).hurt(source, criticalDamage);
                        cir.setReturnValue(result);
                    } finally {
                        sengoku$hurtReentrant.set(false);
                    }

                    appliedCustom = true;
                }
            }

            if (appliedCustom) return;

            try {
                if (playerAttacker != null && attacker instanceof LivingEntity) {
                    LivingEntity livingAttacker = (LivingEntity) attacker;
                    ItemStack held = livingAttacker.getMainHandItem();
                    if (held.getItem() instanceof TantoItem) {
                        TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("minecraft", "one_shottable_from_tanto"));
                        if (self.getType().is(tag)) {
                            boolean noisyBefore = playerWasNoisyBeforeAttack;
                            boolean mobHasDetected = false;
                            if (self instanceof Mob && playerAttacker != null) {
                                Mob mobCheck = (Mob) self;
                                mobHasDetected = (mobCheck.getTarget() == playerAttacker) || (mobCheck.getLastHurtByMob() == playerAttacker);
                            }

                            if (!noisyBefore && !mobHasDetected) {
                                boolean allowedByCooldown = true;
                                try {
                                    if (playerAttacker != null) {
                                        allowedByCooldown = StealthCritCooldownManager.getInstance().tryConsume(playerAttacker, (LivingEntity)self, 100L);
                                    }
                                } catch (Throwable ignored) {}

                                if (allowedByCooldown) {
                                    float newAmount = self.getHealth() + 1.0F;
                                    try {
                                        if (playerAttacker instanceof ServerPlayer) {
                                            ServerPlayer spAtk = (ServerPlayer) playerAttacker;
                                            ItemStack heldStack = livingAttacker.getMainHandItem();
                                            if (!heldStack.isEmpty()) {
                                                spAtk.getCooldowns().addCooldown(heldStack.getItem(), 100);
                                            }
                                        }
                                    } catch (Throwable ignored) {}
                                    try {
                                        if (level != null) {
                                            float pitch = 0.95F + level.random.nextFloat() * 0.1F;
                                            level.playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundRegistry.BACKSTAB, SoundSource.PLAYERS, 1.0F, pitch);

                                            long seed = level.getRandom().nextLong();
                                            Holder<net.minecraft.sounds.SoundEvent> holder = Holder.direct(SoundRegistry.BACKSTAB);
                                            ClientboundSoundPacket pktPlayers = new ClientboundSoundPacket(holder, SoundSource.PLAYERS, hitPos.x, hitPos.y, hitPos.z, 1.0F, pitch, seed);

                                            java.util.List<net.minecraft.world.entity.player.Player> nearbyPlayers = level.getEntitiesOfClass(
                                                    net.minecraft.world.entity.player.Player.class,
                                                    ((net.minecraft.world.entity.Entity)(Object)self).getBoundingBox().inflate(32.0),
                                                    p -> !p.isSpectator()
                                            );
                                            for (net.minecraft.world.entity.player.Player p : nearbyPlayers) {
                                                if (p instanceof ServerPlayer) {
                                                    ServerPlayer sp = (ServerPlayer) p;
                                                    try { sp.connection.send(pktPlayers); } catch (Throwable ignored) {}
                                                }
                                            }
                                            if (playerAttacker instanceof ServerPlayer) {
                                                ServerPlayer spAtk = (ServerPlayer) playerAttacker;
                                                try { spAtk.connection.send(pktPlayers); } catch (Throwable ignored) {}
                                            }
                                        }
                                    } catch (Throwable ignored) {}

                                    applyStealthCritEffects(level, self, hitPos, attackDirection, newAmount, allowBlood, false);

                                    sengoku$skipUpcomingReturnEffects(2);
                                    sengoku$hurtReentrant.set(true);
                                    try {
                                        boolean result = ((LivingEntity)(Object)this).hurt(source, newAmount);
                                        if (result && !self.isAlive() && playerAttacker instanceof ServerPlayer spAtk) {
                                            sengoku$awardAdvancement(spAtk, TANTO_ONESHOT_ADVANCEMENT, TANTO_ONESHOT_CRITERION);
                                        }
                                        cir.setReturnValue(result);
                                    } finally {
                                        sengoku$hurtReentrant.set(false);
                                    }

                                    appliedCustom = true;
                                }
                            }
                        }
                    }
                }
            } catch (Throwable ignored) {}

            if (playerAttacker != null && self instanceof Mob && attacker instanceof LivingEntity) {
                Mob mobTarget = (Mob) self;
                boolean noisy = playerWasNoisyBeforeAttack;
                boolean hasDetected = (mobTarget.getTarget() == playerAttacker) || (mobTarget.getLastHurtByMob() == playerAttacker);
                if (!mobWeaponGuarding && !noisy && !hasDetected) {
                    boolean allowed = StealthCritCooldownManager.getInstance().tryConsume(playerAttacker, mobTarget, 200L);
                    if (allowed) {
                        float critMultiplier = 1.5F;
                        float newAmount = amount * critMultiplier;

                        applyStealthCritEffects(level, self, hitPos, attackDirection, newAmount, allowBlood, true);

                        sengoku$skipUpcomingReturnEffects(2);
                        sengoku$hurtReentrant.set(true);
                        try {
                            boolean result = ((LivingEntity)(Object)this).hurt(source, newAmount);
                            cir.setReturnValue(result);
                        } finally {
                            sengoku$hurtReentrant.set(false);
                        }
                        appliedCustom = true;
                    }
                }
            }
        } catch (Throwable ignored) {}

        if (appliedCustom) return;
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void sengoku$playConfirmedHitEffects(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (self.level().isClientSide()) {
            return;
        }

        if (sengoku$consumeSkippedReturnEffect()) {
            return;
        }

        if (!cir.getReturnValueZ()) {
            return;
        }

        ServerLevel level = (ServerLevel) self.level();
        Entity attacker = source.getDirectEntity();
        Vec3 hitPos = self.position().add(0, self.getBbHeight() * 0.5, 0);
        Vec3 attackDirection = sengoku$resolveAttackDirection(level, attacker);

        boolean mobWeaponGuarding = self instanceof Mob mobTarget && WeaponBlockGoal.isCurrentlyBlocking(mobTarget);
        boolean blocked = false;
        if (self instanceof Player player) {
            blocked = player.isBlocking() || sengoku$isPlayerWeaponGuarding(player);
        }

        if (sengoku$shouldAllowBlood(self, mobWeaponGuarding, blocked)) {
            spawnBloodSpray(level, hitPos, attackDirection, amount);
            try {
                float currentHealth = self.getHealth();
                if (amount >= currentHealth) {
                    int duration = Math.min(6, 1 + (int)Math.ceil(amount * 1.0));
                    BloodSprayManager.startSpray(level, hitPos, duration, amount);
                }
            } catch (Throwable ignored) {}
        }

        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return;
        }

        ItemStack weapon = livingAttacker.getMainHandItem();
        boolean melee = isMeleeWeapon(weapon);
        if (!melee) {
            weapon = livingAttacker.getOffhandItem();
            melee = isMeleeWeapon(weapon);
        }
        if (!melee || blocked || mobWeaponGuarding) {
            return;
        }

        level.playSound(
            null,
            hitPos.x, hitPos.y, hitPos.z,
            SoundRegistry.MELEE_WEAPON_HIT,
            SoundSource.PLAYERS,
            0.8F,
            0.9F + level.random.nextFloat() * 0.2F
        );
    }

    private boolean isMeleeWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item instanceof SwordItem || item instanceof AxeItem || item instanceof HoeItem || item instanceof PickaxeItem || item instanceof ShovelItem) return true;
        if (item instanceof TieredItem) return true;
        return false;
    }

    @org.spongepowered.asm.mixin.Unique
    private void applyStealthCritEffects(ServerLevel level, LivingEntity self, Vec3 hitPos, Vec3 attackDirection, float damage, boolean allowBlood, boolean playSound) {
        try {
            if (level != null) {
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, hitPos.x, hitPos.y + 0.8D, hitPos.z, 24, 0.45D, 0.35D, 0.45D, 0.06D);
                if (playSound) {
                    level.playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundRegistry.BACKSTAB, SoundSource.PLAYERS, 1.0F, 0.95F + level.random.nextFloat() * 0.1F);
                }
            }
        } catch (Throwable ignored) {}

        if (allowBlood) {
            try {
                spawnBloodSpray(level, hitPos, attackDirection, damage);
                float currentHealth = self.getHealth();
                if (damage >= currentHealth) {
                    int duration = Math.min(6, 1 + (int)Math.ceil(damage * 1.0));
                    BloodSprayManager.startSpray(level, hitPos, duration, damage);
                }
            } catch (Throwable ignored) {}
        }
    }

    @org.spongepowered.asm.mixin.Unique
    private static void sengoku$skipUpcomingReturnEffects(int count) {
        if (count <= 0) {
            return;
        }
        sengoku$skipReturnEffectsCount.set(sengoku$skipReturnEffectsCount.get() + count);
    }

    @org.spongepowered.asm.mixin.Unique
    private static boolean sengoku$consumeSkippedReturnEffect() {
        int remaining = sengoku$skipReturnEffectsCount.get();
        if (remaining <= 0) {
            return false;
        }
        remaining--;
        if (remaining <= 0) {
            sengoku$skipReturnEffectsCount.remove();
        } else {
            sengoku$skipReturnEffectsCount.set(remaining);
        }
        return true;
    }

    @org.spongepowered.asm.mixin.Unique
    private static Vec3 sengoku$resolveAttackDirection(ServerLevel level, Entity attacker) {
        if (attacker instanceof LivingEntity livingAttacker) {
            return livingAttacker.getLookAngle();
        }

        double rx = level.random.nextDouble() - 0.5;
        double ry = 0.2 + level.random.nextDouble() * 0.6;
        double rz = level.random.nextDouble() - 0.5;
        return new Vec3(rx, ry, rz).normalize();
    }

    @org.spongepowered.asm.mixin.Unique
    private boolean sengoku$shouldAllowBlood(LivingEntity self, boolean mobWeaponGuarding, boolean blocked) {
        boolean allowBlood = true;
        if (self instanceof Skeleton || self instanceof WitherSkeleton || self instanceof SkeletonHorse || self instanceof IronGolem) {
            allowBlood = false;
        }
        try {
            if (self.getClass().getName().equals("com.shioh.sengoku.entity.GoryoEntity") || self instanceof com.shioh.sengoku.entity.GoryoEntity) {
                allowBlood = false;
            }
            if (self.getClass().getName().equals("com.shioh.sengoku.entity.KojinEntity") || self instanceof com.shioh.sengoku.entity.KojinEntity) {
                allowBlood = false;
            }
            if (self.getClass().getName().equals("com.shioh.sengoku.entity.NingyoEntity") || self instanceof com.shioh.sengoku.entity.NingyoEntity) {
                allowBlood = false;
            }
            if (self.getClass().getName().equals("com.shioh.sengoku.entity.KamiikeHimeEntity") || self instanceof com.shioh.sengoku.entity.KamiikeHimeEntity) {
                allowBlood = false;
            }
            if (self.getClass().getName().equals("com.shioh.sengoku.entity.AkugyoEntity") || self instanceof com.shioh.sengoku.entity.AkugyoEntity) {
                allowBlood = false;
            }
        } catch (Throwable ignored) {}

        return allowBlood && !blocked && !mobWeaponGuarding;
    }

    private void spawnBloodSpray(ServerLevel level, Vec3 hitPos, Vec3 attackDirection, float damage) {
        int fastCount = Math.min(400, Math.max(12, (int)Math.ceil(damage * 12.0)));
        double fastPosSpread = 0.02D;
        double fastSpeed = 0.25 + Math.min(2.0, damage * 0.18);
        level.sendParticles(ParticleRegistry.BLOOD_PARTICLE, hitPos.x, hitPos.y, hitPos.z, fastCount, fastPosSpread, fastPosSpread, fastPosSpread, fastSpeed);

        int slowCount = Math.min(120, Math.max(6, (int)Math.ceil(damage * 3.5)));
        double slowPosSpread = 0.03D;
        double slowSpeed = 0.08 + Math.min(0.8, damage * 0.06);
        level.sendParticles(ParticleRegistry.BLOOD_PARTICLE, hitPos.x, hitPos.y, hitPos.z, slowCount, slowPosSpread, slowPosSpread, slowPosSpread, slowSpeed);

        int splatterCount = Math.min(40, Math.max(2, (int)Math.ceil(damage * 1.2)));
        if (splatterCount > 0) {
            level.sendParticles(ParticleRegistry.BLOOD_PARTICLE, hitPos.x, hitPos.y - 0.1, hitPos.z, splatterCount, 0.12D, 0.02D, 0.12D, 0.01D);
        }
    }
}
