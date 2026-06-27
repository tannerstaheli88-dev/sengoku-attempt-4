package com.shioh.sengoku.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
 

import com.shioh.sengoku.system.PostureComponent;
import com.shioh.sengoku.system.PostureHandler;
import com.shioh.sengoku.system.WeaponPostureStats;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.shioh.sengoku.entity.UmiNyoboEntity;

import java.util.EnumSet;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * AI Goal that makes samurai/ronin block incoming attacks with their weapons.
 * Similar to how players can block with swords, this creates a defensive stance
 * when the target is preparing to attack.
 */
public class WeaponBlockGoal extends Goal {
    private final Mob mob;
    private LivingEntity target;
    
    // Block duration handling: allow effectively continuous blocking while conditions hold
    private int blockDuration = 0;
    private static final int MAX_BLOCK_DURATION = Integer.MAX_VALUE; // effectively unlimited while conditions hold
    private static final int BLOCK_COOLDOWN = 6; // small cooldown to avoid instant re-block spam after manual stops
    private int blockCooldown = 0;
    
    private static final double BLOCK_RANGE = 4.0D; // Block when target is within this range
    
    private static final Map<Mob, Long> DISABLED_BLOCKING_EXPIRY = new WeakHashMap<>();
    private static final Map<Mob, Long> BLOCK_FATIGUE_EXPIRY = new WeakHashMap<>();
    private static final Map<Mob, Integer> CONSECUTIVE_BLOCKS = new WeakHashMap<>();
    private static final Map<Mob, PostureComponent> MOB_POSTURE = new WeakHashMap<>();
    private static final Map<Mob, Long> RECENT_POISE_BREAK = new WeakHashMap<>();
    private static final Map<Mob, Long> STUNNED_MOBS = new WeakHashMap<>();
    private static final int POSTURE_DAMAGE_PER_BLOCK = 4;
    private static final float POSTURE_DAMAGE_MULTIPLIER = 1.1f;
    private static final int MOB_POSTURE_BREAK_DISABLE = 100;
    private static final int MOB_POSTURE_BREAK_STUN = 40;
    private static final float UNBREAKING_POSTURE_BONUS = 0.12F;
    private static final ResourceLocation BROKEN_POISE_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("sengoku", "broken_poise_speed");
    private static final double BROKEN_POISE_SPEED_REDUCTION = -0.85D;

    // Damage threshold above which the heavy block particle is shown
    private static final float HEAVY_HIT_THRESHOLD = 6.0F;

