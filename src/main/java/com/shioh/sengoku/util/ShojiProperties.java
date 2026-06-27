package com.shioh.sengoku.util;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ShojiProperties {
    public static final BooleanProperty HANDLE = BooleanProperty.create("handle");
    // damaged: 1..3 (use 1 as default)
    public static final IntegerProperty DAMAGED = IntegerProperty.create("damaged", 1, 3);
    // aged: true/false (default false)
    public static final BooleanProperty AGED = BooleanProperty.create("aged");
}
