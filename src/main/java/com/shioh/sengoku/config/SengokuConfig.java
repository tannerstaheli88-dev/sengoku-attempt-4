package com.shioh.sengoku.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Configuration file for Sengoku mod.
 * Manages spawn rates for bandit patrols, clan patrols, and Yuki Onna.
 */
public class SengokuConfig {
    private static final Path CONFIG_PATH = Paths.get("config", "sengoku.properties");
    private static final Path CLIENT_CONFIG_PATH = Paths.get("config", "sengoku-client.properties");
    private static SengokuConfig INSTANCE = null;

    // Bandit Patrol Settings
    public int banditPatrolCheckInterval = 1000;  // Base check interval in ticks (default: 50 seconds)
    public boolean banditPatrolsEnabled = true;  // Enable/disable bandit patrol spawning
    
    // Clan Patrol Settings
    public int clanPatrolCheckInterval = 7200;    // Base check interval in ticks (default: 6 minutes)
    public boolean clanPatrolsEnabled = true;    // Enable/disable clan patrol spawning
    public double clanPatrolSpawnChance = 0.50;  // Chance to spawn when checked (default: 50%)
    
    // Yuki Onna Settings
    public int yukiOnnaCheckInterval = 7200;      // Check interval in ticks (default: 1 minute)
    public boolean yukiOnnaEnabled = true;       // Enable/disable Yuki Onna spawning
    public double yukiOnnaSpawnChance = 1.0;    // Chance to spawn when checked (default: 20%)

    // Night Zombie Patrol Settings
    public int nightZombiePatrolCheckInterval = 600; // Base check interval in ticks (default: 30 seconds)
    public double nightZombiePatrolSpawnChance = 0.45; // Chance to spawn when checked (default: 45%)

    // Kuchisaka Onna Patrol Settings
    public int kuchisakaOnnaCheckInterval = 1200; // Base check interval in ticks (default: 60 seconds)
    public double kuchisakaOnnaSpawnChance = 0.35; // Chance to spawn when checked (default: 35%)

    // Shinobi Patrol Settings
    public int shinobiPatrolCheckInterval = 1200; // Base check interval in ticks (default: 60 seconds)
    public boolean shinobiPatrolsEnabled = true; // Enable/disable shinobi patrol spawning
    public double shinobiPatrolSpawnChance = 0.35; // Chance to spawn when checked (default: 35%)

    // Client visual settings
    public boolean concealmentVignetteEnabled = true; // Enable vignette when hiding in grass
    public boolean fogParticlesEnabled = true; // Enable ground fog particles after rain/storm (client-side)
    public boolean mistFogParticlesEnabled = true; // Enable volumetric FOG_MIST particles during mist weather
    public int mistFogMaxRange = 38; // Max radius (blocks) for FOG_MIST spawning during mist weather
    // Warm water system (onsen) - enables particles, server checks, and ice melting
    public boolean warmWaterEnabled = true; // Enable/disable all warm water features
    public int warmWaterParticleRadius = 32; // How far (blocks) warm water fog/flat-fog particles are visible
    
    // Monsters behavior toggles
    public boolean monstersAvoidShideEnabled = true; // Enable mobs avoiding Shide blocks
    // Individual flee toggles
    public boolean llamasFleeEnabled = true;
    public boolean parrotsFleeEnabled = true;
    public boolean crowsFleeEnabled = true;
    public boolean cranesFleeEnabled = true;
    
    // Pig (wild boar) behavior. we added pig physics
    public boolean pigBoarBehaviorEnabled = true; // Enable boar-like retaliation/ally defense for pigs
    public double pigCallForHelpRadius = 16.0;    // Radius in blocks for nearby pigs to be alerted
    public int pigRetaliationDurationTicks = 200; // How long (ticks) pigs will keep retaliating


