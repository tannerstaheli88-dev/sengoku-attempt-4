package com.shioh.sengoku.registry;

import com.shioh.sengoku.Constants;
import com.shioh.sengoku.registry.helper.Reggie;
import com.shioh.sengoku.registry.helper.Reginald;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundRegistry {
    private static final Reginald REGISTRARS = new Reginald();
    private static final Reggie<SoundEvent> SOUND_REGISTRAR = REGISTRARS.get(Registries.SOUND_EVENT);

    public static final SoundEvent POSTURE_BREAK = register("posture_break");
    public static final SoundEvent WEAPON_PARRY = register("weapon_parry");
    public static final SoundEvent PARTIAL_PARRY = register("partial_parry");
    public static final SoundEvent PERFECT_PARRY = register("perfect_parry");
    public static final SoundEvent KENSEI_FINALE = registerWithId("kensei_finale", ResourceLocation.withDefaultNamespace("entity.vindicator.kensei_finale"));
    public static final SoundEvent WARLORD_PHASE_TWO_HURT = register("warlord_phase_two_hurt");
    public static final SoundEvent WARLORD_PHASE_TWO_DEATH = register("warlord_phase_two_death");
    public static final SoundEvent WARLORD_PHASE_TWO_ATTACK = register("warlord_phase_two_attack");
    // Use mod-namespace registered sounds so custom assets from sounds.json are used
    public static final SoundEvent WARLORD_COMBAT_ENTER = register("warlord_combat_enter");
    public static final SoundEvent RONIN_ATTACK = register("ronin_attack");
    public static final SoundEvent GORYO_ATTACK = register("goryo_attack");
    public static final SoundEvent SHINOBI_LORD_ATTACK = register("shinobi_lord_attack");
    public static final SoundEvent SAMURAI_ATTACK = register("samurai_attack");
    public static final SoundEvent GIANT_FOOTSTEPS = register("giant_footsteps"); 
    public static final SoundEvent BULLET_HIT = register("bullet_hit");
    public static final SoundEvent MUSKET_FIRE = register("musket_fire");
    public static final SoundEvent SMOKE_BOMB = register("smoke_bomb");
    
        public static final SoundEvent MELEE_WEAPON_HIT = register("melee_weapon_hit");
        public static final SoundEvent BACKSTAB = register("backstab");
    
        // Ronin sounds
        public static final SoundEvent RONIN_AMBIENT = register("ronin_ambient");
        public static final SoundEvent RONIN_AMBIENT_AGGRO = register("ronin_ambient_aggro");
        // Bandit uses pillager ambient by default; alias for aggro
        public static final SoundEvent BANDIT_AMBIENT_AGGRO = register("bandit_ambient_aggro");
        public static final SoundEvent RONIN_HURT = register("ronin_hurt");
        public static final SoundEvent RONIN_DEATH = register("ronin_death");
        public static final SoundEvent RONIN_CELEBRATE = register("ronin_celebrate");
    
    // Goryo sounds
    public static final SoundEvent GORYO_AMBIENT = register("goryo_ambient");
    public static final SoundEvent GORYO_AMBIENT_AGGRO = register("goryo_ambient_aggro");
    public static final SoundEvent GORYO_HURT = register("goryo_hurt");
    public static final SoundEvent GORYO_DEATH = register("goryo_death");
    public static final SoundEvent GORYO_CELEBRATE = register("goryo_celebrate");

    // Ashigaru sounds
    public static final SoundEvent ASHIGARU_AMBIENT = register("ashigaru_ambient");
    public static final SoundEvent ASHIGARU_AMBIENT_AGGRO = register("ashigaru_ambient_aggro");
    public static final SoundEvent ASHIGARU_HURT = register("ashigaru_hurt");
    public static final SoundEvent ASHIGARU_DEATH = register("ashigaru_death");

    // Omukade sounds
    public static final SoundEvent OMUKADE_AMBIENT = register("omukade_ambient");
    public static final SoundEvent OMUKADE_HURT = register("omukade_hurt");
    public static final SoundEvent OMUKADE_DEATH = register("omukade_death");

    public static final SoundEvent UMI_INU_AMBIENT = register("umi_inu_ambient");
    public static final SoundEvent UMI_INU_HURT = register("umi_inu_hurt");
    public static final SoundEvent UMI_INU_DEATH = register("umi_inu_death");

    public static final SoundEvent IKUCHI_AMBIENT = register("ikuchi_ambient");
    public static final SoundEvent IKUCHI_HURT = register("ikuchi_hurt");
    public static final SoundEvent IKUCHI_DEATH = register("ikuchi_death");

    // Shiryo sounds
    public static final SoundEvent SHIRYO_AMBIENT = register("shiryo_ambient");
    public static final SoundEvent SHIRYO_HURT = register("shiryo_hurt");
    public static final SoundEvent SHIRYO_DEATH = register("shiryo_death");

    // Umi Nyobo sounds
    public static final SoundEvent UMI_NYOBO_AMBIENT = register("umi_nyobo_ambient");
    public static final SoundEvent UMI_NYOBO_AMBIENT_AGGRO = register("umi_nyobo_ambient_aggro");
    public static final SoundEvent UMI_NYOBO_HURT = register("umi_nyobo_hurt");
    public static final SoundEvent UMI_NYOBO_DEATH = register("umi_nyobo_death");
    public static final SoundEvent UMI_NYOBO_CELEBRATE = register("umi_nyobo_celebrate");
    
    // Ningyo sounds
    public static final SoundEvent NINGYO_AMBIENT = register("ningyo_ambient");
    public static final SoundEvent NINGYO_HURT = register("ningyo_hurt");
    public static final SoundEvent NINGYO_DEATH = register("ningyo_death");
    public static final SoundEvent NINGYO_FLOP = register("ningyo_flop");
    
    // Maikubi sounds 
    public static final SoundEvent MAIKUBI_AMBIENT = register("maikubi_ambient");
    public static final SoundEvent MAIKUBI_HURT = register("maikubi_hurt");
    public static final SoundEvent MAIKUBI_DEATH = register("maikubi_death");

    // Oni Brute custom sounds
    public static final SoundEvent ONI_BRUTE_AMBIENT = register("oni_brute_ambient");
    public static final SoundEvent ONI_BRUTE_AMBIENT_AGGRO = register("oni_brute_ambient_aggro");
    public static final SoundEvent ONI_BRUTE_HURT = register("oni_brute_hurt");
    public static final SoundEvent ONI_BRUTE_DEATH = register("oni_brute_death");
    
    // Yuki Onna sounds
    public static final SoundEvent YUKI_ONNA_AMBIENT = register("yuki_onna_ambient");
    public static final SoundEvent YUKI_ONNA_AMBIENT_AGGRO = register("yuki_onna_ambient_aggro");
    public static final SoundEvent YUKI_ONNA_HURT = register("yuki_onna_hurt");
    public static final SoundEvent YUKI_ONNA_DEATH = register("yuki_onna_death");
    public static final SoundEvent YUKI_ONNA_STARE = register("yuki_onna_stare");
    public static final SoundEvent YUKI_ONNA_BREATH = register("yuki_onna_breath");
    public static final SoundEvent YUKI_ONNA_MUSIC = register("yuki_onna_music");
    
    // Sarugami sounds
    public static final SoundEvent SARUGAMI_AMBIENT = register("sarugami_ambient");
    public static final SoundEvent SARUGAMI_HURT = register("sarugami_hurt");
    public static final SoundEvent SARUGAMI_DEATH = register("sarugami_death");

    // Red-Crowned Crane sounds 
    public static final SoundEvent RED_CROWNED_CRANE_AMBIENT = register("red_crowned_crane_ambient");
    public static final SoundEvent RED_CROWNED_CRANE_HURT = register("red_crowned_crane_hurt");
    public static final SoundEvent RED_CROWNED_CRANE_DEATH = register("red_crowned_crane_death");
    
    // Hitotsume Nyudo sounds
    public static final SoundEvent HITOTSUME_NYUDO_AMBIENT = register("hitotsume_nyudo_ambient");
    public static final SoundEvent HITOTSUME_NYUDO_HURT = register("hitotsume_nyudo_hurt");
    public static final SoundEvent HITOTSUME_NYUDO_DEATH = register("hitotsume_nyudo_death");

    // Macaque (monkey) sounds
    public static final SoundEvent MACAQUE_AMBIENT = register("macaque_ambient");
    public static final SoundEvent MACAQUE_HURT = register("macaque_hurt");
    public static final SoundEvent MACAQUE_DEATH = register("macaque_death");

    // Crow sounds
    public static final SoundEvent CROW_AMBIENT = register("crow_ambient");
    public static final SoundEvent CROW_HURT = register("crow_hurt");
    public static final SoundEvent CROW_DEATH = register("crow_death");

    // Music categories for ambient control (day/night/underground system)
    public static final SoundEvent MUSIC_DAY = register("music_day");
    public static final SoundEvent MUSIC_NIGHT = register("music_night");
    public static final SoundEvent MUSIC_UNDERGROUND = register("music_underground");
    public static final SoundEvent MUSIC_DEEP_UNDERGROUND = register("music_deep_underground");
    public static final SoundEvent MUSIC_DEEP_DARK = register("music_deep_dark");
    public static final SoundEvent MUSIC_VILLAGE = register("music_village");
    public static final SoundEvent MUSIC_CASTLE = register("music_castle");
    public static final SoundEvent MUSIC_CASTLE_COMBAT = register("music_castle_combat");
    public static final SoundEvent MUSIC_BASIC_COMBAT = register("music_basic_combat");
    public static final SoundEvent MUSIC_YOMI = register("music_yomi");
    public static final SoundEvent MUSIC_YOMI_COMBAT = register("music_yomi_combat");
    public static final SoundEvent MUSIC_RYUGU = register("music_ryugu");
    public static final SoundEvent MUSIC_RYUGU_COMBAT = register("music_ryugu_combat");
    public static final SoundEvent MUSIC_RAID = register("music_raid");
    public static final SoundEvent MUSIC_VICTORY = register("music_victory");
    public static final SoundEvent MUSIC_DRAGON_PHASE_1 = register("music_dragon_phase_1");
    public static final SoundEvent MUSIC_DRAGON_PHASE_2 = register("music_dragon_phase_2");
    public static final SoundEvent MUSIC_RUINS = register("music_ruins");
    public static final SoundEvent MUSIC_SHINOBI = register("music_shinobi");
    public static final SoundEvent MUSIC_SHINOBI_LORD_PHASE_1 = register("music_shinobi_lord_phase_1");
    public static final SoundEvent MUSIC_SHINOBI_LORD_PHASE_2 = register("music_shinobi_lord_phase_2");
    public static final SoundEvent MUSIC_RONIN = register("music_ronin");
    public static final SoundEvent MUSIC_WARLORD = register("music_warlord");
    public static final SoundEvent MUSIC_WARLORD_PHASE_1 = register("music_warlord_phase_1");
    public static final SoundEvent MUSIC_WARLORD_PHASE_2 = register("music_warlord_phase_2");
    public static final SoundEvent MUSIC_TATARIGAMI = register("music_tatarigami");
    public static final SoundEvent ENEMY_ALERT = register("enemy_alert");
    // Hybrid tracks are merged into night and underground; no separate SoundEvent.

    private static SoundEvent register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, name);
        return SOUND_REGISTRAR.register(name, () -> SoundEvent.createVariableRangeEvent(id)).get();
    }

    private static SoundEvent registerWithId(String name, ResourceLocation id) {
        return SOUND_REGISTRAR.register(name, () -> SoundEvent.createVariableRangeEvent(id)).get();
    }

    public static void init() {
        // force class load
    }
}
