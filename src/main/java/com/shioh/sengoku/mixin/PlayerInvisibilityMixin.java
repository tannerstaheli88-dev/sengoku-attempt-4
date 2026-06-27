package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * When a player turns invisible, clear aggro memory from nearby mobs that were targeting them.
 */
@Mixin(Player.class)
public abstract class PlayerInvisibilityMixin {

    // Track last-known invisibility state per player UUID
    private static final Map<UUID, Boolean> lastInvisibility = new ConcurrentHashMap<>();

    @Inject(method = "tick", at = @At("HEAD"))
    private void sengoku$onPlayerTick(CallbackInfo ci) {
        Player self = (Player) (Object) this;

        if (self.level().isClientSide) return;

        boolean nowInvisible = self.isInvisible();
        UUID id = self.getUUID();
        boolean prevInvisible = lastInvisibility.getOrDefault(id, false);

        // Detect transition: visible -> invisible
        if (nowInvisible && !prevInvisible) {
            // Clear aggro from nearby mobs that currently target this player
            double radius = 32.0D;
            AABB area = new AABB(self.getX() - radius, self.getY() - radius, self.getZ() - radius,
                    self.getX() + radius, self.getY() + radius, self.getZ() + radius);

            for (Mob mob : self.level().getEntitiesOfClass(Mob.class, area)) {
                try {
                    if (mob.getTarget() == self) {
                        try { mob.setTarget(null); } catch (Throwable ignored) {}
                        try { mob.setLastHurtByMob(null); } catch (Throwable ignored) {}
                        try { mob.setLastHurtByPlayer(null); } catch (Throwable ignored) {}
                        try { mob.setLastHurtMob(null); } catch (Throwable ignored) {}
                        try { mob.removeTag("sengoku_alert_shown"); } catch (Throwable ignored) {}
                    }
                } catch (Throwable ignored) {}
            }
        }

        lastInvisibility.put(id, nowInvisible);
    }
}