    public WeaponBlockGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }
    
    @Override
    public boolean canUse() {
        if (this.mob.isPassenger()) return false;
        if (isBlockingDisabled(this.mob)) return false;

        if (blockCooldown > 0) {
            blockCooldown--;
            return false;
        }
        
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target instanceof net.minecraft.world.entity.npc.Villager || target instanceof net.minecraft.world.entity.npc.WanderingTrader) return false;
        
        if (target instanceof Mob && isCurrentlyBlocking((Mob) target)) {
            return false;
        }
        
        ItemStack weapon = this.mob.getMainHandItem();
        if (!isBlockableWeapon(weapon)) {
            return false;
        }
        
        double distSq = this.mob.distanceToSqr(target);
        if (distSq > BLOCK_RANGE * BLOCK_RANGE) {
            return false;
        }

        double dist = Math.sqrt(distSq);
        if (dist < 1.6D) {
            return false;
        }
        if (dist <= 2.2D && this.mob.getRandom().nextFloat() < 0.35F * blockChanceMultiplier(this.mob)) {
            return false;
        }
        
        if (target instanceof Player player) {
            Vec3 playerLook = player.getLookAngle();
            Vec3 toMob = this.mob.position().subtract(player.position()).normalize();
            double dot = playerLook.dot(toMob);
            
            if (dot > 0.5D) {
                float blockChance = distSq < 9.0D ? 0.99F : 0.98F;
                return this.mob.getRandom().nextFloat() < blockChance * blockChanceMultiplier(this.mob);
            }
        }
        
        if (distSq < (BLOCK_RANGE * 0.5) * (BLOCK_RANGE * 0.5)) {
            return this.mob.getRandom().nextFloat() < 0.92F * blockChanceMultiplier(this.mob);
        }
        PostureComponent pcCheck = getOrCreatePosture(this.mob);
        if (pcCheck.isBroken()) return false;
        
        return false;
    }
    
    @Override
    public boolean canContinueToUse() {
        if (this.mob.isPassenger()) return false;
        if (this.target == null || !this.target.isAlive()) return false;
        if (isBlockingDisabled(this.mob)) return false;
        if (isPoiseBroken(this.mob)) return false;
        if (!this.mob.isUsingItem()) return false;
        if (this.target instanceof net.minecraft.world.entity.npc.Villager || this.target instanceof net.minecraft.world.entity.npc.WanderingTrader) return false;
        return this.mob.distanceToSqr(this.target) <= BLOCK_RANGE * BLOCK_RANGE;
    }
    
    @Override
    public void start() {
        if (this.mob.isPassenger()) return;
        this.target = this.mob.getTarget();
        this.blockDuration = 0;
        this.mob.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
        try {
            if (isBlockableWeapon(this.mob.getMainHandItem())) {
                setIllagerWeaponBlocking(this.mob, true);
            }
        } catch (Throwable ignored) {}
    }
    
    @Override
    public void stop() {
        this.mob.stopUsingItem();
        this.target = null;
        this.blockCooldown = BLOCK_COOLDOWN;
        this.blockDuration = 0;
        try {
            if (isBlockableWeapon(this.mob.getMainHandItem())) {
                setIllagerWeaponBlocking(this.mob, false);
            }
        } catch (Throwable ignored) {}
    }
    
    @Override
    public void tick() {
        if (this.mob.isPassenger()) { stop(); return; }
        if (this.target == null) return;
        if (this.target instanceof net.minecraft.world.entity.npc.Villager || this.target instanceof net.minecraft.world.entity.npc.WanderingTrader) { stop(); return; }
        this.blockDuration++;

        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        
        double distSq = this.mob.distanceToSqr(this.target);
        if (distSq > (BLOCK_RANGE * BLOCK_RANGE * 0.6)) {
            try {
                this.mob.getNavigation().moveTo(this.target, 0.45D);
            } catch (Throwable ignored) {}
        }
        
        if (distSq < 4.0D) {
            if (this.mob.getRandom().nextFloat() < 0.8F) {
                this.mob.getMoveControl().strafe(-0.8F, 0.0F);
            }
            if (this.mob.getRandom().nextFloat() < 0.12F) {
                float side = this.mob.getRandom().nextBoolean() ? 0.45F : -0.45F;
                this.mob.getMoveControl().strafe(-0.6F, side);
            }
        } else if (distSq < 9.0D) {
            if (this.mob.getRandom().nextFloat() < 0.3F) {
                float side = this.mob.getRandom().nextBoolean() ? 0.25F : -0.25F;
                this.mob.getMoveControl().strafe(-0.45F, side);
            }
        }

        final int AFK_BREAK_THRESHOLD = 200;
        if (this.blockDuration > AFK_BREAK_THRESHOLD) {
            if (this.blockDuration % 20 == 0) {
                float p = 0.5F;
                if (this.mob.getRandom().nextFloat() < p) {
                    try {
                        disableBlockingFor(this.mob, 6);
                        if (this.target != null) this.mob.setTarget(this.target);
                    } catch (Throwable ignored) {}
                } else {
                    if (this.mob.level() != null) BLOCK_FATIGUE_EXPIRY.put(this.mob, this.mob.level().getGameTime() + 60L);
                }
            }
        }
    }
    
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
    
    private boolean isBlockableWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.getItem() instanceof SwordItem || 
               stack.getItem() instanceof TridentItem ||
               stack.getItem().toString().contains("sword") ||
               stack.getItem().toString().contains("yari") ||
               stack.getItem().toString().contains("kanabo") ||
               stack.getItem().toString().contains("tetsubo") ||
               stack.getItem().toString().contains("odachi") ||
               stack.getItem().toString().contains("katana") ||
               stack.getItem().toString().contains("blade");
    }
    
    public boolean isBlocking() {
        return this.canContinueToUse() && this.blockDuration < MAX_BLOCK_DURATION;
    }
    
    public static boolean isCurrentlyBlocking(Mob mob) {
        if (mob == null) return false;
        if (!mob.isUsingItem()) return false;
        if (isBlockingDisabled(mob)) return false;
        if (isPoiseBroken(mob)) return false;
        try {
            LivingEntity target = mob.getTarget();
            if (target == null || !target.isAlive()) return false;
            if (mob.distanceToSqr(target) > BLOCK_RANGE * BLOCK_RANGE) return false;

            ItemStack stack = mob.getMainHandItem();
            if (stack == null || stack.isEmpty()) return false;
            String itemId = stack.getItem().toString().toLowerCase();
            return stack.getItem() instanceof net.minecraft.world.item.SwordItem ||
                   stack.getItem() instanceof net.minecraft.world.item.TridentItem ||
                   itemId.contains("sword") ||
                   itemId.contains("yari") ||
                   itemId.contains("tetsubo") ||
                   itemId.contains("kanabo") ||
                   itemId.contains("katana") ||
                   itemId.contains("blade");
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void disableBlockingFor(Mob mob, int ticks) {
        if (mob == null || ticks <= 0) return;
        try {
            if (mob.level() != null) {
                long expiry = mob.level().getGameTime() + (long) ticks;
                DISABLED_BLOCKING_EXPIRY.put(mob, expiry);
                try { mob.stopUsingItem(); } catch (Throwable ignored) {}
                try { setIllagerWeaponBlocking(mob, false); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
    }

    private static void setIllagerWeaponBlocking(Mob mob, boolean v) {
        if (mob == null) return;
        try {
            Class<?> cls = mob.getClass();
            try {
                java.lang.reflect.Method m = cls.getMethod("sengoku$setWeaponBlocking", boolean.class);
                m.setAccessible(true);
                m.invoke(mob, v);
                return;
            } catch (NoSuchMethodException ignored) {}
            try {
                java.lang.reflect.Method m2 = cls.getMethod("setWeaponBlocking", boolean.class);
                m2.setAccessible(true);
                m2.invoke(mob, v);
                return;
            } catch (NoSuchMethodException ignored2) {}
            Class<?> sc = net.minecraft.world.entity.monster.AbstractIllager.class;
            try {
                java.lang.reflect.Method ms = sc.getMethod("sengoku$setWeaponBlocking", boolean.class);
                ms.setAccessible(true);
                ms.invoke(mob, v);
                return;
            } catch (NoSuchMethodException ignored3) {}
        } catch (Throwable ignored) {}
    }

    public static void onSuccessfulBlock(Mob mob, LivingEntity attacker) {
        if (mob == null) return;
        int consecutiveBlocks = CONSECUTIVE_BLOCKS.getOrDefault(mob, 0) + 1;
        CONSECUTIVE_BLOCKS.put(mob, consecutiveBlocks);

        PostureComponent pc = getOrCreatePosture(mob);
        try {
            int scaled = POSTURE_DAMAGE_PER_BLOCK;
            if (attacker instanceof LivingEntity livingAttacker) {
                ItemStack weapon = livingAttacker.getMainHandItem();
                float tierMult = computeWeaponTierMultiplier(weapon);
                int unbreaking = 0;
                int est = 0;
                try { est = estimateWeaponDamage(livingAttacker); } catch (Throwable ignored) {}
                if (est <= 0) {
                    try {
                        double atk = livingAttacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        est = Math.max(1, (int)Math.round(atk * POSTURE_DAMAGE_MULTIPLIER));
                    } catch (Throwable ignored) {}
                }
                scaled = Math.max(scaled, est);
                float unbreakingBonus = 1.0F + (UNBREAKING_POSTURE_BONUS * unbreaking);
                scaled = Math.max(1, Math.round(scaled * tierMult * unbreakingBonus));
                scaled = Math.max(1, Math.round(scaled * PostureHandler.getAttackerPostureDamageMultiplier(livingAttacker)));
            }
            pc.damage(scaled);
        } catch (Throwable ignored) {}
            
        try {
            if (pc.isBroken()) {
                triggerPoiseBreak(mob);
            } else {
                float counterChance = 0.40F;
                int disableTicksMin = 6;
                int disableTicksRange = 3;
                if (mob instanceof com.shioh.sengoku.entity.WarlordEntity) {
                    counterChance = Math.min(0.95F, 0.7F + Math.max(0, consecutiveBlocks - 1) * 0.15F);
                    disableTicksMin = 10;
                    disableTicksRange = 5;
                }
                if (mob.getRandom().nextFloat() < counterChance) {
                    disableBlockingFor(mob, mob.getRandom().nextInt(disableTicksRange) + disableTicksMin);
                    if (attacker != null) mob.setTarget(attacker);
                    try { mob.setAggressive(true); } catch (Throwable ignored) {}
                    CONSECUTIVE_BLOCKS.remove(mob);
                } else {
                    if (mob.level() != null) {
                        BLOCK_FATIGUE_EXPIRY.put(mob, mob.level().getGameTime() + 15L);
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Spawn block feedback particles.
     * BLOCK_PARTICLE only plays for heavy hits (amount >= 6); POSTURE_SPARK always plays.
     */
    public static void spawnBlockFeedbackParticles(LivingEntity blocker, float amount) {
    if (blocker == null || blocker.level().isClientSide || !(blocker.level() instanceof ServerLevel serverLevel)) {
        return;
    }
    if (amount >= HEAVY_HIT_THRESHOLD) {
        try {
            serverLevel.sendParticles(
                com.shioh.sengoku.registry.ParticleRegistry.BLOCK_PARTICLE,
                blocker.getX(), blocker.getY() + blocker.getEyeHeight() * 0.5D, blocker.getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D
            );
        } catch (Throwable ignored) {}
    }
    try {
        serverLevel.sendParticles(
            com.shioh.sengoku.registry.ParticleRegistry.POSTURE_SPARK,
            blocker.getX(), blocker.getY() + 1.5D, blocker.getZ(),
            8, 0.3D, 0.3D, 0.3D, 0.1D
        );
    } catch (Throwable ignored) {}
}

public static void spawnBlockFeedbackParticles(LivingEntity blocker) {
    spawnBlockFeedbackParticles(blocker, 6.0F);
}

    private static PostureComponent getOrCreatePosture(Mob mob) {
        if (mob == null) return new PostureComponent(mob, 20);
        int desiredMaxPosture = WeaponPostureStats.getMobMaxPosture(
            mob.getMainHandItem(),
            (float) mob.getAttributeValue(Attributes.MAX_HEALTH)
        );
        try { desiredMaxPosture = Math.round(desiredMaxPosture * MOB_POSTURE_BUFF); } catch (Throwable ignored) {}
        try {
            if (mob instanceof UmiNyoboEntity) {
                desiredMaxPosture = Math.max(desiredMaxPosture, Math.min(64, Math.round(desiredMaxPosture * 1.6F)));
            }
        } catch (Throwable ignored) {}
        try {
            if (mob instanceof com.shioh.sengoku.entity.WarlordEntity) {
                desiredMaxPosture = Math.max(48, Math.min(80, Math.round(desiredMaxPosture * 2.0F)));
            }
        } catch (Throwable ignored) {}

        PostureComponent pc = MOB_POSTURE.get(mob);
        if (pc == null) {
            pc = new PostureComponent(mob, desiredMaxPosture);
            MOB_POSTURE.put(mob, pc);
        } else {
            pc.setMaxPosture(desiredMaxPosture);
        }
        return pc;
    }

    public static boolean applyDirectPostureDamage(Mob mob, int amount, LivingEntity attacker) {
        if (mob == null || amount <= 0) return false;

        PostureComponent posture = getOrCreatePosture(mob);
        if (posture.isBroken()) return true;

        try { posture.damage(amount); } catch (Throwable ignored) {}

        if (posture.isBroken()) {
            triggerPoiseBreak(mob);
            return true;
        }

        return false;
    }

    public static boolean consumeBrokenPoisePunish(Mob mob) {
        if (mob == null) return false;

        PostureComponent posture = MOB_POSTURE.get(mob);
        if (posture == null || !posture.isBroken()) return false;

        try { posture.restoreToFull(); } catch (Throwable ignored) {}
        STUNNED_MOBS.remove(mob);
        RECENT_POISE_BREAK.remove(mob);
        DISABLED_BLOCKING_EXPIRY.remove(mob);
        CONSECUTIVE_BLOCKS.remove(mob);
        syncBrokenPoiseMovementModifier(mob, false);
        return true;
    }

    private static int estimateWeaponDamage(LivingEntity attacker) {
        if (attacker == null) return 0;
        try {
            ItemStack stack = attacker.getMainHandItem();
            if (stack == null || stack.isEmpty()) return 0;
            String id = stack.getItem().toString().toLowerCase();
            if (id.contains("wooden_sword") || id.contains("wood_sword")) return 4;
            if (id.contains("stone_sword")) return 5;
            if (id.contains("iron_sword")) return 6;
            if (id.contains("diamond_sword")) return 7;
            if (id.contains("netherite_sword")) return 8;
            try {
                double atk = attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                return Math.max(1, (int) Math.round(atk * POSTURE_DAMAGE_MULTIPLIER));
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
        return 0;
    }

    private static float computeWeaponTierMultiplier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 1.0F;
        String id;
        try { id = stack.getItem().toString().toLowerCase(); } catch (Throwable e) { return 1.0F; }
        if (id.contains("wood") || id.contains("wooden")) return 0.8F;
        if (id.contains("gold")) return 0.85F;
        if (id.contains("stone")) return 1.0F;
        if (id.contains("iron")) return 1.15F;
        if (id.contains("diamond")) return 1.30F;
        if (id.contains("netherite")) return 1.40F;
        if (id.contains("katana") || id.contains("blade")) return 1.20F;
        return 1.0F;
    }

    public static void resetConsecutiveBlocks(Mob mob) {
        if (mob == null) return;
        CONSECUTIVE_BLOCKS.remove(mob);
    }

    private static final float MOB_POSTURE_BUFF = 1.35F;

    public static void markRecentPoiseBreak(Mob mob, int ticks) {
        if (mob == null || ticks <= 0) return;
        try {
            if (mob.level() != null) {
                RECENT_POISE_BREAK.put(mob, mob.level().getGameTime() + ticks);
            }
        } catch (Throwable ignored) {}
    }

    public static boolean isRecentPoiseBreak(Mob mob) {
        if (mob == null) return false;
        Long v = RECENT_POISE_BREAK.get(mob);
        if (v == null) return false;
        try {
            long now = mob.level() != null ? mob.level().getGameTime() : 0L;
            if (now > v) { RECENT_POISE_BREAK.remove(mob); return false; }
            return true;
        } catch (Throwable ignored) { return true; }
    }

    public static boolean isStunned(Mob mob) {
        if (mob == null) return false;
        Long expiry = STUNNED_MOBS.get(mob);
        if (expiry == null) return false;
        try {
            long now = mob.level() != null ? mob.level().getGameTime() : 0L;
            if (now > expiry) {
                STUNNED_MOBS.remove(mob);
                return false;
            }
            return true;
        } catch (Throwable ignored) {
            return true;
        }
    }

    private static void triggerPoiseBreak(Mob mob) {
        try {
            CONSECUTIVE_BLOCKS.remove(mob);
            disableBlockingFor(mob, MOB_POSTURE_BREAK_DISABLE);
            markRecentPoiseBreak(mob, MOB_POSTURE_BREAK_DISABLE);
            if (mob.level() != null) {
                STUNNED_MOBS.put(mob, mob.level().getGameTime() + (long) MOB_POSTURE_BREAK_STUN);
            }
        } catch (Throwable ignored) {}

        try {
            if (mob.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD, mob.getX(), mob.getY() + 1.0, mob.getZ(), 12, 0.5, 0.8, 0.5, 0.05);
            }
        } catch (Throwable ignored) {}

        try {
            mob.setAggressive(false);
            mob.setTarget(null);
            mob.getNavigation().stop();
            mob.stopUsingItem();
            setIllagerWeaponBlocking(mob, false);
        } catch (Throwable ignored) {}

        if (mob.level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, mob.blockPosition(), SoundRegistry.POSTURE_BREAK, net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, mob.getX(), mob.getY() + 1.0, mob.getZ(), 15, 0.8, 0.8, 0.8, 0.2);
        } else {
            try {
                mob.level().playSound(null, mob.blockPosition(), SoundRegistry.POSTURE_BREAK, net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.0f);
            } catch (Throwable ignored) {}
        }
    }

    public static boolean isPoiseBroken(Mob mob) {
        if (mob == null) return false;
        PostureComponent posture = MOB_POSTURE.get(mob);
        return posture != null && posture.isBroken();
    }

    public static void tickMobCombatState(Mob mob) {
        if (mob == null) return;

        decrementDisableCounter(mob);

        PostureComponent posture = MOB_POSTURE.get(mob);
        if (posture != null) {
            try { posture.tick(); } catch (Throwable ignored) {}
        }

        boolean broken = posture != null && posture.isBroken();
        if (broken) enforceBrokenPoiseState(mob);
        syncBrokenPoiseMovementModifier(mob, broken);

        try {
            if (broken && mob.level() instanceof ServerLevel serverLevel) {
                long t = mob.level().getGameTime();
                if (t % 6L == 0L) {
                    float r = 1.0F, g = 1.0F, b = 1.0F;
                    serverLevel.sendParticles(
                        ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r, g, b),
                        mob.getX(), mob.getY() + mob.getBbHeight() * 0.75D, mob.getZ(),
                        3, mob.getBbWidth() * 0.35D, 0.12D, mob.getBbWidth() * 0.35D, 0.01D
                    );
                    try {
                        serverLevel.sendParticles(com.shioh.sengoku.registry.ParticleRegistry.POSTURE_SPARK,
                            mob.getX(), mob.getY() + mob.getBbHeight() * 0.75D, mob.getZ(),
                            1, mob.getBbWidth() * 0.25D, 0.08D, mob.getBbWidth() * 0.25D, 0.02D);
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
    }

    private static boolean isBlockingDisabled(Mob mob) {
        if (mob == null) return false;
        Long expiry = DISABLED_BLOCKING_EXPIRY.get(mob);
        if (expiry == null) return false;
        try {
            long now = mob.level() != null ? mob.level().getGameTime() : 0L;
            if (now > expiry) {
                DISABLED_BLOCKING_EXPIRY.remove(mob);
                return false;
            }
            return true;
        } catch (Throwable ignored) {
            return expiry != null && expiry > 0L;
        }
    }

    private static void decrementDisableCounter(Mob mob) {
        if (mob == null) return;
        long now = mob.level() != null ? mob.level().getGameTime() : 0L;
        Long d = DISABLED_BLOCKING_EXPIRY.get(mob);
        if (d != null && now > d) DISABLED_BLOCKING_EXPIRY.remove(mob);
        Long f = BLOCK_FATIGUE_EXPIRY.get(mob);
        if (f != null && now > f) BLOCK_FATIGUE_EXPIRY.remove(mob);
        Long r = RECENT_POISE_BREAK.get(mob);
        if (r != null && now > r) RECENT_POISE_BREAK.remove(mob);
    }

    private static float blockChanceMultiplier(Mob mob) {
        if (mob == null) return 1.0F;
        Long f = BLOCK_FATIGUE_EXPIRY.get(mob);
        if (f == null) return 1.0F;
        try {
            long now = mob.level() != null ? mob.level().getGameTime() : 0L;
            if (now > f) {
                BLOCK_FATIGUE_EXPIRY.remove(mob);
                return 1.0F;
            }
            return 0.75F;
        } catch (Throwable ignored) {
            return 0.75F;
        }
    }

    private static void enforceBrokenPoiseState(Mob mob) {
        try { mob.stopUsingItem(); } catch (Throwable ignored) {}
        try { setIllagerWeaponBlocking(mob, false); } catch (Throwable ignored) {}
        try { mob.setAggressive(false); } catch (Throwable ignored) {}
        try { mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 4, false, false, true)); } catch (Throwable ignored) {}
    }

    private static void syncBrokenPoiseMovementModifier(Mob mob, boolean broken) {
        try {
            AttributeInstance movementSpeed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeed == null) return;

            AttributeModifier existingModifier = movementSpeed.getModifier(BROKEN_POISE_SPEED_MODIFIER_ID);
            if (broken) {
                if (existingModifier == null) {
                    movementSpeed.addTransientModifier(new AttributeModifier(
                        BROKEN_POISE_SPEED_MODIFIER_ID,
                        BROKEN_POISE_SPEED_REDUCTION,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ));
                }
            } else if (existingModifier != null) {
                movementSpeed.removeModifier(BROKEN_POISE_SPEED_MODIFIER_ID);
            }
        } catch (Throwable ignored) {}
    }
}