package com.shioh.sengoku.entity;

import com.shioh.sengoku.entity.ai.AdvancedMeleeAttackGoal;
import com.shioh.sengoku.entity.ai.CircleStrafeGoal;
import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/**
 * Goryo entity - a vengeful ghost samurai that haunts the Nether.
 */
public class GoryoEntity extends AbstractIllager {
    private static final EntityDataAccessor<Boolean> DATA_WINDING_UP = SynchedEntityData.defineId(GoryoEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_WINDUP_TICKS = SynchedEntityData.defineId(GoryoEntity.class, EntityDataSerializers.INT);
    private static final double SENGOKU_ELITE_MAX_HEALTH = 50.0D;
    private static final String SENGOKU_ELITE_TAG = "sengoku_elite";
    private boolean sengoku$eliteWeaponInitialized = false;
        private int aggroAmbientCooldown = 0;
    
    public GoryoEntity(EntityType<? extends GoryoEntity> type, Level level) {
        super(type, level);
        this.xpReward = 8; // More XP than Ronin as they're rarer
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WINDING_UP, false);
        builder.define(DATA_WINDUP_TICKS, 0);
    }

    // Windup animation / state helpers (used by AI to synchronize client-side animation playback)
    public void setWindingUp(boolean flag) {
        try { this.entityData.set(DATA_WINDING_UP, flag); } catch (Throwable ignored) {}
    }

    public boolean isWindingUp() {
        try { return this.entityData.get(DATA_WINDING_UP); } catch (Throwable ignored) { return false; }
    }

    public void setWindupTicks(int ticks) {
        try { this.entityData.set(DATA_WINDUP_TICKS, ticks); } catch (Throwable ignored) {}
    }

    public int getWindupTicks() {
        try { return this.entityData.get(DATA_WINDUP_TICKS); } catch (Throwable ignored) { return 0; }
    }
    
