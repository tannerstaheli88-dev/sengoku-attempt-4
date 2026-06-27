package com.shioh.sengoku.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.Creeper;
import com.shioh.sengoku.entity.OmukadePartEntity;
import com.shioh.sengoku.entity.OmukadeEndEntity;
import com.shioh.sengoku.entity.IkuchiPartEntity;
import com.shioh.sengoku.entity.IkuchiEndEntity;
import com.shioh.sengoku.entity.WarlordEntity;
import com.shioh.sengoku.entity.ShinobiLordEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import com.shioh.sengoku.util.HurtTracker;
import java.util.UUID;
import com.shioh.sengoku.registry.ParticleRegistry;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import com.shioh.sengoku.registry.SoundRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevent mobs from setting invisible players as targets and drop
 * existing targets if they turn invisible.
 */
@Mixin(Mob.class)
public abstract class MobInvisibilityTargetMixin {

    // Suppression window (in ticks) after a mob is hurt by a player
    private static final long HURT_SUPPRESS_TICKS = 40L; // ~2 seconds
    // Remember which target UUID we've shown the alert for to avoid replaying
    // the detection particle when the same player becomes the mob's target again.
    private java.util.UUID sengoku$alertShownFor = null;


    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void sengoku$cancelInvisibleTarget(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob)(Object)this;

        // Prevent invisible players from being targeted
        if (target instanceof Player p && p.isInvisible()) {
            // Creepers ignore stealth and may target invisible players.
            if (self instanceof Creeper) return;
            ci.cancel();
            return;
        }

