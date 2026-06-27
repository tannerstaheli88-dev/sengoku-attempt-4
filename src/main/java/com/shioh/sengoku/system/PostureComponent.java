package com.shioh.sengoku.system;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Tracks posture (blocking stamina) for entities.
 */
public class PostureComponent {
    private final LivingEntity entity;

    private int posture;
    private int maxPosture;
    private int cooldownTicks;
    private boolean broken;

    public PostureComponent(LivingEntity entity, int maxPosture) {
        this.entity = entity;
        this.maxPosture = maxPosture;
        this.posture = maxPosture;
        this.cooldownTicks = 0;
        this.broken = false;
    }

    /** Apply posture damage. */
    public void damage(int amount) {
        if (broken) return;

        posture -= amount;
        if (posture <= 0) {
            breakPosture();
        }
    }

    /** Break posture and disable blocking temporarily. */
    private void breakPosture() {
        broken = true;
        cooldownTicks = 100; // 5 seconds @ 20 ticks/sec
        posture = 0;
        // TODO: optionally send packet/trigger animation
    }

    /** Called every tick to regen or handle cooldown. */
    public void tick() {
        if (entity instanceof Player) {
            // Player behavior: passive regeneration when not using an item
            if (broken) {
                if (cooldownTicks > 0) {
                    cooldownTicks--;
                } else {
                    reset();
                }
            } else {
                try {
                    if (!entity.isUsingItem()) {
                        if (posture < maxPosture) posture++;
                    }
                } catch (Throwable ignored) {
                    if (posture < maxPosture) posture++;
                }
            }
} else {
    // Mob behavior
    if (broken) {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        } else {
            reset();
        }
    } else {
        // Passive regen: 1 point every 2 seconds (every 40 ticks)
        if (posture < maxPosture && entity.level().getGameTime() % 10L == 0L) {
            posture++;
        }
    }
}
    }

    /** Reset posture after cooldown. */
    private void reset() {
        posture = maxPosture;
        broken = false;
    }

    /** Forcefully restore posture to full immediately (clear broken state). */
    public void restoreToFull() {
        this.posture = this.maxPosture;
        this.broken = false;
        this.cooldownTicks = 0;
    }

    public void setMaxPosture(int maxPosture) {
        this.maxPosture = Math.max(1, maxPosture);
        if (this.broken) {
            this.posture = 0;
            return;
        }
        this.posture = Math.min(this.posture, this.maxPosture);
    }

        public boolean isBroken() {
        return broken;
    }

    public int getPosture() {
        return posture;
    }

    public int getMaxPosture() {
        return maxPosture;
    }
}
