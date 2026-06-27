package com.shioh.sengoku.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import com.shioh.sengoku.ai.CraneCautiousTemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

/**
 * Red-Crowned Crane entity with natural flight behavior.
 * Features smooth takeoffs, realistic gliding, and graceful landings.
 */
public class RedCrownedCraneEntity extends Animal implements FlyingAnimal {
    // Use raw cod/salmon as breeding/tempt items instead of seeds
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.COD, Items.SALMON);
    
    // Flight state tracking
    private int airborneTicks = 0;
    private boolean isInFlight = false;
    private Vec3 flightTarget = null;
    private int flightCooldown = 0;
    private int flightTicksActive = 0; // safety: count ticks spent in current flight
    
    // Flight phases for smooth transitions
    private FlightPhase flightPhase = FlightPhase.GROUNDED;
    private int phaseTimer = 0;
    // Helpers to detect runaway/moving-away situations during flight
    private double previousDistanceToTarget = Double.NaN;
    private int distanceIncreasingCounter = 0;
    
    // Animation stubs
    private boolean playingDrinkAnimation = false;
    private boolean playingFeatherAnimation = false;
    private static final EntityDataAccessor<Boolean> DATA_DRINKING = SynchedEntityData.defineId(RedCrownedCraneEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_PREENING = SynchedEntityData.defineId(RedCrownedCraneEntity.class, EntityDataSerializers.BOOLEAN);
    // Idle animation timers/cooldowns (ticks)
    private int drinkAnimationTimer = 0;      // remaining time for drink animation
    private int preenAnimationTimer = 0;      // remaining time for preen animation
    private int drinkCooldownTimer = 0;       // cooldown before next possible drink animation
    private int preenCooldownTimer = 0;       // cooldown before next possible preen animation
    
    // Step height maintenance
    private int stepHeightApplyTimer = 0;
    // Stuck detection for stepping single-block obstacles
    private int stepStuckCounter = 0;
    // Resting state after landing to prevent immediate wandering
    private boolean isResting = false;
    private int restTimer = 0;
    // (Simplified) we use vanilla TemptGoal instead of a custom tempt/flee goal

    public boolean isResting() {
        return this.isResting;
    }
    
    // Smooth velocity tracking for natural acceleration
    private Vec3 targetVelocity = Vec3.ZERO;
    // Smoothed movement factor used by renderers to scale walk animation speed (0..1)
    private float moveAnimationFactor = 0.0F;
    // Throttle: how often to run heavier server-side updates (in ticks)
    private static final int HEAVY_TICK_INTERVAL = 4;
    // How often to update velocity smoothing
    private static final int VELOCITY_TICK_INTERVAL = 2;
    // Mark whether we've successfully applied stepHeight via reflection in constructor
    private boolean stepHeightSet = false;
    
    private enum FlightPhase {
        GROUNDED,      // On the ground
        TAKEOFF,       // Initial powerful wingbeats
        ASCENDING,     // Climbing to cruise altitude
        GLIDING,       // Smooth gliding flight
        DESCENDING,    // Preparing to land
        LANDING        // Final approach
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    public RedCrownedCraneEntity(EntityType<? extends Animal> type, Level world) {
        super(type, world);
        // Allow stepping up full blocks
        try {
            java.lang.reflect.Field stepHeightField = net.minecraft.world.entity.Mob.class.getDeclaredField("stepHeight");
            stepHeightField.setAccessible(true);
                stepHeightField.setFloat(this, 3.0F);
                this.stepHeightSet = true;
        } catch (Exception ignored) {
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 8.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.FLYING_SPEED, 0.3D)
            // Ensure step height is exposed as an attribute so it's saved in entity NBT
            .add(Attributes.STEP_HEIGHT, 3.0D);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.4D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        // Use our custom cautious tempt/flee goal (approach when player holds seed-like items;
        // perform a one-shot takeoff when a player gets too close) — respect config
        if (com.shioh.sengoku.config.SengokuConfig.getInstance().cranesFleeEnabled) {
            this.goalSelector.addGoal(3, new CraneCautiousTemptGoal(this, 1.25D));
        }
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        // Override the stroll goal so the crane can "rest" after landing
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                if (RedCrownedCraneEntity.this.isResting) return false;
                return super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    public boolean isFood(ItemStack stack) {
        return FOOD_ITEMS.test(stack);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return com.shioh.sengoku.registry.ModEntities.RED_CROWNED_CRANE.create(level);
    }

    @Override
    public void tick() {
        super.tick();
        
        
        // Track airborne state
        if (!this.onGround()) {
            this.airborneTicks++;
        } else {
            this.airborneTicks = 0;
            if (this.flightPhase != FlightPhase.GROUNDED && this.flightPhase != FlightPhase.LANDING) {
                this.flightPhase = FlightPhase.GROUNDED;
                this.isInFlight = false;
            }
        }

        // Throttle heavier server-side updates to reduce per-tick CPU cost
        if (!this.level().isClientSide()) {
            if (this.tickCount % HEAVY_TICK_INTERVAL == 0) {
                maintainStepHeight();
                handleFlightBehavior();

                // Rest timer runs on server side: decrement and clear resting when expired
                if (this.restTimer > 0) {
                    this.restTimer--;
                    if (this.restTimer <= 0) {
                        this.isResting = false;
                        try {
                            this.getNavigation().stop();
                        } catch (Throwable ignored) {}
                    }
                }

                // Idle animation updates (server-side controls state)
                updateIdleAnimations();
            } else {
                // Light maintenance: still decrement rest timer for accurate timing
                if (this.restTimer > 0) {
                    this.restTimer--;
                    if (this.restTimer <= 0) this.isResting = false;
                }
            }
        }
        
        // If we're resting after landing, keep the crane completely still
        if (!this.level().isClientSide() && this.isResting) {
            try {
                this.getNavigation().stop();
                this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D); // Keep Y for gravity
                this.targetVelocity = Vec3.ZERO;
            } catch (Throwable ignored) {
            }
            return; // skip movement smoothing while resting
        }

        // Apply smooth velocity transitions less often to save CPU
        if (this.tickCount % VELOCITY_TICK_INTERVAL == 0) {
            applyVelocitySmoothing();
        }

        // Update smoothed movement factor for renderers to use when driving walk animations.
        // This maps horizontal speed to a 0..1 factor and smooths it so animations aren't stiff.
        double hvx = this.getDeltaMovement().x;
        double hvz = this.getDeltaMovement().z;
        double horizLen = Math.sqrt(hvx * hvx + hvz * hvz);
        // Map expected walk speed (~0.0 - 0.3) to 0..1, clamp - adjusted for crane's actual speed
        double desired = Math.min(1.0D, horizLen / 0.25D);
        // Smooth with lerp - faster response
        this.moveAnimationFactor = (float)lerp(this.moveAnimationFactor, desired, 0.25D);
    }
    
    private void maintainStepHeight() {
        // Avoid repeated reflection calls every tick; constructor attempts to apply stepHeight.
        if (this.stepHeightApplyTimer <= 0) {
            this.stepHeightApplyTimer = 40;
        } else {
            this.stepHeightApplyTimer--;
        }
    }
    
    private void handleFlightBehavior() {
        if (this.flightCooldown > 0) {
            this.flightCooldown--;
        }
        
        // Simplified: do NOT auto-initiate flight when players are nearby (parrot-like behavior)
        // Flight can still occur through other triggers, but proximity won't force takeoff.
        
        // Handle active flight
        if (this.isInFlight && this.flightTarget != null) {
            this.flightTicksActive++;
            // Safety: if flight lasts too long, force landing to avoid runaway
            // Reduced from 400 ticks (~20s) to 100 ticks (~5s) to prevent excessive flights
            if (this.flightTicksActive > 100) {
                endFlight();
            } else {
                updateFlightMovement();
            }
        }
        
        // Auto-hop over small obstacles when grounded
        if (this.onGround() && this.horizontalCollision && !this.isInFlight) {
            Vec3 motion = this.getDeltaMovement();
            // regular small hop to step up
            this.setDeltaMovement(motion.x, 0.42D, motion.z);

            // detect if we are stuck sliding into the obstacle (very low horizontal speed)
            double horiz = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            if (horiz < 0.06D) {
                this.stepStuckCounter++;
            } else {
                this.stepStuckCounter = 0;
            }

            // If stuck for a couple ticks, force a stronger upward push and small forward nudge
            if (this.stepStuckCounter > 2) {
                Vec3 look = this.getLookAngle();
                // fallback if look is degenerate
                if (look == null || (Math.abs(look.x) < 1e-6 && Math.abs(look.z) < 1e-6)) {
                    look = new Vec3((this.random.nextDouble() - 0.5D), 0.0D, (this.random.nextDouble() - 0.5D)).normalize();
                }

                // check if the block in front is a single-block obstacle (block ahead solid, block above is air)
                int ddx = (int)Math.signum(look.x);
                int ddz = (int)Math.signum(look.z);
                if (ddx == 0 && ddz == 0) {
                    // fallback: nudge forward along current yaw
                    double yawRad = Math.toRadians(this.getYRot());
                    ddx = (int)Math.signum(Math.cos(yawRad));
                    ddz = (int)Math.signum(Math.sin(yawRad));
                    if (ddx == 0 && ddz == 0) {
                        ddx = 1;
                        ddz = 0;
                    }
                }

                BlockPos aheadPos = this.blockPosition().offset(ddx, 0, ddz);
                double nx = aheadPos.getX() + 0.5D;
                double ny = aheadPos.getY() + 1.0D;
                double nz = aheadPos.getZ() + 0.5D;
                // place entity neatly on top of that block and give a stronger forward nudge
                // Instead of teleporting through blocks, do a simple jump like other mobs.
                // Use a much stronger upward impulse and forward nudge so the crane reliably
                // clears full blocks. Call the vanilla jump helper so physics and sound
                // behave like other mobs.
                this.setDeltaMovement(look.x * 0.8D, 1.0D, look.z * 0.8D);
                try {
                    this.jumpFromGround();
                } catch (Throwable ignored) {
                }

                // ensure we're considered grounded/falling appropriately (we're not in flight)
                this.isInFlight = false;
                this.flightPhase = FlightPhase.GROUNDED;

                this.stepStuckCounter = 0;
            }
        }
    }
    
    private boolean isPlayerHoldingFood(Player player) {
        try {
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            return FOOD_ITEMS.test(mainHand) || FOOD_ITEMS.test(offHand);
        } catch (Throwable ignored) {
            return false;
        }
    }
    
    private void initiateTakeoff(Player fromPlayer) {
        // Calculate escape direction
        Vec3 awayDirection = new Vec3(
            this.getX() - fromPlayer.getX(),
            0.0D,
            this.getZ() - fromPlayer.getZ()
        ).normalize();
        // If player is exactly on us or direction is degenerate, pick a random escape direction
        if (awayDirection.equals(Vec3.ZERO)) {
            awayDirection = new Vec3((this.random.nextDouble() - 0.5D), 0.0D, (this.random.nextDouble() - 0.5D)).normalize();
        }
        
        // Set flight target - away from player and upward
        double targetX = this.getX() + awayDirection.x * 20.0D + (this.random.nextDouble() - 0.5D) * 8.0D;
        double targetZ = this.getZ() + awayDirection.z * 20.0D + (this.random.nextDouble() - 0.5D) * 8.0D;
        double targetY = clampFlightTargetY(this.getY() + 15.0D + this.random.nextDouble() * 10.0D);
        
        this.flightTarget = new Vec3(targetX, targetY, targetZ);
        this.isInFlight = true;
        this.flightPhase = FlightPhase.TAKEOFF;
        this.phaseTimer = 20; // 1 second takeoff phase
        this.flightCooldown = 200; // 10 seconds before next takeoff
        
        // Initial gentle upward push
        Vec3 currentMotion = this.getDeltaMovement();
        this.targetVelocity = new Vec3(
            awayDirection.x * 0.3D,
            0.5D,
            awayDirection.z * 0.3D
        );
        // cancel resting if we were resting
        this.isResting = false;
        this.restTimer = 0;
        // reset flight safety counter
        this.flightTicksActive = 0;
        // reset distance helpers
        this.previousDistanceToTarget = Double.NaN;
        this.distanceIncreasingCounter = 0;
    }

    /**
     * Public helper: short-range flee away from a player. This produces a smaller, nearby
     * flight target so the crane flies away a short distance and lands rather than
     * performing the long random flight used by `startTakeoff()`.
     */
    public void fleeAwayFrom(Player fromPlayer) {
        Vec3 awayDirection = new Vec3(
            this.getX() - fromPlayer.getX(),
            0.0D,
            this.getZ() - fromPlayer.getZ()
        );
        if (awayDirection.equals(Vec3.ZERO)) {
            awayDirection = new Vec3((this.random.nextDouble() - 0.5D), 0.0D, (this.random.nextDouble() - 0.5D)).normalize();
        } else {
            awayDirection = awayDirection.normalize();
        }

        // Shorter flee distance so the crane lands soon
        double horizDist = 8.0D + this.random.nextDouble() * 6.0D; // 8..14 blocks
        double targetX = this.getX() + awayDirection.x * horizDist + (this.random.nextDouble() - 0.5D) * 3.0D;
        double targetZ = this.getZ() + awayDirection.z * horizDist + (this.random.nextDouble() - 0.5D) * 3.0D;
        double targetY = clampFlightTargetY(this.getY() + 6.0D + this.random.nextDouble() * 4.0D); // 6..10 blocks up

        this.flightTarget = new Vec3(targetX, targetY, targetZ);
        this.isInFlight = true;
        this.flightPhase = FlightPhase.TAKEOFF;
        this.phaseTimer = 10; // shorter takeoff phase
        this.flightCooldown = 80;

        // Stronger immediate push to get airborne
        this.targetVelocity = new Vec3(awayDirection.x * 0.5D, 0.6D, awayDirection.z * 0.5D);
        this.isResting = false;
        this.restTimer = 0;
        // reset flight safety counter
        this.flightTicksActive = 0;
        // reset distance helpers
        this.previousDistanceToTarget = Double.NaN;
        this.distanceIncreasingCounter = 0;
    }

    /**
     * Flee away from any entity (player or non-player). Mirrors `fleeAwayFrom(Player)`
     * but accepts a generic `Entity` so other fast-moving entities (horses, boats, mobs)
     * can trigger the short flee behavior.
     */
    public void fleeAwayFrom(Entity fromEntity) {
        Vec3 awayDirection = new Vec3(
            this.getX() - fromEntity.getX(),
            0.0D,
            this.getZ() - fromEntity.getZ()
        );
        if (awayDirection.equals(Vec3.ZERO)) {
            awayDirection = new Vec3((this.random.nextDouble() - 0.5D), 0.0D, (this.random.nextDouble() - 0.5D)).normalize();
        } else {
            awayDirection = awayDirection.normalize();
        }

        double horizDist = 8.0D + this.random.nextDouble() * 6.0D; // 8..14 blocks
        double targetX = this.getX() + awayDirection.x * horizDist + (this.random.nextDouble() - 0.5D) * 3.0D;
        double targetZ = this.getZ() + awayDirection.z * horizDist + (this.random.nextDouble() - 0.5D) * 3.0D;
        double targetY = clampFlightTargetY(this.getY() + 6.0D + this.random.nextDouble() * 4.0D);

        this.flightTarget = new Vec3(targetX, targetY, targetZ);
        this.isInFlight = true;
        this.flightPhase = FlightPhase.TAKEOFF;
        this.phaseTimer = 10; // shorter takeoff phase
        this.flightCooldown = 80;

        this.targetVelocity = new Vec3(awayDirection.x * 0.5D, 0.6D, awayDirection.z * 0.5D);
        this.isResting = false;
        this.restTimer = 0;
        this.flightTicksActive = 0;
        this.previousDistanceToTarget = Double.NaN;
        this.distanceIncreasingCounter = 0;
    }
    
    private void updateFlightMovement() {
        Vec3 toTarget = this.flightTarget.subtract(this.position());
        double distanceToTarget = toTarget.length();
        // Detect if distance to target is increasing for many consecutive ticks.
        if (!Double.isNaN(this.previousDistanceToTarget)) {
            if (distanceToTarget > this.previousDistanceToTarget + 0.5D) {
                this.distanceIncreasingCounter++;
            } else {
                this.distanceIncreasingCounter = 0;
            }
            // If we're getting further away for a sustained period, abort flight.
            if (this.distanceIncreasingCounter > 20) {
                endFlight();
                this.previousDistanceToTarget = Double.NaN;
                this.distanceIncreasingCounter = 0;
                return;
            }
        }
        this.previousDistanceToTarget = distanceToTarget;
        
        // Update flight phase based on progress
        updateFlightPhase(distanceToTarget);
        
        // Calculate target velocity based on flight phase
        calculateTargetVelocity(toTarget, distanceToTarget);
        
        // Decrement phase timer
        if (this.phaseTimer > 0) {
            this.phaseTimer--;
        }
        
        // End flight if close to target or on ground during landing
        if (distanceToTarget < 2.0D || (this.onGround() && this.flightPhase == FlightPhase.LANDING)) {
            endFlight();
        }
    }
    
    private void updateFlightPhase(double distanceToTarget) {
        double currentHeight = this.getY();
        double targetHeight = this.flightTarget.y;
        
        switch (this.flightPhase) {
            case TAKEOFF:
                // If we've somehow climbed well above the target, go into descending to recover.
                if (currentHeight > targetHeight + 6.0D) {
                    this.flightPhase = FlightPhase.DESCENDING;
                    this.phaseTimer = 40;
                    break;
                }
                // Transition after the takeoff timer elapses. Use timer-only transition
                // to avoid brittle comparisons against absolute world heights.
                if (this.phaseTimer <= 0) {
                    this.flightPhase = FlightPhase.ASCENDING;
                    this.phaseTimer = 60;
                }
                break;
                
            case ASCENDING:
                // If we've climbed well above the target, switch to descending to avoid runaway
                if (currentHeight > targetHeight + 6.0D) {
                    this.flightPhase = FlightPhase.DESCENDING;
                    this.phaseTimer = 40;
                    break;
                }
                if (currentHeight >= targetHeight * 0.8D || this.phaseTimer <= 0) {
                    this.flightPhase = FlightPhase.GLIDING;
                    this.phaseTimer = 100;
                }
                break;
                
            case GLIDING:
                if (distanceToTarget < 15.0D || this.phaseTimer <= 0) {
                    this.flightPhase = FlightPhase.DESCENDING;
                    this.phaseTimer = 40;
                }
                break;
                
            case DESCENDING:
                if (currentHeight < targetHeight + 5.0D || this.phaseTimer <= 0) {
                    this.flightPhase = FlightPhase.LANDING;
                    this.phaseTimer = 30;
                }
                break;
                
            case LANDING:
                // Will end when touching ground
                break;
                
            default:
                break;
        }
    }
    
    private void calculateTargetVelocity(Vec3 toTarget, double distance) {
        // Separate horizontal and vertical intent to avoid lateral drift
        Vec3 horiz = new Vec3(toTarget.x, 0.0D, toTarget.z);
        double horizDist = horiz.length();

        Vec3 horizDir = horizDist > 1e-6 ? horiz.normalize() : Vec3.ZERO;

        double horizontalSpeed;
        double verticalSpeed;

        switch (this.flightPhase) {
            case TAKEOFF -> {
                horizontalSpeed = 0.35D;
                verticalSpeed = 0.45D;
            }
            case ASCENDING -> {
                horizontalSpeed = 0.5D;
                verticalSpeed = 0.25D;
            }
            case GLIDING -> {
                horizontalSpeed = 0.6D;
                verticalSpeed = -0.05D;
                // subtle flap oscillation (small amplitude)
                double flapPhase = (this.tickCount % 40) / 40.0D * Math.PI * 2.0D;
                verticalSpeed += Math.sin(flapPhase) * 0.02D;
            }
            case DESCENDING -> {
                horizontalSpeed = 0.4D;
                verticalSpeed = -0.15D;
            }
            case LANDING -> {
                horizontalSpeed = 0.2D;
                verticalSpeed = -0.25D;
            }
            default -> {
                horizontalSpeed = 0.0D;
                verticalSpeed = 0.0D;
            }
        }

        // Slow horizontal speed when very close to the horizontal target so we don't overshoot
        if (horizDist < 6.0D) {
            horizontalSpeed *= (horizDist / 6.0D);
        }

        // Banking/sway uses the horizontal direction (not the already-scaled dx/dz)
        double swayPhase = (this.tickCount % 60) / 60.0D * Math.PI * 2.0D;
        double sway = Math.sin(swayPhase) * 0.04D; // smaller magnitude

        Vec3 sideways = horizDir.equals(Vec3.ZERO) ? Vec3.ZERO : new Vec3(-horizDir.z, 0.0D, horizDir.x);

        double dx = horizDir.x * horizontalSpeed + sideways.x * sway;
        double dz = horizDir.z * horizontalSpeed + sideways.z * sway;

        this.targetVelocity = new Vec3(dx, verticalSpeed, dz);
    }
    
    private void applyVelocitySmoothing() {
        Vec3 currentVelocity = this.getDeltaMovement();
        
        // Smooth interpolation factor - faster for takeoff, slower for gliding
        double smoothFactor = switch (this.flightPhase) {
            case TAKEOFF -> 0.3D;
            case ASCENDING -> 0.25D;
            case GLIDING -> 0.15D;
            case DESCENDING -> 0.2D;
            case LANDING -> 0.25D;
            default -> 0.4D;
        };
        
        // Interpolate toward target velocity
        Vec3 newVelocity = new Vec3(
            lerp(currentVelocity.x, this.targetVelocity.x, smoothFactor),
            lerp(currentVelocity.y, this.targetVelocity.y, smoothFactor),
            lerp(currentVelocity.z, this.targetVelocity.z, smoothFactor)
        );
        
        // Clamp to reasonable limits
        newVelocity = new Vec3(
            clamp(newVelocity.x, -0.8D, 0.8D),
            clamp(newVelocity.y, -1.2D, 1.2D),
            clamp(newVelocity.z, -0.8D, 0.8D)
        );
        
        // Apply mild lateral damping to bleed out any accumulated sideways velocity
        double hvx = newVelocity.x;
        double hvz = newVelocity.z;
        double horizLen = Math.sqrt(hvx * hvx + hvz * hvz);
        if (horizLen > 1e-6) {
            // desired direction prefers the target velocity direction if available
            Vec3 desiredHoriz = this.targetVelocity.length() > 1e-6 ? new Vec3(this.targetVelocity.x, 0.0D, this.targetVelocity.z).normalize() : new Vec3(hvx, 0.0D, hvz).normalize();
            Vec3 side = new Vec3(-desiredHoriz.z, 0.0D, desiredHoriz.x);
            double forwardComp = hvx * desiredHoriz.x + hvz * desiredHoriz.z;
            double sideComp = hvx * side.x + hvz * side.z;
            // reduce sideways component to remove persistent drift
            sideComp *= 0.55D;
            double finalHx = desiredHoriz.x * forwardComp + side.x * sideComp;
            double finalHz = desiredHoriz.z * forwardComp + side.z * sideComp;
            newVelocity = new Vec3(finalHx, newVelocity.y, finalHz);

            this.setDeltaMovement(newVelocity);

            // Smoothly rotate to face movement direction when moving horizontally
            double desiredYaw = Math.atan2(newVelocity.z, newVelocity.x) * (180.0D / Math.PI) - 90.0D;
            double currentYaw = this.getYRot();
            double yawDiff = wrapDegrees(desiredYaw - currentYaw);
            double yawChange = yawDiff * Math.min(1.0D, smoothFactor * 2.5D);
            this.setYRot((float)(currentYaw + yawChange));
            this.yHeadRot = this.getYRot();
            this.yBodyRot = this.yHeadRot;
        } else {
            this.setDeltaMovement(newVelocity);
        }
    }

    private double wrapDegrees(double deg) {
        deg %= 360.0D;
        if (deg >= 180.0D) deg -= 360.0D;
        if (deg < -180.0D) deg += 360.0D;
        return deg;
    }
    
    private void endFlight() {
        this.isInFlight = false;
        this.flightTarget = null;
        this.flightPhase = FlightPhase.GROUNDED;
        this.phaseTimer = 0;
        this.targetVelocity = Vec3.ZERO;

        // Reset flight safety counter when flight ends
        this.flightTicksActive = 0;

        // Gentle landing - reduce horizontal velocity and stop navigation
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(0.0D, 0.0D, 0.0D);
        try {
            this.getNavigation().stop();
        } catch (Throwable ignored) {
        }

        // (Old code removed) we no longer notify a custom tempt/flee goal here
        // Enter a resting period to prevent immediate wandering
        this.isResting = true;
        this.restTimer = 200; // ~10 seconds
        // Enter resting: ensure navigation is stopped and let the rest timer manage reactivation
        try { this.getNavigation().stop(); } catch (Throwable ignored) {}
    }

    // Update idle animation timers and trigger animations when the crane is standing still
    private void updateIdleAnimations() {
        if (this.drinkCooldownTimer > 0) this.drinkCooldownTimer--;
        if (this.preenCooldownTimer > 0) this.preenCooldownTimer--;

        if (this.drinkAnimationTimer > 0) {
            this.drinkAnimationTimer--;
            if (this.drinkAnimationTimer <= 0) this.setPlayingDrinkAnimation(false);
        }
        if (this.preenAnimationTimer > 0) {
            this.preenAnimationTimer--;
            if (this.preenAnimationTimer <= 0) this.setPlayingFeatherAnimation(false);
        }

        // Only consider starting new idle animations when on ground, not in flight, not resting,
        // and not currently navigating anywhere. This prevents idle animations while walking.
        boolean canIdle = this.onGround() && !this.isInFlight && !this.isResting && this.getNavigation().isDone();
        if (!canIdle) {
            // If navigation just started or we're otherwise unable to idle, cancel any playing idle animations
            if (this.isPlayingDrinkAnimation()) {
                this.setPlayingDrinkAnimation(false);
                this.drinkAnimationTimer = 0;
            }
            if (this.isPlayingFeatherAnimation()) {
                this.setPlayingFeatherAnimation(false);
                this.preenAnimationTimer = 0;
            }
            return;
        }

        // Require EXTREMELY low velocity to consider the crane stationary
        Vec3 vel = this.getDeltaMovement();
        double horizSq = vel.x * vel.x + vel.z * vel.z;
        double idleThresholdSq = 0.0001D; // Very strict - essentially zero movement
        float moveFactor = this.getMoveAnimationFactor();

        // If we're moving faster than threshold or our smoothed move factor indicates movement,
        // immediately cancel any idle animations
        if (horizSq > idleThresholdSq || moveFactor > 0.001F) {
            if (this.isPlayingDrinkAnimation()) {
                this.setPlayingDrinkAnimation(false);
                this.drinkAnimationTimer = 0;
            }
            if (this.isPlayingFeatherAnimation()) {
                this.setPlayingFeatherAnimation(false);
                this.preenAnimationTimer = 0;
            }
            return; // moving, not idle
        }

        // Extra guard: require smoothed move factor to be VERY low before starting animations
        float serverMoveFactor = this.getMoveAnimationFactor();
        if (serverMoveFactor < 0.001F) {
            // Try starting drink animation occasionally
            if (!this.isPlayingDrinkAnimation() && this.drinkCooldownTimer <= 0) {
                if (this.random.nextInt(1000) < 8) { // ~0.8% chance per tick
                    this.setPlayingDrinkAnimation(true);
                    this.drinkAnimationTimer = 60; // 3s
                    this.drinkCooldownTimer = 200 + this.random.nextInt(200);
                }
            }

            // Try starting preen animation occasionally
            if (!this.isPlayingFeatherAnimation() && this.preenCooldownTimer <= 0) {
                if (this.random.nextInt(1000) < 10) { // ~1.0% chance per tick
                    this.setPlayingFeatherAnimation(true);
                    this.preenAnimationTimer = 80; // 4s
                    this.preenCooldownTimer = 300 + this.random.nextInt(300);
                }
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DRINKING, false);
        builder.define(DATA_PREENING, false);
    }

    private void setPlayingDrinkAnimation(boolean v) {
        this.playingDrinkAnimation = v;
        try {
            this.entityData.set(DATA_DRINKING, v);
        } catch (Throwable ignored) {
        }
    }

    private void setPlayingFeatherAnimation(boolean v) {
        this.playingFeatherAnimation = v;
        try {
            this.entityData.set(DATA_PREENING, v);
        } catch (Throwable ignored) {
        }
    }
    
    private double lerp(double start, double end, double factor) {
        return start + (end - start) * factor;
    }
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamp a requested flight target Y so the crane doesn't try to ascend an excessive
     * amount above its current position. This prevents runaway diagonal climbs.
     */
    private double clampFlightTargetY(double requestedY) {
        double maxAbove = 12.0D; // maximum blocks above current Y the crane will target
        double maxY = this.getY() + maxAbove;
        return Math.min(requestedY, maxY);
    }

    /**
     * Spawn predicate that ensures cranes only spawn on land blocks (not in/above water).
     */
    public static boolean checkRedCrownedCraneSpawnRules(EntityType<RedCrownedCraneEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        try {
            BlockState below = level.getBlockState(pos.below());
            // Disallow spawning when the block below is water or otherwise liquid
            if (below.getFluidState().is(FluidTags.WATER)) return false;
            // Allow spawn eggs to place cranes anywhere regardless of spawn-surface tag.
            if (spawnType == MobSpawnType.SPAWN_EGG) {
                return checkMobSpawnRules(type, level, spawnType, pos, random);
            }
            // Require natural/datapack spawns to be above the world's sea level
            try {
                if (level instanceof Level) {
                    Level lv = (Level) level;
                    int sea = lv.getSeaLevel();
                    if (pos.getY() <= sea) return false;
                }
            } catch (Throwable ignored) {}

            // Prevent spawning on top of deep water even if there's air between the
            // spawn position and the water surface. Find the first non-air block
            // beneath the spawn pos (up to 8 blocks). If that block is water, reject.
            try {
                int maxLook = 8;
                // Require at least one nearby (3x3) column to have solid non-water ground.
                boolean foundSolidNearby = false;
                for (int dx = -1; dx <= 1 && !foundSolidNearby; dx++) {
                    for (int dz = -1; dz <= 1 && !foundSolidNearby; dz++) {
                        BlockPos base = pos.offset(dx, 0, dz);
                        for (int i = 1; i <= maxLook; i++) {
                            BlockPos check = base.below(i);
                            BlockState bs = level.getBlockState(check);
                            if (bs.isAir()) continue; // keep looking deeper
                            if (bs.getFluidState().is(FluidTags.WATER)) {
                                break; // this column is water; try other columns
                            }
                            // Found solid ground here (non-air, non-water)
                            foundSolidNearby = true;
                            break;
                        }
                    }
                }
                if (!foundSolidNearby) return false;
            } catch (Throwable ignored) {}
            // Enforce block-tag-based spawn surface for natural/datapack spawns:
            try {
                net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, net.minecraft.resources.ResourceLocation.parse("sengoku:red_crowned_crane_spawnable_on"));
                if (!below.is(tag)) return false;
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {
        }
        return checkMobSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    public boolean isFlying() {
        // Consider the crane 'flying' when it's in controlled flight, or when it's been
        // airborne for a sustained period — short hops (a few ticks) won't trigger flying
        // animations so the crane uses normal mob jump visuals for step-ups.
        return this.isInFlight || this.airborneTicks > 6;
    }

    public boolean isPlayingDrinkAnimation() {
        try {
            return this.entityData.get(DATA_DRINKING);
        } catch (Throwable ignored) {
            return this.playingDrinkAnimation;
        }
    }

    public boolean isPlayingFeatherAnimation() {
        try {
            return this.entityData.get(DATA_PREENING);
        } catch (Throwable ignored) {
            return this.playingFeatherAnimation;
        }
    }

    /**
     * Returns a smoothed 0..1 factor representing how fast the crane is moving horizontally.
     * Renderers or animation predicates should multiply their walk-cycle speed by this value
     * and optionally blend idle <-> walk animations based on it.
     */
    public float getMoveAnimationFactor() {
        return this.moveAnimationFactor;
    }
    
    public FlightPhase getFlightPhase() {
        return this.flightPhase;
    }

    // Helper method to manually trigger takeoff (can be used by other systems)
    public void startTakeoff() {
        if (this.onGround() && !this.isInFlight) {
            Vec3 randomDirection = new Vec3(
                (this.random.nextDouble() - 0.5D) * 2.0D,
                0.0D,
                (this.random.nextDouble() - 0.5D) * 2.0D
            ).normalize();
            
            double targetX = this.getX() + randomDirection.x * 20.0D;
            double targetZ = this.getZ() + randomDirection.z * 20.0D;
            double targetY = clampFlightTargetY(this.getY() + 15.0D);
            
            this.flightTarget = new Vec3(targetX, targetY, targetZ);
            this.isInFlight = true;
            this.flightPhase = FlightPhase.TAKEOFF;
            this.phaseTimer = 20;
            
            this.targetVelocity = new Vec3(randomDirection.x * 0.3D, 0.5D, randomDirection.z * 0.3D);
            this.previousDistanceToTarget = Double.NaN;
            this.distanceIncreasingCounter = 0;
        }
    }

    @Override
    public net.minecraft.sounds.SoundEvent getAmbientSound() {
        return com.shioh.sengoku.registry.SoundRegistry.RED_CROWNED_CRANE_AMBIENT;
    }

    @Override
    public net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource ds) {
        return com.shioh.sengoku.registry.SoundRegistry.RED_CROWNED_CRANE_HURT;
    }

    @Override
    public net.minecraft.sounds.SoundEvent getDeathSound() {
        return com.shioh.sengoku.registry.SoundRegistry.RED_CROWNED_CRANE_DEATH;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false; // Cranes don't take fall damage
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // No-op: prevent fall damage handling
    }
}
