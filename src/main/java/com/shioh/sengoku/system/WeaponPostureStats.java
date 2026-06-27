package com.shioh.sengoku.system;

import com.shioh.sengoku.item.KanaboItem;
import com.shioh.sengoku.item.NaginataItem;
import com.shioh.sengoku.item.OdachiItem;
import com.shioh.sengoku.item.TantoItem;
import com.shioh.sengoku.item.TetsuboItem;
import com.shioh.sengoku.item.YariItem;
import com.shioh.sengoku.struct.WeaponType;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.Tiers;

public final class WeaponPostureStats {
    private static final int DEFAULT_POSTURE = 20;
    private static final int MIN_POSTURE = 6;
    private static final int MAX_POSTURE = 28;
    private static final float MOB_HEALTH_SCALE_BASE = 0.75F;
    private static final float MOB_HEALTH_SCALE_DIVISOR = 80.0F;
    private static final float MIN_MOB_HEALTH_MULTIPLIER = 0.85F;
    private static final float MAX_MOB_HEALTH_MULTIPLIER = 2.25F;

    private WeaponPostureStats() {
    }

    public static int getMaxPosture(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return DEFAULT_POSTURE;
        }

        float base = resolveTypeBase(stack);
        float materialMultiplier = resolveTierMultiplier(stack);
        int computed = Math.round(base * materialMultiplier);
        return Math.max(MIN_POSTURE, Math.min(MAX_POSTURE, computed));
    }

    public static int getMobMaxPosture(ItemStack stack, float maxHealth) {
        int weaponPosture = getMaxPosture(stack);
        float healthMultiplier = MOB_HEALTH_SCALE_BASE + (Math.max(1.0F, maxHealth) / MOB_HEALTH_SCALE_DIVISOR);
        healthMultiplier = Math.max(MIN_MOB_HEALTH_MULTIPLIER, Math.min(MAX_MOB_HEALTH_MULTIPLIER, healthMultiplier));
        return Math.max(MIN_POSTURE, Math.round(weaponPosture * healthMultiplier));
    }

    private static float resolveTypeBase(ItemStack stack) {
        Item item = stack.getItem();
        String itemId = item.toString().toLowerCase();

        if (item instanceof TantoItem || itemId.contains("tanto")) {
            return baseFromType(WeaponType.TANTO);
        }
        if (item instanceof TetsuboItem || itemId.contains("tetsubo")) {
            return baseFromType(WeaponType.TETSUBO);
        }
        if (item instanceof KanaboItem || item instanceof MaceItem || itemId.contains("kanabo") || itemId.contains("mace")) {
            return Math.max(baseFromType(WeaponType.KANABO), 22.0F);
        }
        if (item instanceof NaginataItem || itemId.contains("naginata")) {
            return baseFromType(WeaponType.NAGINATA);
        }
        if (item instanceof YariItem || itemId.contains("yari") || itemId.contains("spear")) {
            return baseFromType(WeaponType.YARI);
        }
        if (item instanceof OdachiItem || itemId.contains("odachi")) {
            return baseFromType(WeaponType.ODACHI);
        }
        if (item instanceof AxeItem || itemId.contains("axe")) {
            return 18.0F;
        }
        if (item instanceof TridentItem || itemId.contains("trident")) {
            return 19.0F;
        }
        if (item instanceof SwordItem || itemId.contains("sword") || itemId.contains("katana") || itemId.contains("blade")) {
            return 16.0F;
        }

        return DEFAULT_POSTURE;
    }

    private static float resolveTierMultiplier(ItemStack stack) {
        Tier tier = null;
        if (stack.getItem() instanceof TieredItem tieredItem) {
            tier = tieredItem.getTier();
        }

        if (tier == Tiers.WOOD) {
            return 0.70F;
        }
        if (tier == Tiers.GOLD) {
            return 0.75F;
        }
        if (tier == Tiers.STONE) {
            return 0.90F;
        }
        if (tier == Tiers.IRON) {
            return 1.00F;
        }
        if (tier == Tiers.DIAMOND) {
            return 1.08F;
        }
        if (tier == Tiers.NETHERITE) {
            return 1.15F;
        }

        String itemId = stack.getItem().toString().toLowerCase();
        if (itemId.contains("wood") || itemId.contains("wooden")) {
            return 0.70F;
        }
        if (itemId.contains("gold")) {
            return 0.75F;
        }
        if (itemId.contains("stone")) {
            return 0.90F;
        }
        if (itemId.contains("iron") || itemId.contains("bronze")) {
            return 1.00F;
        }
        if (itemId.contains("diamond")) {
            return 1.08F;
        }
        if (itemId.contains("netherite")) {
            return 1.15F;
        }

        return 1.00F;
    }

    private static float baseFromType(WeaponType type) {
        return type.getBasePoise() * 0.5F;
    }
}