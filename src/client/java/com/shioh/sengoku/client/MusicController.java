package com.shioh.sengoku.client;

import com.shioh.sengoku.mixin.client.BossHealthOverlayAccessor;
import com.shioh.sengoku.registry.SoundRegistry;
import com.shioh.sengoku.sengokuFabric;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;

/**
 * Client-side music controller for Sengoku mod.
 * Overrides vanilla Overworld music with custom day/night/underground tracks.
 * Features smooth volume fade transitions between categories.
 * 
 * Fade-out takes 2 seconds (40 ticks) with gradual volume decrease.
 * After a brief 0.5 second pause, new music fades in over 1 second (20 ticks).
 */
public class MusicController {
    private enum Category { NONE, DAY, NIGHT, UNDERGROUND, DEEP_UNDERGROUND, DEEP_DARK, YOMI, COMBAT_RYUGU, RYUGU, VILLAGE, CASTLE, CASTLE_COMBAT, COMBAT_BASIC, COMBAT_YOMI, RAID, RONIN, SHINOBI, SHINOBI_LORD_PHASE_1, SHINOBI_LORD_PHASE_2, TATARIGAMI, WARLORD_PHASE_1, WARLORD_PHASE_2, DRAGON_PHASE_1, DRAGON_PHASE_2, RUINS }
    
    // Warlord (boss) server active flag
    private static boolean warlordServerActiveFlag = false;
    // Tatarigami (Wither) server active flag
    private static boolean tatarigamiServerActiveFlag = false;

    private static int checkCooldown = 0;
    private static int fadeCooldown = 0;
    private static final int CHECK_INTERVAL = 20; // check once per second
    private static final int FADE_OUT_TICKS = 40; // 2 seconds fade out
    private static final int FADE_IN_DELAY = 10; // 0.5 second pause before fade in
    private static final int FADE_IN_TICKS = 20; // 1 second fade in
    private static final int REPLAY_INTERVAL = 9600; // 8 minutes (480 seconds) before playing next track
    private static final int RAID_REPLAY_INTERVAL = 6000; // 5 minutes (300 seconds) for raid music
    private static final int WARLORD_PHASE_1_REPLAY_INTERVAL = 4700; // 3:55 (235s)
    private static final int WARLORD_PHASE_2_REPLAY_INTERVAL = 5000; // 4:10 (250s)
    private static final int DRAGON_PHASE_1_REPLAY_INTERVAL = 2640; // mimic warlord timings
    private static final int DRAGON_PHASE_2_REPLAY_INTERVAL = 3000;
    private static final int SHINOBI_LORD_PHASE_1_REPLAY_INTERVAL = 2100; // 1:45 (105s)
    private static final int SHINOBI_LORD_PHASE_2_REPLAY_INTERVAL = 3540; // 3:40 (220s)
    private static final int TATARIGAMI_REPLAY_INTERVAL = 3900; // 3:15 (ticks)
    private static final int RYUGU_REPLAY_INTERVAL = 3760; // 3:08 (188s -> 3760 ticks)
    private static final int COMBAT_RYUGU_REPLAY_INTERVAL = 1800; // 1:30 (90s -> 1800 ticks)
    private static final int DAY_REPLAY_INTERVAL = 4400; // 3:40 (220s)
    private static final int NIGHT_REPLAY_INTERVAL = 7400; // 8 minutes
    private static final int UNDERGROUND_REPLAY_INTERVAL = 7400; // 8 minutes
    private static final int DEEP_UNDERGROUND_REPLAY_INTERVAL = 5200; // 8 minutes
    private static final int DEEP_DARK_REPLAY_INTERVAL = 6900; // 8 minutes
    private static final int YOMI_REPLAY_INTERVAL = 6200; // 8 minutes
    private static final int COMBAT_YOMI_REPLAY_INTERVAL = 1200; // used to be 3200
    private static final int RUINS_REPLAY_INTERVAL = 3800; // 8 minutes
    private static final int VILLAGE_REPLAY_INTERVAL = 7600; // 8 minutes
    private static final int CASTLE_REPLAY_INTERVAL = 3100; // 8 minutes
    private static final int CASTLE_COMBAT_REPLAY_INTERVAL = 1200; // used to be 3500
    private static final int COMBAT_BASIC_REPLAY_INTERVAL = 1200; // used to be 4000
    private static final int RONIN_REPLAY_INTERVAL = 1200; // used to be 4000
    private static final int SHINOBI_REPLAY_INTERVAL = 1200; // used to be 4000
    
    private static Category currentCategory = Category.NONE;
    private static Category targetCategory = Category.NONE;
    private static int replayTicksRemaining = 0; // countdown until next track plays in same category
    private static boolean isFading = false;
    private static boolean isFadingIn = false;
    private static FadingMusicInstance currentMusicInstance = null;
    private static FadingMusicInstance victoryMusicInstance = null;
    private static boolean victoryPending = false;
    private static int victoryCooldownTicks = 0; // ticks to wait after victory before playing music again
    private static int victoryDebugTicks = 0; // ticks until debug indicator fires (82s)
    private static boolean victoryDebugShown = false;
    // Retry logic to attempt resuming music if the first resume attempt fails
    private static int resumeRetryAttempts = 0;
    private static int resumeRetryTimer = 0;
    private static final int RESUME_RETRY_INTERVAL = 20; // ticks between attempts
    private static int fadeInTicksRemaining = 0;
    
    // Volume fade state
    private static float originalMusicVolume = -1.0f;
    private static int fadeTicksRemaining = 0;
    // Grace period after queueing a track to avoid treating it as ended
    private static int justQueuedTicks = 0; // decremented each tick; while >0 we won't restart

    // Server-synced flags
    // - Being inside a village-tagged structure
    private static boolean isInVillageStructure = false;
    private static int villageHoldTicks = 0; // brief client-side hold to mask packet jitter
    private static final int VILLAGE_HOLD_DURATION = 40; // 2 seconds
    // - Being inside a castle-tagged structure
    private static boolean isInCastleStructure = false;
    private static int castleHoldTicks = 0; // brief client-side hold
    private static final int CASTLE_HOLD_DURATION = 40;
    // - Whether a raid is active near the player (server-detected)
    private static boolean isRaidActive = false;
    // When a victory song is playing, this flag helps the tick loop treat it as a normal track until it ends
    private static boolean victorySongPlaying = false;
    // Yuki Onna suppression flags
    // - local: client-side stalking overlay detection
    private static boolean yukiOnnaLocalStalkFlag = false;
    // - server: server-synced aggro/stalk active near the player
    private static boolean yukiOnnaServerActiveFlag = false;
    // Shinobi (Illusioner) aggro music flag (server-synced)
    private static boolean shinobiServerActiveFlag = false;
    // Shinobi Lord (boss) aggro music phase (server-synced)
    private static int shinobiLordServerPhase = 0;
    // Warlord (boss) aggro music flag (server-synced)
    private static int warlordServerPhase = 0;
    // Tatarigami (Wither) aggro music flag (server-synced, client)
    private static boolean tatarigamiServerActiveFlag_client = false;
    // Patrol (bandit/clan) aggro music flag (server-synced)
    private static boolean patrolServerActiveFlag = false;
    // Castle combat server-synced flag (one-or-more hostiles aggro inside castle)
    private static boolean castleCombatServerActiveFlag = false;
    // Basic universal combat server-synced flag (lowest-priority combat music)
    private static boolean combatBasicServerActiveFlag = false;
    // Nether/Yomi combat server-synced flag
    private static boolean combatYomiServerActiveFlag = false;
    // Ryugu (End) combat server-synced flag
    private static boolean combatRyuguServerActiveFlag = false;
    // Ender Dragon (boss) server-synced phase
    private static int dragonServerPhase = 0;

