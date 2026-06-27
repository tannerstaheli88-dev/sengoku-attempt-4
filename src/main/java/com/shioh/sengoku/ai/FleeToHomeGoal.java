package com.shioh.sengoku.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;
import com.shioh.sengoku.entity.BanditEntity;
import com.shioh.sengoku.entity.RoninEntity;
import com.shioh.sengoku.entity.OnikumaEntity;
import com.shioh.sengoku.entity.SarugamiEntity;
import com.shioh.sengoku.entity.HitotsumeNyudoEntity;

import java.util.EnumSet;
import java.util.List;

public class FleeToHomeGoal extends Goal {

    private static final int    RECHECK_INTERVAL    = 20;    // ticks between path re-evaluations
    private static final float  THREAT_DETECT_RANGE = 16.0F;
    private static final float  THREAT_DANGER_ZONE  = 6.0F;  // blocks — path closer than this is "through" the mob
    private static final float  WAYPOINT_ARC_DIST   = 10.0F; // how far the arc swings sideways
    private static final float  GOLEM_SEARCH_RANGE  = 32.0F; // wider than threat range — see a golem across the village
    private static final double GOLEM_SHELTER_DIST  = 3.0;   // how far behind the golem the villager shelters
    private static final double GOLEM_REACHED_DIST  = 2.5;   // stop when this close to the shelter point
    private static final double FLEE_SPEED          = 0.8D;
    private static final double HOME_REACHED_DIST   = 2.5D;

    private final Villager villager;
    private BlockPos homePos;
    private LivingEntity primaryThreat;
    private IronGolem nearestGolem;
    private int tickCount;
    private Phase phase;

    private enum Phase {
        GOLEM,    // heading to shelter point behind nearest iron golem
        ARCING,   // heading to a safe waypoint beside/behind the threat
        HOMING,   // clear path to home, heading straight there
        FALLBACK  // no golem, no home memory — just run away
    }

