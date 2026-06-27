package com.shioh.sengoku.entity;

import com.shioh.sengoku.entity.ai.StalkPlayerGoal;
import com.shioh.sengoku.entity.ai.YukiOnnaChargeAttackGoal;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import com.shioh.sengoku.entity.ai.YukiOnnaPatrolSpawner;

/**
 * Yuki Onna (Snow Woman) - A ghostly yokai
 * Stalks players from a distance, gets aggressive when stared at
 * Flies towards targets with raised arms, teleports during combat
 * Inflicts slowness and freezing effects on players
 */
public class YukiOnnaEntity extends Monster {
    
    private static final EntityDataAccessor<Boolean> DATA_CREEPY = 
        SynchedEntityData.defineId(YukiOnnaEntity.class, EntityDataSerializers.BOOLEAN);
    
    private static final EntityDataAccessor<Boolean> DATA_STARED_AT = 
        SynchedEntityData.defineId(YukiOnnaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> DATA_STALKING_TARGET =
        SynchedEntityData.defineId(YukiOnnaEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    
    private int targetChangeTime;
    private static final int STARE_ATTACK_COOLDOWN = 100;
    private static final int ICE_BREATH_DURATION = 20;
    private static final int ICE_BREATH_COOLDOWN_TICKS = 80;
    private int iceBreathTicks;
    private int iceBreathCooldown;
    private boolean wasAggro = false; // Track previous aggro state for music management
    private int teleportCooldownAfterBreath = 0; // Prevent immediate teleporting after using ice breath
    
    public YukiOnnaEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 10;
        this.moveControl = new net.minecraft.world.entity.ai.control.FlyingMoveControl(this, 20, true);
    }
    
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // When aggressive, fly charge towards player like a vex
        this.goalSelector.addGoal(1, new YukiOnnaChargeAttackGoal(this));
        this.goalSelector.addGoal(2, new StalkPlayerGoal(this, 0.25)); // Slow stalking speed
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 48.0F)); // Always watch from far away
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Only target players who get too close (8 blocks) and aren't in creative/spectator
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, 
            (player) -> {
                if (player instanceof Player p) {
                    // Don't target creative or spectator players
                    if (p.isCreative() || p.isSpectator()) {
                        return false;
                    }
                    // Only target if player is within 8 blocks (close range)
                    return this.distanceToSqr(p) < 64.0; // 8 * 8 = 64
                }
                return false;
            }));
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 70.0)
            .add(Attributes.MOVEMENT_SPEED, 0.36)
            .add(Attributes.ATTACK_DAMAGE, 11.0)
            .add(Attributes.FOLLOW_RANGE, 64.0)
            .add(Attributes.ARMOR, 6.0)
            .add(Attributes.ARMOR_TOUGHNESS, 2.0)
            .add(Attributes.FLYING_SPEED, 0.85); // Flying speed for when aggressive
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CREEPY, false);
        builder.define(DATA_STARED_AT, false);
        builder.define(DATA_STALKING_TARGET, Optional.empty());
    }
    
    @Override
    public void tick() {
        LivingEntity target = this.getTarget();
        boolean isAggro = target != null;
        
        if (!this.level().isClientSide && target != null && this.getStalkingTargetUuid().isPresent()) {
            this.clearStalkingTarget();
        }
        
        // Handle music transitions
        if (!this.level().isClientSide && isAggro != wasAggro) {
            if (isAggro) {
                // Entered aggro - play music for all nearby players
                playMusicForNearbyPlayers();
            } else {
                // Left aggro - stop music for all nearby players
                stopMusicForNearbyPlayers();
            }
            wasAggro = isAggro;
        }
        
        // Allow flying (ignore gravity when aggressive)
        if (target != null) {
            this.setNoGravity(true);
        } else {
            this.setNoGravity(false);
        }
        
        if (this.level().isClientSide) {
            // Spawn snow particles around Yuki Onna
            if (this.random.nextInt(10) == 0) {
                this.level().addParticle(ParticleTypes.SNOWFLAKE, 
                    this.getRandomX(0.5), 
                    this.getRandomY(), 
                    this.getRandomZ(0.5), 
                    0.0, -0.1, 0.0);
            }
        } else {
            // Check if being stared at by a player (like enderman)
            if (target == null) {
                Player staringPlayer = this.getPlayerStaringAtMe();
                if (staringPlayer != null) {
                    // Player is staring - get angry!
                    this.setTarget(staringPlayer);
                    this.playSound(SoundRegistry.YUKI_ONNA_STARE, 1.0F, 1.0F);
                    target = staringPlayer;
                }
            }
            if (target != null) {
                this.handleIceBreathAttack(target);
            } else {
                this.resetIceBreath();
            }
        }

        // Reduce teleport-after-breath cooldown if present
        if (!this.level().isClientSide && this.teleportCooldownAfterBreath > 0) {
            --this.teleportCooldownAfterBreath;
        }
        
        super.tick();
    }
    
    private void playMusicForNearbyPlayers() {
        if (this.level() instanceof ServerLevel serverLevel) {
            // Find all players within hearing distance (64 blocks)
            java.util.List<Player> nearbyPlayers = serverLevel.getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(64.0),
                player -> !player.isSpectator()
            );
            
            for (Player player : nearbyPlayers) {
                // Play music at the player's position so they hear it
                serverLevel.playSound(null, player.blockPosition(), 
                    SoundRegistry.YUKI_ONNA_MUSIC, 
                    net.minecraft.sounds.SoundSource.MUSIC, 
                    1.0F, 1.0F);
            }
        }
    }
    
    private void stopMusicForNearbyPlayers() {
        // Explicitly stop the boss music for nearby players by sending a stop sound packet.
        // RAM prices are up bc of this btw
        // This mirrors the behaviour of boss music like the Ender Dragon: when the
        // boss is no longer active (lost aggro or dead) the music must be stopped.
        if (this.level() instanceof ServerLevel serverLevel) {
            java.util.List<Player> nearbyPlayers = serverLevel.getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(64.0),
                player -> !player.isSpectator()
            );

            ResourceLocation musicId = com.shioh.sengoku.Constants.ID("yuki_onna_music");

            for (Player player : nearbyPlayers) {
                if (player instanceof ServerPlayer serverPlayer) {
                    ClientboundStopSoundPacket pkt = new ClientboundStopSoundPacket(musicId, SoundSource.MUSIC);
                    serverPlayer.connection.send(pkt);
                }
            }
        }
    }
    
    @Override
    public void die(DamageSource damageSource) {
        super.die(damageSource);
        // Stop music on death
        if (!this.level().isClientSide) {
            stopMusicForNearbyPlayers();
            if (this.level() instanceof ServerLevel serverLevel) {
                YukiOnnaPatrolSpawner.recordDeath(serverLevel, serverLevel.getGameTime());
            }
        }
    }
    
    public boolean isCreepy() {
        return this.entityData.get(DATA_CREEPY);
    }
    
    public void setCreepy(boolean creepy) {
        this.entityData.set(DATA_CREEPY, creepy);
    }

    public Optional<UUID> getStalkingTargetUuid() {
        return this.entityData.get(DATA_STALKING_TARGET);
    }

    public void setStalkingTarget(@Nullable UUID uuid) {
        this.entityData.set(DATA_STALKING_TARGET, Optional.ofNullable(uuid));
    }

    public void setStalkingTarget(@Nullable Player player) {
        this.setStalkingTarget(player != null ? player.getUUID() : null);
    }

    public void clearStalkingTarget() {
        this.entityData.set(DATA_STALKING_TARGET, Optional.empty());
    }
    
    public boolean isBeingStaredAt() {
        return this.entityData.get(DATA_STARED_AT);
    }
    
    /**
     * Check if any player is looking directly at this Yuki Onna (like enderman detection)
     * @return The player staring at her, or null if none
     */
    private Player getPlayerStaringAtMe() {
        // Find all nearby players within 64 blocks
        java.util.List<Player> nearbyPlayers = this.level().getEntitiesOfClass(
            Player.class,
            this.getBoundingBox().inflate(64.0),
            player -> !player.isSpectator() && !player.isCreative()
        );
        
        for (Player player : nearbyPlayers) {
            // Check if player is looking at us
            if (this.isLookingAtMe(player)) {
                return player;
            }
        }
        
        return null;
    }
    
    /**
     * Check if a specific player is looking directly at this entity
     * Based on vanilla enderman detection logic but much stricter
     */
    private boolean isLookingAtMe(Player player) {
        // Get player's look vector
        Vec3 playerLook = player.getViewVector(1.0F).normalize();
        Vec3 eyePos = player.getEyePosition(1.0F);
        
        // Get vector from player to this entity's eyes specifically
        Vec3 toEntity = new Vec3(
            this.getX() - player.getX(),
            this.getEyeY() - player.getEyeY(),
            this.getZ() - player.getZ()
        );
        
        double distance = toEntity.length();
        toEntity = toEntity.normalize();
        
        // Dot product tells us if player is looking in our direction
        double dotProduct = playerLook.dot(toEntity);
        
        // Require very precise focus (roughly within 7 degrees)
        if (dotProduct <= 0.992D) {
            return false;
        }

        if (distance >= 48.0D) {
            return false;
        }

        if (!player.hasLineOfSight(this)) {
            return false;
        }

        Vec3 reach = eyePos.add(playerLook.scale(distance + 1.0D));
        AABB targetBox = this.getBoundingBox().inflate(0.25D);
        return targetBox.clip(eyePos, reach).isPresent();
    }

    private void handleIceBreathAttack(LivingEntity target) {
        if (this.iceBreathCooldown > 0) {
            this.iceBreathCooldown--;
        }

        double distanceSq = this.distanceToSqr(target);
        boolean inReach = distanceSq <= 16.0D; // Roughly four blocks for closer combat

        if (!inReach || !this.hasLineOfSight(target)) {
            if (this.iceBreathTicks > 0) {
                this.iceBreathTicks = 0;
                this.iceBreathCooldown = Math.max(this.iceBreathCooldown, 20);
            }
            return;
        }

        if (this.iceBreathTicks == 0 && this.iceBreathCooldown <= 0) {
            this.iceBreathTicks = ICE_BREATH_DURATION;
            this.playSound(SoundRegistry.YUKI_ONNA_BREATH, 1.1F, 0.9F + this.getRandom().nextFloat() * 0.2F);
        }

        if (this.iceBreathTicks > 0) {
            this.performIceBreathAttack(target);
            this.iceBreathTicks--;
            if (this.iceBreathTicks == 0) {
                this.iceBreathCooldown = ICE_BREATH_COOLDOWN_TICKS;
                // Set a short cooldown to prevent immediate teleporting after the breath finishes
                this.teleportCooldownAfterBreath = 60;
            }
        }
    }

    private void resetIceBreath() {
        this.iceBreathTicks = 0;
        if (this.iceBreathCooldown > 0) {
            this.iceBreathCooldown = Math.max(this.iceBreathCooldown - 1, 0);
        }
    }

    public static boolean checkYukiOnnaSpawnRules(EntityType<YukiOnnaEntity> type, ServerLevel level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (spawnType == MobSpawnType.SPAWNER || spawnType == MobSpawnType.COMMAND || spawnType == MobSpawnType.EVENT) {
            return true;
        }

        if (pos.getY() < level.getSeaLevel() - 6) {
            return false;
        }

        Holder<Biome> biome = level.getBiome(pos);
        if (!biome.value().coldEnoughToSnow(pos)) {
            return false;
        }

        if (level.getBrightness(LightLayer.BLOCK, pos) > 7) {
            return false;
        }

        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    private void performIceBreathAttack(LivingEntity target) {
        this.getLookControl().setLookAt(target, 45.0F, 30.0F);

        Vec3 origin = this.position().add(0.0D, this.getEyeHeight(), 0.0D);
        Vec3 targetPos = target.getEyePosition(1.0F);
        Vec3 direction = targetPos.subtract(origin);

        if (direction.lengthSqr() < 1.0E-4D) {
            return;
        }

        direction = direction.normalize();

        Vec3 breathEnd = origin.add(direction.scale(4.5D));

        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 1; i <= 8; i++) {
                Vec3 particlePos = origin.add(direction.scale(0.6D * i));
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, particlePos.x, particlePos.y, particlePos.z, 3,
                    direction.x * 0.05D, direction.y * 0.02D, direction.z * 0.05D, 0.0D);
            }
        } else {
            for (int i = 1; i <= 8; i++) {
                Vec3 particlePos = origin.add(direction.scale(0.6D * i));
                this.level().addParticle(ParticleTypes.SNOWFLAKE, particlePos.x, particlePos.y, particlePos.z,
                    direction.x * 0.02D, direction.y * 0.01D, direction.z * 0.02D);
            }
        }

        if (!this.level().isClientSide && this.iceBreathTicks % 4 == 0) {
            AABB breathZone = new AABB(origin, breathEnd).inflate(0.6D);
            if (target.getBoundingBox().inflate(0.3D).intersects(breathZone)) {
                target.hurt(this.damageSources().mobAttack(this), 3.5F);
                boolean blockedByPlayer = (target instanceof Player p && p.isBlocking());
                // If the target is a player and is blocking (shield), do not apply movement
                // slowdown or freezing ticks. Otherwise apply both effects.
                if (!blockedByPlayer) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 2));
                    target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze(), target.getTicksFrozen() + 60));
                }
            }
        }
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        
        // Teleport when hurt (reduced chance to be less annoying)
        if (source.getEntity() != null && this.random.nextFloat() < 0.08F) {
            this.teleportRandomly();
        }
        
        return super.hurt(source, amount);
    }
    
    /**
     * Teleports randomly nearby (like enderman)
     */
    protected boolean teleportRandomly() {
        if (!this.level().isClientSide() && this.isAlive()) {
            double x = this.getX() + (this.random.nextDouble() - 0.5) * 16.0;
            double y = this.getY() + (double)(this.random.nextInt(16) - 8);
            double z = this.getZ() + (this.random.nextDouble() - 0.5) * 16.0;
            
            return this.teleportToLocation(x, y, z);
        }
        
        return false;
    }
    
    /**
     * Teleports near target during combat (flanking position)
     */
    public boolean teleportNearTarget(LivingEntity target) {
        if (!this.level().isClientSide() && this.isAlive() && target != null) {
            // Respect teleport cooldown after using ice breath
            if (this.teleportCooldownAfterBreath > 0) return false;
            // Teleport to a position around the target (8-12 blocks away)
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double distance = 8.0 + this.random.nextDouble() * 4.0;
            
            double x = target.getX() + Math.cos(angle) * distance;
            double y = target.getY() + 2.0; // Slightly above target
            double z = target.getZ() + Math.sin(angle) * distance;
            
            return this.teleportToLocation(x, y, z);
        }
        
        return false;
    }
    
    /**
     * Teleports to specific coordinates
     */
    private boolean teleportToLocation(double x, double y, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
        
        while (pos.getY() > this.level().getMinBuildHeight() && !this.level().getBlockState(pos).blocksMotion()) {
            pos.move(0, -1, 0);
        }
        
        BlockState blockState = this.level().getBlockState(pos);
        boolean canTeleport = blockState.blocksMotion();
        boolean hasSpace = this.level().noCollision(this, this.getBoundingBox().move(pos));
        
        if (canTeleport && hasSpace && !this.level().isClientSide) {
            double prevX = this.getX();
            double prevY = this.getY();
            double prevZ = this.getZ();
            
            // Use custom teleport instead of randomTeleport to avoid vanilla particles
            this.teleportTo(x, y, z);
            boolean success = true;
            
            if (success && this.level() instanceof ServerLevel serverLevel) {
                spawnTeleportParticles(serverLevel, prevX, prevY, prevZ);
                spawnTeleportParticles(serverLevel, this.getX(), this.getY(), this.getZ());
                this.playSound(SoundRegistry.YUKI_ONNA_AMBIENT, 1.0F, 1.0F);
            }
            return success;
        }

        return false;
    }

    private void spawnTeleportParticles(ServerLevel level, double x, double y, double z) {
        level.sendParticles(ParticleTypes.SNOWFLAKE, x, y + 0.6D, z, 40, 0.6D, 0.8D, 0.6D, 0.02D);
        level.sendParticles(ParticleTypes.CLOUD, x, y + 0.2D, z, 6, 0.3D, 0.3D, 0.3D, 0.0D);
    }
    
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        
        if (hurt && target instanceof LivingEntity livingTarget) {
            // Apply freezing and slowness (MOVEMENT_SLOWNESS changed to SLOWNESS in 1.21)
            livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
            livingTarget.setTicksFrozen(livingTarget.getTicksFrozen() + 80);
        }
        
        return hurt;
    }
    
    @Override
    protected SoundEvent getAmbientSound() {
        // Use different ambient sounds based on aggro state (target != null)
        if (this.getTarget() != null) {
            return SoundRegistry.YUKI_ONNA_AMBIENT_AGGRO;
        }
        return SoundRegistry.YUKI_ONNA_AMBIENT;
    }
    
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.YUKI_ONNA_HURT;
    }
    
    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.YUKI_ONNA_DEATH;
    }
    
    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundRegistry.YUKI_ONNA_AMBIENT, 0.15F, 1.0F);
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Creepy", this.isCreepy());
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setCreepy(tag.getBoolean("Creepy"));
    }
    
    @Override
    public boolean canFreeze() {
        return false; // Immune to freezing
    }
    
    @Override
    public boolean isSensitiveToWater() {
        return false;
    }
    
    public boolean canWalkOnPowderedSnow() {
        return true; // Can walk on powder snow without sinking
    }
    
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false; // No fall damage (she floats)
    }
    
    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // Don't take fall damage
    }
}
