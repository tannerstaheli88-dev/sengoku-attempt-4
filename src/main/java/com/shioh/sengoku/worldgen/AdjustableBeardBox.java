package com.shioh.sengoku.worldgen;

import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class AdjustableBeardBox {
    public final BoundingBox box;
    public final int radius;
    public final float beardStrength;
    public final float carveStrength;

    public AdjustableBeardBox(BoundingBox box, int radius, float beardStrength, float carveStrength) {
        this.box = box;
        this.radius = Math.max(1, radius);
        this.beardStrength = beardStrength;
        this.carveStrength = carveStrength;
    }

    public double computeContribution(int x, int y, int z) {
        int dx = x < box.minX() ? box.minX() - x : (x > box.maxX() ? x - box.maxX() : 0);
        int dz = z < box.minZ() ? box.minZ() - z : (z > box.maxZ() ? z - box.maxZ() : 0);
        boolean isOutside = dx > 0 || dz > 0;

        double horizontalFactor;
        if (isOutside) {
            if (dx > radius || dz > radius) {
                return 0.0D;
            }

            double outsideDistanceSq = (double) dx * dx + (double) dz * dz;
            double radiusSq = (double) radius * radius;
            if (outsideDistanceSq >= radiusSq) {
                return 0.0D;
            }

            double outsideDistance = Math.sqrt(outsideDistanceSq);
            double t = outsideDistance / (double) radius;
            horizontalFactor = Math.exp(-t * t * 4.0D);
        } else {
            int distanceToEdge = Math.min(
                Math.min(x - box.minX(), box.maxX() - x),
                Math.min(z - box.minZ(), box.maxZ() - z)
            );
            double interiorT = Math.min(1.0D, distanceToEdge / (double) radius);
            horizontalFactor = Math.exp(-interiorT * interiorT * 2.0D);
        }

        int verticalDelta = y - box.maxY();
        double verticalFalloff = Math.exp(-Math.abs(verticalDelta) * 0.2D);

        double finalStrength = horizontalFactor * verticalFalloff;
        return beardStrength * finalStrength - carveStrength * finalStrength;
    }
}