    @Override
    protected void registerGoals() {
        super.registerGoals();
        
        // Basic AI goals
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Avoid zombified piglins (like piglins do)
        try {
            this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, ZombifiedPiglin.class, 8.0F, 1.0D, 1.2D));
        } catch (Throwable ignored) {}
        
        // Advanced combat AI - Ghost of Tsushima/Sekiro style
        this.goalSelector.addGoal(1, new WeaponBlockGoal(this)); // Block incoming attacks
        this.goalSelector.addGoal(2, new AdvancedMeleeAttackGoal(this, 1.0, false)); // Tactical melee attacks with timing
        this.goalSelector.addGoal(3, new CircleStrafeGoal(this)); // Circle around target like vexes
        // Fallback vanilla melee attack while mounted or when advanced goals are disabled
        this.goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.0D, true));
        
        // Movement and patrol
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
        
        // Targeting - attack players and nether inhabitants
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        
        // Target piglins and hoglins as well
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.piglin.Piglin.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.hoglin.Hoglin.class, true));
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.35) // Slightly faster than Ronin
            .add(Attributes.FOLLOW_RANGE, 35.0)
            .add(Attributes.MAX_HEALTH, 35.0) // Nerfed slightly (was 35)
            .add(Attributes.ATTACK_DAMAGE, 2.0) // Higher damage with iron sword
            .add(Attributes.ARMOR, 2.0); // More armor 
    }
    
    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // Goryo carry iron swords
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
    }
    
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.GORYO_AMBIENT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.GORYO_DEATH;
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.GORYO_HURT;
    }

    @Override
    public void playAmbientSound() {
        // Make ambient sound very rare; play ~1/1200 of the usual checks
        try {
            if (this.random.nextInt(1200) == 0) {
                super.playAmbientSound();
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void setAggressive(boolean aggressive) {
        boolean wasAggressive = this.isAggressive();
        super.setAggressive(aggressive);
        if (!this.level().isClientSide && aggressive && !wasAggressive) {
            if (this.aggroAmbientCooldown <= 0) {
                try {
                    this.playSound(SoundRegistry.GORYO_AMBIENT_AGGRO, 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
                } catch (Throwable ignored) {}
                this.aggroAmbientCooldown = 100;
            }
        }
    }
    
    @Override
    public IllagerArmPose getArmPose() {
        try {
            if (com.shioh.sengoku.entity.ai.WeaponBlockGoal.isCurrentlyBlocking(this)) {
                return IllagerArmPose.ATTACKING;
            }
        } catch (Throwable ignored) {}

        if (this.isUsingItem() && this.hasRangedWeapon()) {
            return IllagerArmPose.BOW_AND_ARROW;
        }
        if (this.hasRangedWeapon()) {
            return IllagerArmPose.CROSSBOW_HOLD;
        }
        if (this.isAggressive()) {
            return IllagerArmPose.ATTACKING;
        }
        return IllagerArmPose.NEUTRAL;
    }

    public boolean hasRangedWeapon() {
        return this.getMainHandItem().getItem() == Items.BOW;
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Check if currently blocking
        if (WeaponBlockGoal.isCurrentlyBlocking(this)) {
            // Completely block the damage
            
            // Play parry sound
            this.playSound(amount >= 6.0F ? SoundRegistry.PARTIAL_PARRY : SoundRegistry.WEAPON_PARRY, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            
            // Spawn posture spark particle (same as player weapon block)
            WeaponBlockGoal.spawnBlockFeedbackParticles(this, amount);

            // Apply slight knockback to attacker for visual feedback
            Entity attacker = source.getEntity();
            if (!this.level().isClientSide && attacker instanceof LivingEntity livingAttacker) {
                // Push the blocker (this) slightly away from the attacker instead
                Vec3 dir = this.position().subtract(livingAttacker.position()).normalize();
                this.setDeltaMovement(this.getDeltaMovement().add(dir.scale(0.25)));
                this.hurtMarked = true; // ensure motion sync
            }
            
            // Return false to completely block the damage
            // After a successful block, briefly drop blocking and auto-counter: target attacker and allow attack AI
            try {
                if (attacker instanceof LivingEntity) {
                    this.setTarget((LivingEntity) attacker);
                    this.setAggressive(true);
                }
            } catch (Throwable ignored) {}
            try { WeaponBlockGoal.onSuccessfulBlock(this, attacker instanceof LivingEntity ? (LivingEntity) attacker : null); } catch (Throwable ignored) {}
            return false;
        }
        
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // Play swing animation on attack
        this.swing(InteractionHand.MAIN_HAND);
        boolean result = super.doHurtTarget(target);
        if (!this.level().isClientSide && result && target instanceof LivingEntity living) {
            // If target is weapon-blocking (using item that can block), knock this entity back slightly
            if (living.isUsingItem()) {
                ItemStack stack = living.getUseItem();
                if (stack.getItem() instanceof net.minecraft.world.item.SwordItem || stack.getItem() instanceof net.minecraft.world.item.TridentItem) {
                    Vec3 dir = this.position().subtract(living.position()).normalize();
                    this.setDeltaMovement(this.getDeltaMovement().add(dir.scale(0.30)));
                    this.hurtMarked = true;
                    try { com.shioh.sengoku.entity.ai.WeaponBlockGoal.resetConsecutiveBlocks(this); } catch (Throwable ignored) {}
                }
            }
        }
        return result;
    }
    
    @Override
    public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, DifficultyInstance difficulty, 
                                       MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), difficulty);
        boolean makeElite = this.getTags().contains(SENGOKU_ELITE_TAG);
        if (makeElite) {
            var maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(SENGOKU_ELITE_MAX_HEALTH);
                this.setHealth(this.getMaxHealth());
            }
            this.sengoku$applyEliteWeaponEnchantments();
        }
        // Let super finalize spawn (may assign raid/patrol equipment), then clear head slot
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData);
        try {
            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        } catch (Throwable ignored) {}
        // If this entity somehow spawned in mid-air (biome/placement mismatch), move it down to the nearest safe ground.
        try {
            if (!this.level().isClientSide && (reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION || reason == MobSpawnType.STRUCTURE)) {
                BlockPos.MutableBlockPos cursor = this.blockPosition().mutable();
                // Start from one block below current pos and search downwards
                cursor.move(0, -1, 0);
                int steps = 0;
                // Limit search so we don't scan the whole world
                while (steps < 64 && level.getBlockState(cursor).isAir()) {
                    cursor.move(0, -1, 0);
                    steps++;
                }
                // If found a block that blocks motion and isn't lava, teleport the entity there (one block above)
                if (steps < 64) {
                    net.minecraft.world.level.block.state.BlockState ground = level.getBlockState(cursor);
                    if (ground.blocksMotion() && !ground.getFluidState().is(net.minecraft.tags.FluidTags.LAVA)) {
                        this.teleportTo(cursor.getX() + 0.5D, cursor.getY() + 1.0D, cursor.getZ() + 0.5D);
                    }
                }
            }
        } catch (Throwable ignored) {}
        return result;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Elite", this.getAttributeValue(Attributes.MAX_HEALTH) >= (SENGOKU_ELITE_MAX_HEALTH - 0.1D));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("Elite")) {
            var maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(SENGOKU_ELITE_MAX_HEALTH);
                this.setHealth(this.getMaxHealth());
            }
            if (this.getMainHandItem().isEmpty()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }
            this.sengoku$applyEliteWeaponEnchantments();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.aggroAmbientCooldown > 0) {
        this.aggroAmbientCooldown--;
        }
        if (!this.level().isClientSide && !this.sengoku$eliteWeaponInitialized
                && this.getAttributeValue(Attributes.MAX_HEALTH) >= (SENGOKU_ELITE_MAX_HEALTH - 0.1D)) {
            this.sengoku$eliteWeaponInitialized = true;
            if (this.getMainHandItem().isEmpty()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            }
            this.sengoku$applyEliteWeaponEnchantments();
        }
    }

    private void sengoku$applyEliteWeaponEnchantments() {
        try {
            ItemStack weapon = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (weapon.isEmpty()) return;
            ItemEnchantments current = weapon.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(current);
            var enchantments = this.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
            enchantments.getHolder(Enchantments.SHARPNESS).ifPresent(h -> mutable.set(h, 3));
            enchantments.getHolder(Enchantments.UNBREAKING).ifPresent(h -> mutable.set(h, 2));
            weapon.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        } catch (Throwable ignored) {}
    }

    /**
     * Spawn rule: only spawn in the Nether and on the ground (no mid-air spawns).
     */
    public static boolean checkGoryoSpawnRules(EntityType<GoryoEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        // Allow forced spawns (spawners/commands/events) regardless of blocks
        if (spawnType == MobSpawnType.SPAWNER || spawnType == MobSpawnType.COMMAND || spawnType == MobSpawnType.EVENT) {
            return true;
        }

        // Check the block below the spawn pos is in our allowed tag
        try {
            net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> allowed = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "goryo_spawnable_on"));
            BlockPos below = pos.below();
            boolean matches = level.getBlockState(below).is(allowed);
            return matches;
        } catch (Throwable ignored) {
            return false;
        }
    }
    
    // Required Raider abstract methods
    @Override
    public void applyRaidBuffs(ServerLevel level, int wave, boolean unusedFalse) {
        // Apply buffs based on wave
        ItemStack weapon = this.getItemBySlot(EquipmentSlot.MAINHAND);
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), level.getCurrentDifficultyAt(this.blockPosition()));
    }
    
    @Override
    public SoundEvent getCelebrateSound() {
        return SoundRegistry.GORYO_CELEBRATE;
    }
    
    // Immune to fire damage 
    @Override
    public boolean fireImmune() {
        return true;
    }
}