        // Additional protection for vanilla illagers: prevent them from acquiring
        // player targets when the player is not visible or is separated by floors.
        // Allow exceptions when the illager was directly hurt by that player (retaliation)
        // or the hurt event is recent (suppression window).
        try {
            if (target instanceof Player player && self instanceof net.minecraft.world.entity.monster.AbstractIllager) {
                // Allow direct retaliation
                try {
                    LivingEntity last = self.getLastHurtByMob();
                    if (last == player) return; // allow
                } catch (Throwable ignored) {}

                // Allow within recent hurt suppression window
                try {
                    java.util.UUID lastPlayer = HurtTracker.getLastPlayer(self.getUUID());
                    Long t = HurtTracker.getLastTime(self.getUUID());
                    if (lastPlayer != null && lastPlayer.equals(player.getUUID()) && t != null) {
                        long now = self.level().getGameTime();
                        if (now - t <= HURT_SUPPRESS_TICKS) return; // allow
                    }
                } catch (Throwable ignored) {}

                boolean hasLOS = false;
                try { hasLOS = self.getSensing().hasLineOfSight(player); } catch (Throwable ignored) {}
                if (hasLOS) return; // allow normally if visible

                // Vertical separation check
                try {
                    double dy = Math.abs(self.getY() - player.getY());
                    if (dy > 3.5D) { ci.cancel(); return; }
                } catch (Throwable ignored) {}

                // Check for hard floor/ceiling between them by sampling columns
                try {
                    int mobY = self.blockPosition().getY();
                    int targetY = player.blockPosition().getY();
                    int minY = Math.min(mobY, targetY);
                    int maxY = Math.max(mobY, targetY);
                    if (maxY - minY >= 2) {
                        net.minecraft.world.phys.Vec3[] sampleXZ = new net.minecraft.world.phys.Vec3[] {
                            new net.minecraft.world.phys.Vec3(self.getX(), 0.0D, self.getZ()),
                            new net.minecraft.world.phys.Vec3(player.getX(), 0.0D, player.getZ()),
                            new net.minecraft.world.phys.Vec3((self.getX() + player.getX()) * 0.5D, 0.0D, (self.getZ() + player.getZ()) * 0.5D)
                        };

                        boolean blocked = false;
                        for (net.minecraft.world.phys.Vec3 s : sampleXZ) {
                            for (int y = minY + 1; y < maxY; y++) {
                                net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos((int)Math.floor(s.x), y, (int)Math.floor(s.z));
                                net.minecraft.world.level.block.state.BlockState state = self.level().getBlockState(pos);
                                if (!state.isAir()) { blocked = true; break; }
                            }
                            if (blocked) break;
                        }

                        if (blocked) { ci.cancel(); return; }
                    }
                } catch (Throwable ignored) {}

                // If we reach here, there was no LOS but also no clear blocking floor; default to cancelling.
                ci.cancel();
            }
        } catch (Throwable ignored) {}
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void sengoku$clearInvisibleCurrentTarget(CallbackInfo ci) {
        Mob self = (Mob)(Object)this;
        LivingEntity current = self.getTarget();
        if (current instanceof Player p && p.isInvisible()) {
            // Creepers ignore stealth and should keep tracking invisible targets.
            if (self instanceof Creeper) {
                return;
            }
            // Clear the current target and also wipe revenge/last-hurt memory so the mob
            // doesn't immediately reacquire the player when they become visible again.
            try {
                self.setTarget(null);
            } catch (Throwable ignored) {}

            // Clear various "last hurt" references that mobs use to re-acquire targets
            try { self.setLastHurtByMob(null); } catch (Throwable ignored) {}
            try { self.setLastHurtByPlayer(null); } catch (Throwable ignored) {}
            try { self.setLastHurtMob(null); } catch (Throwable ignored) {}
        }

        // Clear the recent-hurt tag when the suppression window elapses
        try {
            UUID mobId = self.getUUID();
            Long t = com.shioh.sengoku.util.HurtTracker.getLastTime(mobId);
            if (t != null) {
                long now = self.level().getGameTime();
                if (now - t > HURT_SUPPRESS_TICKS) {
                    try { self.removeTag("sengoku_recently_hurt"); } catch (Throwable ignored) {}
                    try { com.shioh.sengoku.util.HurtTracker.clear(mobId); } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
        
        // === Proximity detection: if a player is walking/running within 3 blocks
        // the mob should detect them even if it doesn't have line-of-sight.
        // Exception: invisible players are never detected; sneaking (crouching)
        // still allows players to approach quietly and avoid detection.
        // For clan mobs (Takeda, Kobayakawa, Satomi), only detect if player has the clan advancement.
        try {
            // Only perform proximity detection for hostile monster mobs.
            if (self instanceof Monster && !(self instanceof EnderMan) && !(self instanceof ZombifiedPiglin) && !(self instanceof OmukadePartEntity) && !(self instanceof OmukadeEndEntity) && !(self instanceof IkuchiPartEntity) && !(self instanceof IkuchiEndEntity) && !self.level().isClientSide() && self.getTarget() == null && self.level() instanceof ServerLevel server) {
                java.util.List<net.minecraft.world.entity.player.Player> nearby = server.getEntitiesOfClass(
                    net.minecraft.world.entity.player.Player.class,
                    self.getBoundingBox().inflate(3.0D),
                    p -> !p.isSpectator() && !p.isCreative()
                );

                for (net.minecraft.world.entity.player.Player p : nearby) {
                    try {
                        // Skip invisible/sneaking players for most mobs, but creepers ignore stealth.
                        if (!(self instanceof Creeper)) {
                            if (p.isInvisible()) continue;
                            if (p.isCrouching()) continue; // sneaking avoids proximity detection
                        }

                        // Check if this is a clan mob that requires advancement or structure
                        String entityName = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(self.getType()).toString();
                        if (entityName.startsWith("sengoku:takeda_") || entityName.startsWith("sengoku:kobayakawa_") || entityName.startsWith("sengoku:satomi_")) {
                            // This is a clan mob - verify player has clan advancement OR is in guarded structure
                            if (!hasMatchingClanAdvancement(p, entityName) && !isInGuardedStructure(p, server)) {
                                continue; // Skip this player, they don't meet requirements
                            }
                        }

                        // Found a nearby walking/running player — set as target
                        try { self.setTarget(p); } catch (Throwable ignored) {}
                        break; // only acquire a single nearby player
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
    }

    // Hurt events are recorded in `LivingEntityHurtMixin` and stored in `HurtTracker`.

    @Inject(method = "setTarget", at = @At("RETURN"))
    private void sengoku$spawnAlertParticle(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob)(Object)this;
        String tag = "sengoku_alert_shown";

        // If target cleared, allow future alerts again
        if (target == null) {
            self.removeTag(tag);
            try { HurtTracker.clear(self.getUUID()); } catch (Throwable ignored) {}
            return;
        }

        // Only spawn a brief indicator when a player target is newly assigned
        if (!(target instanceof Player)) return;

        // Only show the detection particle for hostile mobs (Monsters), excluding Endermen, Zombified Piglins, and Omukade parts/end.
        if (!(self instanceof Monster) || (self instanceof EnderMan) || (self instanceof ZombifiedPiglin) || (self instanceof Creeper) || (self instanceof OmukadePartEntity) || (self instanceof OmukadeEndEntity) || (self instanceof IkuchiPartEntity) || (self instanceof IkuchiEndEntity)) return;
        // Warlord and Shinobi Lord should detect/target normally but without stealth alert VFX/SFX.
        if (self instanceof WarlordEntity || self instanceof ShinobiLordEntity) return;
        // If we've already shown the alert for this player's UUID, don't show it again
        try {
            java.util.UUID shown = sengoku$alertShownFor;
            if (shown != null && shown.equals(target.getUUID())) return;
        } catch (Throwable ignored) {}

        // Do not spawn the detection indicator when the mob is simply retaliating
        // because it was just hurt by the player (i.e., target was set due to damage).
        try {
            UUID mobId = self.getUUID();
            UUID lastPlayer = HurtTracker.getLastPlayer(mobId);
            Long t = HurtTracker.getLastTime(mobId);
            if (lastPlayer != null && lastPlayer.equals(target.getUUID()) && t != null) {
                long now = self.level().getGameTime();
                if (now - t <= HURT_SUPPRESS_TICKS) return;
            }
        } catch (Throwable ignored) {}

        Level level = self.level();
        if (!level.isClientSide() && level instanceof ServerLevel server) {
            double x = self.getX();
            double y = self.getEyeY() + 0.5D;
            double z = self.getZ();
            // Spawn a single detection particle above the mob to indicate detection (single '!')
            // Use no horizontal spread so the indicator is a single clear mark rather than stacked duplicates.
            server.sendParticles(ParticleRegistry.DETECTION_PARTICLE, x, y, z, 1, 0.0D, 0.08D, 0.0D, 0.12D);

            // Play an alert sound to accompany the detection particle (non-streaming custom sound).
                try {
                SoundEvent alert = SoundRegistry.ENEMY_ALERT;
                if (alert == null) {
                    try { com.shioh.sengoku.sengokuFabric.LOGGER.warn("enemy_alert SoundEvent not registered (SoundRegistry.ENEMY_ALERT is null); scheduling retry and running command fallback"); } catch (Throwable ignored) {}

                    // Command fallback: run a playsound command targeted at nearby players as a guaranteed client-side delivery.
                    try {
                        net.minecraft.server.MinecraftServer mc = server.getServer();
                        if (mc != null) {
                            java.util.List<net.minecraft.world.entity.player.Player> nearbyPlayersForCmd = server.getEntitiesOfClass(
                                    net.minecraft.world.entity.player.Player.class,
                                    self.getBoundingBox().inflate(32.0),
                                    p -> !p.isSpectator()
                            );

                            for (net.minecraft.world.entity.player.Player p : nearbyPlayersForCmd) {
                                try {
                                    String playerName = p.getName().getString();
                                    // playsound syntax: playsound <sound> <source> <targets> [x] [y] [z] [volume] [pitch]
                                    String cmd = String.format("playsound %s %s %s %f %f %f 1 1", "sengoku:enemy_alert", "hostile", playerName, x, y, z);
                                    mc.getCommands().performPrefixedCommand(mc.createCommandSourceStack().withPermission(2), cmd);
                                } catch (Throwable ignored) {}
                            }
                        }
                    } catch (Throwable ignored) {}

                    // Schedule a one-time retry shortly after to allow registry/asset loading to complete.
                    final ServerLevel srv = server;
                    final Mob mobRef = self;
                    java.util.concurrent.ScheduledExecutorService exec = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
                    exec.schedule(() -> {
                        try {
                            srv.getServer().execute(() -> {
                                try {
                                    SoundEvent retryAlert = SoundRegistry.ENEMY_ALERT;
                                    if (retryAlert == null) {
                                        try { com.shioh.sengoku.sengokuFabric.LOGGER.warn("enemy_alert SoundEvent still not registered on retry"); } catch (Throwable ignored) {}
                                        return;
                                    }

                                    double rx = mobRef.getX();
                                    double ry = mobRef.getEyeY() + 0.5D;
                                    double rz = mobRef.getZ();

                                    java.util.List<net.minecraft.world.entity.player.Player> nearbyPlayers = srv.getEntitiesOfClass(
                                            net.minecraft.world.entity.player.Player.class,
                                            mobRef.getBoundingBox().inflate(32.0),
                                            p -> !p.isSpectator()
                                    );

                                    if (nearbyPlayers.isEmpty()) {
                                        srv.playSound(null, rx, ry, rz, retryAlert, SoundSource.HOSTILE, 5.0F, 1.0F);
                                        try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Played enemy_alert (retry fallback) at {}", mobRef.blockPosition()); } catch (Throwable ignored) {}
                                    } else {
                                        for (net.minecraft.world.entity.player.Player p : nearbyPlayers) {
                                            try { srv.playSound(p, rx, ry, rz, retryAlert, SoundSource.HOSTILE, 5.0F, 1.0F); } catch (Throwable ignored) {}
                                        }
                                        try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Played enemy_alert on retry for {} players at {}", nearbyPlayers.size(), mobRef.blockPosition()); } catch (Throwable ignored) {}
                                    }
                                } catch (Throwable ignored) {}
                            });
                        } catch (Throwable ignored) {}
                        try { exec.shutdown(); } catch (Throwable ignored) {}
                    }, 100, java.util.concurrent.TimeUnit.MILLISECONDS);

                    // Direct packet fallback: send a ClientboundSoundPacket to nearby ServerPlayers
                    try {
                        long seed = server.getRandom().nextLong();
                        Holder<SoundEvent> holder = Holder.direct(SoundRegistry.ENEMY_ALERT);
                        java.util.List<net.minecraft.world.entity.player.Player> nearbyForPkt = server.getEntitiesOfClass(
                                net.minecraft.world.entity.player.Player.class,
                                self.getBoundingBox().inflate(32.0),
                                p -> !p.isSpectator()
                        );
                        int sent = 0;
                        for (net.minecraft.world.entity.player.Player p : nearbyForPkt) {
                            if (p instanceof ServerPlayer sp) {
                                try {
                                    // Send an entity-attached sound packet so the client perceives the sound as coming
                                    // from the mob (the enemy) rather than from the player's position.
                                    ClientboundSoundEntityPacket pkt = new ClientboundSoundEntityPacket(holder, SoundSource.HOSTILE, (net.minecraft.world.entity.Entity) (Object) self, 5.0F, 1.0F, seed);
                                    sp.connection.send(pkt);
                                    sent++;
                                } catch (Throwable ignored) {}
                            }
                        }
                        if (sent > 0) {
                            try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Sent direct enemy_alert (entity) packet to {} players at {}", sent, self.blockPosition()); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}
                } else {
                    // Count nearby players and log debug info to help diagnose playback issues
                    int playersNearby = 0;
                    try {
                        java.util.List<net.minecraft.world.entity.player.Player> nearby = server.getEntitiesOfClass(
                                net.minecraft.world.entity.player.Player.class,
                                self.getBoundingBox().inflate(32.0),
                                p -> !p.isSpectator()
                        );
                        playersNearby = nearby.size();
                    } catch (Throwable ignored) {}

                    try {
                        // Log that we are about to attempt playback and include the SoundEvent id when available
                        try {
                            com.shioh.sengoku.sengokuFabric.LOGGER.info("Attempting enemy_alert playback: alert={} playersNearby={} mobPos={}",
                                    alert == null ? "<null>" : alert.getLocation(), playersNearby, self.blockPosition());
                        } catch (Throwable ignored) {}

                        // Play the alert explicitly for each nearby player to ensure audibility
                        java.util.List<net.minecraft.world.entity.player.Player> nearbyPlayers = server.getEntitiesOfClass(
                                net.minecraft.world.entity.player.Player.class,
                                self.getBoundingBox().inflate(32.0),
                                p -> !p.isSpectator()
                        );

                        if (nearbyPlayers.isEmpty()) {
                            // Fallback to world-wide playSound (use elevated volume so players farther away hear it)
                            server.playSound(null, x, y, z, alert, SoundSource.HOSTILE, 5.0F, 1.0F);
                            try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Played enemy_alert (fallback) at {}", self.blockPosition()); } catch (Throwable ignored) {}
                        } else {
                            int success = 0;
                            for (net.minecraft.world.entity.player.Player p : nearbyPlayers) {
                                if (p instanceof ServerPlayer sp) {
                                        try {
                                        server.playSound(sp, x, y, z, alert, SoundSource.HOSTILE, 5.0F, 1.0F);
                                        success++;
                                    } catch (Throwable playEx) {
                                        try { com.shioh.sengoku.sengokuFabric.LOGGER.warn("Failed to play enemy_alert for {}: {}", p.getName().getString(), playEx.getMessage()); } catch (Throwable ignored) {}
                                    }
                                } else {
                                    try {
                                        server.playSound(p, x, y, z, alert, SoundSource.HOSTILE, 5.0F, 1.0F);
                                        success++;
                                    } catch (Throwable playEx) {
                                        try { com.shioh.sengoku.sengokuFabric.LOGGER.warn("Failed to play enemy_alert for {}: {}", p.getName().getString(), playEx.getMessage()); } catch (Throwable ignored) {}
                                    }
                                }
                            }
                            try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Played enemy_alert for {} players (attempted {}), mobPos={}", success, nearbyPlayers.size(), self.blockPosition()); } catch (Throwable ignored) {}
                        }

                        // Direct packet fallback in the non-null branch: send an entity-attached sound packet
                        try {
                            long seed = server.getRandom().nextLong();
                            Holder<SoundEvent> holder = Holder.direct(alert);
                            int pktSent = 0;
                            for (net.minecraft.world.entity.player.Player p : nearbyPlayers) {
                                if (p instanceof ServerPlayer sp) {
                                    try {
                                        ClientboundSoundEntityPacket pkt = new ClientboundSoundEntityPacket(holder, SoundSource.HOSTILE, (net.minecraft.world.entity.Entity) (Object) self, 1.0F, 1.0F, seed);
                                        sp.connection.send(pkt);
                                        pktSent++;
                                    } catch (Throwable ignored) {}
                                }
                            }
                            try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Sent direct enemy_alert (entity) packet to {} players at {}", pktSent, self.blockPosition()); } catch (Throwable ignored) {}
                        } catch (Throwable ignored) {}

                        // (No further fallback) we've already attempted per-player and world playSound above
                    } catch (Throwable t) {
                        try { com.shioh.sengoku.sengokuFabric.LOGGER.warn("Failed to play enemy_alert overall: {}", t.getMessage()); } catch (Throwable ignored) {}
                    }
                }
            } catch (Throwable ignored) {}

            // Play the mob's ambient (idle) sound once and stop any currently-playing instance
            try {
                // getAmbientSound() is protected in Mob, so access it via reflection
                SoundEvent ambient = null;
                try {
                    java.lang.reflect.Method m = net.minecraft.world.entity.Mob.class.getDeclaredMethod("getAmbientSound");
                    m.setAccessible(true);
                    ambient = (SoundEvent) m.invoke(self);
                } catch (Throwable ignore) {
                    // fallback: attempt to call via public getMethod (unlikely to exist)
                    try {
                        java.lang.reflect.Method m2 = net.minecraft.world.entity.Mob.class.getMethod("getAmbientSound");
                        ambient = (SoundEvent) m2.invoke(self);
                    } catch (Throwable ignored) {}
                }
                if (ambient != null) {
                    try {
                        ResourceLocation soundId = ambient.getLocation();
                        SoundSource src = self.getSoundSource();
                        try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Mob ambient sound id={} source={}", soundId, src); } catch (Throwable ignored) {}

                        // Avoid interfering with the enemy_alert playback: if the ambient sound ID equals our custom alert, skip stopping/playing it.
                        if (soundId != null && soundId.toString().equals("sengoku:enemy_alert")) {
                            try { com.shioh.sengoku.sengokuFabric.LOGGER.info("Skipping ambient stop/play because ambient is enemy_alert"); } catch (Throwable ignored) {}
                        } else {
                            // Stop the same ambient sound for nearby players so it doesn't overlap
                            try {
                                java.util.List<net.minecraft.world.entity.player.Player> nearby = server.getEntitiesOfClass(
                                    net.minecraft.world.entity.player.Player.class,
                                    self.getBoundingBox().inflate(32.0),
                                    p -> !p.isSpectator()
                                );
                                for (net.minecraft.world.entity.player.Player p : nearby) {
                                    if (p instanceof ServerPlayer sp) {
                                        try {
                                            ClientboundStopSoundPacket pkt = new ClientboundStopSoundPacket(soundId, src);
                                            sp.connection.send(pkt);
                                        } catch (Throwable ignored) {}
                                    }
                                }
                            } catch (Throwable ignored) {}

                            // Prevent the mob from immediately playing its own ambient again
                            try { self.ambientSoundTime = -self.getAmbientSoundInterval(); } catch (Throwable ignored) {}

                            // Play the ambient sound at the mob's location for nearby players
                            try { server.playSound(null, x, y, z, ambient, src, 1.0F, 1.0F); } catch (Throwable ignored) {}
                        }
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}
        }

        // Mark that we've shown the alert for this target UUID
        try { sengoku$alertShownFor = target.getUUID(); } catch (Throwable ignored) {}
    }

    /**
     * Check if the player has the matching clan advancement for this mob entity.
     */
    private static boolean hasMatchingClanAdvancement(net.minecraft.world.entity.player.Player player, String entityName) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;

        // Extract clan name from entity name (e.g., "sengoku:takeda_ashigaru" -> "takeda")
        String clanName = null;
        if (entityName.startsWith("sengoku:takeda_")) {
            clanName = "takeda";
        } else if (entityName.startsWith("sengoku:kobayakawa_")) {
            clanName = "kobayakawa";
        } else if (entityName.startsWith("sengoku:satomi_")) {
            clanName = "satomi";
        }

        if (clanName == null) return false;

        // Check for the clan-specific kill advancement
        ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath("shioh", "main/kill_" + clanName + "_sohei");
        
        try {
            var advancements = serverPlayer.getAdvancements();
            var progress = advancements.getOrStartProgress(
                serverPlayer.server.getAdvancements().get(advancementId)
            );
            return progress != null && progress.isDone();
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Check if player is in a samurai_will_guard structure.
     */
    private static boolean isInGuardedStructure(net.minecraft.world.entity.player.Player player, ServerLevel serverLevel) {
        try {
            net.minecraft.tags.TagKey<net.minecraft.world.level.levelgen.structure.Structure> structureTag = 
                net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.STRUCTURE,
                    ResourceLocation.fromNamespaceAndPath("shioh", "samurai_will_guard"));
            
            var structureRegistry = serverLevel.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
            net.minecraft.world.level.StructureManager structureManager = serverLevel.structureManager();

            for (net.minecraft.core.Holder<net.minecraft.world.level.levelgen.structure.Structure> holder : structureRegistry.getTagOrEmpty(structureTag)) {
                net.minecraft.world.level.levelgen.structure.Structure structure = holder.value();
                net.minecraft.world.level.levelgen.structure.StructureStart start = structureManager.getStructureAt(player.blockPosition(), structure);
                
                if (start != null && start.isValid() && start.getBoundingBox().isInside(player.blockPosition())) {
                    return true;
                }
            }
        } catch (Throwable e) {
            return false;
        }

        return false;
    }
}