    /**
     * Get the singleton config instance.
     * Loads from disk if not already loaded.
     */
    public static SengokuConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    /**
     * Load config from disk, or create default if it doesn't exist.
     */
    private static SengokuConfig load() {
        SengokuConfig config = new SengokuConfig();
        
        if (Files.exists(CONFIG_PATH)) {
            try {
                Properties props = new Properties();
                props.load(Files.newInputStream(CONFIG_PATH));
                
                config.banditPatrolCheckInterval = Integer.parseInt(
                    props.getProperty("banditPatrolCheckInterval", "1000"));
                config.banditPatrolsEnabled = Boolean.parseBoolean(
                    props.getProperty("banditPatrolsEnabled", "true"));
                    
                config.clanPatrolCheckInterval = Integer.parseInt(
                    props.getProperty("clanPatrolCheckInterval", "7200"));
                config.clanPatrolsEnabled = Boolean.parseBoolean(
                    props.getProperty("clanPatrolsEnabled", "true"));
                config.clanPatrolSpawnChance = Double.parseDouble(
                    props.getProperty("clanPatrolSpawnChance", "0.50"));
                    
                config.yukiOnnaCheckInterval = Integer.parseInt(
                    props.getProperty("yukiOnnaCheckInterval", "200"));
                config.yukiOnnaEnabled = Boolean.parseBoolean(
                    props.getProperty("yukiOnnaEnabled", "true"));
                config.yukiOnnaSpawnChance = Double.parseDouble(
                    props.getProperty("yukiOnnaSpawnChance", "0.8"));
                config.clanPatrolsEnabled = Boolean.parseBoolean(
                    props.getProperty("clanPatrolsEnabled", "true"));
                config.nightZombiePatrolCheckInterval = Integer.parseInt(
                    props.getProperty("nightZombiePatrolCheckInterval", "600"));
                config.nightZombiePatrolSpawnChance = Double.parseDouble(
                    props.getProperty("nightZombiePatrolSpawnChance", "0.45"));
                config.kuchisakaOnnaCheckInterval = Integer.parseInt(
                    props.getProperty("kuchisakaOnnaCheckInterval", "1200"));
                config.kuchisakaOnnaSpawnChance = Double.parseDouble(
                    props.getProperty("kuchisakaOnnaSpawnChance", "0.35"));
                config.shinobiPatrolCheckInterval = Integer.parseInt(
                    props.getProperty("shinobiPatrolCheckInterval", "1200"));
                config.shinobiPatrolsEnabled = Boolean.parseBoolean(
                    props.getProperty("shinobiPatrolsEnabled", "true"));
                config.shinobiPatrolSpawnChance = Double.parseDouble(
                    props.getProperty("shinobiPatrolSpawnChance", "0.35"));
                config.concealmentVignetteEnabled = Boolean.parseBoolean(
                    props.getProperty("concealmentVignetteEnabled", "true"));
                config.fogParticlesEnabled = Boolean.parseBoolean(
                    props.getProperty("fogParticlesEnabled", "true"));
                config.mistFogParticlesEnabled = Boolean.parseBoolean(
                    props.getProperty("mistFogParticlesEnabled", "true"));
                config.mistFogMaxRange = Integer.parseInt(
                    props.getProperty("mistFogMaxRange", "38"));
                config.warmWaterEnabled = Boolean.parseBoolean(
                    props.getProperty("warmWaterEnabled", "true"));
                config.warmWaterParticleRadius = Integer.parseInt(
                    props.getProperty("warmWaterParticleRadius", "32"));
                config.monstersAvoidShideEnabled = Boolean.parseBoolean(
                    props.getProperty("monstersAvoidShideEnabled", "true"));
                config.llamasFleeEnabled = Boolean.parseBoolean(
                    props.getProperty("llamasFleeEnabled", "true"));
                config.parrotsFleeEnabled = Boolean.parseBoolean(
                    props.getProperty("parrotsFleeEnabled", "true"));
                config.crowsFleeEnabled = Boolean.parseBoolean(
                    props.getProperty("crowsFleeEnabled", "true"));
                config.cranesFleeEnabled = Boolean.parseBoolean(
                    props.getProperty("cranesFleeEnabled", "true"));
                config.pigBoarBehaviorEnabled = Boolean.parseBoolean(
                    props.getProperty("pigBoarBehaviorEnabled", "true"));
                config.pigCallForHelpRadius = Double.parseDouble(
                    props.getProperty("pigCallForHelpRadius", "16.0"));
                config.pigRetaliationDurationTicks = Integer.parseInt(
                    props.getProperty("pigRetaliationDurationTicks", "200"));
                    
            } catch (IOException | NumberFormatException e) {
                System.err.println("Failed to load Sengoku config, using defaults: " + e.getMessage());
            }
        }
        
        // Load client-side overrides if present
        if (Files.exists(CLIENT_CONFIG_PATH)) {
            try {
                Properties cprops = new Properties();
                cprops.load(Files.newInputStream(CLIENT_CONFIG_PATH));
                config.concealmentVignetteEnabled = Boolean.parseBoolean(cprops.getProperty("concealmentVignetteEnabled", String.valueOf(config.concealmentVignetteEnabled)));
                config.fogParticlesEnabled = Boolean.parseBoolean(cprops.getProperty("fogParticlesEnabled", String.valueOf(config.fogParticlesEnabled)));
                config.mistFogParticlesEnabled = Boolean.parseBoolean(cprops.getProperty("mistFogParticlesEnabled", String.valueOf(config.mistFogParticlesEnabled)));
                config.mistFogMaxRange = Integer.parseInt(cprops.getProperty("mistFogMaxRange", String.valueOf(config.mistFogMaxRange)));
                config.warmWaterEnabled = Boolean.parseBoolean(cprops.getProperty("warmWaterEnabled", String.valueOf(config.warmWaterEnabled)));
                config.warmWaterParticleRadius = Integer.parseInt(cprops.getProperty("warmWaterParticleRadius", String.valueOf(config.warmWaterParticleRadius)));
            } catch (IOException | NumberFormatException e) {
                System.err.println("Failed to load Sengoku client config, using defaults: " + e.getMessage());
            }
        }

        // Save to ensure file exists with current values
        config.save();
        return config;
    }

