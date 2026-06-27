package com.shioh.sengoku.struct;

import com.shioh.sengoku.config.PostureValues;
import com.shioh.sengoku.item.*;
import com.shioh.sengoku.materialpack.EarlyLoadedMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

public enum WeaponType {
    TANTO("tanto", 1f, -1.6f, 0, PostureValues.TANTO, TantoItem::new),
    TETSUBO("tetsubo", 5f, -2.8f, 0, PostureValues.TETSUBO, TetsuboItem::new),
    KANABO("kanabo", 4f, -3.0f, 0, PostureValues.KANABO, KanaboItem::new),
    NAGINATA("naginata", 3f, -2.8f, 2, PostureValues.NAGINATA, NaginataItem::new),
    YARI("yari", 2f, -2.5f, 1.25, PostureValues.YARI, YariItem::new),
    ODACHI("odachi", 5f, -3.2f, 1.25, PostureValues.ODACHI, OdachiItem::new);

    private final String id;
    private final float baseDamage;
    private final float baseSpeed;
    private final double baseReach;
    private final int basePoise; // NEW
    private final WeaponFactory factory;

    WeaponType(String id, float baseDamage, float baseSpeed, double baseReach, int basePoise, WeaponFactory factory) {
        this.id = id;
        this.baseDamage = baseDamage;
        this.baseSpeed = baseSpeed;
        this.baseReach = baseReach;
        this.basePoise = basePoise;
        this.factory = factory;
    }

    public String getId() {
        return id;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public float getBaseSpeed() {
        return baseSpeed;
    }

    public double getBaseReach() {
        return baseReach;
    }

    public int getBasePoise() {
        return basePoise;
    }

    public Item create(Tier material, float damageModifier, float speedModifier, float reachModifier, Item.Properties properties) {
        float extraSpeed = speedModifier;
        double extraReach = reachModifier;
        if (material instanceof EarlyLoadedMaterial.TierWithReach) {
            extraSpeed += ((EarlyLoadedMaterial.TierWithReach) material).getAttackSpeedBonus();
            extraReach += ((EarlyLoadedMaterial.TierWithReach) material).getReachBonus();
        }

        return factory.create(
            material,
            baseDamage + damageModifier,
            baseSpeed + extraSpeed,
            baseReach + extraReach,
            properties
        );
    }

    /** Damage modifier rules */
    public static float getDamageModifier(WeaponType type, Tier material) {
        if (type == WeaponType.TANTO && material == Tiers.GOLD) return 0;
        if (type == WeaponType.TETSUBO) {
            if (material == Tiers.WOOD) return -2;
            if (material == Tiers.STONE) return -1;
            if (material == Tiers.GOLD) return -1;
            return -1;
        }
        return 0;
    }

    /** Speed modifier rules */
    public static float getSpeedModifier(WeaponType type, Tier material) {
        if (type == WeaponType.TANTO && material == Tiers.GOLD) return 1;
        if (type == WeaponType.TETSUBO) {
            if (material == Tiers.WOOD) return 0.4f;
            if (material == Tiers.STONE) return 0.2f;
            if (material == Tiers.GOLD) return 0.6f;
            if (material == Tiers.NETHERITE) return 0.2f;
            return 0.1f;
        }
        if (type == WeaponType.KANABO || type == WeaponType.NAGINATA || type == WeaponType.ODACHI) {
            if (material == Tiers.DIAMOND) return 0.1f;
            if (material == Tiers.NETHERITE) return 0.15f;
        }
        return 0;
    }

    /** Reach modifier rules */
    public static float getReachModifier(WeaponType type, Tier material) {
        return 0; // future-proofing
    }

    @FunctionalInterface
    public interface WeaponFactory {
        Item create(Tier material, float damage, float speed, double reach, Item.Properties properties);
    }
}
