package com.shioh.sengoku.system;

/**
 * Holds posture state for weapons.
 */
public class PostureData {
    private int posture;
    private int maxPosture;

    public static final PostureData EMPTY = new PostureData(0, 0);

    public PostureData(int posture, int maxPosture) {
        this.posture = posture;
        this.maxPosture = maxPosture;
    }

    public int getPosture() {
        return posture;
    }

    public void setPosture(int posture) {
        this.posture = Math.max(0, Math.min(posture, maxPosture));
    }

    public void damage(int amount) {
        setPosture(posture - amount);
    }

    public void regenerate(int amount) {
        setPosture(posture + amount);
    }

    public boolean isBroken() {
        return posture <= 0;
    }

    public int getMaxPosture() {
        return maxPosture;
    }

    public void reset() {
        this.posture = this.maxPosture;
    }
}
