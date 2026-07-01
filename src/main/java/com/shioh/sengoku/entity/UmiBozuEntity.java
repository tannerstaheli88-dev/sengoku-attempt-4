package com.shioh.sengoku.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class UmiBozuEntity extends Monster {

    private int curseCooldown = 0;
    private static final int CURSE_COOLDOWN_TICKS = 300;
    private int beamDisruptTicks = 0;
    private static final int BEAM_DISRUPT_DURATION = 5;
    boolean offeringDistracted = false;

    public UmiBozuEntity(EntityType<? extends UmiBozuEntity> type, Level level) {
        super(type, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 45, 10, 0.28F, 0.08F, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 4);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ELDER_GUARDIAN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ELDER_GUARDIAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ELDER_GUARDIAN_DEATH;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 300.0D)
            .add(Attributes.ATTACK_DAMAGE, 12.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.58D)
            .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 3.0D)
            .add(Attributes.FOLLOW_RANGE, 128.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new OfferingDistractGoal(this));
        this.goalSelector.addGoal(1, new DetectPlayerGoal(this));
        this.goalSelector.addGoal(2, new RiseToBossSurfaceGoal(this));
        this.goalSelector.addGoal(3, new SinkToFloorGoal(this));
        this.goalSelector.addGoal(3, new BeamBoatGoal(this));
        this.goalSelector.addGoal(4, new PursueAndAttackGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 128.0F, 1.0F));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isInWater()) {
            this.setAirSupply(300);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !this.level().isClientSide) {
            beamDisruptTicks = BEAM_DISRUPT_DURATION;
            offeringDistracted = false; // attacks cancel the offering distraction

            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.CRIT,
                    this.getX(), this.getEyeY(), this.getZ(),
                    20, 1.5, 1.0, 1.5, 0.1
                );
            }
        }
        return result;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            for (int b = 0; b < 4; b++) {
                double px = this.getX() + (this.random.nextDouble() - 0.5) * 3.0;
                double pz = this.getZ() + (this.random.nextDouble() - 0.5) * 3.0;
                double py = this.getY();
                for (int i = 0; i < 16; i++) {
                    BlockPos check = BlockPos.containing(px, py + i, pz);
                    if (!this.level().getFluidState(check).is(FluidTags.WATER)) {
                        py = py + i;
                        break;
                    }
                }
                this.level().addParticle(com.shioh.sengoku.registry.ParticleRegistry.FOG_FLAT, px, py - 0.5, pz, 0, 0.1, 0);
                if (this.random.nextInt(3) == 0) {
                    this.level().addParticle(net.minecraft.core.particles.ParticleTypes.SPLASH, px, py, pz,
                        (this.random.nextDouble() - 0.5) * 0.5, 0.2, (this.random.nextDouble() - 0.5) * 0.5);
                }
            }
            return;
        }

        if (beamDisruptTicks > 0) beamDisruptTicks--;
        if (curseCooldown > 0) curseCooldown--;

        LivingEntity target = this.getTarget();
        if (target != null && curseCooldown == 0 && !offeringDistracted) {
            if (target instanceof Player player) {
                player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 260, 1, false, true, true));
            }
            curseCooldown = CURSE_COOLDOWN_TICKS;
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2));
        }
        return hit;
    }

    // ── Offering distract goal ────────────────────────────────────────────
    static class OfferingDistractGoal extends Goal {
        private final UmiBozuEntity mob;
        private net.minecraft.world.entity.item.ItemEntity targetItem = null;
        private int distractedTicks = 0;
        private boolean distracted = false;
        private static final int DISTRACT_DURATION = 200;
        private static final double PICKUP_RANGE = 48.0D;

        public OfferingDistractGoal(UmiBozuEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.noneOf(Flag.class));
        }

        @Override
        public boolean canUse() {
            if (distracted && distractedTicks > 0 && mob.offeringDistracted) return true;

            AABB box = mob.getBoundingBox().inflate(PICKUP_RANGE);
            List<net.minecraft.world.entity.item.ItemEntity> items = mob.level().getEntitiesOfClass(
                net.minecraft.world.entity.item.ItemEntity.class, box,
                item -> !item.isRemoved() && isOffering(item)
            );

            if (items.isEmpty()) return false;

            targetItem = null;
            double best = Double.MAX_VALUE;
            for (net.minecraft.world.entity.item.ItemEntity item : items) {
                double d = mob.distanceToSqr(item);
                if (d < best) { best = d; targetItem = item; }
            }
            return targetItem != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (!mob.offeringDistracted) return false;
            if (distracted && distractedTicks > 0) return true;
            if (targetItem == null || targetItem.isRemoved()) return false;
            return mob.distanceTo(targetItem) < PICKUP_RANGE;
        }

        @Override
        public void start() {
            distracted = false;
            distractedTicks = 0;
        }

        @Override
        public void stop() {
            targetItem = null;
            distracted = false;
            distractedTicks = 0;
            mob.offeringDistracted = false;
        }

        @Override
        public void tick() {
            if (distracted) {
                distractedTicks--;
                mob.setTarget(null);

                if (mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    if (mob.random.nextInt(3) == 0) {
                        double angle = mob.random.nextDouble() * Math.PI * 2.0;
                        double r = 1.5 + mob.random.nextDouble() * 2.0;
                        serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                            mob.getX() + Math.cos(angle) * r,
                            mob.getY() + 1.0 + mob.random.nextDouble() * 2.0,
                            mob.getZ() + Math.sin(angle) * r,
                            1, 0, 0.02, 0, 0.01
                        );
                    }
                }

                if (distractedTicks <= 0) {
                    distracted = false;
                    mob.offeringDistracted = false;
                    targetItem = null;
                }
                return;
            }

            if (targetItem == null || targetItem.isRemoved()) return;

            double dx = targetItem.getX() - mob.getX();
            double dy = targetItem.getY() - mob.getY();
            double dz = targetItem.getZ() - mob.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            mob.getLookControl().setLookAt(targetItem, 30.0F, 30.0F);
            mob.setTarget(null);

            if (dist > 1.5) {
                Vec3 vel = mob.getDeltaMovement();
                double speed = 0.55D;
                double ax = ((dx / dist) * speed - vel.x) * 0.25;
                double ay = ((dy / dist) * speed - vel.y) * 0.1;
                double az = ((dz / dist) * speed - vel.z) * 0.25;
                mob.setDeltaMovement(vel.x + ax, vel.y + ay, vel.z + az);
            } else {
                mob.playSound(SoundEvents.ELDER_GUARDIAN_AMBIENT, 2.0F, 0.4F);

                if (mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                        targetItem.getX(), targetItem.getY(), targetItem.getZ(),
                        30, 1.5, 1.0, 1.5, 0.15
                    );
                }

                // Grant advancement to nearby players before discarding
