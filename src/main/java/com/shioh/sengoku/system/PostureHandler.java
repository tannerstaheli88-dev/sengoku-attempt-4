package com.shioh.sengoku.system;

import com.shioh.sengoku.item.KanaboItem;
import com.shioh.sengoku.item.OdachiItem;
import com.shioh.sengoku.item.TantoItem;
import com.shioh.sengoku.item.TetsuboItem;
import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import com.shioh.sengoku.registry.ParticleRegistry;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.core.particles.ParticleTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostureHandler {
    private static final float PERFECT_PARRY_REDIRECT_MULTIPLIER = 0.30f;
    private static final float PERFECT_PARRY_DIAMOND_AND_ABOVE_RESISTANCE = 1.00f;
    private static final float PERFECT_PARRY_IRON_RESISTANCE = 0.70f;
    private static final float PERFECT_PARRY_STONE_AND_GOLD_RESISTANCE = 0.40f;
    private static final float PERFECT_PARRY_WOOD_RESISTANCE = 0.20f;

    private static final int GUARD_BREAK_COOLDOWN_TICKS = 60; // 3 seconds (20 ticks * 3)
    private static final int GUARD_BREAK_SLOW_TICKS = 24;
    private static final float SWORD_POSTURE_DAMAGE_MULTIPLIER = 0.85f;
    private static final float TANTO_POSTURE_DAMAGE_MULTIPLIER = 0.70f;
    private static final float YARI_POSTURE_DAMAGE_MULTIPLIER = 0.80f;
    private static final float TETSUBO_POSTURE_DAMAGE_MULTIPLIER = 1.3f;
    private static final float KANABO_POSTURE_DAMAGE_MULTIPLIER = 1.8f;
    private static final float ODACHI_POSTURE_DAMAGE_MULTIPLIER = 1.8f;
    private static final ResourceLocation BROKEN_GUARD_SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("sengoku", "broken_guard_speed");
    private static final double BROKEN_GUARD_SPEED_REDUCTION = -0.20D;

    // Track posture per player (UUID → current posture value)
    private static final Map<UUID, Float> postureMap = new HashMap<>();
    // Track which players are guard-broken and until when (UUID → expiry tick)
    private static final Map<UUID, Long> guardBrokenPlayers = new HashMap<>();
    // Track a brief slow window after a player is guard-broken.
    private static final Map<UUID, Long> guardBrokenSlowPlayers = new HashMap<>();

    /**
     * Apply posture damage when blocking.
     * Also damages the weapon durability based on incoming damage.
     * @return true if guard is broken
     */
    public static boolean applyPostureDamage(Player player, float damage, LivingEntity attacker) {
        return applyRawPostureDamage(player, computeIncomingPostureDamage(damage, attacker), attacker, true, false);
    }

    public static void redirectPerfectParryPostureDamage(LivingEntity attacker, float damage, LivingEntity defender) {
        float redirectedDamage = computeIncomingPostureDamage(damage, attacker) * PERFECT_PARRY_REDIRECT_MULTIPLIER;
        if (redirectedDamage <= 0.0F || attacker == null) {
            return;
        }

        if (attacker instanceof Player playerAttacker) {
            applyRawPostureDamage(playerAttacker, redirectedDamage, defender, false, true);
            return;
        }

        if (attacker instanceof Mob mobAttacker) {
            WeaponBlockGoal.applyDirectPostureDamage(mobAttacker, Math.max(1, Math.round(redirectedDamage)), defender);
        }
    }

    public static boolean applyPerfectParryPostureDamage(Player player, float damage, LivingEntity attacker) {
        if (player == null) {
            return false;
        }

        float incomingPostureDamage = computeIncomingPostureDamage(damage, attacker);
        if (incomingPostureDamage <= 0.0F) {
            return false;
        }

        float resistance = getPerfectParryPostureResistance(getPostureWeapon(player));
        float reducedPostureDamage = incomingPostureDamage * (1.0F - resistance);
        if (reducedPostureDamage <= 0.0F) {
            return false;
        }

        return applyRawPostureDamage(player, reducedPostureDamage, attacker, false, true);
    }
public static boolean applyPartialParryPostureDamage(Player player, float damage, LivingEntity attacker) {
    return applyRawPostureDamage(player, computeIncomingPostureDamage(damage, attacker), attacker, true, true);
}
    public static int getPerfectParryWindowTicks(ItemStack stack, int baseWindow) {
        int clampedBaseWindow = Math.max(1, baseWindow);
        PerfectParryTier tier = classifyPerfectParryTier(stack);
        return switch (tier) {
            case DIAMOND_AND_ABOVE -> clampedBaseWindow;
            case IRON -> Math.max(1, clampedBaseWindow - 1);
            case STONE_OR_GOLD -> Math.max(1, clampedBaseWindow - 1);
            case WOOD -> Math.max(1, clampedBaseWindow - 1);
            case UNKNOWN -> clampedBaseWindow;
        };
    }

    public static float computeIncomingPostureDamage(float damage, LivingEntity attacker) {
        return damage * getAttackerPostureDamageMultiplier(attacker);
    }

    private static boolean applyRawPostureDamage(Player player, float postureDamage, LivingEntity attacker, boolean damageBlockingItem, boolean isParry) {
        if (player == null) return false;

        UUID id = player.getUUID();
        float maxPosture = getMaxPostureValue(player);

        // If guard is already broken, don’t apply posture
        if (isGuardBroken(player)) {
            return false;
        }

        float current = Math.min(postureMap.getOrDefault(id, 0f), maxPosture);
        float newValue = current + postureDamage;
        postureMap.put(id, Math.min(newValue, maxPosture));

        // Get the posture-bearing weapon to check if it's a weapon or shield
        ItemStack blockingItem = getPostureWeapon(player);
        boolean isWeaponBlock = blockingItem.getItem() instanceof net.minecraft.world.item.SwordItem 
                             || blockingItem.getItem() instanceof net.minecraft.world.item.AxeItem
                             || blockingItem.getItem() instanceof com.shioh.sengoku.item.SweeplessItem;

        // ✨ Sparks on posture hit (only for weapons, not shields)
        if (isWeaponBlock && player.level() != null && !player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Show posture sparks when blocking with weapons
            serverPlayer.serverLevel().sendParticles(
                    ParticleRegistry.POSTURE_SPARK,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    8,
                    0.3, 0.3, 0.3,
                    0.1
            );

            // ✅ Block flash particle (single big particle in front of player like sweep attack)
            // Calculate position in front of player's eyes
            double lookX = -Math.sin(Math.toRadians(player.getYRot())) * Math.cos(Math.toRadians(player.getXRot()));
            double lookY = -Math.sin(Math.toRadians(player.getXRot()));
            double lookZ = Math.cos(Math.toRadians(player.getYRot())) * Math.cos(Math.toRadians(player.getXRot()));
            
if (isParry) {
    serverPlayer.serverLevel().sendParticles(
            ParticleRegistry.BLOCK_PARTICLE,
            player.getX() + lookX * 1.5, player.getEyeY() + lookY * 1.5, player.getZ() + lookZ * 1.5,
            1,
            0.0, 0.0, 0.0,
            0.0
    );
}
        }

        // ✅ Damage the blocking weapon durability (like shields)
        if (damageBlockingItem && !blockingItem.isEmpty()) {
            int durabilityLoss = 1 + Mth.floor(postureDamage * 0.35f);
            durabilityLoss = Math.min(durabilityLoss, 2);
            blockingItem.hurtAndBreak(durabilityLoss, player, EquipmentSlot.MAINHAND);
        }

        if (newValue >= maxPosture) {
            // Mark as guard broken (using game ticks instead of system millis)
            guardBrokenPlayers.put(id, player.level().getGameTime() + GUARD_BREAK_COOLDOWN_TICKS);
            guardBrokenSlowPlayers.put(id, player.level().getGameTime() + GUARD_BREAK_SLOW_TICKS);
            postureMap.put(id, maxPosture);
            
            player.stopUsingItem();
            // GUI cooldown applied to the item in use
            if (!player.level().isClientSide && !blockingItem.isEmpty()) {
                player.getCooldowns().addCooldown(blockingItem.getItem(), GUARD_BREAK_COOLDOWN_TICKS);
            }

            // 🔊 Custom posture break sound
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundRegistry.POSTURE_BREAK,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    1.0f,
                    1.0f
            );

            // ✨ Burst of particles on guard break
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.serverLevel().sendParticles(
                        ParticleTypes.CRIT,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        16,
                        1.0, 1.0, 1.0,
                        0.2
                );
            }

            return true;
        }

        return false;
    }

    public static float getAttackerPostureDamageMultiplier(LivingEntity attacker) {
        if (attacker == null) {
            return 1.0f;
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) {
            return 1.0f;
        }

        if (weapon.getItem() instanceof TetsuboItem) {
            return TETSUBO_POSTURE_DAMAGE_MULTIPLIER;
        }
        if (weapon.getItem() instanceof KanaboItem) {
            return KANABO_POSTURE_DAMAGE_MULTIPLIER;
        }
        if (weapon.getItem() instanceof OdachiItem) {
            return ODACHI_POSTURE_DAMAGE_MULTIPLIER;
        }
        if (weapon.getItem() instanceof TantoItem) {
            return TANTO_POSTURE_DAMAGE_MULTIPLIER;
        }
        if (weapon.getItem() instanceof com.shioh.sengoku.item.YariItem) {
            return YARI_POSTURE_DAMAGE_MULTIPLIER;
        }

        // Vanilla mace and axes should also do bonus posture damage
        if (weapon.getItem() instanceof net.minecraft.world.item.MaceItem) {
            return KANABO_POSTURE_DAMAGE_MULTIPLIER;
        }

        if (weapon.getItem() instanceof net.minecraft.world.item.AxeItem) {
            return TETSUBO_POSTURE_DAMAGE_MULTIPLIER;
        }

        if (weapon.getItem() instanceof net.minecraft.world.item.SwordItem
                || weapon.getItem() instanceof com.shioh.sengoku.item.BasicWeaponItem) {
            return SWORD_POSTURE_DAMAGE_MULTIPLIER;
        }

        return 1.0f;
    }

    /**
     * Gradually recovers posture over time.
     * Call this from a player tick.
     */
    public static void tick(Player player) {
        if (player == null) return;

        float maxPosture = getMaxPostureValue(player);
        UUID id = player.getUUID();
        float current = postureMap.getOrDefault(id, 0f);
        if (current > maxPosture) {
            postureMap.put(id, maxPosture);
        }

        syncBrokenGuardMovementModifier(player);

        // regen only every 5 ticks (reduce workload by 80%)
        if (player.tickCount % 5 != 0) return;

        current = postureMap.getOrDefault(id, 0f);

        if (!isGuardBroken(player)) {
            boolean blocking = player.isBlocking();
            // Adjust recovery for 5-tick interval
            float recoveryRate = blocking ? 1f : 5f;

            if (current > 0) {
                float newValue = Math.max(0f, current - recoveryRate);
                postureMap.put(id, newValue);
            }
        }
    }

    /**
     * Check if the player's guard is currently broken.
     */
    public static boolean isGuardBroken(Player player) {
        if (player == null) return false;

        UUID id = player.getUUID();
        Long expiry = guardBrokenPlayers.get(id);

        if (expiry == null) return false;
        if (player.level().getGameTime() > expiry) {
            guardBrokenPlayers.remove(id);
            return false;
        }

        return true;
    }

    public static float getCurrentPosture(Player player) {
        if (player == null) return 0f;
        return Math.min(postureMap.getOrDefault(player.getUUID(), 0f), getMaxPostureValue(player));
    }

    public static float getMaxPostureValue(Player player) {
        if (player == null) {
            return WeaponPostureStats.getMaxPosture(ItemStack.EMPTY);
        }
        return WeaponPostureStats.getMaxPosture(getPostureWeapon(player));
    }

    /**
     * Reset a player's posture manually (e.g., on death or logout).
     */
    public static void resetPosture(Player player) {
        if (player != null) {
            postureMap.remove(player.getUUID());
            guardBrokenPlayers.remove(player.getUUID());
            guardBrokenSlowPlayers.remove(player.getUUID());
            syncBrokenGuardMovementModifier(player);
        }
    }

    private static ItemStack getPostureWeapon(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack useItem = player.getUseItem();
        if (isPostureWeapon(useItem)) {
            return useItem;
        }

        ItemStack mainHand = player.getMainHandItem();
        if (isPostureWeapon(mainHand)) {
            return mainHand;
        }

        return ItemStack.EMPTY;
    }

    private static boolean isPostureWeapon(ItemStack stack) {
        return stack != null && !stack.isEmpty() && (
            stack.getItem() instanceof net.minecraft.world.item.SwordItem
            || stack.getItem() instanceof net.minecraft.world.item.AxeItem
            || stack.getItem() instanceof net.minecraft.world.item.TridentItem
            || stack.getItem() instanceof net.minecraft.world.item.MaceItem
            || stack.getItem() instanceof com.shioh.sengoku.item.SweeplessItem
            || stack.getItem() instanceof com.shioh.sengoku.item.BasicWeaponItem
        );
    }

    private static float getPerfectParryPostureResistance(ItemStack stack) {
        PerfectParryTier tier = classifyPerfectParryTier(stack);
        return switch (tier) {
            case DIAMOND_AND_ABOVE -> PERFECT_PARRY_DIAMOND_AND_ABOVE_RESISTANCE;
            case IRON -> PERFECT_PARRY_IRON_RESISTANCE;
            case STONE_OR_GOLD -> PERFECT_PARRY_STONE_AND_GOLD_RESISTANCE;
            case WOOD -> PERFECT_PARRY_WOOD_RESISTANCE;
            case UNKNOWN -> 0.0F;
        };
    }

    private static PerfectParryTier classifyPerfectParryTier(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return PerfectParryTier.UNKNOWN;
        }

        if (!(stack.getItem() instanceof TieredItem tieredItem)) {
            return PerfectParryTier.DIAMOND_AND_ABOVE;
        }

        Tier tier = tieredItem.getTier();
        String tierName = tier == null ? "" : tier.toString().toLowerCase();

        if (tierName.contains("netherite") || tierName.contains("diamond")) {
            return PerfectParryTier.DIAMOND_AND_ABOVE;
        }
        if (tierName.contains("iron")) {
            return PerfectParryTier.IRON;
        }
        if (tierName.contains("gold")) {
            return PerfectParryTier.STONE_OR_GOLD;
        }
        if (tierName.contains("stone")) {
            return PerfectParryTier.STONE_OR_GOLD;
        }
        if (tierName.contains("wood")) {
            return PerfectParryTier.WOOD;
        }

        int tierUses;
        try {
            tierUses = tier == null ? -1 : tier.getUses();
        } catch (Throwable ignored) {
            tierUses = -1;
        }

        if (tierUses >= 1500) {
            return PerfectParryTier.DIAMOND_AND_ABOVE;
        }
        if (tierUses >= 250) {
            return PerfectParryTier.IRON;
        }
        if (tierUses >= 100 || tierUses == 32) {
            return PerfectParryTier.STONE_OR_GOLD;
        }
        if (tierUses > 0) {
            return PerfectParryTier.WOOD;
        }

        return PerfectParryTier.DIAMOND_AND_ABOVE;
    }

    private enum PerfectParryTier {
        DIAMOND_AND_ABOVE,
        IRON,
        STONE_OR_GOLD,
        WOOD,
        UNKNOWN
    }

    private static void syncBrokenGuardMovementModifier(Player player) {
        if (player == null) {
            return;
        }

        boolean slowed = false;
        Long slowExpiry = guardBrokenSlowPlayers.get(player.getUUID());
        if (slowExpiry != null) {
            long now = player.level().getGameTime();
            if (now <= slowExpiry) {
                slowed = true;
            } else {
                guardBrokenSlowPlayers.remove(player.getUUID());
            }
        }

        try {
            AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movementSpeed == null) {
                return;
            }

            AttributeModifier existingModifier = movementSpeed.getModifier(BROKEN_GUARD_SPEED_MODIFIER_ID);
            if (slowed) {
                if (existingModifier == null) {
                    movementSpeed.addTransientModifier(new AttributeModifier(
                        BROKEN_GUARD_SPEED_MODIFIER_ID,
                        BROKEN_GUARD_SPEED_REDUCTION,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ));
                }
            } else if (existingModifier != null) {
                movementSpeed.removeModifier(BROKEN_GUARD_SPEED_MODIFIER_ID);
            }
        } catch (Throwable ignored) {}
    }
}
