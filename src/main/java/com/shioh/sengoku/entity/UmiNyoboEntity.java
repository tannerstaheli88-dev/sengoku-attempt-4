package com.shioh.sengoku.entity;

import com.shioh.sengoku.entity.ai.AdvancedMeleeAttackGoal;
import com.shioh.sengoku.entity.ai.CircleStrafeGoal;
import com.shioh.sengoku.entity.ai.WeaponBlockGoal;
import com.shioh.sengoku.entity.ai.PatrolCautiousEngagementGoal;
import com.shioh.sengoku.entity.ai.BanditBowAttackGoal;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

/**
 * Umi Nyobo entity - a coastal yokai swordswoman with samurai-like combat.
 */
public class UmiNyoboEntity extends AbstractIllager implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> DATA_WINDING_UP = SynchedEntityData.defineId(UmiNyoboEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_WINDUP_TICKS = SynchedEntityData.defineId(UmiNyoboEntity.class, EntityDataSerializers.INT);

    public UmiNyoboEntity(EntityType<? extends UmiNyoboEntity> type, Level level) {
        super(type, level);
        this.xpReward = 6;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_WINDING_UP, false);
        builder.define(DATA_WINDUP_TICKS, 0);
    }

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

        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Patrol gating goal - makes patrols cautious and watch from distance
        this.goalSelector.addGoal(1, new PatrolCautiousEngagementGoal(this));
        // Bow-capable behavior (only active when holding a bow)
        this.goalSelector.addGoal(2, new BanditBowAttackGoal(this, 1.0, 20, 15.0F));
        // Place blocking lower priority than bow so bow-charge animation runs correctly
        this.goalSelector.addGoal(3, new WeaponBlockGoal(this));
        this.goalSelector.addGoal(4, new AdvancedMeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(4, new CircleStrafeGoal(this));
        this.goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.33)
            .add(Attributes.FOLLOW_RANGE, 30.0)
            .add(Attributes.MAX_HEALTH, 60.0)
            .add(Attributes.ATTACK_DAMAGE, 8.0)
            .add(Attributes.ARMOR, 2.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // Small chance to spawn as an archer (uses a bow and fires tipped harming arrows)
        try {
            if (random.nextFloat() < 0.20f) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                return;
            }
        } catch (Throwable ignored) {}

        // Prefer mod's diamond naginata when available, fall back to iron sword
        try {
            net.minecraft.world.item.Item naginata = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("sengoku", "diamond_naginata"));
            if (naginata != null && naginata != Items.AIR) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(naginata));
                return;
            }
        } catch (Throwable ignored) {}
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
    }

    // RangedAttackMob implementation - fire tipped harming arrows when using a bow
    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack bow = this.getMainHandItem();
        if (bow.getItem() == Items.BOW) {
            // Create a single tipped arrow itemstack with HARMING potion (set NBT directly)
            ItemStack tipped = new ItemStack(Items.TIPPED_ARROW);
            // Prefer using Arrow#setEffectsFromItem when available so potion effects are applied
            Arrow arrow = new Arrow(this.level(), this, tipped, null);
            double deltaX = target.getX() - this.getX();
            double deltaY = target.getY(0.3333333333333333D) - arrow.getY();
            double deltaZ = target.getZ() - this.getZ();
            double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            arrow.shoot(deltaX, deltaY + distance * 0.20000000298023224D, deltaZ, 1.6F, 14 - this.level().getDifficulty().getId() * 4);
            this.playSound(net.minecraft.sounds.SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level().addFreshEntity(arrow);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.UMI_NYOBO_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.UMI_NYOBO_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.UMI_NYOBO_HURT;
    }

    @Override
    public void playAmbientSound() {
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
            try {
                this.playSound(SoundRegistry.UMI_NYOBO_AMBIENT_AGGRO, 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        try {
            if (com.shioh.sengoku.entity.ai.WeaponBlockGoal.isCurrentlyBlocking(this)) {
                return AbstractIllager.IllagerArmPose.ATTACKING;
            }
        } catch (Throwable ignored) {}

        if (this.isUsingItem() && this.hasRangedWeapon()) {
            return AbstractIllager.IllagerArmPose.BOW_AND_ARROW;
        }
        if (this.hasRangedWeapon()) {
            return AbstractIllager.IllagerArmPose.CROSSBOW_HOLD;
        }
        if (this.isAggressive()) {
            return AbstractIllager.IllagerArmPose.ATTACKING;
        }
        return AbstractIllager.IllagerArmPose.NEUTRAL;
    }

    public boolean hasRangedWeapon() {
        return this.getMainHandItem().getItem() == Items.BOW;
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        try {
            if (other == null) return false;
            // Treat iron golems as allies
            if (other instanceof net.minecraft.world.entity.animal.IronGolem) return true;
            // Same type are allies
            if (other instanceof UmiNyoboEntity) return true;
            // Explicitly treat vanilla illagers as NOT allied so faction targeting works
            if (other instanceof AbstractIllager) return false;
        } catch (Throwable ignored) {}
        return super.isAlliedTo(other);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        // Don't attack same type
        if (target instanceof UmiNyoboEntity) return false;
        // Don't attack creative/spectator players
        if (target instanceof Player player) {
            if (player.isCreative() || player.isSpectator()) return false;
        }
        return target.isAlive();
    }

    /**
     * Spawn rule for Umi Nyobo: only spawn on solid ground (no fluids), and not in Peaceful.
     */
    public static boolean checkUmiNyoboSpawnRules(EntityType<UmiNyoboEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        try {
            // The spawn position provided by SpawnPlacements may be slightly above the ground
            // (e.g. due to heightmap quirks). Walk downward a few blocks to find the first
            // solid, non-fluid block with a collision shape and validate it as ground.
            BlockPos groundPos = null;
            final int MAX_SEARCH_DOWN = 3;
            for (int i = 1; i <= MAX_SEARCH_DOWN; i++) {
                BlockPos candidate = pos.below(i);
                BlockState candidateState = level.getBlockState(candidate);
                if (candidateState.isAir()) continue;
                FluidState fs = candidateState.getFluidState();
                if (!fs.isEmpty()) continue;
                if (candidateState.getCollisionShape(level, candidate).isEmpty()) continue;
                groundPos = candidate;
                break;
            }
            if (groundPos == null) return false;
            BlockState groundState = level.getBlockState(groundPos);
            // Only allow endstone or endstone bricks as valid ground for Umi Nyobo
            if (!(groundState.is(Blocks.END_STONE) || groundState.is(Blocks.END_STONE_BRICKS))) return false;
        } catch (Throwable ignored) {
            return false;
        }
        return Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (WeaponBlockGoal.isCurrentlyBlocking(this)) {
            this.playSound(amount >= 6.0F ? SoundRegistry.PARTIAL_PARRY : SoundRegistry.WEAPON_PARRY, 1.0F, 0.8F + this.random.nextFloat() * 0.4F);

            WeaponBlockGoal.spawnBlockFeedbackParticles(this, amount);

            Entity attacker = source.getEntity();
            if (!this.level().isClientSide && attacker instanceof LivingEntity livingAttacker) {
                Vec3 dir = this.position().subtract(livingAttacker.position()).normalize();
                this.setDeltaMovement(this.getDeltaMovement().add(dir.scale(0.25)));
                this.hurtMarked = true;
            }

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
        this.swing(InteractionHand.MAIN_HAND);
        boolean result = super.doHurtTarget(target);
        if (!this.level().isClientSide && result && target instanceof LivingEntity living) {
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
        // Small chance to drop weapon like other samurai variants
        this.setDropChance(EquipmentSlot.MAINHAND, 0.05F);
        // Ensure head slot is empty and cannot drop banners introduced by mixins
        try {
            this.setItemSlot(EquipmentSlot.HEAD, net.minecraft.world.item.ItemStack.EMPTY);
            this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        } catch (Throwable ignored) {}
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    public void applyRaidBuffs(ServerLevel level, int wave, boolean unusedFalse) {
        ItemStack weapon = this.getItemBySlot(EquipmentSlot.MAINHAND);
        this.populateDefaultEquipmentEnchantments(level, level.getRandom(), level.getCurrentDifficultyAt(this.blockPosition()));
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundRegistry.UMI_NYOBO_CELEBRATE;
    }
}
