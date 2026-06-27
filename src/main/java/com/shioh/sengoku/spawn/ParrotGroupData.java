package com.shioh.sengoku.spawn;

import net.minecraft.world.entity.SpawnGroupData;

public final class ParrotGroupData implements SpawnGroupData {
    public final int variant;

    public ParrotGroupData(int variant) {
        this.variant = variant;
    }
}
