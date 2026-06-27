package com.shioh.sengoku.entity.ai;

import com.shioh.sengoku.entity.MacaqueEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.EnumSet;

/**
 * GroomGoal: when two nearby adult macaques are idle, they groom each other.
 * Spawns heart particles and grants a short regeneration buff.
 * popular for discord mods
 */
public class GroomGoal extends Goal {
    private final MacaqueEntity macaque;
    private MacaqueEntity partner;
    private int groomTicks = 0;
    // small delay to allow the groomer to sit visually before playing the grooming animation
    private int preGroomTicks = 0;
    private static final int PRE_GROOM_DELAY = 10; // half a second
    private final int maxGroomTicks = 80; // 4 seconds at 20tps
    private final int searchRadius;

    public GroomGoal(MacaqueEntity macaque, int searchRadius) {
        this.macaque = macaque;
        this.searchRadius = searchRadius;
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        try {
            // Only macaques assigned as sitting-role during settle may groom
            if (!this.macaque.isPackRoleSitting()) return false;
            if (this.macaque.isBaby()) return false;
            // respect per-entity groom cooldown so grooming doesn't immediately restart
            try { if (this.macaque.getGroomCooldown() > 0) return false; } catch (Throwable ignored) {}
            // if another macaque requested grooming (collision), honor that first
            try {
                int req = this.macaque.getGroomRequest();
                if (req != 0) {
                    var ent = this.macaque.level().getEntity(req);
                    if (ent instanceof MacaqueEntity) {
                        MacaqueEntity mreq = (MacaqueEntity) ent;
                        if (!mreq.isBaby() && !mreq.isGrooming() && !mreq.isBeingGroomed()) {
                            this.partner = mreq;
                            // clear request now; GroomGoal.start will set flags
                            this.macaque.setGroomRequest(0);
                            return true;
                        }
                    }
                }
            } catch (Throwable ignored) {}
            if (this.macaque.isGrooming()) return false;
            if (this.macaque.isBeingGroomed()) return false;
            // find a nearby adult macaque that is idle (nav done or sitting) and not grooming
            var level = this.macaque.level();
            for (var e : level.getEntitiesOfClass(MacaqueEntity.class, this.macaque.getBoundingBox().inflate(this.searchRadius, 1.5D, this.searchRadius))) {
                if (e == this.macaque) continue;
                MacaqueEntity m = (MacaqueEntity) e;
                if (m.isBaby()) continue;
                try { if (m.getGroomCooldown() > 0) continue; } catch (Throwable ignored) {}
                if (m.isGrooming()) continue;
                if (m.isBeingGroomed()) continue;
                boolean idle = false;
                try { idle = (m.getNavigation().isDone() || m.isSitting()); } catch (Throwable ignored) {}
                if (!idle) continue;
                this.partner = m;
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    @Override
    public void start() {
        this.groomTicks = 0;
        // enter pre-groom sitting phase so the groomer transitions from sitting -> grooming
        try {
            this.preGroomTicks = PRE_GROOM_DELAY;
            this.macaque.setForceSitting(true);
            this.macaque.setSitting(true);
        } catch (Throwable ignored) {}
        try {
            if (this.partner != null) {
                // partner should be sitting while waiting to be groomed
                this.partner.setBeingGroomed(true);
                this.partner.setSitting(true);
            }
        } catch (Throwable ignored) {}
        // stop moving and face partner while preparing to groom
        try { this.macaque.getNavigation().stop(); } catch (Throwable ignored) {}
        try { if (this.partner != null) this.macaque.getLookControl().setLookAt(this.partner, 30.0F, 30.0F); } catch (Throwable ignored) {}
    }

    @Override
    public boolean canContinueToUse() {
        if (this.partner == null) return false;
        if (this.partner.isRemoved()) return false;
        if (!this.partner.isAlive()) return false;
        if (this.partner.level() != this.macaque.level()) return false;
        if (this.groomTicks >= this.maxGroomTicks) return false;
        // require a reasonably close distance to remain grooming (4 blocks)
        double dx = this.partner.getX() - this.macaque.getX();
        double dy = this.partner.getY() - this.macaque.getY();
        double dz = this.partner.getZ() - this.macaque.getZ();
        double distSq = dx*dx + dy*dy + dz*dz;
        if (distSq > 16.0D) return false; // >4 blocks
        // if partner is no longer flagged as being groomed, cancel
        try { if (!this.partner.isBeingGroomed()) return false; } catch (Throwable ignored) {}
        return true;
    }

    @Override
    public void tick() {
        // If we're in the pre-groom phase, count down until actual grooming starts.
        if (this.preGroomTicks > 0) {
            this.preGroomTicks--;
            // ensure we face the partner during the pre-groom wait
            try { if (this.partner != null) this.macaque.getLookControl().setLookAt(this.partner, 30.0F, 30.0F); } catch (Throwable ignored) {}
            if (this.preGroomTicks == 0) {
                try { this.macaque.setForceSitting(false); } catch (Throwable ignored) {}
                try { this.macaque.setGrooming(true); } catch (Throwable ignored) {}
                this.groomTicks = 0; // start grooming tick count
            }
            return;
        }

        this.groomTicks++;
        // ensure we face the partner while grooming
        try { if (this.partner != null) this.macaque.getLookControl().setLookAt(this.partner, 30.0F, 30.0F); } catch (Throwable ignored) {}
        // safety: if partner disappeared or moved away mid-groom, stop early
        try {
            if (this.partner == null || this.partner.isRemoved() || !this.partner.isAlive()) {
                this.stop();
                return;
            }
            double dx = this.partner.getX() - this.macaque.getX();
            double dy = this.partner.getY() - this.macaque.getY();
            double dz = this.partner.getZ() - this.macaque.getZ();
            double distSq = dx*dx + dy*dy + dz*dz;
            if (distSq > 16.0D) {
                this.stop();
                return;
            }
        } catch (Throwable ignored) {}
        // apply a short regeneration buff once, at the start (server-side only)
        try {
            if (!this.macaque.level().isClientSide()) {
                if (this.groomTicks == 5) {
                    try {
                        // apply regen without showing potion particles (non-intrusive)
                        this.macaque.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false));
                        if (this.partner != null) this.partner.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, false));
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        // spawn tiny dirt/block-crack particles from partner to simulate debris
        try {
            if (this.partner != null && this.groomTicks > 0 && this.groomTicks % 4 == 0) {
                if (!this.macaque.level().isClientSide()) {
                    ServerLevel sl = (ServerLevel) this.macaque.level();
                    BlockState bs = Blocks.DIRT.defaultBlockState();
                    BlockParticleOption opt = new BlockParticleOption(ParticleTypes.BLOCK, bs);
                    double px = this.partner.getX();
                    double py = this.partner.getY() + 0.6D;
                    double pz = this.partner.getZ();
                    sl.sendParticles(opt, px, py, pz, 6, 0.25D, 0.12D, 0.25D, 0.03D);
                }
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void stop() {
        try { this.macaque.setGrooming(false); } catch (Throwable ignored) {}
        try {
            if (this.partner != null) {
                this.partner.setBeingGroomed(false);
                // partner can stop sitting when grooming ends (unless other reasons keep it sitting)
                this.partner.setSitting(false);
            }
        } catch (Throwable ignored) {}
        // set a fixed 10s (200 ticks) cooldown so grooming cannot immediately re-trigger
        try { this.macaque.setGroomCooldown(200); } catch (Throwable ignored) {}
        try { if (this.partner != null) this.partner.setGroomCooldown(200); } catch (Throwable ignored) {}
        this.partner = null;
        this.groomTicks = 0;
    }
}
