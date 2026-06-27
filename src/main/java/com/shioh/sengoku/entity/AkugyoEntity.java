package com.shioh.sengoku.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import java.util.EnumSet;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

/**
 * Akugyo – literally meaning evil fish.
 * Focus: sense players from far away and relentlessly chase them in water.
 */
public class AkugyoEntity extends Monster {

	public AkugyoEntity(EntityType<? extends AkugyoEntity> type, Level level) {
		super(type, level);
		// Snappy, stable swimming with reduced look jitter
		this.moveControl = new SmoothSwimmingMoveControl(this, 45, 10, 0.28F, 0.08F, true);
		this.lookControl = new SmoothSwimmingLookControl(this, 4);
		// Prevent vanilla despawn
		this.setPersistenceRequired();
	}

	protected SoundEvent getAmbientSound() {
		return SoundEvents.ELDER_GUARDIAN_AMBIENT;
	}

	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.ELDER_GUARDIAN_HURT;
	}

	protected SoundEvent getDeathSound() {
		return SoundEvents.ELDER_GUARDIAN_DEATH;
	}

	protected SoundEvent getFlopSound() {
		return SoundEvents.ELDER_GUARDIAN_AMBIENT;
	}

	/**
	 * Goal that detects nearby players and sets them as target depending on
	 * proximity to the guard center (stronghold). Runs periodically.
	 */
	static class DetectPlayerGoal extends Goal {
		private final AkugyoEntity akugyo;
		private int cooldown = 0;
		private static final double ALERT_RANGE = 128.0D;
		private static final double PASSIVE_RANGE = 24.0D;
		private static final double NEAR_STRONGHOLD_RANGE = 200.0D;

		public DetectPlayerGoal(AkugyoEntity akugyo) {
			this.akugyo = akugyo;
			this.setFlags(EnumSet.noneOf(Flag.class));
		}

		@Override
		public boolean canUse() {
			if (this.cooldown-- > 0) return false;
			this.cooldown = 20; // check every second
			if (this.akugyo.returningToCenter) return false;
			
			// In The End, use much larger detection ranges for aggressive hunting behavior
			boolean inEnd = this.akugyo.level().dimension() == Level.END;
			double alertRange = inEnd ? 256.0D : ALERT_RANGE;
			double passiveRange = inEnd ? 128.0D : PASSIVE_RANGE;
			
			// Find nearest player within range, with an increased vertical search to include Y-levels
			double vertical = inEnd ? 64.0D : 32.0D;
			AABB searchBox = new AABB(this.akugyo.getX() - alertRange, this.akugyo.getY() - vertical, this.akugyo.getZ() - alertRange,
						this.akugyo.getX() + alertRange, this.akugyo.getY() + vertical, this.akugyo.getZ() + alertRange);
			// Filter players strictly in The End: require survival/adventure and being in water.
			// Outside The End keep original broader behavior (non-spectator only).
			// Globally require non-creative, non-spectator players who are in water OR in a boat.
			java.util.function.Predicate<Player> candidatePred = p -> !p.isSpectator() && !p.isCreative() && (p.isInWater() || p.getVehicle() instanceof Boat);
			List<Player> candidates = this.akugyo.level().getEntitiesOfClass(Player.class, searchBox, candidatePred::test);
			if (candidates.isEmpty()) return false;
			Player p = null;
			double best = Double.MAX_VALUE;
			for (Player c : candidates) {
				double dx = c.getX() - this.akugyo.getX();
				double dy = c.getY() - this.akugyo.getY();
				double dz = c.getZ() - this.akugyo.getZ();
				double d2 = dx*dx + dy*dy + dz*dz;
				if (d2 < best) { best = d2; p = c; }
			}
			if (p == null) return false;
			double detectRange = passiveRange;
			if (this.akugyo.hasRestriction()) {
				BlockPos center = this.akugyo.getRestrictCenter();
				double dx = p.getX() - (center.getX() + 0.5D);
				double dy = p.getY() - (center.getY() + 0.5D);
				double dz = p.getZ() - (center.getZ() + 0.5D);
				double distSq = dx*dx + dy*dy + dz*dz;
				if (distSq <= NEAR_STRONGHOLD_RANGE * NEAR_STRONGHOLD_RANGE) {
					detectRange = alertRange;
				}
			}
			if (this.akugyo.distanceToSqr(p) <= detectRange * detectRange) {
				this.akugyo.setTarget(p);
				return true;
			}
			return false;
		}

		@Override
		public boolean canContinueToUse() {
			LivingEntity t = this.akugyo.getTarget();
			if (t == null || !t.isAlive() || this.akugyo.returningToCenter) return false;
			
			// Stop chasing if the target is a player who exited the water and is not in a boat
			if (t instanceof Player player) {
				if (!player.isInWater() && !(player.getVehicle() instanceof Boat)) {
					this.akugyo.setTarget(null);
					return false;
				}
			}
			return true;
		}

		@Override
		public void stop() {
			// nothing special
		}
	}

	/**
	 * Search for a nearby block tagged as an Akugyo guard (e.g., stronghold/end portal frame)
	 * and set our restriction center to it so the Akugyo remains territorial.
	 */
	private void findAndSetGuardCenter() {
		// Do not attach a guard center while in The End - allow free roaming there.
		if (this.level().isClientSide) return;
		if (this.level().dimension() == Level.END) return;
		if (this.hasRestriction()) return;
		try {
			// Prefer attaching the guard to the nearest stronghold structure start when available.
			if (this.level() instanceof net.minecraft.server.level.ServerLevel server) {
				net.minecraft.core.Registry<net.minecraft.world.level.levelgen.structure.Structure> structureRegistry = server.registryAccess().registryOrThrow(Registries.STRUCTURE);
				net.minecraft.world.level.levelgen.structure.Structure stronghold = structureRegistry.get(ResourceLocation.parse("minecraft:stronghold"));
				if (stronghold != null) {
					net.minecraft.world.level.StructureManager sm = server.structureManager();
					BlockPos origin = this.blockPosition();
					int searchRadius = 200;
					// coarse scan in 16-block steps to find a nearby structure start
					for (int dx = -searchRadius; dx <= searchRadius; dx += 16) {
						for (int dz = -searchRadius; dz <= searchRadius; dz += 16) {
							for (int dy = -8; dy <= 8; dy += 4) {
								BlockPos check = origin.offset(dx, dy, dz);
								try {
									net.minecraft.world.level.levelgen.structure.StructureStart start = sm.getStructureAt(check, stronghold);
									if (start != null && start.isValid()) {
										// Determine sea floor under the structure start and place center just above it.
										int tentativeY = check.getY() + 3;
										int seaFloorY = tentativeY;
										try {
											int minY = Math.max(this.level().getMinBuildHeight(), tentativeY - 80);
											for (int y = tentativeY; y >= minY; y--) {
												BlockPos p = new BlockPos(check.getX(), y, check.getZ());
												if (!this.level().getFluidState(p).is(FluidTags.WATER)) { seaFloorY = y; break; }
											}
										} catch (Throwable ignored) {}
										int cy = Math.min(seaFloorY + 20, 50); // at least 20 above sea floor, cap to Y=50
										BlockPos centerUp = new BlockPos(check.getX(), cy, check.getZ());
										this.restrictTo(centerUp, 200);
										try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Akugyo [{}] attached guard center to stronghold at {} (sea-floor)", this.getUUID(), centerUp); } catch (Throwable ignored) {}
										return;
									}
								} catch (Throwable ignored) {}
							}
						}
					}
				}
			}
			// Fallback: previous block-tag scan using `sengoku:akugyo_guards`
			TagKey<Block> tag = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("sengoku", "akugyo_guards"));
			BlockPos origin = this.blockPosition();
			int searchRadius = 200;
			for (int dx = -searchRadius; dx <= searchRadius; dx++) {
				for (int dz = -searchRadius; dz <= searchRadius; dz++) {
					for (int dy = -8; dy <= 8; dy++) {
						BlockPos check = origin.offset(dx, dy, dz);
						BlockState bs = this.level().getBlockState(check);
										if (bs.is(tag)) {
											int tentativeY = check.getY() + 3;
											int seaFloorY = tentativeY;
											try {
												int minY = Math.max(this.level().getMinBuildHeight(), tentativeY - 80);
												for (int y = tentativeY; y >= minY; y--) {
													BlockPos p = new BlockPos(check.getX(), y, check.getZ());
													if (!this.level().getFluidState(p).is(FluidTags.WATER)) { seaFloorY = y; break; }
												}
											} catch (Throwable ignored) {}
											int cy = Math.min(seaFloorY + 20, 50);
											BlockPos centerUp = new BlockPos(check.getX(), cy, check.getZ());
											this.restrictTo(centerUp, 200);
											try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Akugyo [{}] attached guard center to tag block at {} (sea-floor)", this.getUUID(), centerUp); } catch (Throwable ignored) {}
											return;
										}
					}
				}
			}
		} catch (Throwable ignored) {}
	}

	private int guardSearchCooldown = 0;
	// When true the Akugyo is returning to its guard center and should not pursue targets
	private boolean returningToCenter = false;

	// Fire breath attack fields
	private static final int FIRE_BREATH_DURATION = 20;
	private static final int FIRE_BREATH_COOLDOWN_TICKS = 100;
	private int fireBreathTicks = 0;
	private int fireBreathCooldown = 0;

	// Public accessor for client renderers
	public boolean isReturningToCenter() {
		return this.returningToCenter;
	}

	/**
	 * Performs a fire breath attack as they do in the folk lore
	 */
	private void performFireBreathAttack(LivingEntity target) {
		this.getLookControl().setLookAt(target, 45.0F, 30.0F);

		Vec3 origin = this.position().add(0.0D, this.getEyeHeight(), 0.0D);
		Vec3 targetPos = target.getEyePosition(1.0F);
		Vec3 direction = targetPos.subtract(origin);

		if (direction.lengthSqr() < 1.0E-4D) {
			return;
		}

		direction = direction.normalize();

		Vec3 breathEnd = origin.add(direction.scale(5.0D));

		if (this.level() instanceof ServerLevel serverLevel) {
			for (int i = 1; i <= 10; i++) {
				Vec3 particlePos = origin.add(direction.scale(0.5D * i));
				serverLevel.sendParticles(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 5,
					direction.x * 0.05D, direction.y * 0.02D, direction.z * 0.05D, 0.0D);
				serverLevel.sendParticles(ParticleTypes.SMOKE, particlePos.x, particlePos.y, particlePos.z, 2,
					direction.x * 0.03D, direction.y * 0.01D, direction.z * 0.03D, 0.0D);
			}
		} else {
			for (int i = 1; i <= 10; i++) {
				Vec3 particlePos = origin.add(direction.scale(0.5D * i));
				this.level().addParticle(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z,
					direction.x * 0.02D, direction.y * 0.01D, direction.z * 0.02D);
				this.level().addParticle(ParticleTypes.SMOKE, particlePos.x, particlePos.y, particlePos.z,
					direction.x * 0.01D, direction.y * 0.005D, direction.z * 0.01D);
			}
		}

		if (!this.level().isClientSide && this.fireBreathTicks % 4 == 0) {
			AABB breathZone = new AABB(origin, breathEnd).inflate(0.6D);
			if (target.getBoundingBox().inflate(0.3D).intersects(breathZone)) {
				target.hurt(this.damageSources().mobAttack(this), 4.5F);
				boolean blockedByPlayer = (target instanceof Player p && p.isBlocking());
				// If the target is a player and is blocking (shield), do not set on fire
				if (!blockedByPlayer) {
					target.setRemainingFireTicks(160); // 8 seconds of fire (160 ticks)
				}
			}
		}
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new WaterBoundPathNavigation(this, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		// Shark-like: fast swim, high detection range, decent damage
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, 250.0)
			// Increased base damage for a dangerous aquatic predator
			.add(Attributes.ATTACK_DAMAGE, 14.0)
			.add(Attributes.FOLLOW_RANGE, 128.0)
			.add(Attributes.MOVEMENT_SPEED, 2.4)
			// Built-in armor values so Akugyo soaks up some hits naturally
			.add(Attributes.ARMOR, 6.0)
			.add(Attributes.ARMOR_TOUGHNESS, 2.0)
			.add(Attributes.ATTACK_KNOCKBACK, 1.0)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.7);
	}

	// Spawn rule used by registry: require water and non-peaceful difficulty.
	public static boolean checkAkugyoSpawnRules(EntityType<AkugyoEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
		if (level.getDifficulty() == Difficulty.PEACEFUL) return false;
		try {
			// Always require the spawn block to be water.
			if (!level.getFluidState(pos).is(FluidTags.WATER)) return false;
			// Also require the block below to be water to avoid spawning on shallow edges or land.
			if (!level.getFluidState(pos.below()).is(FluidTags.WATER)) return false;
			// Akugyo is tall; require a continuous water column that fits its height.
			// Try searching downward a few blocks so surface-chosen positions can still
			// spawn if a valid fully-submerged column exists slightly below.
			try {
				double h = 4.2D; // entity height
				int need = Math.max(1, (int)Math.floor(h));
				boolean found = false;
				for (int down = 0; down >= -4; down--) {
					BlockPos base = pos.offset(0, down, 0);
					boolean ok = true;
					for (int i = 0; i < need; i++) {
						BlockPos p = base.above(i);
						if (!level.getFluidState(p).is(FluidTags.WATER)) { ok = false; break; }
					}
					if (ok) { found = true; break; }
				}
				if (!found) return false;
			} catch (Throwable ignored) { }
		} catch (Throwable ignored) {}
		return Mob.checkMobSpawnRules(type, level, spawnType, pos, random);
	}

	@Override
	protected void registerGoals() {
		// Relentless pursuit and attack
		this.goalSelector.addGoal(1, new SharkPursueGoal(this, 3.0D));
		// Territory-specific wandering behavior
		if (this.level() != null && this.level().dimension() == Level.END) {
			// Free-roaming wandering for The End
			this.goalSelector.addGoal(4, new FreeRoamGoal(this));
		} else {
			// Guard swim: patrol around restrict center in Overworld
			this.goalSelector.addGoal(4, new GuardSwimGoal(this));
		}
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 12.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		// Custom detection goal decides when to set targets based on proximity
		// to the guarded stronghold; it replaces the generic nearest-target goal.
		this.goalSelector.addGoal(0, new DetectPlayerGoal(this));
	}

	@Override
	public void aiStep() {
		super.aiStep();
		// Never drown; keep air supply topped while in water
		if (this.isInWater()) {
			this.setAirSupply(300);
			// Apply gentle upward velocity when in water to prevent sinking
			// This is essential for aquatic mobs when no active goal is applying movement
			if (this.getTarget() == null && !this.returningToCenter) {
				// Check if we're significantly below the water surface
				BlockPos pos = this.blockPosition();
				double waterSurface = pos.getY();
				for (int y = pos.getY(); y <= pos.getY() + 12; y++) {
					BlockPos check = new BlockPos(pos.getX(), y, pos.getZ());
					if (!this.level().getFluidState(check).is(FluidTags.WATER)) {
						waterSurface = y - 1.0;
						break;
					}
				}
				double buoyancyTarget = waterSurface - 1.0; // stay ~1 block below surface when idle
				if (this.getY() < buoyancyTarget - 0.5) {
					// Too low, apply upward buoyancy
					Vec3 vel = this.getDeltaMovement();
					this.setDeltaMovement(vel.x, vel.y + 0.08, vel.z);
				}
			}
		}

		// If in The End, completely disable territorial behavior:
		// clear restrictions, clear returning state, and prevent any guard logic
		if (this.level().dimension() == Level.END) {
			if (this.returningToCenter) this.returningToCenter = false;
			if (this.hasRestriction()) this.clearRestriction();
		}
		// Occasionally find a nearby guard block and set our restriction center
		if (!this.level().isClientSide) {
			// Do not search for or attach guard centers while in The End.
			if (this.level().dimension() != Level.END) {
				if (this.guardSearchCooldown-- <= 0 && !this.hasRestriction()) {
					this.guardSearchCooldown = 200 + this.random.nextInt(200);
					findAndSetGuardCenter();
				}
			}
		}

		// Server-side: apply mining fatigue to nearby players (elder-guardian-like)
		// Does NOT apply this effect when Akugyo is in The End dimension.
		if (!this.level().isClientSide) {
			if (this.level().dimension() != Level.END) {
				for (Player p : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0D))) {
					try {
						p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 200, 2, false, true));
					} catch (Throwable ignored) {}
				}
			}
		}

		// Stabilize rotation while swimming: align yaw to motion to avoid oscillation/spins
		if (this.isInWater()) {
			Vec3 vel = this.getDeltaMovement();
			double horiz = vel.x * vel.x + vel.z * vel.z;
			if (horiz > 1.0E-6) {
				float desiredYaw = (float)(Mth.atan2(vel.z, vel.x) * 180.0D / Math.PI) - 90.0F;
				float yawDiff = Mth.wrapDegrees(desiredYaw - this.getYRot());
				float maxTurn = 6.0F;
				float newYaw = this.getYRot() + Mth.clamp(yawDiff, -maxTurn, maxTurn);
				this.setYRot(newYaw);
				this.yBodyRot = this.getYRot();
				this.yHeadRot = this.getYRot();
			}
		}

		// If we have a restriction (guard center) and chased a player too far,
		// abandon the target and swim back to the center. While returning, disable pursuit.
		// SKIP ENTIRELY in The End dimension - no territorial behavior there at all
		if (this.level().dimension() != Level.END && this.hasRestriction()) {
			LivingEntity target = this.getTarget();
			double guardMax = 100.0D; // max guard radius
			BlockPos center = this.getRestrictCenter();
			double cx = center.getX() + 0.5D;
			double cy = center.getY() + 0.5D;
			double cz = center.getZ() + 0.5D;
			// If currently returning, allow resuming pursuit if a player approaches the stronghold.
			if (this.returningToCenter) {
				double rx = this.getX() - cx;
				double ry = this.getY() - cy;
				double rz = this.getZ() - cz;
				double rdistSq = rx*rx + ry*ry + rz*rz;
				if (rdistSq <= 4.0D * 4.0D) {
					this.returningToCenter = false;
					try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Akugyo [{}] returned to guard center {}", this.getUUID(), center); } catch (Throwable ignored) {}
				} else {
					// Check for players near the guard center; if a player is close enough to the
					// stronghold and within alert range of the Akugyo, resume pursuit.
					if (!this.level().isClientSide) {
						double resumeNearStronghold = 100.0D;
						AABB box = new AABB(cx - resumeNearStronghold, cy - 8.0D, cz - resumeNearStronghold,
							cx + resumeNearStronghold, cy + 8.0D, cz + resumeNearStronghold);
						// Resume only for players in survival/adventure who are in water when in The End.
						// Globally require non-creative, non-spectator players who are in water OR in a boat for resume.
						java.util.function.Predicate<Player> resumePred = p -> !p.isSpectator() && !p.isCreative() && (p.isInWater() || p.getVehicle() instanceof Boat);
						List<Player> players = this.level().getEntitiesOfClass(Player.class, box, resumePred::test);
						if (!players.isEmpty()) {
							Player p = players.get(0);
							this.returningToCenter = false;
							this.setTarget(p);
							try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Akugyo [{}] resumed pursuit of {} near guard center {}", this.getUUID(), p.getUUID(), center); } catch (Throwable ignored) {}
							return; // resume pursuit this tick
						}
					}
					// continue returning if no player triggered resume
					// Use direct velocity while in water to ensure fast, smooth return movement
					if (this.isInWater()) {
						double dx = cx - this.getX();
						double dy = cy - this.getY();
						double dz = cz - this.getZ();
						double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
						if (dist > 1.0E-6) {
							double nx = dx / dist;
							double ny = dy / dist;
							double nz = dz / dist;
							Vec3 vel = this.getDeltaMovement();
							// increase return speed in the End
							double returnSpeed = this.level().dimension() == Level.END ? 2.0D : 1.2D;
							double ax = (nx * returnSpeed - vel.x) * 0.32;
							double ay = (ny * returnSpeed - vel.y) * 0.32;
							double az = (nz * returnSpeed - vel.z) * 0.32;
							this.setDeltaMovement(vel.add(ax, ay, az));
						}
						this.getNavigation().stop();
					} else {
						this.getNavigation().moveTo(cx, cy, cz, 6.0D);
					}
					return; // skip pursuit while returning
				}
			}
			// If we have a target, check its distance to the guard center
			if (target != null) {
				double dx = target.getX() - cx;
				double dy = target.getY() - cy;
				double dz = target.getZ() - cz;
				double distSq = dx*dx + dy*dy + dz*dz;
				if (distSq > guardMax * guardMax) {
					// Before abandoning, ensure there are no players inside the stronghold bounds.
					if (!this.level().isClientSide) {
						double strongholdCheckRadius = 100.0D;
						AABB strongBox = new AABB(cx - strongholdCheckRadius, cy - 8.0D, cz - strongholdCheckRadius,
							cx + strongholdCheckRadius, cy + 8.0D, cz + strongholdCheckRadius);
						// Only consider non-creative, non-spectator players who are in water OR in a boat when in The End.
						java.util.function.Predicate<Player> strongPred;
						if (this.level().dimension() == Level.END) {
							strongPred = p -> !p.isSpectator() && !p.isCreative() && (p.isInWater() || p.getVehicle() instanceof Boat);
						} else {
							strongPred = p -> !p.isSpectator();
						}
						List<Player> playersInBox = this.level().getEntitiesOfClass(Player.class, strongBox, strongPred::test);
						if (!playersInBox.isEmpty()) {
							// Player present inside the stronghold bounds — switch to aggro
							Player p = playersInBox.get(0);
							this.setTarget(p);
							this.returningToCenter = false;
							return;
						}
					}
					// No players inside the stronghold bounds; abandon and return
					this.setTarget(null);
					this.returningToCenter = true;
					try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Akugyo [{}] abandoning target and returning to guard center {}", this.getUUID(), center); } catch (Throwable ignored) {}
					return; // immediately stop other AI for this tick; return movement handled by returning block
				}
			}
		} // End of restriction logic block
	}

	/**
	 * Free-roaming goal for The End: smooth, natural-looking wandering in any direction.
	 */
	static class FreeRoamGoal extends Goal {
		private final AkugyoEntity akugyo;
		private Vec3 roamTarget;
		private int roamTime = 0;
		private static final double ROAM_DISTANCE = 24.0D;

		public FreeRoamGoal(AkugyoEntity akugyo) {
			this.akugyo = akugyo;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (this.akugyo.getTarget() != null) return false;
			if (this.akugyo.returningToCenter) return false;
			double angle = this.akugyo.random.nextDouble() * Math.PI * 2.0;
			double pitch = (this.akugyo.random.nextDouble() - 0.5) * Math.PI * 0.5; //
			double dist = 8.0 + this.akugyo.random.nextDouble() * ROAM_DISTANCE;
			double horizDist = Math.cos(pitch) * dist;
			double tx = this.akugyo.getX() + Math.cos(angle) * horizDist;
			double ty = this.akugyo.getY() + Math.sin(pitch) * dist;
			double tz = this.akugyo.getZ() + Math.sin(angle) * horizDist;
			this.roamTarget = new Vec3(tx, ty, tz);
			this.roamTime = 0;
			return true;
		}

		@Override
		public boolean canContinueToUse() {
			if (this.akugyo.getTarget() != null) return false;
			if (this.akugyo.returningToCenter) return false;
			if (this.roamTarget == null) return false;
			double dx = this.roamTarget.x - this.akugyo.getX();
			double dy = this.roamTarget.y - this.akugyo.getY();
			double dz = this.roamTarget.z - this.akugyo.getZ();
			double d2 = dx*dx + dy*dy + dz*dz;
			// Stop when close enough or after timeout
			return d2 > 4.0D && this.roamTime < 300;
		}

		@Override
		public void start() {
			this.roamTime = 0;
		}

		@Override
		public void stop() {
			this.roamTarget = null;
			// Don't stop navigation - let momentum carry
		}

		@Override
		public void tick() {
			if (this.roamTarget == null) return;
			this.roamTime++;

			if (this.akugyo.isInWater()) {
				double dx = this.roamTarget.x - this.akugyo.getX();
				double dy = this.roamTarget.y - this.akugyo.getY();
				double dz = this.roamTarget.z - this.akugyo.getZ();
				double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
				if (dist > 1.0E-6) {
					double nx = dx / dist;
					double ny = dy / dist;
					double nz = dz / dist;
					Vec3 vel = this.akugyo.getDeltaMovement();
					// Gentle but visible wandering speed
					double targetSpeed = 0.35;
					double accel = 0.07;
					double ax = (nx * targetSpeed - vel.x) * accel;
					double ay = (ny * targetSpeed - vel.y) * accel;
					double az = (nz * targetSpeed - vel.z) * accel;
					this.akugyo.setDeltaMovement(vel.add(ax, ay, az));
				}
				this.akugyo.getLookControl().setLookAt(this.roamTarget.x, this.roamTarget.y, this.roamTarget.z);
				// Stop navigation to avoid interference
				if (!this.akugyo.getNavigation().isDone()) {
					this.akugyo.getNavigation().stop();
				}
			}
		}
	}

	/**
	 * Pursuit goal: continuously navigates toward the target and performs melee when in reach.
	 */
	static class GuardSwimGoal extends Goal {
		private final AkugyoEntity akugyo;
		private Vec3 wanderTarget;
		private int lookTime = 0;
		private static final double GUARD_SWIM_RADIUS = 12.0D;

		public GuardSwimGoal(AkugyoEntity akugyo) {
			this.akugyo = akugyo;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			// Never run guard swim behavior in The End dimension
			if (this.akugyo.level().dimension() == Level.END) return false;
			if (this.akugyo.returningToCenter) return false;
			if (this.akugyo.getTarget() != null) return false;
			if (!this.akugyo.hasRestriction()) return false;
			BlockPos center = this.akugyo.getRestrictCenter();
			double cx = center.getX() + 0.5D;
			double cy = center.getY() + 0.5D;
			double cz = center.getZ() + 0.5D;
			// Choose a random point within the guard swim radius and try to prefer water
			double angle = this.akugyo.random.nextDouble() * Math.PI * 2.0;
			double dist = 3.0 + this.akugyo.random.nextDouble() * (GUARD_SWIM_RADIUS - 3.0);
			double tx = cx + Math.cos(angle) * dist;
			double tz = cz + Math.sin(angle) * dist;
			double ty = cy + (this.akugyo.random.nextInt(7) - 3); // +/-3 vertical
			BlockPos check = new BlockPos((int)Math.floor(tx), (int)Math.floor(ty), (int)Math.floor(tz));
			// try to find a nearby water block up to +/-3 Y
			boolean found = false;
			for (int dy = -3; dy <= 3; dy++) {
				BlockPos p = check.offset(0, dy, 0);
				try {
					if (this.akugyo.level().getFluidState(p).is(FluidTags.WATER)) { check = p; found = true; break; }
				} catch (Throwable ignored) {}
			}
			if (!found) return false;
			this.wanderTarget = new Vec3(check.getX() + 0.5D, check.getY() + 0.5D, check.getZ() + 0.5D);
			this.lookTime = 0;
			return true;
		}

		@Override
		public boolean canContinueToUse() {
			// Never continue guard swim behavior in The End dimension
			if (this.akugyo.level().dimension() == Level.END) return false;
			if (this.akugyo.returningToCenter) return false;
			if (this.akugyo.getTarget() != null) return false;
			if (this.wanderTarget == null) return false;
			double dx = this.wanderTarget.x - this.akugyo.getX();
			double dy = this.wanderTarget.y - this.akugyo.getY();
			double dz = this.wanderTarget.z - this.akugyo.getZ();
			double d2 = dx*dx + dy*dy + dz*dz;
			return d2 > 1.0D && this.lookTime < 200;
		}

		@Override
		public void start() {
			this.lookTime = 0;
			if (this.wanderTarget != null) {
				this.akugyo.getNavigation().moveTo(this.wanderTarget.x, this.wanderTarget.y, this.wanderTarget.z, 1.0D);
			}
		}

		@Override
		public void stop() {
			this.wanderTarget = null;
			this.akugyo.getNavigation().stop();
		}

		@Override
		public void tick() {
			if (this.wanderTarget == null) return;
			this.lookTime++;
			// If in water, use gentle direct velocity toward target for smooth swimming
			if (this.akugyo.isInWater()) {
				double dx = this.wanderTarget.x - this.akugyo.getX();
				double dy = this.wanderTarget.y - this.akugyo.getY();
				double dz = this.wanderTarget.z - this.akugyo.getZ();
				double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
				if (dist > 1.0E-6) {
					double nx = dx / dist;
					double ny = dy / dist;
					double nz = dz / dist;
					Vec3 vel = this.akugyo.getDeltaMovement();
					double targetSpeed = this.akugyo.level().dimension() == Level.END ? 1.5D : 0.6D; // patrol speed (faster in End)
					double ax = (nx * targetSpeed - vel.x) * 0.12;
					double ay = (ny * targetSpeed - vel.y) * 0.12;
					double az = (nz * targetSpeed - vel.z) * 0.12;
					this.akugyo.setDeltaMovement(vel.add(ax, ay, az));
				}
				this.akugyo.getLookControl().setLookAt(this.wanderTarget.x, this.wanderTarget.y, this.wanderTarget.z);
			} else {
				this.akugyo.getNavigation().moveTo(this.wanderTarget.x, this.wanderTarget.y, this.wanderTarget.z, 1.0D);
			}
		}
	}

	static class SharkPursueGoal extends Goal {
		private final AkugyoEntity akugyo;
		private int attackCooldown = 0;
		private final double speed;
private int circleTimer = 0;
private float circleSide = 1.0F;
private boolean ramming = false;
private int ramTimer = 0;
private static final int CIRCLE_DURATION = 80;
private static final int RAM_DURATION = 40;

		public SharkPursueGoal(AkugyoEntity akugyo, double speed) {
			this.akugyo = akugyo;
			this.speed = speed;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity t = this.akugyo.getTarget();
			if (this.akugyo.returningToCenter) return false;
			return t != null && t.isAlive();
		}

		@Override
		public boolean canContinueToUse() {
			LivingEntity t = this.akugyo.getTarget();
			if (this.akugyo.returningToCenter) return false;
			return t != null && t.isAlive();
		}

@Override
public void start() {
    this.attackCooldown = 0;
    this.circleTimer = 0;
    this.ramming = false;
    this.ramTimer = 0;
    this.circleSide = this.akugyo.random.nextBoolean() ? 1.0F : -1.0F;
}
		@Override
		public void stop() {
			this.akugyo.getNavigation().stop();
		}

		@Override
		public void tick() {
			LivingEntity t = this.akugyo.getTarget();
			if (t == null) return;
			if (this.akugyo.returningToCenter) {
				this.akugyo.getNavigation().stop();
				return;
			}

			// Ensure the attack cooldown always counts down each tick, even while in range.
			if (this.attackCooldown > 0) this.attackCooldown--;

			// Countdown fire breath cooldown
			if (this.akugyo.fireBreathCooldown > 0) {
				this.akugyo.fireBreathCooldown--;
			}

			// Relentlessly move toward the target. Use direct velocity in water
			// to ensure chase speed is effective even if pathing caps movement.
if (this.akugyo.isInWater()) {
    double dx = t.getX() - this.akugyo.getX();
    double dy = t.getY() - this.akugyo.getY();
    double dz = t.getZ() - this.akugyo.getZ();
    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
    Vec3 vel = this.akugyo.getDeltaMovement();

    if (this.ramming) {
        this.ramTimer++;
        if (dist > 1.0E-6) {
            double nx = dx / dist;
            double ny = dy / dist;
            double nz = dz / dist;
            double targetSpeed = this.akugyo.level().dimension() == Level.END ? 1.55 : 1.05;
            double ax = (nx * targetSpeed - vel.x) * 0.10;
            double ay = (ny * targetSpeed - vel.y) * 0.10;
            double az = (nz * targetSpeed - vel.z) * 0.10;
            this.akugyo.setDeltaMovement(vel.add(ax, ay, az));
        }
        if (this.ramTimer >= RAM_DURATION || dist < 2.0) {
            this.ramming = false;
            this.ramTimer = 0;
            this.circleTimer = 0;
            this.circleSide = -this.circleSide;
        }
    } else {
        this.circleTimer++;
        if (this.circleTimer >= CIRCLE_DURATION) {
            if (this.akugyo.random.nextFloat() < 0.6F) {
                this.ramming = true;
                this.ramTimer = 0;
            } else {
                this.circleSide = -this.circleSide;
                this.circleTimer = 0;
            }
        }
        double orbitRadius = Math.min(dist * 1.2, 22.0);
        double perpX = -dz * circleSide;
        double perpZ = dx * circleSide;
        double perpLen = Math.sqrt(perpX * perpX + perpZ * perpZ);
        if (perpLen > 1.0E-6) {
            perpX = (perpX / perpLen) * orbitRadius;
            perpZ = (perpZ / perpLen) * orbitRadius;
        }
        double targetX = t.getX() + perpX - this.akugyo.getX();
        double targetZ = t.getZ() + perpZ - this.akugyo.getZ();
        double targetDist = Math.sqrt(targetX * targetX + dy * dy + targetZ * targetZ);
        if (targetDist > 1.0E-6) {
            double nx = targetX / targetDist;
            double ny = dy / targetDist;
            double nz = targetZ / targetDist;
            double circleSpeed = this.akugyo.level().dimension() == Level.END ? 0.85 : 0.65;
            double ax = (nx * circleSpeed - vel.x) * 0.7;
            double ay = (ny * circleSpeed - vel.y) * 0.7;
            double az = (nz * circleSpeed - vel.z) * 0.7;
            this.akugyo.setDeltaMovement(vel.add(ax, ay, az));
        }
    }

    if (!this.akugyo.getNavigation().isDone()) {
        this.akugyo.getNavigation().stop();
    }
} else {
    this.akugyo.getNavigation().moveTo(t, this.speed);
}
			// Keep eyes on prey without jitter
			this.akugyo.getLookControl().setLookAt(t, 8.0F, 8.0F);
			this.akugyo.yBodyRot = this.akugyo.getYRot();

			double distSq = this.akugyo.distanceToSqr(t);

			// Fire breath attack at medium range (3-7 blocks)
			double dist = Math.sqrt(distSq);
			boolean inFireBreathRange = dist >= 3.0D && dist <= 8.0D;
			
			// Cancel fire breath if target gets too close or too far
			if (this.akugyo.fireBreathTicks > 0 && (dist < 2.5D || dist > 9.0D)) {
				this.akugyo.fireBreathTicks = 0;
				this.akugyo.fireBreathCooldown = Math.max(this.akugyo.fireBreathCooldown, 20);
			}

			// Start fire breath if at medium range and cooldown is ready
			if (inFireBreathRange && this.akugyo.fireBreathTicks == 0 && this.akugyo.fireBreathCooldown <= 0) {
				this.akugyo.fireBreathTicks = AkugyoEntity.FIRE_BREATH_DURATION;
				this.akugyo.playSound(SoundEvents.BLAZE_SHOOT, 1.2F, 0.8F + this.akugyo.getRandom().nextFloat() * 0.4F);
			}

			// Perform fire breath attack
			if (this.akugyo.fireBreathTicks > 0) {
				this.akugyo.performFireBreathAttack(t);
				this.akugyo.fireBreathTicks--;
				if (this.akugyo.fireBreathTicks == 0) {
					this.akugyo.fireBreathCooldown = AkugyoEntity.FIRE_BREATH_COOLDOWN_TICKS;
				}
			}

			// Use the standard melee reach check and only perform damage on the server.
			if (this.akugyo.isWithinMeleeAttackRange(t)) {
				if (this.attackCooldown <= 0) {
					this.attackCooldown = 20; // attack every 20 ticks
					if (!this.akugyo.level().isClientSide) {
						if (t instanceof Player p && (p.isCreative() || p.isSpectator())) {
							// don't attempt to damage creative/spectator players
						} else {
							// If the target is in a boat, destroy the boat first.
							if (t instanceof Player p) {
								if (p.getVehicle() instanceof Boat boat) {
									try { boat.hurt(this.akugyo.damageSources().mobAttack(this.akugyo), 999.0F); } catch (Throwable ignored) {}
									if (!boat.isRemoved()) {
										try { boat.discard(); } catch (Throwable ignored) {}
									}
								}
							}
							try { this.akugyo.swing(net.minecraft.world.InteractionHand.MAIN_HAND); } catch (Throwable ignored) {}
							float dmg = (float)this.akugyo.getAttributeValue(Attributes.ATTACK_DAMAGE);						// Apply bonus damage when in the End dimension
						if (this.akugyo.level().dimension() == Level.END) {
							dmg *= 1.5f; // 50% bonus damage in the End
						}							boolean hit = false;
							try {
								hit = t.hurt(this.akugyo.damageSources().mobAttack(this.akugyo), dmg);
							} catch (Throwable ignored) {
								try { hit = this.akugyo.doHurtTarget(t); } catch (Throwable ignored2) { hit = false; }
							}
							if (!hit) {
								try {
									net.minecraft.world.damagesource.DamageSource ds = this.akugyo.damageSources().mobAttack(this.akugyo);
									boolean inv = false;
									try { inv = t.isInvulnerableTo(ds); } catch (Throwable ignored) {}
									float hp = 0.0F;
									float abs = 0.0F;
									try { hp = (t instanceof net.minecraft.world.entity.LivingEntity le) ? le.getHealth() : 0.0F; } catch (Throwable ignored) {}
									try { abs = (t instanceof net.minecraft.world.entity.LivingEntity le2) ? le2.getAbsorptionAmount() : 0.0F; } catch (Throwable ignored) {}
									com.shioh.sengoku.sengokuFabric.LOGGER.warn("Akugyo attack failed: akugyo={} target={} distSq={} attackDamage={} invulnerableToSource={} targetHP={} targetAbs={}", this.akugyo.getUUID(), t.getUUID(), distSq, this.akugyo.getAttributeValue(Attributes.ATTACK_DAMAGE), inv, hp, abs);
								} catch (Throwable ignored) {}
							}
						}
					}
				}
			} else {
				if (this.attackCooldown > 0) this.attackCooldown--;
			}
		}
	}
}