    public FleeToHomeGoal(Villager villager) {
        this.villager = villager;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    // -------------------------------------------------------------------------
    // Goal lifecycle
    // -------------------------------------------------------------------------

@Override
public boolean canUse() {

primaryThreat = getNearestThreat();
if (primaryThreat == null) return false;
if (primaryThreat.isInWater()) return false;

    nearestGolem = getNearestGolem();
    homePos = villager.getBrain()
            .getMemory(MemoryModuleType.HOME)
            .map(GlobalPos::pos)
            .orElse(null);

    return true;
}

@Override
public boolean canContinueToUse() {
    LivingEntity current = getNearestThreat();
    if (current == null) return false;
    if (current.isInWater()) return false;
    primaryThreat = current;

    if (nearestGolem != null) {
        if (!nearestGolem.isAlive()) return false;
        Vec3 shelter = computeGolemShelterPoint();
        return villager.position().distanceToSqr(shelter) > GOLEM_REACHED_DIST * GOLEM_REACHED_DIST;
    }

    if (homePos != null) {
        return villager.blockPosition().distSqr(homePos) > HOME_REACHED_DIST * HOME_REACHED_DIST;
    }

    return true;
}

    @Override
    public void start() {
        tickCount = 0;
        choosePath();
    }

    @Override
    public void stop() {
        villager.getNavigation().stop();
        primaryThreat = null;
        nearestGolem  = null;
        homePos       = null;
    }

@Override
public void tick() {
    tickCount++;
    if (tickCount % RECHECK_INTERVAL != 0) return;

    primaryThreat = getNearestThreat();
    if (primaryThreat == null) return;

    nearestGolem = getNearestGolem();
    homePos = villager.getBrain()
            .getMemory(MemoryModuleType.HOME)
            .map(GlobalPos::pos)
            .orElse(null);

    choosePath();
}

    // -------------------------------------------------------------------------
    // Path selection
    // -------------------------------------------------------------------------

    /**
     * Priority: shelter behind golem → safe arc home → straight home → fallback
     */
private void choosePath() {
    if (homePos != null) {
        if (pathWouldPassThreatZone(villager.blockPosition(), homePos)) {
            // Path home is blocked — arc around or shelter behind golem if available
            if (nearestGolem != null && nearestGolem.isAlive()) {
                phase = Phase.GOLEM;
                Vec3 shelter = computeGolemShelterPoint();
                villager.getNavigation().moveTo(shelter.x, shelter.y, shelter.z, FLEE_SPEED);
            } else {
                Vec3 waypoint = computeArcWaypoint();
                phase = Phase.ARCING;
                villager.getNavigation().moveTo(waypoint.x, waypoint.y, waypoint.z, FLEE_SPEED);
            }
        } else {
            phase = Phase.HOMING;
            villager.getNavigation().moveTo(homePos.getX(), homePos.getY(), homePos.getZ(), FLEE_SPEED);
        }
        return;
    }

    // No home — shelter behind golem if possible, otherwise just run
    if (nearestGolem != null && nearestGolem.isAlive()) {
        phase = Phase.GOLEM;
        Vec3 shelter = computeGolemShelterPoint();
        villager.getNavigation().moveTo(shelter.x, shelter.y, shelter.z, FLEE_SPEED);
        return;
    }

    fallback();
}

    /**
     * Find a point behind the golem on the opposite side from the threat.
     * The villager shelters here rather than running into the golem.
     */
    private Vec3 computeGolemShelterPoint() {
        Vec3 golemPos  = nearestGolem.position();
        Vec3 threatPos = primaryThreat.position();

        // direction from threat toward golem — villager wants to be on this side
        Vec3 awayFromThreat = golemPos.subtract(threatPos).normalize();

        // GOLEM_SHELTER_DIST blocks behind the golem, away from the threat
        return golemPos.add(awayFromThreat.scale(GOLEM_SHELTER_DIST));
    }

    /**
     * Check whether the straight line from {@code from} to {@code to} passes
     * within THREAT_DANGER_ZONE blocks of any nearby threat.
     */
    private boolean pathWouldPassThreatZone(BlockPos from, BlockPos to) {
        List<LivingEntity> threats = getNearbyThreats();
        if (threats.isEmpty()) return false;

        Vec3 start    = Vec3.atCenterOf(from);
        Vec3 end      = Vec3.atCenterOf(to);
        Vec3 seg      = end.subtract(start);
        double segLenSq = seg.lengthSqr();

        for (LivingEntity threat : threats) {
            Vec3 tp = threat.position();
            double t = Math.max(0, Math.min(1, tp.subtract(start).dot(seg) / segLenSq));
            Vec3 closest = start.add(seg.scale(t));
            if (closest.distanceToSqr(tp) < THREAT_DANGER_ZONE * THREAT_DANGER_ZONE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compute a waypoint that arcs around the primary threat.
     * Picks the perpendicular side closest to the villager so they don't
     * have to cross behind the mob to reach the waypoint.
     */
    private Vec3 computeArcWaypoint() {
        Vec3 villagerPos = villager.position();
        Vec3 threatPos   = primaryThreat.position();

        Vec3 toHome = Vec3.atCenterOf(homePos).subtract(threatPos).normalize();
        Vec3 perp   = new Vec3(-toHome.z, 0, toHome.x);

        Vec3 candidateA = threatPos.add(perp.scale(WAYPOINT_ARC_DIST));
        Vec3 candidateB = threatPos.subtract(perp.scale(WAYPOINT_ARC_DIST));

        return candidateA.distanceToSqr(villagerPos) <= candidateB.distanceToSqr(villagerPos)
                ? candidateA : candidateB;
    }

    /**
     * Homeless, golem-less fallback — run directly away from the primary threat.
     */
    private void fallback() {
        phase = Phase.FALLBACK;
        Vec3 away   = villager.position().subtract(primaryThreat.position()).normalize().scale(12);
        Vec3 target = villager.position().add(away);
        villager.getNavigation().moveTo(target.x, target.y, target.z, FLEE_SPEED);
    }

    // -------------------------------------------------------------------------
    // Entity helpers
    // -------------------------------------------------------------------------

    private IronGolem getNearestGolem() {
        List<IronGolem> golems = villager.level().getEntitiesOfClass(
                IronGolem.class,
                villager.getBoundingBox().inflate(GOLEM_SEARCH_RANGE),
                IronGolem::isAlive
        );
        if (golems.isEmpty()) return null;
        return golems.stream()
                .min((a, b) -> Double.compare(a.distanceToSqr(villager), b.distanceToSqr(villager)))
                .orElse(null);
    }

    private LivingEntity getNearestThreat() {
        List<LivingEntity> threats = getNearbyThreats();
        if (threats.isEmpty()) return null;
        return threats.stream()
                .min((a, b) -> Double.compare(a.distanceToSqr(villager), b.distanceToSqr(villager)))
                .orElse(null);
    }

    private List<LivingEntity> getNearbyThreats() {
        return villager.level().getEntitiesOfClass(
                LivingEntity.class,
                villager.getBoundingBox().inflate(THREAT_DETECT_RANGE),
                this::isValidThreat
        );
    }

    /**
     * Returns true for any entity the villager should consider a threat.
     * Add or remove classes here to match your mod's hostile mob roster.
     */
private boolean isValidThreat(LivingEntity entity) {
    if (entity == villager) return false;
    if (!entity.isAlive()) return false;

    boolean isThreatType = entity instanceof AbstractSkeleton
        || entity instanceof Spider
        || entity instanceof AbstractIllager
        || entity instanceof Zombie
        || entity instanceof BanditEntity
        || entity instanceof RoninEntity
        || entity instanceof OnikumaEntity
        || entity instanceof SarugamiEntity
        || entity instanceof HitotsumeNyudoEntity;

    if (!isThreatType) return false;

    // Only care if the threat is targeting this villager or has line of sight
    if (entity instanceof Mob mob) {
        boolean targetingMe = mob.getTarget() == villager;
        boolean canSeeMe = mob.getSensing().hasLineOfSight(villager);
        return targetingMe || canSeeMe;
    }

    return false;
}
}