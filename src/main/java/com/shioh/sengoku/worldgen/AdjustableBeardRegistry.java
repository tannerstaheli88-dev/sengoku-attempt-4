package com.shioh.sengoku.worldgen;

import java.util.ArrayList;
import java.util.List;

public final class AdjustableBeardRegistry {
    private static final List<AdjustableBeardBox> ACTIVE_BEARDS = new ArrayList<>();

    private AdjustableBeardRegistry() {
    }

    public static void clear() {
        synchronized (ACTIVE_BEARDS) {
            ACTIVE_BEARDS.clear();
        }
    }

    public static void add(AdjustableBeardBox beardBox) {
        synchronized (ACTIVE_BEARDS) {
            for (AdjustableBeardBox existing : ACTIVE_BEARDS) {
                if (same(existing, beardBox)) {
                    return;
                }
            }
            ACTIVE_BEARDS.add(beardBox);
        }
    }

    public static double computeContribution(int x, int y, int z) {
        double total = 0.0D;
        synchronized (ACTIVE_BEARDS) {
            for (AdjustableBeardBox beardBox : ACTIVE_BEARDS) {
                total += beardBox.computeContribution(x, y, z);
            }
        }
        return total;
    }

    private static boolean same(AdjustableBeardBox a, AdjustableBeardBox b) {
        return a.box.minX() == b.box.minX()
            && a.box.minY() == b.box.minY()
            && a.box.minZ() == b.box.minZ()
            && a.box.maxX() == b.box.maxX()
            && a.box.maxY() == b.box.maxY()
            && a.box.maxZ() == b.box.maxZ()
            && a.radius == b.radius
            && Float.compare(a.beardStrength, b.beardStrength) == 0
            && Float.compare(a.carveStrength, b.carveStrength) == 0;
    }
}