    /**
     * Save current config to disk.
     */
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            
            Properties props = new Properties();
            props.setProperty("banditPatrolCheckInterval", String.valueOf(banditPatrolCheckInterval));
            props.setProperty("banditPatrolsEnabled", String.valueOf(banditPatrolsEnabled));
            props.setProperty("clanPatrolCheckInterval", String.valueOf(clanPatrolCheckInterval));
            props.setProperty("clanPatrolsEnabled", String.valueOf(clanPatrolsEnabled));
            props.setProperty("clanPatrolSpawnChance", String.valueOf(clanPatrolSpawnChance));
            props.setProperty("yukiOnnaCheckInterval", String.valueOf(yukiOnnaCheckInterval));
            props.setProperty("yukiOnnaEnabled", String.valueOf(yukiOnnaEnabled));
            props.setProperty("yukiOnnaSpawnChance", String.valueOf(yukiOnnaSpawnChance));
            props.setProperty("nightZombiePatrolCheckInterval", String.valueOf(nightZombiePatrolCheckInterval));
            props.setProperty("nightZombiePatrolSpawnChance", String.valueOf(nightZombiePatrolSpawnChance));
            props.setProperty("kuchisakaOnnaCheckInterval", String.valueOf(kuchisakaOnnaCheckInterval));
            props.setProperty("kuchisakaOnnaSpawnChance", String.valueOf(kuchisakaOnnaSpawnChance));
            props.setProperty("shinobiPatrolCheckInterval", String.valueOf(shinobiPatrolCheckInterval));
            props.setProperty("shinobiPatrolsEnabled", String.valueOf(shinobiPatrolsEnabled));
            props.setProperty("shinobiPatrolSpawnChance", String.valueOf(shinobiPatrolSpawnChance));
            props.setProperty("clanPatrolsEnabled", String.valueOf(clanPatrolsEnabled));
            props.setProperty("yukiOnnaEnabled", String.valueOf(yukiOnnaEnabled));
            props.setProperty("concealmentVignetteEnabled", String.valueOf(concealmentVignetteEnabled));
            props.setProperty("fogParticlesEnabled", String.valueOf(fogParticlesEnabled));
            props.setProperty("mistFogParticlesEnabled", String.valueOf(mistFogParticlesEnabled));
            props.setProperty("mistFogMaxRange", String.valueOf(mistFogMaxRange));
            props.setProperty("warmWaterEnabled", String.valueOf(warmWaterEnabled));
            props.setProperty("warmWaterParticleRadius", String.valueOf(warmWaterParticleRadius));
            props.setProperty("monstersAvoidShideEnabled", String.valueOf(monstersAvoidShideEnabled));
            props.setProperty("llamasFleeEnabled", String.valueOf(llamasFleeEnabled));
            props.setProperty("parrotsFleeEnabled", String.valueOf(parrotsFleeEnabled));
            props.setProperty("crowsFleeEnabled", String.valueOf(crowsFleeEnabled));
            props.setProperty("cranesFleeEnabled", String.valueOf(cranesFleeEnabled));
            props.setProperty("pigBoarBehaviorEnabled", String.valueOf(pigBoarBehaviorEnabled));
            props.setProperty("pigCallForHelpRadius", String.valueOf(pigCallForHelpRadius));
            props.setProperty("pigRetaliationDurationTicks", String.valueOf(pigRetaliationDurationTicks));
            
            props.store(Files.newOutputStream(CONFIG_PATH), 
                "Sengoku Jidai Configuration\n" +
                "Spawn intervals are in ticks (20 ticks = 1 second)\n" +
                "Spawn chances are percentages from 0.0 to 1.0");
                
            // Also write a client-side file for visual/input overrides
            Properties cprops = new Properties();
            cprops.setProperty("concealmentVignetteEnabled", String.valueOf(concealmentVignetteEnabled));
            cprops.setProperty("fogParticlesEnabled", String.valueOf(fogParticlesEnabled));
            cprops.setProperty("mistFogParticlesEnabled", String.valueOf(mistFogParticlesEnabled));
            cprops.setProperty("mistFogMaxRange", String.valueOf(mistFogMaxRange));
            cprops.setProperty("warmWaterEnabled", String.valueOf(warmWaterEnabled));
            cprops.setProperty("warmWaterParticleRadius", String.valueOf(warmWaterParticleRadius));
            Files.createDirectories(CLIENT_CONFIG_PATH.getParent());
            cprops.store(Files.newOutputStream(CLIENT_CONFIG_PATH), 
                "Sengoku Jidai Client Configuration\n" +
                "Client-side visual and input timing settings\n");
        } catch (IOException e) {
            System.err.println("Failed to save Sengoku config: " + e.getMessage());
        }
    }

    /**
     * Reload config from disk.
     */
    public static void reload() {
        INSTANCE = load();
    }
}
