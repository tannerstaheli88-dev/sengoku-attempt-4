package com.shioh.sengoku.mixin;

import com.shioh.sengoku.entity.ShiryoEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Generic mixin applied to all `Mob` instances to suppress redundant calls to
 * `setTarget(LivingEntity)` when the same target is being re-applied within a
 * short cooldown window. Uses @Unique transient fields (non-persistent) so
 * behavior is per-instance and not saved to disk.
 */
@Mixin(Mob.class)
public class MobSetTargetMixin {

    @Unique
    private UUID sengoku$lastTargetUuid = null;

    @Unique
    private long sengoku$lastTargetTime = 0L;

    private static final long SENGOKU_COOLDOWN_TICKS = 40L; // 2s

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void sengoku$onSetTarget(LivingEntity target, CallbackInfo ci) {
        try {
            Mob self = (Mob) (Object) this;
            // Prevent Iron Golems from targeting clan members (takeda, satomi, kobayakawa)
            try {
                if (self instanceof net.minecraft.world.entity.animal.IronGolem && target != null) {
                    net.minecraft.world.entity.EntityType<?> t = target.getType();
                    if (t.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "takeda_clan"))) ||
                        t.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "satomi_clan"))) ||
                        t.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("sengoku", "kobayakawa_clan")))) {
                        ci.cancel();
                        return;
                    }
                }
            } catch (Throwable ignored) {}
            try {
                if (target instanceof ShiryoEntity && self instanceof net.minecraft.world.entity.monster.Monster) {
                    ci.cancel();
                    return;
                }
            } catch (Throwable ignored) {}
            if (target == null) {
                // allow clearing target and reset transient state
                this.sengoku$lastTargetUuid = null;
                this.sengoku$lastTargetTime = 0L;
                return;
            }

            Level level = self.level();
            if (level == null) return; // defensive

            long now = level.getGameTime();

            UUID targetUuid = target.getUUID();
            if (this.sengoku$lastTargetUuid != null && this.sengoku$lastTargetUuid.equals(targetUuid)) {
                if (now - this.sengoku$lastTargetTime <= SENGOKU_COOLDOWN_TICKS) {
                    // suppress redundant re-targeting to avoid replaying anger/detection effects
                    ci.cancel();
                    return;
                }
            }

            // record this target/timestamp for future suppressions
            this.sengoku$lastTargetUuid = targetUuid;
            this.sengoku$lastTargetTime = now;
        } catch (Throwable ignored) {}
    }
}
