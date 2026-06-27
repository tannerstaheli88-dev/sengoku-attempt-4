package com.shioh.sengoku.client.animation;

/**
 * Small data-holder exposing the main animation timing constants from
 * the Blockbench export. This file is a lightweight bridge so model
 * code can refer to the exported animation's durations. If you want
 * the full Blockbench runtime applied exactly, I can integrate the
 * runtime next.
 */
public final class MacaqueAnimations {
    private MacaqueAnimations() {}

    // Durations (seconds) taken from the Blockbench export
    public static final float IDLE_STANDING_PERIOD = 1.75F;
    public static final float WALKING_PERIOD = 1.5F;
    public static final float RUNNING_PERIOD = 0.4167F;
    public static final float IDLE_SITTING_PERIOD = 1.75F;
}