    // Reflection cache for LerpingBossEvent progress access
    private static volatile Method cachedLerpingProgressMethod = null;
    private static volatile Field cachedLerpingProgressField = null;
    private static volatile boolean lerpingDiscoveryAttempted = false;

    private static boolean isYukiSuppressed() {
        return yukiOnnaLocalStalkFlag || yukiOnnaServerActiveFlag;
    }

    // Reflectively discover and read the progress/percent value from LerpingBossEvent.
    // Caches a working Method or Field to avoid repeated discovery cost.
    private static Float readLerpingBossEventProgress(LerpingBossEvent event) {
        if (event == null) return null;
        try {
            if (cachedLerpingProgressMethod != null) {
                try {
                    Object v = cachedLerpingProgressMethod.invoke(event);
                    if (v instanceof Number) return ((Number) v).floatValue();
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    sengokuFabric.LOGGER.warn("[Music][DEBUG] Cached LerpingBossEvent method failed: {}", ex.toString());
                    cachedLerpingProgressMethod = null; // reset and try discovery
                }
            }
            if (cachedLerpingProgressField != null) {
                try {
                    Object fv = cachedLerpingProgressField.get(event);
                    if (fv instanceof Number) return ((Number) fv).floatValue();
                } catch (IllegalAccessException ex) {
                    sengokuFabric.LOGGER.warn("[Music][DEBUG] Cached LerpingBossEvent field failed: {}", ex.toString());
                    cachedLerpingProgressField = null;
                }
            }

            if (!lerpingDiscoveryAttempted) {
                lerpingDiscoveryAttempted = true;
                Class<?> cls = LerpingBossEvent.class;
                // Try common method names first
                String[] methodNames = new String[] {"getPercent", "getProgress", "percent", "progress", "getPct", "getValue"};
                for (String name : methodNames) {
                    try {
                        Method m = cls.getDeclaredMethod(name);
                        m.setAccessible(true);
                        Class<?> rt = m.getReturnType();
                        if (rt == float.class || rt == Float.class || rt == double.class || rt == Double.class) {
                            cachedLerpingProgressMethod = m;
                            Object v = m.invoke(event);
                            if (v instanceof Number) return ((Number) v).floatValue();
                        }
                    } catch (NoSuchMethodException ignored) {
                    }
                }

                // Try fields: pick first numeric float/double field
                java.lang.reflect.Field[] fields = cls.getDeclaredFields();
                for (Field f : fields) {
                    Class<?> ft = f.getType();
                    if (ft == float.class || ft == Float.class || ft == double.class || ft == Double.class) {
                        try {
                            f.setAccessible(true);
                            cachedLerpingProgressField = f;
                            Object fv = f.get(event);
                            if (fv instanceof Number) return ((Number) fv).floatValue();
                        } catch (IllegalAccessException ignored) {}
                    }
                }

                // If we got here, discovery failed; log available methods/fields for debugging
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Available LerpingBossEvent methods: ");
                    for (Method m : cls.getDeclaredMethods()) sb.append(m.getName()).append(",");
                    sb.append(" fields: ");
                    for (Field f : fields) sb.append(f.getName()).append("(").append(f.getType().getSimpleName()).append("),");
                    sengokuFabric.LOGGER.warn("[Music][DEBUG] LerpingBossEvent discovery failed. {}", sb.toString());
                } catch (Throwable ignored) {}
            }
        } catch (Throwable t) {
            try { sengokuFabric.LOGGER.warn("[Music][DEBUG] Error discovering LerpingBossEvent progress: {}", t.toString()); } catch (Throwable ignored) {}
        }
        return null;
    }

    // Returns 0 = no dragon boss bar, 1 = phase1 (>50% health), 2 = phase2 (<=50% health)
    private static int getDragonBossPhase() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.gui == null || client.gui.getBossOverlay() == null) return 0;
        try {
            // Access lerping events via mixin accessor
            Map<UUID, LerpingBossEvent> events = ((BossHealthOverlayAccessor) client.gui.getBossOverlay()).sengoku$getEvents();
            if (events == null || events.isEmpty()) return 0;
            for (LerpingBossEvent event : events.values()) {
                if (event == null || event.getName() == null) continue;
                try { sengokuFabric.LOGGER.debug("[Music][DEBUG] Boss entry name: {}", event.getName().getString()); } catch (Throwable ignored) {}
                if (event.getName().getContents() instanceof TranslatableContents translatable
                    && "entity.minecraft.ender_dragon".equals(translatable.getKey())) {
                    // Use reflective reader to read progress (remap-safe)
                    try {
                        Float progressVal = readLerpingBossEventProgress(event);
                        if (progressVal != null) {
                            float progress = progressVal.floatValue();
                            if (progress <= 0.5f) return 2;
                            return 1;
                        } else {
                            try { sengokuFabric.LOGGER.warn("[Music][DEBUG] Could not discover LerpingBossEvent progress accessor"); } catch (Throwable ignored2) {}
                            return 1;
                        }
                    } catch (Throwable ignored) {
                        return 1;
                    }
                }
                // Fallback: check display name for 'dragon' or common localized variants
                try {
                    String plainName = event.getName().getString();
                    if (plainName != null) {
                        String lower = plainName.toLowerCase(Locale.ROOT);
                        if (lower.contains("dragon") || lower.contains("ryu") || lower.contains("ryū")) {
                            try {
                                Float progressVal = readLerpingBossEventProgress(event);
                                if (progressVal != null) {
                                    float progress = progressVal.floatValue();
                                    if (progress <= 0.5f) return 2;
                                    return 1;
                                }
                                return 1;
                            } catch (Throwable ignored) {
                                return 1;
                            }
                        }
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {}
        return 0;
    }

    public static String getCurrentMusicCategoryLabel() {
        Category active = currentCategory != null && currentCategory != Category.NONE ? currentCategory : targetCategory;
        String label = active != null && active != Category.NONE ? active.name().replace('_', ' ') : "NONE";
        return label;
    }

    public static void setVillageStructureFlag(boolean inside) {
        isInVillageStructure = inside;
        if (inside) villageHoldTicks = VILLAGE_HOLD_DURATION;
    }

    public static void setCastleStructureFlag(boolean inside) {
        isInCastleStructure = inside;
        if (inside) castleHoldTicks = CASTLE_HOLD_DURATION;
    }

    /**
     * Called by client-local detection (overlay) when stalking state changes.
     * Local false will NOT forcibly resume music if the server still reports Yuki Onna active.
     */
    public static void setYukiOnnaLocalStalk(boolean active) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.execute(() -> {
                yukiOnnaLocalStalkFlag = active;
                if (active) {
                    try { client.getMusicManager().stopPlaying(); } catch (Throwable ignored) {}
                    try { if (currentMusicInstance != null) client.getSoundManager().stop(currentMusicInstance); } catch (Throwable ignored) {}
                    currentMusicInstance = null;
                    currentCategory = Category.NONE;
                    targetCategory = Category.NONE;
                    isFading = false;
                    isFadingIn = false;
                } else {
                    // Only force re-evaluation when neither local nor server suppression remain
                    if (!yukiOnnaServerActiveFlag) {
                        checkCooldown = 0;
                    }
                }
            });
        } catch (Throwable ignored) {}
    }

    /**
     * Called by server packet receiver when server reports a nearby Yuki Onna is aggro/stalking.
     * Server-side true forces suppression; false will only resume if local stalk is also false.
     */
    public static void setYukiOnnaServerActive(boolean active) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.execute(() -> {
                yukiOnnaServerActiveFlag = active;
                if (active) {
                    try { client.getMusicManager().stopPlaying(); } catch (Throwable ignored) {}
                    try { if (currentMusicInstance != null) client.getSoundManager().stop(currentMusicInstance); } catch (Throwable ignored) {}
                    currentMusicInstance = null;
                    currentCategory = Category.NONE;
                    targetCategory = Category.NONE;
                    isFading = false;
                    isFadingIn = false;
                } else {
                    if (!yukiOnnaLocalStalkFlag) {
                        checkCooldown = 0;
                    }
                }
            });
        } catch (Throwable ignored) {}
    }

    // Called by server packet receiver when an Illusioner near the player is aggro (targeting the player)
    public static void setShinobiServerActive(boolean active) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.execute(() -> {
                shinobiServerActiveFlag = active;
                if (active) {
                    // Force immediate re-evaluation and fade out current music if needed
                    checkCooldown = 0;
                } else {
                    // Resume normal music selection
                    checkCooldown = 0;
                }
            });
        } catch (Throwable ignored) {}
    }

    // Called by server packet receiver when server reports a nearby Shinobi Lord phase.
    public static void setShinobiLordServerPhase(int phase) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.execute(() -> {
                int normalizedPhase = Math.max(0, Math.min(2, phase));
                boolean wasActive = shinobiLordServerPhase > 0;
                boolean isActive = normalizedPhase > 0;
                boolean phaseChanged = shinobiLordServerPhase != normalizedPhase;
                shinobiLordServerPhase = normalizedPhase;
                if (isActive && (!wasActive || phaseChanged)) {
                    try { client.getMusicManager().stopPlaying(); } catch (Throwable ignored) {}
                    try { if (currentMusicInstance != null) client.getSoundManager().stop(currentMusicInstance); } catch (Throwable ignored) {}
                    currentMusicInstance = null;
                    currentCategory = Category.NONE;
                    targetCategory = Category.NONE;
                    isFading = false;
                    isFadingIn = false;
                    replayTicksRemaining = 0;
                    checkCooldown = 0;
                } else if (!isActive) {
                    checkCooldown = 0;
                }
            });
        } catch (Throwable ignored) {}
    }

    // Called by server packet receiver when server reports a nearby Warlord is aggro.
    public static void setWarlordServerPhase(int phase) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.execute(() -> {
                int normalizedPhase = Math.max(0, Math.min(2, phase));
                boolean wasActive = warlordServerPhase > 0;
                boolean isActive = normalizedPhase > 0;
                boolean phaseChanged = warlordServerPhase != normalizedPhase;
                // For Warlord we want an immediate re-evaluation; stop current music so boss music can start
                warlordServerPhase = normalizedPhase;
                if (isActive && (!wasActive || phaseChanged)) {
                    try { client.getMusicManager().stopPlaying(); } catch (Throwable ignored) {}
                    try { if (currentMusicInstance != null) client.getSoundManager().stop(currentMusicInstance); } catch (Throwable ignored) {}
                    currentMusicInstance = null;
                    currentCategory = Category.NONE;
                    targetCategory = Category.NONE;
                    isFading = false;
                    isFadingIn = false;
                    replayTicksRemaining = 0;
                    checkCooldown = 0;
                } else if (!isActive) {
                    // Force immediate re-evaluation when flag clears
                    checkCooldown = 0;
                }
            });
        } catch (Throwable ignored) {}
    }

    // Called by server packet receiver when server reports a nearby Wither (Tatarigami) is aggro.
    public static void setTatarigamiServerActive(boolean active) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.execute(() -> {
                try { sengokuFabric.LOGGER.info("[Music] Received Tatarigami server flag: {}", active); } catch (Throwable ignored) {}
                // For boss music we want an immediate re-evaluation; stop current music so boss music can start
                tatarigamiServerActiveFlag_client = active;
                if (active) {
                    try { client.getMusicManager().stopPlaying(); } catch (Throwable ignored) {}
                    try { if (currentMusicInstance != null) client.getSoundManager().stop(currentMusicInstance); } catch (Throwable ignored) {}
                    currentMusicInstance = null;
                    currentCategory = Category.NONE;
                    // Force category selection to TATARIGAMI immediately
                    targetCategory = Category.TATARIGAMI;
                    // Force immediate re-evaluation next tick so the "Switching NONE -> TATARIGAMI" log appears
                    checkCooldown = 0;
                } else {
                    // Clear flag and force immediate re-evaluation when flag clears
                    checkCooldown = 0;
                }
            });
        } catch (Throwable ignored) {}
    }

    // Called by server packet receiver when server reports a nearby Ender Dragon phase.
    public static void setDragonServerPhase(int phase) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.execute(() -> {
                int normalizedPhase = Math.max(0, Math.min(2, phase));
                boolean wasActive = dragonServerPhase > 0;
                boolean isActive = normalizedPhase > 0;
                boolean phaseChanged = dragonServerPhase != normalizedPhase;
                dragonServerPhase = normalizedPhase;
                if (isActive && (!wasActive || phaseChanged)) {
                    try { client.getMusicManager().stopPlaying(); } catch (Throwable ignored) {}
                    try { if (currentMusicInstance != null) client.getSoundManager().stop(currentMusicInstance); } catch (Throwable ignored) {}
                    currentMusicInstance = null;
                    currentCategory = Category.NONE;
                    targetCategory = Category.NONE;
                    isFading = false;
                    isFadingIn = false;
                    replayTicksRemaining = 0;
                    checkCooldown = 0;
                } else if (!isActive) {
                    checkCooldown = 0;
                }
            });
        } catch (Throwable ignored) {}
    }

    // Called by server packet receiver when a patrol (bandit/clan) is aggro near the player
    public static void setPatrolServerActive(boolean active) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.execute(() -> {
                patrolServerActiveFlag = active;
                // Force immediate re-evaluation
                checkCooldown = 0;
            });
        } catch (Throwable ignored) {}
    }

    // Called by server packet receiver when castle combat starts/stops near the player
    public static void setCastleCombatServerActive(boolean active) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.execute(() -> {
                try { sengokuFabric.LOGGER.info("[Music] Received castle combat server flag: {}", active); } catch (Throwable ignored) {}
                // If a Warlord boss music is active and combat started, do not override it
                if ((warlordServerPhase > 0 || tatarigamiServerActiveFlag_client) && active) {
                    try { sengokuFabric.LOGGER.info("[Music] Boss active; ignoring castle combat flag"); } catch (Throwable ignored) {}
                    return;
                }
                castleCombatServerActiveFlag = active;
                // Force re-evaluation so the normal music selection logic can handle transitions
                checkCooldown = 0;
            });
        } catch (Throwable ignored) {}
    }

    // Called by server packet receiver when any tagged combat mob is aggro near the player
    public static void setCombatBasicServerActive(boolean active) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.execute(() -> {
                try { sengokuFabric.LOGGER.info("[Music] Received basic combat server flag: {}", active); } catch (Throwable ignored) {}
                // Do not override boss music (Warlord or Tatarigami)
                if ((warlordServerPhase > 0 || tatarigamiServerActiveFlag_client) && active) {
                    try { sengokuFabric.LOGGER.info("[Music] Boss active; ignoring basic combat flag"); } catch (Throwable ignored) {}
                    return;
                }
                combatBasicServerActiveFlag = active;
                // Force re-evaluation so the normal music selection logic can handle transitions
                checkCooldown = 0;
            });
        } catch (Throwable ignored) {}
    }

    // Called by server packet receiver when Nether-tagged combat mobs are aggro near the player
    public static void setCombatYomiServerActive(boolean active) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.execute(() -> {
                try { sengokuFabric.LOGGER.info("[Music] Received Yomi combat server flag: {}", active); } catch (Throwable ignored) {}
                // Do not override boss music (Warlord or Tatarigami)
                if ((warlordServerPhase > 0 || tatarigamiServerActiveFlag_client) && active) {
                    try { sengokuFabric.LOGGER.info("[Music] Boss active; ignoring Yomi combat flag"); } catch (Throwable ignored) {}
                    return;
                }
                combatYomiServerActiveFlag = active;
                // Force re-evaluation so the normal music selection logic can handle transitions
                checkCooldown = 0;
            });
        } catch (Throwable ignored) {}
    }

    public static void setCombatRyuguServerActive(boolean active) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) return;
        try {
            client.execute(() -> {
                try { sengokuFabric.LOGGER.info("[Music] Received Ryugu combat server flag: {}", active); } catch (Throwable ignored) {}
                // Do not override boss music (Warlord or Tatarigami)
                if ((warlordServerPhase > 0 || tatarigamiServerActiveFlag_client) && active) {
                    try { sengokuFabric.LOGGER.info("[Music] Boss active; ignoring Ryugu combat flag"); } catch (Throwable ignored) {}
                    return;
                }
                combatRyuguServerActiveFlag = active;
                // Force re-evaluation so the normal music selection logic can handle transitions
                checkCooldown = 0;
            });
        } catch (Throwable ignored) {}
    }

    

    public static void setRaidActiveFlag(boolean active) {
        isRaidActive = active;
    }

    public static void setRaidState(int state) {
        // 0 = NONE, 1 = ACTIVE, 2 = VICTORY
        try {
            try { sengokuFabric.LOGGER.info("[Music] Received raid state: {}", state); } catch (Throwable ignored) {}
            if (state == 1) {
                isRaidActive = true;
            } else {
                isRaidActive = false;
            }

            if (state == 2) {
                // Ignore duplicate or late victory triggers while we already handled one
                if (victorySongPlaying || victoryPending || victoryCooldownTicks > 0) {
                    try { sengokuFabric.LOGGER.info("[Music] Ignoring duplicate VICTORY trigger"); } catch (Throwable ignored) {}
                    return;
                }
                // Raid victory: fade out current raid music, then play victory track at the village bell.
                Minecraft client = Minecraft.getInstance();
                if (client != null) {
                    try {
                        client.execute(() -> {
                            try {
                                // If music is playing, fade it out over FADE_OUT_TICKS and mark victory pending
                                if (currentMusicInstance != null && currentMusicInstance.isPlaying()) {
                                    try { sengokuFabric.LOGGER.info("[Music] Victory pending - fading current music"); } catch (Throwable ignored) {}
                                    isFading = true;
                                    fadeCooldown = FADE_OUT_TICKS;
                                    currentMusicInstance.setTargetVolume(0.01f, FADE_OUT_TICKS);
                                    victoryPending = true;
                                    // suppress category selection while pending
                                    targetCategory = Category.NONE;
                                } else {
                                    // No music playing; start victory immediately and set cooldown (82s)
                                    try { sengokuFabric.LOGGER.info("[Music] Starting victory track immediately"); } catch (Throwable ignored) {}
                                    victoryMusicInstance = FadingMusicInstance.forMusic(com.shioh.sengoku.registry.SoundRegistry.MUSIC_VICTORY, 1.0f);
                                    client.getSoundManager().play(victoryMusicInstance);
                                    justQueuedTicks = 20;
                                    victorySongPlaying = true;
                                    victoryCooldownTicks = 82 * 20;
                                    victoryDebugTicks = 82 * 20;
                                    victoryDebugShown = false;
                                    // ensure no category is targeted during cooldown
                                    targetCategory = Category.NONE;
                                    currentCategory = Category.NONE;
                                }
                            } catch (Throwable ignored) {}
                        });
                    } catch (Throwable ignored) {}
                }
            }
        } catch (Throwable ignored) {}
    }

    public static void clientTick() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.isPaused()) return;
        LocalPlayer player = client.player;
        if (player == null) return;
        // If Ender Dragon boss is active (boss bar) or alive in The End, suppress Ryugu music immediately and skip music logic.
        try {
            // no-op: rely on detectCategory() entity check to suppress Ryugu when dragon present
        } catch (Throwable ignored) {}

        // If player is dead or has zero health, clear any stalking/aggro suppression so music can resume
        try {
            if (player.getHealth() <= 0.0f) {
                if (yukiOnnaLocalStalkFlag || yukiOnnaServerActiveFlag) {
                    setYukiOnnaLocalStalk(false);
                    setYukiOnnaServerActive(false);
                }
            }
        } catch (Throwable ignored) {}

        // If Yuki Onna suppression is active (local overlay OR server aggro), ensure no music plays and skip all music logic.
        if (isYukiSuppressed()) {
            try { client.getMusicManager().stopPlaying(); } catch (Throwable ignored) {}
            try { if (currentMusicInstance != null) client.getSoundManager().stop(currentMusicInstance); } catch (Throwable ignored) {}
            currentMusicInstance = null;
            try { if (victoryMusicInstance != null) client.getSoundManager().stop(victoryMusicInstance); } catch (Throwable ignored) {}
            victoryMusicInstance = null;
            // Clear transient flags that would otherwise start new tracks
            victoryPending = false;
            victorySongPlaying = false;
            isFading = false;
            isFadingIn = false;
            targetCategory = Category.NONE;
            currentCategory = Category.NONE;
            return;
        }

        if (justQueuedTicks > 0) justQueuedTicks--; // countdown queue grace
        boolean justExpired = false;
        if (victoryCooldownTicks > 0) {
            victoryCooldownTicks--; // global cooldown after victory
            if (victoryCooldownTicks == 0) justExpired = true;
        }
        if (victoryDebugTicks > 0) victoryDebugTicks--; // countdown until debug indicator
        
        // Countdown replay timer - when it expires, play next track in same category
        if (replayTicksRemaining > 0) {
            replayTicksRemaining--;
            if (replayTicksRemaining == 0 && currentCategory != Category.NONE) {
                int replayTicks = REPLAY_INTERVAL;
                switch (currentCategory) {
                    case RAID:
                        replayTicks = RAID_REPLAY_INTERVAL; break;
                    case SHINOBI_LORD_PHASE_1:
                        replayTicks = SHINOBI_LORD_PHASE_1_REPLAY_INTERVAL; break;
                    case SHINOBI_LORD_PHASE_2:
                        replayTicks = SHINOBI_LORD_PHASE_2_REPLAY_INTERVAL; break;
                    case WARLORD_PHASE_1:
                        replayTicks = WARLORD_PHASE_1_REPLAY_INTERVAL; break;
                    case WARLORD_PHASE_2:
                        replayTicks = WARLORD_PHASE_2_REPLAY_INTERVAL; break;
                    case TATARIGAMI:
                        replayTicks = TATARIGAMI_REPLAY_INTERVAL; break;
                    default:
                        replayTicks = REPLAY_INTERVAL; break;
                }
                double seconds = replayTicks / 20.0;
                try { sengokuFabric.LOGGER.info("[Music] Replay timer expired after {}s ({} ticks) for category {}; playing next track", seconds, replayTicks, currentCategory); } catch (Throwable ignored) {}
                playMusicForCategory(client, currentCategory);
            }
        }

        // If a victory track is playing, keep it exclusive: stop vanilla music and skip
        // category/fade processing until the victory instance completes.
        if (victorySongPlaying) {
            try { client.getMusicManager().stopPlaying(); } catch (Throwable ignored) {}
            try {
                if (victoryMusicInstance != null && victoryMusicInstance.hasStopped()) {
                    try { sengokuFabric.LOGGER.info("[Music] Victory track finished"); } catch (Throwable ignored) {}
                    // Victory finished; clear and let normal music resume
                    victoryMusicInstance = null;
                    victorySongPlaying = false;
                    // Force re-evaluation next tick
                    checkCooldown = 0;
                } else if (victoryCooldownTicks <= 0) {
                    // Cooldown expired but the instance may still be flagged as playing
                    try { sengokuFabric.LOGGER.info("[Music] Victory cooldown expired while instance still active; forcing stop"); } catch (Throwable ignored) {}
                    try { if (victoryMusicInstance != null) client.getSoundManager().stop(victoryMusicInstance); } catch (Throwable ignored) {}
                    victoryMusicInstance = null;
                    victorySongPlaying = false;
                    // Force re-evaluation next tick
                    checkCooldown = 0;
                } else {
                    return; // still playing and cooldown not expired, do nothing else
                }
            } catch (Throwable ignored) {}
        }

        // If the victory cooldown just expired, force a reset and show a debug indicator so
        // it's easy to verify the client will resume normal music.
        if (justExpired && !victoryDebugShown) {
            try { sengokuFabric.LOGGER.info("[Music] Victory cooldown expired (82s) - forcing music resume"); } catch (Throwable ignored) {}
            // Debug chat removed per user request
            victoryDebugShown = true;
            // Clear transient state so detectCategory and normal music selection can run
            victoryPending = false;
            victorySongPlaying = false;
            isFading = false;
            isFadingIn = false;
            currentMusicInstance = null;
            currentCategory = Category.NONE;
            targetCategory = Category.NONE;
            // Force immediate re-evaluation and play the selected category right away
            checkCooldown = 0; // force immediate re-evaluation
            Category resumeCat = detectCategory(player);
            if (resumeCat != Category.NONE) {
                currentCategory = resumeCat;
                targetCategory = resumeCat;
                playMusicForCategory(client, resumeCat);
                // start retry attempts if playback didn't start immediately
                if (currentMusicInstance == null) {
                    resumeRetryAttempts = 5;
                    resumeRetryTimer = 0;
                } else {
                    try { sengokuFabric.LOGGER.info("[Music] Resume successful immediately: {}", resumeCat); } catch (Throwable ignored) {}
                }
            }
        }

        // Retry loop: if initial resume didn't start playback, retry a few times
        if (resumeRetryAttempts > 0) {
            if (resumeRetryTimer > 0) resumeRetryTimer--;
            if (resumeRetryTimer <= 0) {
                try {
                    Category retryCat = detectCategory(player);
                    if (retryCat != Category.NONE) {
                        try { sengokuFabric.LOGGER.info("[Music] Retry resume attempt for category {} ({} attempts left)", retryCat, resumeRetryAttempts); } catch (Throwable ignored) {}
                        playMusicForCategory(client, retryCat);
                        if (currentMusicInstance != null) {
                            // resumed
                            resumeRetryAttempts = 0;
                            resumeRetryTimer = 0;
                            try { sengokuFabric.LOGGER.info("[Music] Resume successful on retry: {}", retryCat); } catch (Throwable ignored) {}
                            // Debug chat removed per user request
                        } else {
                            resumeRetryAttempts--;
                            resumeRetryTimer = RESUME_RETRY_INTERVAL;
                        }
                    } else {
                        // Nothing to play; stop retrying
                        resumeRetryAttempts = 0;
                        resumeRetryTimer = 0;
                    }
                } catch (Throwable ignored) {}
            }
        }

        // Manage music in the Overworld, Nether (Yomi), and the End (Ryugu). Other dimensions are ignored.
        if (!isOverworld(player.level()) && !isNether(player.level()) && !isEnd(player.level())) {
            stopCurrentMusic(client);
            currentCategory = Category.NONE;
            targetCategory = Category.NONE;
            return;
        }

        // Stop vanilla music manager so we have full control
        // NOTE: do NOT stop the vanilla music manager while in the End so we don't suppress
        // the vanilla End music. Only stop it for Overworld/Nether-managed dimensions.
        if (!isEnd(player.level())) {
            try {
                client.getMusicManager().stopPlaying();
            } catch (Throwable ignored) {}
        }

        // Check category periodically
        if (--checkCooldown <= 0) {
            checkCooldown = CHECK_INTERVAL;
            Category detected = detectCategory(player);
            if (detected != targetCategory) {
                targetCategory = detected;
            }
        }

        // Handle fade transitions
        if (targetCategory != currentCategory && !isFading) {
            // Reset replay timer when switching categories
            replayTicksRemaining = 0;
            
            // Start fade out if music is playing
            if (currentMusicInstance != null && currentMusicInstance.isPlaying()) {
                try { sengokuFabric.LOGGER.info("[Music] Switching {} -> {} (start fade out)", currentCategory, targetCategory); } catch (Throwable ignored) {}
                isFading = true;
                fadeCooldown = FADE_OUT_TICKS;
                // Tell instance to fade out
                currentMusicInstance.setTargetVolume(0.01f, FADE_OUT_TICKS);
                fadeTicksRemaining = FADE_OUT_TICKS;
            } else {
                // No music playing, switch immediately
                try { sengokuFabric.LOGGER.info("[Music] Switching {} -> {} (no current music; play immediately)", currentCategory, targetCategory); } catch (Throwable ignored) {}
                currentCategory = targetCategory;
                playMusicForCategory(client, currentCategory);
            }
        }

        // Fade-out countdown
        if (isFading) {
            fadeCooldown--;
            if (fadeCooldown <= 0) {
                // Stop old track now
                stopCurrentMusic(client);
                isFading = false;
                isFadingIn = true;
                fadeInTicksRemaining = FADE_IN_TICKS;
                // If a victory is pending, start that instead of the normal category
                if (victoryPending) {
                    try { sengokuFabric.LOGGER.info("[Music] Fade complete; starting victory track"); } catch (Throwable ignored) {}
                    victoryPending = false;
                    try {
                        victoryMusicInstance = FadingMusicInstance.forMusic(com.shioh.sengoku.registry.SoundRegistry.MUSIC_VICTORY, 1.0f);
                        client.getSoundManager().play(victoryMusicInstance);
                        justQueuedTicks = 20;
                        victorySongPlaying = true;
                        victoryCooldownTicks = 82 * 20;
                    } catch (Throwable ignored) {}
                    // ensure we don't also start a category track
                    currentCategory = Category.NONE;
                    currentMusicInstance = null;
                } else {
                    currentCategory = targetCategory;
                    try { sengokuFabric.LOGGER.info("[Music] Fade complete; now playing category {}", currentCategory); } catch (Throwable ignored) {}
                    playMusicForCategory(client, currentCategory);
                    // Tell new instance to fade in
                    if (currentMusicInstance != null) {
                        currentMusicInstance.setTargetVolume(1.0f, FADE_IN_TICKS);
                    }
                }
            }
        }

        // Fade-in countdown; once complete, return to normal start volume behaviour
        if (isFadingIn) {
            if (--fadeInTicksRemaining <= 0) {
                isFadingIn = false;
                fadeInTicksRemaining = 0;
            }
        }
    }

    private static boolean isOverworld(Level level) {
        try {
            // Check dimension key directly
            return level.dimension().location().getPath().equals("overworld");
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean isNether(Level level) {
        try {
            // Accept both the dimension constant and direct key name
            return level.dimension() == net.minecraft.world.level.Level.NETHER || level.dimension().location().getPath().equals("the_nether");
        } catch (Throwable t) {
            return false;
        }
    }
    
    private static boolean isEnd(Level level) {
        try {
            return level.dimension() == net.minecraft.world.level.Level.END || level.dimension().location().getPath().equals("the_end");
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean isDragonBossBarVisible() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.gui == null || client.gui.getBossOverlay() == null) {
            return false;
        }

        try {
            Map<?, LerpingBossEvent> events = ((BossHealthOverlayAccessor) client.gui.getBossOverlay()).sengoku$getEvents();
            for (LerpingBossEvent event : events.values()) {
                if (event == null || event.getName() == null) {
                    continue;
                }

                if (event.getName().getContents() instanceof TranslatableContents translatable
                    && "entity.minecraft.ender_dragon".equals(translatable.getKey())) {
                    return true;
                }

                String plainName = event.getName().getString();
                if (plainName != null && plainName.equalsIgnoreCase("Ender Dragon")) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }
    

    private static Category detectCategory(LocalPlayer player) {
        // Suppress all category selection while Yuki Onna suppression is active
        if (isYukiSuppressed()) return Category.NONE;
        
        Level level = player.level();
        // If the client is showing an end-credits / win / credits-like screen while in the End,
        // treat this as no-music (NONE) so the credits play silently.
        try {
            Minecraft client = Minecraft.getInstance();
            if (client != null && client.screen != null) {
                String clsName = client.screen.getClass().getName().toLowerCase(Locale.ROOT);
                boolean looksLikeCredits = clsName.contains("credit") || clsName.contains("credits") || clsName.contains("win");
                if (looksLikeCredits) {
                    try { sengokuFabric.LOGGER.info("[Music] Credits-like screen detected: {}", client.screen.getClass().getName()); } catch (Throwable ignored) {}
                    if (level != null) {
                        try {
                            if (level.dimension() == net.minecraft.world.level.Level.END || level.dimension().location().getPath().equals("the_end")) {
                                return Category.NONE;
                            }
                        } catch (Throwable ignored) {
                            return Category.NONE;
                        }
                    } else {
                        return Category.NONE;
                    }
                }
            }
        } catch (Throwable ignored) {}
        BlockPos pos = player.blockPosition();
        int playerY = pos.getY();
        long dayTime = level.getDayTime() % 24000L;
        // Shift music schedule forward slightly:
        // - Day starts a little after dawn to avoid very-early morning tracks.
        // - Night begins a bit later so night music kicks in later in the evening.
        // Adjust these constants to tweak timing (ticks; 20 ticks = 1 second).
        final long DAY_START_TICK = 0L; // start at dawn
        final long DAY_END_TICK = 13000L; // night starts at 13000 instead of 12000
        boolean isDay = dayTime >= DAY_START_TICK && dayTime < DAY_END_TICK;
        
        // Respect victory cooldown: while cooling down, suppress normal music
        if (victoryCooldownTicks > 0) return Category.NONE;

        // If a victory song is playing, suppress other category selection
        if (victorySongPlaying) return Category.NONE;

        // Server-synced raid activity near the player
        boolean isInRaid = isRaidActive;
        boolean isPatrolActive = patrolServerActiveFlag;
        boolean isShinobiActive = shinobiServerActiveFlag; // Illusioner aggro music
        int shinobiLordPhase = shinobiLordServerPhase;
        
        // Priority 1: Raid music (whenever a raid boss bar is active near the player)
        if (isInRaid) {
            return Category.RAID;
        }

        // Priority 2: Patrol (bandit/clan) aggro overrides ambient categories except raids
        if (isPatrolActive) {
            return Category.RONIN;
        }

        // Priority 3: Shinobi Lord boss music phases
        if (shinobiLordPhase >= 2) {
            return Category.SHINOBI_LORD_PHASE_2;
        }

        if (shinobiLordPhase == 1) {
            return Category.SHINOBI_LORD_PHASE_1;
        }

        // Priority 3.1: Shinobi (Illusioner aggro)
        if (isShinobiActive) {
            return Category.SHINOBI;
        }

        // Priority 3.2: Tatarigami (Wither) boss - highest-priority boss music
        if (tatarigamiServerActiveFlag_client) {
            return Category.TATARIGAMI;
        }

        // Priority 3.5: Warlord (boss) overrides castle/village music when active
        if (warlordServerPhase >= 2) {
            return Category.WARLORD_PHASE_2;
        }

        if (warlordServerPhase == 1) {
            return Category.WARLORD_PHASE_1;
        }

        // Priority 3.75: Castle combat (server-synced) - overrides regular castle music but NOT Warlord
        if (castleCombatServerActiveFlag) {
            return Category.CASTLE_COMBAT;
        }

        // Priority 3.8: Yomi (Nether) combat (server-synced) - scoped to Nether
        if (level.dimension() == net.minecraft.world.level.Level.NETHER && combatYomiServerActiveFlag) {
            return Category.COMBAT_YOMI;
        }

        

        // Priority 3.9: Basic universal combat (server-synced) - lowest-priority combat music
        if (combatBasicServerActiveFlag) {
            return Category.COMBAT_BASIC;
        }

        // Priority 4: Ambient Yomi when in Nether and no combat flags
        if (level.dimension() == net.minecraft.world.level.Level.NETHER) {
            return Category.YOMI;
        }
        
        // Priority 4.5: Ryugu (End) - override End music with boss music when dragon active.
        if (level.dimension() == net.minecraft.world.level.Level.END) {
            // Server-driven Dragon phase takes precedence
            if (dragonServerPhase >= 2) {
                return Category.DRAGON_PHASE_2;
            }
            if (dragonServerPhase == 1) {
                return Category.DRAGON_PHASE_1;
            }

            // Fallback: if the client's boss bar shows the dragon, detect phase via boss progress
            int localPhase = getDragonBossPhase();
            if (localPhase == 2) return Category.DRAGON_PHASE_2;
            if (localPhase == 1) return Category.DRAGON_PHASE_1;

            // If a generic combat flag is active, play Ryugu combat; otherwise ambient Ryugu
            if (combatRyuguServerActiveFlag) {
                return Category.COMBAT_RYUGU;
            }
            return Category.RYUGU;
        }

        

        
        // Priority 3: Deep Dark biome (overrides deep underground)
        try {
            net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biomeHolder = level.getBiome(pos);
            net.minecraft.resources.ResourceLocation biomeLoc = biomeHolder.unwrapKey()
                .map(key -> key.location())
                .orElse(null);
            if (biomeLoc != null && "deep_dark".equals(biomeLoc.getPath())) {
                return Category.DEEP_DARK;
            }
        } catch (Throwable ignored) {}

        // Priority 4: Ruins (desert) - night only, surface only (must be able to see sky)
        // Move this check above the underground checks to ensure surface desert ruins
        // are detected correctly (requires exposure to sky and night time).
        boolean canSeeSky = level.canSeeSky(pos);
        try {
            net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biomeHolder3 = level.getBiome(pos);
            net.minecraft.resources.ResourceLocation biomeLoc3 = biomeHolder3.unwrapKey()
                .map(key -> key.location())
                .orElse(null);
            if (biomeLoc3 != null && biomeLoc3.getPath().toLowerCase().contains("desert")) {
                try { sengokuFabric.LOGGER.debug("[Music] Ruins detected at biome {} (dayTime={}, isDay={})", biomeLoc3, dayTime, isDay); } catch (Throwable ignored) {}
                return Category.RUINS;
            }
        } catch (Throwable ignored) {}
        
        // Absolute Y-based logic + sky visibility:
        // Y < 0 AND not exposed to sky => deep underground
        // Y <= 54 AND !canSeeSky => underground (BOTH conditions required)
        // If player is at deep-underground Y but exposed to sky, treat as surface (day/night decides)
// NEW - hysteresis bands to prevent music flickering at boundary Y levels
boolean currentlyDeepUnderground = currentCategory == Category.DEEP_UNDERGROUND;
boolean currentlyUnderground = currentCategory == Category.UNDERGROUND;

int deepEnterY = -10;   // must drop to here to trigger deep underground
int deepExitY  = 0;     // must rise above here to leave deep underground

int ugEnterY   = 45;    // must drop to here to trigger underground
int ugExitY    = 54;    // must rise above here to leave underground

if (!canSeeSky) {
    // Already in deep underground: stay until player rises above deepExitY
    if (currentlyDeepUnderground) {
        if (playerY <= deepExitY) return Category.DEEP_UNDERGROUND;
        // else fall through to check if still qualifies for regular underground
    } else {
        // Enter deep underground only when dropping to deepEnterY
        if (playerY <= deepEnterY) return Category.DEEP_UNDERGROUND;
    }

    // Already in underground: stay until player rises above ugExitY
    if (currentlyUnderground) {
        if (playerY <= ugExitY) return Category.UNDERGROUND;
        // else fall through to surface logic
    } else {
        // Enter underground only when dropping to ugEnterY
        if (playerY <= ugEnterY) return Category.UNDERGROUND;
    }
}

        // Priority: Ruins (desert) - night only, surface only (must be able to see sky)
        try {
            net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biomeHolder3 = level.getBiome(pos);
            net.minecraft.resources.ResourceLocation biomeLoc3 = biomeHolder3.unwrapKey()
                .map(key -> key.location())
                .orElse(null);
            if (biomeLoc3 != null && biomeLoc3.getPath().toLowerCase().contains("desert") && !isDay) {
                return Category.RUINS;
            }
        } catch (Throwable ignored) {}

        // Priority 5: Castle music (server-synced). Similar to village, prefer daytime; holds briefly.
        if (isDay && !isInRaid) {
            if (isInCastleStructure || castleHoldTicks > 0) {
                castleHoldTicks = Math.max(0, castleHoldTicks - CHECK_INTERVAL);
                return Category.CASTLE;
            }
        } else {
            castleHoldTicks = Math.max(0, castleHoldTicks - CHECK_INTERVAL);
        }

        // Priority 6: Village music (server-synced, daytime only)
        if (isDay && !isInRaid) {
            if (isInVillageStructure || villageHoldTicks > 0) {
                villageHoldTicks = Math.max(0, villageHoldTicks - CHECK_INTERVAL);
                return Category.VILLAGE;
            }
        } else {
            villageHoldTicks = Math.max(0, villageHoldTicks - CHECK_INTERVAL);
        }

        // Surface: check day/night
        if (isDay) {
            return Category.DAY;
        } else {
            return Category.NIGHT;
        }
    }

    private static void playMusicForCategory(Minecraft client, Category category) {
        // Do not start any music while Yuki Onna suppression is active
        if (isYukiSuppressed()) {
            try { sengokuFabric.LOGGER.info("[Music][DEBUG] Suppressed by Yuki Onna; not playing category {}", category); } catch (Throwable ignored) {}
            return;
        }

        if (category == Category.NONE) {
            try { sengokuFabric.LOGGER.debug("[Music][DEBUG] playMusicForCategory called with NONE; skipping"); } catch (Throwable ignored) {}
            return;
        }
        
        SoundEvent soundEvent = null;
        switch (category) {
            case NONE:
                return;
            case DAY:
                soundEvent = SoundRegistry.MUSIC_DAY;
                break;
            case NIGHT:
                soundEvent = SoundRegistry.MUSIC_NIGHT;
                break;
            case UNDERGROUND:
                soundEvent = SoundRegistry.MUSIC_UNDERGROUND;
                break;
            case DEEP_UNDERGROUND:
                soundEvent = SoundRegistry.MUSIC_DEEP_UNDERGROUND;
                break;
            case DEEP_DARK:
                soundEvent = SoundRegistry.MUSIC_DEEP_DARK;
                break;
            case YOMI:
                soundEvent = SoundRegistry.MUSIC_YOMI;
                break;
            case COMBAT_RYUGU:
                soundEvent = SoundRegistry.MUSIC_RYUGU_COMBAT;
                break;
            case RYUGU:
                soundEvent = SoundRegistry.MUSIC_RYUGU;
                break;
            
            case RUINS:
                soundEvent = SoundRegistry.MUSIC_RUINS;
                break;
            case VILLAGE:
                soundEvent = SoundRegistry.MUSIC_VILLAGE;
                break;
            case CASTLE:
                soundEvent = SoundRegistry.MUSIC_CASTLE;
                break;
            case CASTLE_COMBAT:
                soundEvent = SoundRegistry.MUSIC_CASTLE_COMBAT;
                break;
            case COMBAT_BASIC:
                soundEvent = SoundRegistry.MUSIC_BASIC_COMBAT;
                break;
            case COMBAT_YOMI:
                // Use dedicated Yomi combat track
                soundEvent = SoundRegistry.MUSIC_YOMI_COMBAT;
                break;
            
            case RONIN:
                soundEvent = SoundRegistry.MUSIC_RONIN;
                break;
            case WARLORD_PHASE_1:
                soundEvent = SoundRegistry.MUSIC_WARLORD_PHASE_1;
                break;
            case WARLORD_PHASE_2:
                soundEvent = SoundRegistry.MUSIC_WARLORD_PHASE_2;
                break;
            case DRAGON_PHASE_1:
                soundEvent = SoundRegistry.MUSIC_DRAGON_PHASE_1;
                break;
            case DRAGON_PHASE_2:
                soundEvent = SoundRegistry.MUSIC_DRAGON_PHASE_2;
                break;
            case TATARIGAMI:
                soundEvent = SoundRegistry.MUSIC_TATARIGAMI;
                break;
            case RAID:
                soundEvent = SoundRegistry.MUSIC_RAID;
                break;
            case SHINOBI:
                soundEvent = SoundRegistry.MUSIC_SHINOBI;
                break;
            case SHINOBI_LORD_PHASE_1:
                soundEvent = SoundRegistry.MUSIC_SHINOBI_LORD_PHASE_1;
                break;
            case SHINOBI_LORD_PHASE_2:
                soundEvent = SoundRegistry.MUSIC_SHINOBI_LORD_PHASE_2;
                break;
        }
        
        if (soundEvent == null) return;
        
        try {
            // If another track is still playing, stop it to avoid overlap
            if (currentMusicInstance != null && currentMusicInstance.isPlaying()) {
                try { client.getSoundManager().stop(currentMusicInstance); } catch (Throwable ignored) {}
            }
            // Create fading music instance starting at full or fade-in volume
            float startVolume = isFadingIn ? 0.01f : 1.0f;
            currentMusicInstance = FadingMusicInstance.forMusic(soundEvent, startVolume);
            
            try { sengokuFabric.LOGGER.info("[Music] Playing category {} via {} (start vol: {})", category, soundEvent.getLocation(), startVolume); } catch (Throwable ignored) {}
            // Log current options volumes to help diagnose silent playback (Music & Master sliders)
            try {
                float musicVol = 0.0f;
                float masterVol = 0.0f;
                try {
                    musicVol = client.options.getSoundSourceVolume(net.minecraft.sounds.SoundSource.MUSIC);
                } catch (Throwable ignored) {}
                try {
                    masterVol = client.options.getSoundSourceVolume(net.minecraft.sounds.SoundSource.MASTER);
                } catch (Throwable ignored) {}
                try { sengokuFabric.LOGGER.info("[Music][DEBUG] Option volumes - MUSIC={}, MASTER={}", musicVol, masterVol); } catch (Throwable ignored) {}
            } catch (Throwable ignored) {}
            client.getSoundManager().play(currentMusicInstance);
            try { sengokuFabric.LOGGER.info("[Music] play(...) called for {} ; soundManager.isActive={}", soundEvent.getLocation(), client.getSoundManager().isActive(currentMusicInstance)); } catch (Throwable ignored) {}
            justQueuedTicks = 20; // 1s grace to allow SoundManager to mark active
            // Start replay timer (custom per-category intervals)
            switch (category) {
                case RAID:
                    replayTicksRemaining = RAID_REPLAY_INTERVAL; break;
                case SHINOBI_LORD_PHASE_1:
                    replayTicksRemaining = SHINOBI_LORD_PHASE_1_REPLAY_INTERVAL; break;
                case SHINOBI_LORD_PHASE_2:
                    replayTicksRemaining = SHINOBI_LORD_PHASE_2_REPLAY_INTERVAL; break;
                case WARLORD_PHASE_1:
                    replayTicksRemaining = WARLORD_PHASE_1_REPLAY_INTERVAL; break;
                case WARLORD_PHASE_2:
                    replayTicksRemaining = WARLORD_PHASE_2_REPLAY_INTERVAL; break;
                case DRAGON_PHASE_1:
                    replayTicksRemaining = DRAGON_PHASE_1_REPLAY_INTERVAL; break;
                case DRAGON_PHASE_2:
                    replayTicksRemaining = DRAGON_PHASE_2_REPLAY_INTERVAL; break;
                case TATARIGAMI:
                    replayTicksRemaining = TATARIGAMI_REPLAY_INTERVAL; break;
                case RYUGU:
                    replayTicksRemaining = RYUGU_REPLAY_INTERVAL; break;
                case COMBAT_RYUGU:
                    replayTicksRemaining = COMBAT_RYUGU_REPLAY_INTERVAL; break;
                case DAY:
                    replayTicksRemaining = DAY_REPLAY_INTERVAL; break;
                case NIGHT:
                    replayTicksRemaining = NIGHT_REPLAY_INTERVAL; break;
                case UNDERGROUND:
                    replayTicksRemaining = UNDERGROUND_REPLAY_INTERVAL; break;
                case DEEP_UNDERGROUND:
                    replayTicksRemaining = DEEP_UNDERGROUND_REPLAY_INTERVAL; break;
                case DEEP_DARK:
                    replayTicksRemaining = DEEP_DARK_REPLAY_INTERVAL; break;
                case YOMI:
                    replayTicksRemaining = YOMI_REPLAY_INTERVAL; break;
                case COMBAT_YOMI:
                    replayTicksRemaining = COMBAT_YOMI_REPLAY_INTERVAL; break;
                case RUINS:
                    replayTicksRemaining = RUINS_REPLAY_INTERVAL; break;
                case VILLAGE:
                    replayTicksRemaining = VILLAGE_REPLAY_INTERVAL; break;
                case CASTLE:
                    replayTicksRemaining = CASTLE_REPLAY_INTERVAL; break;
                case CASTLE_COMBAT:
                    replayTicksRemaining = CASTLE_COMBAT_REPLAY_INTERVAL; break;
                case COMBAT_BASIC:
                    replayTicksRemaining = COMBAT_BASIC_REPLAY_INTERVAL; break;
                case RONIN:
                    replayTicksRemaining = RONIN_REPLAY_INTERVAL; break;
                case SHINOBI:
                    replayTicksRemaining = SHINOBI_REPLAY_INTERVAL; break;
                default:
                    replayTicksRemaining = REPLAY_INTERVAL; break;
            }
            try { sengokuFabric.LOGGER.info("[Music] Started replay timer ({} ticks) for category {}", replayTicksRemaining, category); } catch (Throwable ignored) {}
        } catch (Throwable t) {
            // Swallow errors - music is non-critical
            try { sengokuFabric.LOGGER.error("[Music][ERROR] Failed to play music for category {}", category, t); } catch (Throwable ignored) {}
        }
    }

    private static void stopCurrentMusic(Minecraft client) {
        if (currentMusicInstance != null) {
            try { client.getSoundManager().stop(currentMusicInstance); } catch (Throwable ignored) {}
            currentMusicInstance = null;
        }
    }

    // removed stopRyuguMusic helper (relying on entity check instead)

    // removed reflective boss overlay check

    // removed isEnderDragonAlive helper; detectCategory uses direct entity query instead
}
