package com.shioh.sengoku.fabric;

import com.shioh.sengoku.config.SengokuConfig;

import com.shioh.sengoku.system.PostureHandler;
import com.shioh.sengoku.system.WarmWaterSystem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import com.shioh.sengoku.entity.KojinEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EquipmentSlot;

public class FabricEventManager {
    private static final net.minecraft.resources.ResourceLocation PERFECT_PARRY_ADVANCEMENT =
        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "perfect_parry");
    private static final String PERFECT_PARRY_CRITERION = "land_perfect_parry";

    // Track recent weapon-use start ticks per player (server-side) to detect short-tap perfect parries
    private static final java.util.Map<java.util.UUID, Long> WEAPON_USE_START_TICKS = new java.util.HashMap<>();
    private static final long IMMEDIATE_GUARD_GRACE_TICKS = 4L;

    // Partial parry: fixed tick range immediately after the perfect parry window ends.
    // e.g. if perfect window is 5 ticks, partial parry covers ticks 6–15 (PARTIAL_PARRY_OFFSET to PARTIAL_PARRY_OFFSET + PARTIAL_PARRY_DURATION).
    private static final long PARTIAL_PARRY_OFFSET = 0L;   // ticks after perfect window ends
    private static final long PARTIAL_PARRY_DURATION = 12L; // how many ticks the partial window lasts
    // Posture damage multiplier for partial parry (less than normal block, more than perfect parry)
    private static final float PARTIAL_PARRY_POSTURE_MULTIPLIER = 0.5f;

    private static boolean isWeaponBlockItem(ItemStack stack) {
        return !stack.isEmpty() && (
                stack.getItem() instanceof net.minecraft.world.item.SwordItem
                || stack.getItem() instanceof net.minecraft.world.item.AxeItem
                || stack.getItem() instanceof com.shioh.sengoku.item.SweeplessItem
        );
    }

    private static boolean hasShieldEquipped(Player player) {
        return player.getMainHandItem().getItem() instanceof net.minecraft.world.item.ShieldItem
                || player.getOffhandItem().getItem() instanceof net.minecraft.world.item.ShieldItem;
    }

    private static boolean hasImmediateWeaponGuard(Player player) {
        if (player.level() == null || PostureHandler.isGuardBroken(player) || hasShieldEquipped(player)) {
            return false;
        }

        Long startTick = WEAPON_USE_START_TICKS.get(player.getUUID());
        if (startTick == null) {
            return false;
        }

        long elapsed = player.level().getGameTime() - startTick;
        return elapsed >= 0 && elapsed <= IMMEDIATE_GUARD_GRACE_TICKS && isWeaponBlockItem(player.getMainHandItem());
    }

    private static ItemStack getBlockingItem(Player player) {
        ItemStack useItem = player.getUseItem();
        if (!useItem.isEmpty()) {
            return useItem;
        }
        if (hasImmediateWeaponGuard(player)) {
            return player.getMainHandItem();
        }
        return ItemStack.EMPTY;
    }

    /**
     * Returns true if the player is within the partial parry timing window.
     * The partial parry window begins immediately after the perfect parry window ends
     * and lasts for PARTIAL_PARRY_DURATION ticks.
     */
    private static boolean hasPartialParry(Player player, int perfectWindow) {
        if (player.level() == null || PostureHandler.isGuardBroken(player) || hasShieldEquipped(player)) {
            return false;
        }
        if (!isWeaponBlockItem(player.getMainHandItem())) {
            return false;
        }
        Long start = WEAPON_USE_START_TICKS.get(player.getUUID());
        if (start == null) return false;
        long elapsed = player.level().getGameTime() - start;
        long windowStart = perfectWindow + PARTIAL_PARRY_OFFSET;
        long windowEnd   = windowStart + PARTIAL_PARRY_DURATION;
        return elapsed >= windowStart && elapsed <= windowEnd;
    }

    private static void awardAdvancement(net.minecraft.server.level.ServerPlayer player, net.minecraft.resources.ResourceLocation advancementId, String criterion) {
        net.minecraft.advancements.AdvancementHolder holder = player.server.getAdvancements().get(advancementId);
        if (holder != null) {
            player.getAdvancements().award(holder, criterion);
        }
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            // Server starting; posture system active (debug print removed)
        });

        // Listen before damage is applied
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, originalAmount) -> {
            // Prevent Kojin, Ningyo, Kamiike Hime, and Akugyo from taking drowning damage (they are aquatic)
            try {
                if (entity instanceof KojinEntity || entity instanceof com.shioh.sengoku.entity.NingyoEntity || entity instanceof com.shioh.sengoku.entity.KamiikeHimeEntity || entity instanceof com.shioh.sengoku.entity.AkugyoEntity) {
                    try {
                        java.lang.reflect.Method m = source.getClass().getMethod("getMsgId");
                        Object id = m.invoke(source);
                        if (id != null) {
                            String sid = id.toString().toLowerCase();
                            if (sid.contains("drown")) return false;
                        }
                    } catch (NoSuchMethodException nsme) {
                        // Older mappings may not have getMsgId — fall back to name check via toString()
                        try {
                            String s = source.toString().toLowerCase();
                            if (s.contains("drown")) return false;
                        } catch (Throwable ignored) {}
                    }
                }
            } catch (Throwable ignored) {}
            if (entity instanceof Player player) {
                boolean immediateWeaponGuard = hasImmediateWeaponGuard(player);
                if (player.isBlocking() || immediateWeaponGuard) {
                    // If this damage source has no attacker (environmental damage like fire/lava/drowning/fall/starve),
                    // don't treat it as a blockable hit — let vanilla damage proceed.
                    if (source.getDirectEntity() == null && source.getEntity() == null) {
                        return true; // allow vanilla damage
                    }
                    ItemStack blockingItem = getBlockingItem(player);
                    if (blockingItem.isEmpty()) {
                        return true;
                    }
                    boolean isShield = blockingItem.getItem() instanceof net.minecraft.world.item.ShieldItem;
                    boolean isWeaponBlock = isWeaponBlockItem(blockingItem);
                    net.minecraft.world.entity.LivingEntity attacker = null;
                    
                    // If attacker exists and is a living entity, give a small knockback when they hit a blocking player
                    try {
                        net.minecraft.world.entity.Entity direct = source.getDirectEntity();
                        net.minecraft.world.entity.Entity trueSource = source.getEntity();
                        if (direct instanceof net.minecraft.world.entity.LivingEntity) attacker = (net.minecraft.world.entity.LivingEntity) direct;
                        else if (trueSource instanceof net.minecraft.world.entity.LivingEntity) attacker = (net.minecraft.world.entity.LivingEntity) trueSource;

                        if (attacker != null) {

                            // PERFECT PARRY: short-tap weapon blocks create a timing window
                            try {
                                int ticksUsing = 0;
                                try { ticksUsing = player.getTicksUsingItem(); } catch (Throwable ignored) {}
                                final int BASE_PERFECT_WINDOW = 3;
                                final int PERFECT_WINDOW = !isShield && isWeaponBlock
                                    ? PostureHandler.getPerfectParryWindowTicks(blockingItem, BASE_PERFECT_WINDOW)
                                    : BASE_PERFECT_WINDOW;
                                // Prefer server-side recorded start tick (robust to client/server delays)
                                boolean perfect = false;
                                try {
                                    Long start = WEAPON_USE_START_TICKS.get(player.getUUID());
                                    if (start != null && player.level() != null) {
                                        long now = player.level().getGameTime();
                                        if (now - start <= PERFECT_WINDOW) perfect = true;
                                    }
                                } catch (Throwable ignored) {}
                                if (!perfect) {
                                    if (!isShield && isWeaponBlock && ticksUsing > 0 && ticksUsing <= PERFECT_WINDOW) perfect = true;
                                }
                                if (perfect) {
                                    if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                                        awardAdvancement(serverPlayer, PERFECT_PARRY_ADVANCEMENT, PERFECT_PARRY_CRITERION);
                                    }
                                    try {
                                        if (!player.level().isClientSide && player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                                            serverLevel.playSound(null, player.blockPosition(), com.shioh.sengoku.registry.SoundRegistry.PERFECT_PARRY, SoundSource.PLAYERS, 1.0F, 0.9F + player.level().random.nextFloat() * 0.2F);
                                            double lookX = -Math.sin(Math.toRadians(player.getYRot())) * Math.cos(Math.toRadians(player.getXRot()));
double lookY = -Math.sin(Math.toRadians(player.getXRot()));
double lookZ = Math.cos(Math.toRadians(player.getYRot())) * Math.cos(Math.toRadians(player.getXRot()));
serverLevel.sendParticles(com.shioh.sengoku.registry.ParticleRegistry.BLOCK_PARTICLE, player.getX() + lookX * 1.5, player.getEyeY() + lookY * 1.5, player.getZ() + lookZ * 1.5, 1, 0.0, 0.0, 0.0, 0.0);
                                                // Also emit extra posture sparks for a satisfying perfect-parry feedback
                                                serverLevel.sendParticles(com.shioh.sengoku.registry.ParticleRegistry.POSTURE_SPARK, player.getX(), player.getY() + 1.5, player.getZ(), 20, 0.4, 0.4, 0.4, 0.12);
                                        } else {
                                                try { player.level().playSound(null, player.getX(), player.getY(), player.getZ(), com.shioh.sengoku.registry.SoundRegistry.PERFECT_PARRY, SoundSource.PLAYERS, 1.0F, 0.9F + player.level().random.nextFloat() * 0.2F); } catch (Throwable ignored) {}
                                                try { player.level().addParticle(com.shioh.sengoku.registry.ParticleRegistry.POSTURE_SPARK, player.getX(), player.getY() + 1.5, player.getZ(), 0.0, 0.0, 0.0); } catch (Throwable ignored) {}
                                        }
                                    } catch (Throwable ignored) {}

                                    // Strong knockback + brief slow on attacker
                                    try {
                                        if (attacker instanceof net.minecraft.world.entity.LivingEntity livingAttacker) {
                                            if (!(livingAttacker instanceof com.shioh.sengoku.entity.WarlordEntity)) {
                                                net.minecraft.world.phys.Vec3 dir = new net.minecraft.world.phys.Vec3(
                                                    livingAttacker.getX() - player.getX(), 0.0, livingAttacker.getZ() - player.getZ());
                                                if (dir.length() > 1.0E-6) dir = dir.normalize(); else dir = new net.minecraft.world.phys.Vec3(0.0, 0.0, 0.0);
                                                livingAttacker.setDeltaMovement(livingAttacker.getDeltaMovement().add(dir.scale(1.2)));
                                                livingAttacker.hurtMarked = true;
                                                try { livingAttacker.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 20, 4, false, false, true)); } catch (Throwable ignored) {}
                                            }
                                        }
                                    } catch (Throwable ignored) {}

                                    try {
                                        if (attacker instanceof net.minecraft.world.entity.LivingEntity livingAttacker) {
                                            PostureHandler.redirectPerfectParryPostureDamage(livingAttacker, originalAmount, player);
                                            PostureHandler.applyPerfectParryPostureDamage(player, originalAmount, livingAttacker);
                                        }
                                    } catch (Throwable ignored) {}

                                    // Cancel vanilla damage on perfect parry
                                    return false;
                                }

                                // PARTIAL PARRY: window just after the perfect parry window
                                if (!isShield && isWeaponBlock) {
                                    boolean partial = hasPartialParry(player, PERFECT_WINDOW);
                                    if (partial) {
                                        // Play partial parry sound
                                        try {
                                            if (!player.level().isClientSide && player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                                                serverLevel.playSound(null, player.blockPosition(), com.shioh.sengoku.registry.SoundRegistry.PARTIAL_PARRY, SoundSource.PLAYERS, 1.0F, 0.85F + player.level().random.nextFloat() * 0.2F);
                                            } else {
                                                try { player.level().playSound(null, player.getX(), player.getY(), player.getZ(), com.shioh.sengoku.registry.SoundRegistry.PARTIAL_PARRY, SoundSource.PLAYERS, 1.0F, 0.85F + player.level().random.nextFloat() * 0.2F); } catch (Throwable ignored) {}
                                            }
                                        } catch (Throwable ignored) {}

                                        // Apply reduced posture damage (less than a normal block)
                                        try {
                                            PostureHandler.applyPartialParryPostureDamage(player, originalAmount * PARTIAL_PARRY_POSTURE_MULTIPLIER, attacker);
                                        } catch (Throwable ignored) {}

                                        // Apply normal attacker pushback (same as regular block)
                                        try {
                                            double attackerPower = 0.0;
                                            try {
                                                var attr = attacker.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                                                if (attr != null) attackerPower = attr.getValue();
                                            } catch (Throwable ignored2) {}
                                            double damageForScale = Math.max(originalAmount, attackerPower);
                                            double attackerPushScale = Math.min(0.25 * damageForScale, 1.0);
                                            net.minecraft.world.phys.Vec3 dir = new net.minecraft.world.phys.Vec3(
                                                player.getX() - attacker.getX(), 0.0, player.getZ() - attacker.getZ()).normalize();
                                            attacker.setDeltaMovement(attacker.getDeltaMovement().add(dir.scale(attackerPushScale)));
                                            attacker.hurtMarked = true;
                                        } catch (Throwable ignored) {}

                                        // Cancel vanilla damage
                                        return false;
                                    }
                                }
                            } catch (Throwable ignored) {}

                            // push attacker slightly away from the player (normal block case)
                            try {
                                // Use attacker's attack attribute as a hint for how strong the hit was
                                double attackerPower = 0.0;
                                try {
                                    if (attacker instanceof net.minecraft.world.entity.LivingEntity le) {
                                        var attr = le.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                                        if (attr != null) attackerPower = attr.getValue();
                                    }
                                } catch (Throwable ignored2) {}

                                double damageForScale = Math.max(originalAmount, attackerPower);
                                double attackerPushScale = Math.min(0.25 * damageForScale, 1.0);

                                net.minecraft.world.phys.Vec3 dir = new net.minecraft.world.phys.Vec3(
                                    player.getX() - attacker.getX(),
                                    0.0, // horizontal only
                                    player.getZ() - attacker.getZ()
                                ).normalize();
                                attacker.setDeltaMovement(attacker.getDeltaMovement().add(dir.scale(attackerPushScale)));
                                attacker.hurtMarked = true;
                            } catch (Throwable ignored) {}
                        }
                        // Also apply a knockback to the player when they successfully block.
                        // Scale the push magnitude with the incoming damage so heavier blows
                        // push the player back more. Keep a sensible cap to avoid extreme values.
                        try {
                            // Determine scale based on either the original damage or the attacker's
                            // attack-attribute (if available) so powerful mobs push harder.
                            double attackerPower = 0.0;
                            try {
                                if (attacker instanceof net.minecraft.world.entity.LivingEntity le) {
                                    var attr = le.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                                    if (attr != null) attackerPower = attr.getValue();
                                }
                            } catch (Throwable ignored2) {}

                            double damageForScale = Math.max(originalAmount, attackerPower);
                            // scale factor per damage point (tuneable)
                            double scale = Math.min(0.12 * damageForScale, 0.5);

                            if (attacker != null) {
                                net.minecraft.world.phys.Vec3 pushTo = new net.minecraft.world.phys.Vec3(
                                    player.getX() - attacker.getX(), 0.0, player.getZ() - attacker.getZ()
                                );
                                if (pushTo.length() > 0.0001D) pushTo = pushTo.normalize().scale(scale); else pushTo = new net.minecraft.world.phys.Vec3(0, 0, 0);
                                player.setDeltaMovement(player.getDeltaMovement().add(pushTo));
                                player.hurtMarked = true;
                            } else {
                                // Fallback: push player slightly backward relative to their look direction
                                net.minecraft.world.phys.Vec3 look = player.getLookAngle();
                                net.minecraft.world.phys.Vec3 back = new net.minecraft.world.phys.Vec3(-look.x, 0.0, -look.z);
                                if (back.length() < 1.0E-6) back = new net.minecraft.world.phys.Vec3(0.0, 0.0, 1.0);
                                back = back.normalize().scale(Math.max(0.06, scale * 0.9));
                                player.setDeltaMovement(player.getDeltaMovement().add(back));
                                player.hurtMarked = true;
                            }
                        } catch (Throwable ignored) {}
                    } catch (Throwable ignored) {}

                    // ⛔ Only apply posture damage to weapons, NOT shields (we still want to damage shields separately)
                    boolean broken = false;
                    if (!isShield) {
                        broken = PostureHandler.applyPostureDamage(player, originalAmount, attacker);
                    } else {
                        // Damage the shield item durability when blocking
                        try {
                            if (!blockingItem.isEmpty()) {
                                int durabilityLoss = 1 + net.minecraft.util.Mth.floor(originalAmount * 0.5f);
                                durabilityLoss = Math.min(durabilityLoss, 2);
                                // Determine equipment slot for the blocking item
                                net.minecraft.world.entity.EquipmentSlot slot = net.minecraft.world.entity.EquipmentSlot.MAINHAND;
                                if (player.getItemInHand(net.minecraft.world.InteractionHand.OFF_HAND) == blockingItem) {
                                    slot = net.minecraft.world.entity.EquipmentSlot.OFFHAND;
                                } else if (player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND) == blockingItem) {
                                    slot = net.minecraft.world.entity.EquipmentSlot.MAINHAND;
                                } else {
                                    // fallback: if it's not in either hand, assume offhand for shields
                                    slot = net.minecraft.world.entity.EquipmentSlot.OFFHAND;
                                }
                                blockingItem.hurtAndBreak(durabilityLoss, player, slot);
                            }
                        } catch (Throwable ignored) {}
                    }

                    // ✅ play block sound + particles
                    playBlockEffects(player, source);

                    if (broken) {
                        player.disableShield();
                    }

                    return false; // cancel vanilla damage
                }
            }
            return true; // let vanilla damage through
        });

        // Ensure weapon 'use' (right-click) is registered server-side immediately
        UseItemCallback.EVENT.register((player, world, hand) -> {
            try {
                ItemStack stack = player.getItemInHand(hand);
                boolean isWeapon = stack != null && !stack.isEmpty() && (
                        stack.getItem() instanceof net.minecraft.world.item.SwordItem ||
                        stack.getItem() instanceof net.minecraft.world.item.AxeItem ||
                        stack.getItem() instanceof com.shioh.sengoku.item.SweeplessItem
                );
                if (isWeapon) {
                    try { player.startUsingItem(hand); } catch (Throwable ignored) {}
                    try {
                        if (!world.isClientSide) {
                            try { WEAPON_USE_START_TICKS.put(player.getUUID(), world.getGameTime()); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
            return net.minecraft.world.InteractionResultHolder.pass(player.getItemInHand(hand));
        });

        // Handle trapdoor 'stiff' interactions using Fabric event API instead of fragile mixin injections.
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            try {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);
                
                // First: handle Shoji frame variant interactions (shears, paper, and dyes)
                try {
                    if (state.getBlock() instanceof com.shioh.sengoku.block.ShojiFrameVariantBlock) {
                        ItemStack stack = player.getItemInHand(hand);
                        // handle shears
                        if (stack != null && !stack.isEmpty() && stack.getItem() == Items.SHEARS) {
                            int current = state.getValue(com.shioh.sengoku.util.ShojiProperties.DAMAGED);
                            if (!world.isClientSide && current < 3) {
                                world.setBlock(pos, state.setValue(com.shioh.sengoku.util.ShojiProperties.DAMAGED, current + 1), 3);
                                EquipmentSlot slot = (hand == InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                                stack.hurtAndBreak(1, player, slot);
                                // play painting break sound on damage
                                world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, net.minecraft.sounds.SoundEvents.PAINTING_BREAK, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return InteractionResult.sidedSuccess(world.isClientSide);
                        }

                        // paper -> repair by one stage (reverse of shears damage)
                        if (stack != null && !stack.isEmpty() && stack.getItem() == Items.PAPER) {
                            int current = state.getValue(com.shioh.sengoku.util.ShojiProperties.DAMAGED);
                            if (!world.isClientSide && current > 1) {
                                world.setBlock(pos, state.setValue(com.shioh.sengoku.util.ShojiProperties.DAMAGED, current - 1), 3);
                                if (!player.isCreative()) stack.shrink(1);
                                world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, net.minecraft.sounds.SoundEvents.PAINTING_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return InteractionResult.sidedSuccess(world.isClientSide);
                        }

                        // yellow dye -> aged = true
                        if (stack != null && !stack.isEmpty() && stack.getItem() == Items.YELLOW_DYE) {
                            if (!world.isClientSide && !state.getValue(com.shioh.sengoku.util.ShojiProperties.AGED)) {
                                world.setBlock(pos, state.setValue(com.shioh.sengoku.util.ShojiProperties.AGED, true), 3);
                                if (!player.isCreative()) stack.shrink(1);
                                // play painting place sound when aging
                                world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, net.minecraft.sounds.SoundEvents.PAINTING_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return InteractionResult.sidedSuccess(world.isClientSide);
                        }

                        // white dye -> aged = false
                        if (stack != null && !stack.isEmpty() && stack.getItem() == Items.WHITE_DYE) {
                            if (!world.isClientSide && state.getValue(com.shioh.sengoku.util.ShojiProperties.AGED)) {
                                world.setBlock(pos, state.setValue(com.shioh.sengoku.util.ShojiProperties.AGED, false), 3);
                                if (!player.isCreative()) stack.shrink(1);
                                // play painting break sound when removing aging
                                world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, net.minecraft.sounds.SoundEvents.PAINTING_BREAK, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                            }
                            return InteractionResult.sidedSuccess(world.isClientSide);
                        }
                    }
                } catch (Exception ignored) {}

                // Lacquering: use slime ball on vanilla logs/woods to convert to lacquered variants
                try {
                    ItemStack held = player.getItemInHand(hand);
                    if (held != null && !held.isEmpty() && held.getItem() == Items.SLIME_BALL) {
                        ConversionTarget target = getLacqueredConversion(state.getBlock());
                        if (target != null) {
                            Block lacquered = target.isLog
                                    ? com.shioh.sengoku.registry.SengokuBlocks.LACQUERED_LOGS.get("lacquered_" + target.woodName + "_log")
                                    : com.shioh.sengoku.registry.SengokuBlocks.LACQUERED_WOODS.get("lacquered_" + target.woodName + "_wood");
                            if (lacquered != null) {
                                if (!world.isClientSide) {
                                    Direction.Axis axis = state.hasProperty(RotatedPillarBlock.AXIS)
                                            ? state.getValue(RotatedPillarBlock.AXIS)
                                            : Direction.Axis.Y;
                                    BlockState newState = lacquered.defaultBlockState();
                                    if (newState.hasProperty(RotatedPillarBlock.AXIS)) {
                                        newState = newState.setValue(RotatedPillarBlock.AXIS, axis);
                                    }
                                    world.setBlock(pos, newState, 3);
                                    if (!player.isCreative()) held.shrink(1);
                                    world.playSound(null, pos, net.minecraft.sounds.SoundEvents.SLIME_BLOCK_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                                }
                                return InteractionResult.sidedSuccess(world.isClientSide);
                            }
                        }
                    }
                } catch (Exception ignored) {}

                if (!(state.getBlock() instanceof TrapDoorBlock)) return InteractionResult.PASS;

                ItemStack stack = player.getItemInHand(hand);
                boolean holdingDebug = stack != null && !stack.isEmpty() && stack.getItem() == Items.DEBUG_STICK;

                // Slime ball makes trapdoor stiff and plays sticky placement sound.
                if (stack != null && !stack.isEmpty() && stack.getItem() == Items.SLIME_BALL) {
                    if (!world.isClientSide) {
                        boolean curr = state.getValue(com.shioh.sengoku.util.TrapdoorStiffProperties.STIFF);
                        if (!curr) {
                            world.setBlock(pos, state.setValue(com.shioh.sengoku.util.TrapdoorStiffProperties.STIFF, true), 3);
                            if (!player.isCreative()) stack.shrink(1);
                            world.playSound(null, pos, net.minecraft.sounds.SoundEvents.SLIME_BLOCK_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
                        }
                    }
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }

                if (holdingDebug) {
                    if (!world.isClientSide) {
                        boolean curr = state.getValue(com.shioh.sengoku.util.TrapdoorStiffProperties.STIFF);
                        world.setBlock(pos, state.setValue(com.shioh.sengoku.util.TrapdoorStiffProperties.STIFF, !curr), 3);
                    }
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }

                if (state.getValue(com.shioh.sengoku.util.TrapdoorStiffProperties.STIFF)) {
                    return InteractionResult.FAIL;
                }
            } catch (Exception ignored) {}
            return InteractionResult.PASS;
        });

        // Register warm water system - check players in water near magma blocks
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (var player : server.getPlayerList().getPlayers()) {
                try {
                    // Skip warm water handling entirely in the Nether
                    if (isNether(player.serverLevel())) continue;
                } catch (Throwable ignored) {}
                WarmWaterSystem.handlePlayerInWater(player);
            }

            // Melt ice near magma blocks in all loaded dimensions, skip Nether
            for (var level : server.getAllLevels()) {
                try {
                    if (isNether(level)) continue;
                } catch (Throwable ignored) {}
                WarmWaterSystem.meltIceNearMagma(level);
            }

            // Ambient particle effects are handled client-side for better performance
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = server.overworld().getGameTime();
            WEAPON_USE_START_TICKS.entrySet().removeIf(entry -> now - entry.getValue() > 40L);
        });
    }

    private static boolean isNether(net.minecraft.world.level.Level level) {
        try {
            var loc = level.dimension().location();
            if (loc != null) return "the_nether".equals(loc.getPath());
        } catch (Throwable ignored) {}
        return false;
    }

    // Helper record for conversion mapping
    private static class ConversionTarget {
        final String woodName; final boolean isLog;
        ConversionTarget(String woodName, boolean isLog) { this.woodName = woodName; this.isLog = isLog; }
    }

    // Map vanilla blocks to our lacquered families and whether they are logs or woods
    private static ConversionTarget getLacqueredConversion(Block source) {
        // Overworld woods
        if (source == Blocks.OAK_LOG) return new ConversionTarget("oak", true);
        if (source == Blocks.OAK_WOOD) return new ConversionTarget("oak", false);

        if (source == Blocks.BIRCH_LOG) return new ConversionTarget("birch", true);
        if (source == Blocks.BIRCH_WOOD) return new ConversionTarget("birch", false);

        if (source == Blocks.SPRUCE_LOG) return new ConversionTarget("black_pine", true);
        if (source == Blocks.SPRUCE_WOOD) return new ConversionTarget("black_pine", false);

        if (source == Blocks.DARK_OAK_LOG) return new ConversionTarget("dark_cedar", true);
        if (source == Blocks.DARK_OAK_WOOD) return new ConversionTarget("dark_cedar", false);

        if (source == Blocks.ACACIA_LOG) return new ConversionTarget("keyaki", true);
        if (source == Blocks.ACACIA_WOOD) return new ConversionTarget("keyaki", false);

        if (source == Blocks.JUNGLE_LOG) return new ConversionTarget("kiso", true);
        if (source == Blocks.JUNGLE_WOOD) return new ConversionTarget("kiso", false);

        if (source == Blocks.MANGROVE_LOG) return new ConversionTarget("mangrove", true);
        if (source == Blocks.MANGROVE_WOOD) return new ConversionTarget("mangrove", false);

        // Bamboo uses the bamboo block for both variants
        if (source == Blocks.BAMBOO_BLOCK) return new ConversionTarget("bamboo", true); // treat as log

        // Nether woods (stems/hyphae)
        if (source == Blocks.CRIMSON_STEM) return new ConversionTarget("bloodgood", true);
        if (source == Blocks.CRIMSON_HYPHAE) return new ConversionTarget("bloodgood", false);

        if (source == Blocks.WARPED_STEM) return new ConversionTarget("weeping_willow", true);
        if (source == Blocks.WARPED_HYPHAE) return new ConversionTarget("weeping_willow", false);

        // Cherry
        if (source == Blocks.CHERRY_LOG) return new ConversionTarget("sakura", true);
        if (source == Blocks.CHERRY_WOOD) return new ConversionTarget("sakura", false);

        return null;
    }

    private static void playBlockEffects(Player player, DamageSource source) {
        // Determine which sound to play based on the item being used for blocking
        ItemStack blockingItem = player.getUseItem();
        boolean isWeaponBlock = blockingItem.getItem() instanceof net.minecraft.world.item.SwordItem 
                             || blockingItem.getItem() instanceof net.minecraft.world.item.AxeItem
                             || blockingItem.getItem() instanceof com.shioh.sengoku.item.SweeplessItem;
        
        if (isWeaponBlock) {
            // Play custom weapon parry sound for swords, axes, and custom weapons
            player.level().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    com.shioh.sengoku.registry.SoundRegistry.WEAPON_PARRY,
                    SoundSource.PLAYERS,
                    1.0F, 0.8F + player.level().random.nextFloat() * 0.4F
            );
        } else {
            // Play the vanilla shield block sound for shields
            player.level().playSound(
                    null,
                    player.getX(), player.getY(), player.getZ(),
                    SoundEvents.SHIELD_BLOCK,
                    SoundSource.PLAYERS,
                    1.0F, 0.8F + player.level().random.nextFloat() * 0.4F
            );
        }

        // TODO: spawn particles like vanilla (optional)
        // Example: player.level().addParticle(ParticleTypes.CRIT, ...);
    }
}