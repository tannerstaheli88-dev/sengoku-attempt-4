package com.shioh.sengoku.mixin;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class TrapdoorStiffProperties {
    // Shared property holder so multiple mixins can reference the same property
    public static final BooleanProperty STIFF = BooleanProperty.create("stiff");
}
