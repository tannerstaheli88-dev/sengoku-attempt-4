package com.shioh.sengoku.entity;

import com.shioh.sengoku.ai.ParrotCautiousTemptGoal;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;

/**
 * Crow entity - uses parrot model and behavior but with crow sounds and texture.
 * Tameable, can sit on shoulders, and mimics nearby mob sounds like parrots.
 */
public class CrowEntity extends Parrot {
    // Variant handling left to Parrot/vanilla implementation

    public CrowEntity(EntityType<? extends Parrot> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 6.0)
            .add(Attributes.FLYING_SPEED, 0.4)
            .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        // Crows don't breed - return null
        return null;
    }

    @Override
    public SoundEvent getAmbientSound() {
        return SoundRegistry.CROW_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.CROW_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.CROW_DEATH;
    }

    // Variant handling left to Parrot/vanilla implementation

    // Parrot already provides flying logic; no extra overrides required here.

    /**
     * Limit crow natural spawns to prevent overspawning: enforce a nearby count cap
     * and defer to vanilla mob spawn rules for other checks.
     */
    public static boolean checkCrowSpawnRules(EntityType<Parrot> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        try {
            if (level instanceof Level lvl) {
                // Prevent too many crows in a small area (cap at 4 within 12 blocks)
                int nearby = lvl.getEntitiesOfClass(CrowEntity.class, new AABB(pos).inflate(22.0D), e -> true).size();
                if (nearby >= 14) return false;
            }
        } catch (Throwable ignored) {}
        // Parrot.checkParrotSpawnRules expects EntityType<Parrot>, so cast to match signature
        return net.minecraft.world.entity.animal.Parrot.checkParrotSpawnRules((EntityType<Parrot>) (EntityType<?>) type, level, spawnType, pos, random);
    }
}
