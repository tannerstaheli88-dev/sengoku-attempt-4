package com.shioh.sengoku.entity;

import com.shioh.sengoku.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import com.shioh.sengoku.system.StealthCritCooldownManager;
import com.shioh.sengoku.util.PlayerNoiseTracker;

/**
 * Kunai projectile entity with arrow-like physics.
 * Sticks to blocks and entities, has gravity and drag.
 */
public class KunaiEntity extends AbstractArrow {

    // NOTE: We do NOT redeclare PIERCE_LEVEL here — AbstractArrow already manages it.
    // Use super.getPierceLevel() / super.setPierceLevel() for pierce level access.

    private ItemStack kunaiItem;
    private double baseDamage = 3.0D;
    private int knockback = 0;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public KunaiEntity(EntityType<? extends KunaiEntity> entityType, Level level) {
        super(entityType, level);
        // Fallback item — will be overwritten by NBT load if this came from disk
        this.kunaiItem = new ItemStack(com.shioh.sengoku.init.ThrowableArrowReg.POISON_KUNAI);
    }

    public KunaiEntity(Level level, LivingEntity shooter, ItemStack stack) {
        super(ModEntities.KUNAI, shooter, level, stack, null);
        this.kunaiItem = stack.copy();
        this.baseDamage = 3.0D;
    }

    public KunaiEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntities.KUNAI, x, y, z, level, stack, null);
        this.kunaiItem = stack.copy();
    }

    // -------------------------------------------------------------------------
    // Synched data — only register what the parent hasn't already defined
    // -------------------------------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        // No extra fields needed right now; pierce level is owned by AbstractArrow.
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        super.tick();

        // Rotate the kunai to face its velocity direction while airborne
        if (!this.inGround) {
            Vec3 motion = this.getDeltaMovement();
            if (motion.lengthSqr() > 0.0001) {
                double horizontalDistance = motion.horizontalDistance();
                this.setYRot((float) (Math.atan2(motion.x, motion.z) * (180.0 / Math.PI)));
                this.setXRot((float) (Math.atan2(motion.y, horizontalDistance) * (180.0 / Math.PI)));
                this.yRotO = this.getYRot();
                this.xRotO = this.getXRot();
            }
        }

        // Despawn after 1 minute (1200 ticks) if stuck in the ground
        if (this.inGround && this.tickCount > 1200) {
            this.discard();
        }
    }

    // -------------------------------------------------------------------------
    // Hit — entity
    // -------------------------------------------------------------------------

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        Entity owner  = this.getOwner();

        // Base damage scales with velocity
        float velocity = (float) this.getDeltaMovement().length();
        float damage   = Math.max(1.0F, (float) Math.ceil(velocity * this.baseDamage));

        // Stealth bonus: 2× damage against mobs that haven't detected the player
        if (owner instanceof Player player && entity instanceof Mob mob) {
            boolean noisy = false;
            try { noisy = PlayerNoiseTracker.getInstance().isNoisy(player); } catch (Throwable ignored) {}

            boolean hasDetected = (mob.getTarget() == player) || (mob.getLastHurtByMob() == player);
            if (!noisy && !hasDetected) {
                boolean allowed = true;
                try {
                    // 200 ticks (~10 s) cooldown per target — value is in TICKS, not ms
                    allowed = StealthCritCooldownManager.getInstance()
                            .tryConsume(player, (LivingEntity) entity, 200L);
                } catch (Throwable ignored) {}
                if (allowed) damage *= 2.0F;
            }
        }

        DamageSource damageSource = this.damageSources().arrow(this, owner != null ? owner : this);

        if (entity.hurt(damageSource, damage)) {
            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (entity instanceof LivingEntity livingEntity) {

                // Knockback
                if (this.knockback > 0) {
                    Vec3 kb = this.getDeltaMovement()
                            .multiply(1.0, 0.0, 1.0)
                            .normalize()
                            .scale(this.knockback * 0.6);
                    if (kb.lengthSqr() > 0.0) {
                        livingEntity.push(kb.x, 0.1, kb.z);
                    }
                }

                // Potion effects stored on the kunai item
                if (kunaiItem != null && !kunaiItem.isEmpty()
                        && kunaiItem.has(DataComponents.POTION_CONTENTS)) {

                    PotionContents contents = kunaiItem.get(DataComponents.POTION_CONTENTS);
                    if (contents != null && !contents.equals(PotionContents.EMPTY)) {
                        for (MobEffectInstance effect : contents.getAllEffects()) {
                            // Apply the full duration — do NOT divide by 8 like splash potions do
                            livingEntity.addEffect(new MobEffectInstance(
                                    effect.getEffect(),
                                    effect.getDuration(),
                                    effect.getAmplifier(),
                                    effect.isAmbient(),
                                    effect.isVisible()
                            ), owner);
                        }
                    }
                }

                // Enchantment post-attack effects (server only)
                if (!this.level().isClientSide && owner instanceof LivingEntity) {
                    EnchantmentHelper.doPostAttackEffects(
                            (ServerLevel) this.level(), livingEntity, damageSource);
                }

                this.doPostHurtEffects(livingEntity);
            }

            // Allow pickup by the throwing player; disallow for mob-thrown kunai
            if (!this.level().isClientSide) {
                this.pickup = (owner instanceof Player)
                        ? AbstractArrow.Pickup.ALLOWED
                        : AbstractArrow.Pickup.DISALLOWED;
                this.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F);
            }

        }
        // No else-branch calling super.onHitEntity — if hurt() returns false the
        // arrow sticks naturally via AbstractArrow's own logic.
    }

    // -------------------------------------------------------------------------
    // Hit — block
    // -------------------------------------------------------------------------

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (!this.level().isClientSide) {
            Entity owner = this.getOwner();
            this.pickup = (owner instanceof Player)
                    ? AbstractArrow.Pickup.ALLOWED
                    : AbstractArrow.Pickup.DISALLOWED;
        }

        this.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.0F);
    }

    // -------------------------------------------------------------------------
    // Item helpers
    // -------------------------------------------------------------------------

    private ItemStack safeKunaiItem() {
        return (kunaiItem != null && !kunaiItem.isEmpty())
                ? kunaiItem
                : new ItemStack(com.shioh.sengoku.init.ThrowableArrowReg.POISON_KUNAI);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        // Copy here because the caller may store/modify the returned stack
        return safeKunaiItem().copy();
    }

    @Override
    public ItemStack getWeaponItem() {
        return safeKunaiItem().copy();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.ARROW_HIT;
    }

    // shouldRender: removed override — let AbstractArrow / Entity handle distance
    // culling normally. Returning `true` unconditionally defeats Minecraft's LOD.

    // -------------------------------------------------------------------------
    // NBT — use a distinct key to avoid shadowing AbstractArrow's "damage" field
    // -------------------------------------------------------------------------

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("KunaiItem", 10)) {
            this.kunaiItem = ItemStack.parseOptional(this.registryAccess(), tag.getCompound("KunaiItem"));
        }
        if (tag.contains("kunai_damage")) {
            this.baseDamage = tag.getDouble("kunai_damage");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("KunaiItem", safeKunaiItem().save(this.registryAccess()));
        tag.putDouble("kunai_damage", this.baseDamage);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public void setBaseDamage(double damage) {
        this.baseDamage = damage;
    }

    public double getBaseDamage() {
        return this.baseDamage;
    }

    public void setKnockback(int knockback) {
        this.knockback = knockback;
    }

}
