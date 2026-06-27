package com.shioh.sengoku.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.shioh.sengoku.config.SengokuConfig;

/**
 * Mod Menu integration for Sengoku config screen.
 * This class must be in the client source set because it references
 * client GUI classes (Screen) and Cloth Config.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> createConfigScreen(parent);
    }

    private static Screen createConfigScreen(Screen parent) {
        SengokuConfig config = SengokuConfig.getInstance();
        
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Sengoku Jidai Configuration"))
            .setSavingRunnable(config::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Bandit Patrol Category
        ConfigCategory banditCategory = builder.getOrCreateCategory(
            Component.literal("Bandit Patrols"));
        
        // Check interval is fixed; spawning enabled toggle available below.
        
        banditCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Allow bandit patrols"),
                config.banditPatrolsEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable or disable bandit patrol spawning entirely. Default: enabled"))
            .setSaveConsumer(value -> config.banditPatrolsEnabled = value)
            .build());

        // Clan Patrol Category 
        ConfigCategory clanCategory = builder.getOrCreateCategory(
            Component.literal("Clan Patrols"));
        
        // Check interval is fixed; spawning enabled toggle available below.
        
        clanCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Allow clan patrols"),
                config.clanPatrolsEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable or disable clan patrol spawning entirely. Default: enabled"))
            .setSaveConsumer(value -> config.clanPatrolsEnabled = value)
            .build());

        // Yuki Onna Category
        ConfigCategory yukiOnnaCategory = builder.getOrCreateCategory(
            Component.literal("Yuki Onna"));
        
        // Check interval is fixed; spawning enabled toggle available below.
        
        yukiOnnaCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Allow Yuki Onna"),
                config.yukiOnnaEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable or disable Yuki Onna spawning entirely. Default: enabled"))
            .setSaveConsumer(value -> config.yukiOnnaEnabled = value)
            .build());

        // Misc category: aggregated toggles and numeric options
        ConfigCategory miscCategory = builder.getOrCreateCategory(
            Component.literal("Misc"));

        miscCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Allow night oni patrols"),
                config.nightZombiePatrolCheckInterval > 0)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable or disable night oni patrol spawning. Default: enabled"))
            .setSaveConsumer(value -> config.nightZombiePatrolCheckInterval = value ? Math.max(1, config.nightZombiePatrolCheckInterval) : 0)
            .build());

        miscCategory.addEntry(entryBuilder.startDoubleField(
                Component.literal("Night oni spawn chance"),
                config.nightZombiePatrolSpawnChance)
            .setDefaultValue(0.45)
            .setMin(0.0)
            .setMax(1.0)
            .setTooltip(Component.literal("Chance to spawn when checked (0.0 - 1.0). Default: 0.45"))
            .setSaveConsumer(value -> config.nightZombiePatrolSpawnChance = value)
            .build());

        miscCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Allow Kuchisaka Onna"),
                config.kuchisakaOnnaCheckInterval > 0)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable or disable Kuchisaka Onna spawning. Default: enabled"))
            .setSaveConsumer(value -> config.kuchisakaOnnaCheckInterval = value ? Math.max(1, config.kuchisakaOnnaCheckInterval) : 0)
            .build());

        miscCategory.addEntry(entryBuilder.startDoubleField(
                Component.literal("Kuchisaka spawn chance"),
                config.kuchisakaOnnaSpawnChance)
            .setDefaultValue(0.35)
            .setMin(0.0)
            .setMax(1.0)
            .setTooltip(Component.literal("Chance to spawn when checked (0.0 - 1.0). Default: 0.35"))
            .setSaveConsumer(value -> config.kuchisakaOnnaSpawnChance = value)
            .build());

        miscCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Enable boar neutral behavior"),
                config.pigBoarBehaviorEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable or disable boar behavior (calls for help/retaliation). Default: enabled"))
            .setSaveConsumer(value -> config.pigBoarBehaviorEnabled = value)
            .build());

        miscCategory.addEntry(entryBuilder.startDoubleField(
                Component.literal("Boar call-for-help radius"),
                config.pigCallForHelpRadius)
            .setDefaultValue(16.0)
            .setMin(0.0)
            .setMax(64.0)
            .setTooltip(Component.literal("Radius in blocks for nearby boar to be alerted. Default: 16.0"))
            .setSaveConsumer(value -> config.pigCallForHelpRadius = value)
            .build());

        miscCategory.addEntry(entryBuilder.startIntField(
                Component.literal("Boar retaliation duration (ticks)"),
                config.pigRetaliationDurationTicks)
            .setDefaultValue(200)
            .setMin(0)
            .setMax(2000)
            .setTooltip(Component.literal("How long (ticks) boar will keep retaliating. Default: 200"))
            .setSaveConsumer(value -> config.pigRetaliationDurationTicks = value)
            .build());

        miscCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Yokai avoid Shide blocks"),
                config.monstersAvoidShideEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable or disable mobs avoiding Shide (Shinto paper) blocks incase this causes stutters. Default: enabled"))
            .setSaveConsumer(value -> config.monstersAvoidShideEnabled = value)
            .build());

        // Flee toggles for animals
        miscCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Deer flee from players"),
                config.llamasFleeEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable/disable deer fleeing from players. Default: enabled"))
            .setSaveConsumer(value -> config.llamasFleeEnabled = value)
            .build());

        miscCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Birds flee from players"),
                config.parrotsFleeEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable/disable birds fleeing from players. Default: enabled"))
            .setSaveConsumer(value -> config.parrotsFleeEnabled = value)
            .build());

        miscCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Crows flee from players"),
                config.crowsFleeEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable/disable crows fleeing from players. Default: enabled"))
            .setSaveConsumer(value -> config.crowsFleeEnabled = value)
            .build());

        miscCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Cranes flee from players"),
                config.cranesFleeEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable/disable cranes fleeing from players. Default: enabled"))
            .setSaveConsumer(value -> config.cranesFleeEnabled = value)
            .build());

        // Visuals / Client options
        ConfigCategory visualsCategory = builder.getOrCreateCategory(
            Component.literal("Visuals"));

        visualsCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Concealment vignette (hide in grass)"),
                config.concealmentVignetteEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable subtle vignette overlay when hiding in tall grass or certain flowers."))
            .setSaveConsumer(value -> config.concealmentVignetteEnabled = value)
            .build());

        visualsCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Weather ground fog particles (rain/storm)"),
                config.fogParticlesEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable or disable the client-side ground fog particles that appear after rain or storms."))
            .setSaveConsumer(value -> config.fogParticlesEnabled = value)
            .build());

        visualsCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Mist weather volumetric fog particles"),
                config.mistFogParticlesEnabled)
            .setDefaultValue(false)
            .setTooltip(Component.literal("Enable or disable larger fog particles during the mist weather event only. Very experiental, may cause performance issues on lower-end machines. Default: disabled"))
            .setSaveConsumer(value -> config.mistFogParticlesEnabled = value)
            .build());

        visualsCategory.addEntry(entryBuilder.startIntSlider(
                Component.literal("Mist weather fog max range (blocks)"),
                config.mistFogMaxRange, 8, 128)
            .setDefaultValue(38)
            .setTooltip(Component.literal("Maximum spawn radius for FOG_MIST during mist weather. Higher values spread fog farther out."))
            .setSaveConsumer(value -> config.mistFogMaxRange = value)
            .build());

        visualsCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Enable warm water (onsen) features"),
                config.warmWaterEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable or disable all warm water features (particles, server checks, ice melting). Default: enabled"))
            .setSaveConsumer(value -> config.warmWaterEnabled = value)
            .build());

        visualsCategory.addEntry(entryBuilder.startIntSlider(
                Component.literal("Warm water particle radius (blocks)"),
                config.warmWaterParticleRadius, 8, 128)
            .setDefaultValue(32)
            .setTooltip(Component.literal("How far from the spring fog and flat-fog particles are visible. Lower = fewer particles, better performance. Default: 32"))
            .setSaveConsumer(value -> {
                config.warmWaterParticleRadius = value;
                // Also invalidate the client-side cache so it rebuilds with the new radius immediately
                com.shioh.sengoku.sengokuClient.invalidateWarmWaterCache();
            })
            .build());

        return builder.build();
    }
}