AABB rewardBox = mob.getBoundingBox().inflate(64.0D);
List<Player> nearbyPlayers = mob.level().getEntitiesOfClass(Player.class, rewardBox,
    p -> !p.isSpectator());
for (Player p : nearbyPlayers) {
    if (p instanceof net.minecraft.server.level.ServerPlayer sp) {
        net.minecraft.advancements.AdvancementHolder holder = sp.server.getAdvancements().get(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "sea_offering")
        );
        if (holder != null) {
            sp.getAdvancements().award(holder, "distract_umibozu");
        }
    }
}
targetItem.discard();
                distracted = true;
                mob.offeringDistracted = true;
                distractedTicks = DISTRACT_DURATION;
            }
        }

        private boolean isOffering(net.minecraft.world.entity.item.ItemEntity item) {
            try {
                net.minecraft.world.item.ItemStack stack = item.getItem();
                net.minecraft.core.Holder<net.minecraft.world.item.Item> holder =
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem());
                net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag =
                    net.minecraft.tags.TagKey.create(
                        net.minecraft.core.registries.Registries.ITEM,
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "umibozu_offering")
                    );
                return holder.is(tag);
            } catch (Throwable ignored) {
                return false;
            }
        }
    }

    // ── Detect player at night only ───────────────────────────────────────
    static class DetectPlayerGoal extends Goal {
        private final UmiBozuEntity mob;
        private int cooldown = 0;
        private static final double DETECT_RANGE = 128.0D;

        public DetectPlayerGoal(UmiBozuEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.noneOf(Flag.class));
        }

        @Override
        public boolean canUse() {
            if (mob.level().isDay()) {
                mob.setTarget(null);
                return false;
            }
            if (mob.offeringDistracted) return false;
            if (--cooldown > 0) return false;
            cooldown = 20;

            AABB box = new AABB(
                mob.getX() - DETECT_RANGE, mob.getY() - 64, mob.getZ() - DETECT_RANGE,
                mob.getX() + DETECT_RANGE, mob.getY() + 64, mob.getZ() + DETECT_RANGE
            );

            List<Player> candidates = mob.level().getEntitiesOfClass(Player.class, box,
                p -> !p.isSpectator() && !p.isCreative());

            if (candidates.isEmpty()) return false;

            Player nearest = null;
            double bestDist = Double.MAX_VALUE;
            for (Player p : candidates) {
                double d = mob.distanceToSqr(p);
                if (d < bestDist) { bestDist = d; nearest = p; }
            }

            if (nearest == null) return false;
            mob.setTarget(nearest);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (mob.level().isDay()) {
                mob.setTarget(null);
                return false;
            }
            if (mob.offeringDistracted) return false;
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void stop() {}
    }

    // ── Rise to surface when targeting a player at night ──────────────────
    static class RiseToBossSurfaceGoal extends Goal {
        private final UmiBozuEntity mob;

        public RiseToBossSurfaceGoal(UmiBozuEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (mob.level().isDay()) return false;
            if (mob.offeringDistracted) return false;
            if (mob.getTarget() == null) return false;
            return mob.isUnderWater();
        }

        @Override
        public boolean canContinueToUse() {
            return mob.getTarget() != null && mob.isUnderWater() && !mob.level().isDay() && !mob.offeringDistracted;
        }

        @Override
        public void tick() {
            Vec3 motion = mob.getDeltaMovement();
            mob.setDeltaMovement(motion.x, Math.min(motion.y + 0.15, 0.8), motion.z);
        }
    }

    // ── Sink to floor when no target or daytime ───────────────────────────
    static class SinkToFloorGoal extends Goal {
        private final UmiBozuEntity mob;

        public SinkToFloorGoal(UmiBozuEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return (mob.getTarget() == null || mob.level().isDay() || mob.offeringDistracted) && mob.isInWater();
        }

        @Override
        public boolean canContinueToUse() {
            return (mob.getTarget() == null || mob.level().isDay() || mob.offeringDistracted) && mob.isInWater();
        }

        @Override
        public void tick() {
            Vec3 motion = mob.getDeltaMovement();
            mob.setDeltaMovement(motion.x * 0.8, Math.max(motion.y - 0.04, -0.3), motion.z * 0.8);
        }
    }

    // ── Beam boat goal ────────────────────────────────────────────────────
    static class BeamBoatGoal extends Goal {
        private final UmiBozuEntity mob;
        private Boat targetBoat = null;
        private int beamCooldown = 0;
        private static final int BEAM_RANGE = 32;

        public BeamBoatGoal(UmiBozuEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.noneOf(Flag.class));
        }

        @Override
        public boolean canUse() {
            if (mob.level().isDay()) return false;
            if (mob.beamDisruptTicks > 0) return false;
            if (mob.offeringDistracted) return false;
            if (beamCooldown > 0) { beamCooldown--; return false; }

            AABB box = mob.getBoundingBox().inflate(BEAM_RANGE);
            List<Boat> boats = mob.level().getEntitiesOfClass(Boat.class, box,
                b -> b.getPassengers().stream().anyMatch(e -> e instanceof Player));

            if (boats.isEmpty()) return false;

            targetBoat = null;
            double best = Double.MAX_VALUE;
            for (Boat b : boats) {
                double d = mob.distanceToSqr(b);
                if (d < best) { best = d; targetBoat = b; }
            }
            return targetBoat != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (mob.level().isDay()) return false;
            if (mob.beamDisruptTicks > 0) return false;
            if (mob.offeringDistracted) return false;
            if (targetBoat == null || !targetBoat.isAlive()) return false;
            if (mob.distanceTo(targetBoat) > BEAM_RANGE) return false;
            return targetBoat.getPassengers().stream().anyMatch(e -> e instanceof Player);
        }

        @Override
        public void stop() {
            targetBoat = null;
            beamCooldown = 100;
        }

        @Override
        public void tick() {
            if (targetBoat == null) return;

            Vec3 start = mob.getEyePosition();
            Vec3 end = targetBoat.position().add(0, 0.5, 0);
            Vec3 dir = end.subtract(start);
            double length = dir.length();
            Vec3 step = dir.normalize().scale(0.5);
            int numParticles = (int)(length / 0.5);

            if (mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                for (int i = 0; i < numParticles; i++) {
                    Vec3 pos = start.add(step.scale(i));
                    serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.SOUL,
                        pos.x, pos.y, pos.z,
                        1, 0, 0, 0, 0
                    );
                    if (i % 3 == 0) {
                        serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.SMOKE,
                            pos.x, pos.y, pos.z,
                            1,
                            (mob.random.nextDouble() - 0.5) * 0.1,
                            0.02,
                            (mob.random.nextDouble() - 0.5) * 0.1,
                            0
                        );
                    }
                }
            }

            Vec3 boatVel = targetBoat.getDeltaMovement();
            targetBoat.setDeltaMovement(boatVel.x * 0.7, boatVel.y, boatVel.z * 0.7);
            targetBoat.hurtMarked = true;

            for (net.minecraft.world.entity.Entity passenger : targetBoat.getPassengers()) {
                if (passenger instanceof Player player) {
                    player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false, true));
                }
            }
        }
    }

    // ── Pursue and attack with windup indicator ───────────────────────────
    static class PursueAndAttackGoal extends Goal {
        private final UmiBozuEntity mob;
        private int attackCooldown = 0;
        private int windupTicks = 0;
        private static final int WINDUP_DURATION = 25;
        private boolean windingUp = false;
        private LivingEntity windupTarget = null;

        public PursueAndAttackGoal(UmiBozuEntity mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (mob.level().isDay()) return false;
            if (mob.offeringDistracted) return false;
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive() && !mob.isUnderWater();
        }

        @Override
        public boolean canContinueToUse() {
            if (mob.offeringDistracted) return false;
            LivingEntity target = mob.getTarget();
            return target != null && target.isAlive() && !mob.level().isDay();
        }

        @Override
        public void start() {
            attackCooldown = 0;
            windingUp = false;
            windupTicks = 0;
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
            windingUp = false;
            windupTicks = 0;
        }

@Override
public void tick() {
    LivingEntity target = mob.getTarget();
    if (target == null) return;

    if (attackCooldown > 0) attackCooldown--;

    mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

    double dx = target.getX() - mob.getX();
    double dz = target.getZ() - mob.getZ();
    double dist = Math.sqrt(dx * dx + dz * dz);

    if (!windingUp && dist > 1.0E-6) {
        double nx = dx / dist;
        double nz = dz / dist;

        // Movement (back to direct velocity control, no pathfinding)
        Vec3 vel = mob.getDeltaMovement();
        double speed = 0.28D;
        double ax = (nx * speed - vel.x) * 0.2;
        double az = (nz * speed - vel.z) * 0.2;
        mob.setDeltaMovement(vel.x + ax, vel.y, vel.z + az);

        // Turn the BODY to face the travel direction — this is what
        // was actually causing the moonwalk. yBodyRot drives the
        // walk/swim animation direction; it was never being updated.
        float targetYaw = (float) (Math.atan2(nz, nx) * (180.0 / Math.PI)) - 90.0F;
        float currentYaw = mob.getYRot();
        float delta = Mth.wrapDegrees(targetYaw - currentYaw);
        float maxTurnPerTick = 12.0F; // tune: higher = snappier turning
        float newYaw = currentYaw + Mth.clamp(delta, -maxTurnPerTick, maxTurnPerTick);

        mob.setYRot(newYaw);
        mob.yBodyRot = newYaw;
    }

    if (mob.level().isClientSide) return;

    if (dist < 5.0 && attackCooldown <= 0 && !windingUp) {
        windingUp = true;
        windupTicks = 0;
        windupTarget = target;
        mob.playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 2.0F, 0.5F);
    }

            if (windingUp) {
                windupTicks++;

                if (mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    float progress = (float) windupTicks / WINDUP_DURATION;
                    int particleCount = (int)(2 + progress * 8);
                    double spread = 0.5 + progress * 2.0;

                    for (int i = 0; i < particleCount; i++) {
                        double angle = mob.random.nextDouble() * Math.PI * 2.0;
                        double r = mob.random.nextDouble() * spread;
                        serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                            mob.getX() + Math.cos(angle) * r,
                            mob.getEyeY() - 0.5 + mob.random.nextDouble() * 1.5,
                            mob.getZ() + Math.sin(angle) * r,
                            1, 0, 0.05, 0, 0.02
                        );
                    }

                    if (windupTicks >= WINDUP_DURATION) {
                        serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                            mob.getX(), mob.getEyeY(), mob.getZ(),
                            3, 1.0, 0.5, 1.0, 0
                        );
                        serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.SOUL,
                            mob.getX(), mob.getEyeY(), mob.getZ(),
                            15, 2.0, 1.0, 2.0, 0.1
                        );
                    }
                }

                if (windupTicks >= WINDUP_DURATION) {
                    windingUp = false;
                    windupTicks = 0;

                    if (windupTarget != null && windupTarget.isAlive()) {
                        if (windupTarget instanceof Player p && (p.isCreative() || p.isSpectator())) return;
                        try { mob.swing(net.minecraft.world.InteractionHand.MAIN_HAND); } catch (Throwable ignored) {}
                        float dmg = (float) mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        try { windupTarget.hurt(mob.damageSources().mobAttack(mob), dmg); } catch (Throwable ignored) {}

                        double kbDx = windupTarget.getX() - mob.getX();
                        double kbDz = windupTarget.getZ() - mob.getZ();
                        double kbDist = Math.sqrt(kbDx * kbDx + kbDz * kbDz);
                        if (kbDist > 1.0E-6) {
                            double nx = kbDx / kbDist;
                            double nz = kbDz / kbDist;
                            double kbStrength = 2.5;
                            windupTarget.setDeltaMovement(nx * kbStrength, 0.4, nz * kbStrength);
                            windupTarget.hurtMarked = true;

                            if (windupTarget instanceof Player p && p.getVehicle() instanceof Boat boat) {
                                boat.setDeltaMovement(nx * kbStrength, 0.4, nz * kbStrength);
                                boat.hurtMarked = true;
                            }
                        }
                    }
                    windupTarget = null;
                    attackCooldown = 60;
                }
            }
        }
    }

    public static boolean checkUmiBozuSpawnRules(EntityType<UmiBozuEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
        try {
            if (!level.getFluidState(pos).is(FluidTags.WATER)) return false;
            if (!level.getFluidState(pos.below()).is(FluidTags.WATER)) return false;
            int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
            if (blockLight > 4) return false;
        } catch (Throwable ignored) {}
        return checkMobSpawnRules(type, level, spawnType, pos, random);
    }
}