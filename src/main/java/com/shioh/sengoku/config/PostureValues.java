package com.shioh.sengoku.config;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

/**
 * Central place to configure posture (poise) damage for all weapons.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * HOW TO USE
 * ─────────────────────────────────────────────────────────────────────────────
 * Just change the numbers here — every weapon that references these constants
 * will automatically pick up the new values on the next build.
 *
 * Sections:
 *   WEAPON TYPE DEFAULTS  — iron-tier base values; lower tiers scale down,
 *                           higher tiers scale up automatically.
 *   TIER MULTIPLIERS      — how much each tier multiplies the base. Edit here
 *                           to change the gap between tiers globally.
 *   KENSEI (SPECIAL)      — diamond / netherite overrides for unique boss weapons
 *   VANILLA WEAPONS       — computed values shown on vanilla swords/axes/trident/mace
 * ─────────────────────────────────────────────────────────────────────────────
 */
public final class PostureValues {

    private PostureValues() {}

    // =========================================================================
    // WEAPON TYPE DEFAULTS  (iron-tier baseline)
    // Lower tiers automatically scale down; higher tiers scale up.
    // Use getScaledPosture() in registry code — don't hard-code tier values.
    // =========================================================================

    /** Tanto — fast, light dagger. */
    public static final int TANTO     = 15;

    /** Tetsubo — heavy club. */
    public static final int TETSUBO   = 40;

    /** Kanabo — massive iron club. */
    public static final int KANABO    = 45;

    /** Naginata — two-handed polearm. */
    public static final int NAGINATA  = 35;

    /** Yari — spear, moderate posture. */
    public static final int YARI      = 30;

    /** Odachi — great sword, highest modded posture. */
    public static final int ODACHI    = 40;

    // =========================================================================
    // TIER MULTIPLIERS
    // Applied on top of the iron-tier base value above.
    // Example: iron naginata = 35, diamond naginata = round(35 * 1.25) = 44
    // =========================================================================

    /** Wood and Gold tiers — lighter / weaker materials. */
    public static final double TIER_WOOD_GOLD  = 0.50;

    /** Stone tier. */
    public static final double TIER_STONE      = 0.75;

    /** Iron tier — the baseline (1.0 = no scaling). */
    public static final double TIER_IRON       = 1.00;

    /** Diamond tier. */
    public static final double TIER_DIAMOND    = 1.25;

    /** Netherite tier. */
    public static final double TIER_NETHERITE  = 1.50;

    /**
     * Returns posture damage for a weapon type at a given tier.
     * Uses the multipliers above. Unknown/modpack tiers fall back to the iron base.
     *
     * @param ironBase  the iron-tier posture constant from this class
     * @param tier      the material tier of the item being registered
     */
    public static int getScaledPosture(int ironBase, Tier tier) {
        double multiplier;
        if (tier == Tiers.WOOD || tier == Tiers.GOLD)  multiplier = TIER_WOOD_GOLD;
        else if (tier == Tiers.STONE)                   multiplier = TIER_STONE;
        else if (tier == Tiers.DIAMOND)                 multiplier = TIER_DIAMOND;
        else if (tier == Tiers.NETHERITE)               multiplier = TIER_NETHERITE;
        else                                            multiplier = TIER_IRON; // bronze / custom
        return Math.max(1, (int) Math.round(ironBase * multiplier));
    }

    // =========================================================================
    // KENSEI (SPECIAL) WEAPONS  —  diamond / netherite boss drops
    // =========================================================================

    public static final int KENSEI_SWORD_DIAMOND     = 35;
    public static final int KENSEI_SWORD_NETHERITE   = 40;

    public static final int KENSEI_AXE_DIAMOND       = 50;
    public static final int KENSEI_AXE_NETHERITE     = 55;

    public static final int KENSEI_TANTO_DIAMOND     = 20;
    public static final int KENSEI_TANTO_NETHERITE   = 25;

    public static final int KENSEI_TETSUBO_DIAMOND   = 50;
    public static final int KENSEI_TETSUBO_NETHERITE = 55;

    public static final int KENSEI_KANABO_DIAMOND    = 50;
    public static final int KENSEI_KANABO_NETHERITE  = 55;

    public static final int KENSEI_NAGINATA_DIAMOND  = 40;
    public static final int KENSEI_NAGINATA_NETHERITE= 45;

    public static final int KENSEI_YARI_DIAMOND      = 35;
    public static final int KENSEI_YARI_NETHERITE    = 40;

    public static final int KENSEI_ODACHI_DIAMOND    = 50;
    public static final int KENSEI_ODACHI_NETHERITE  = 52;

    // =========================================================================
    // VANILLA WEAPONS  —  shown on vanilla items via computed tooltip
    // ─────────────────────────────────────────────────────────────────────────
    // Tier base values (swords use these directly; axes add AXE_BONUS on top).
    // =========================================================================

    public static final int VANILLA_WOOD_GOLD_BASE   = 12;
    public static final int VANILLA_STONE_BASE       = 18;
    public static final int VANILLA_IRON_BASE        = 24;
    public static final int VANILLA_DIAMOND_BASE     = 30;
    public static final int VANILLA_NETHERITE_BASE   = 36;

    /** Extra posture axes gain on top of their tier base. */
    public static final int VANILLA_AXE_BONUS        = 6;

    /** Trident (flat value, not tier-based). */
    public static final int VANILLA_TRIDENT          = 34;

    /** Mace (flat value, not tier-based). */
    public static final int VANILLA_MACE             = 48;
}
