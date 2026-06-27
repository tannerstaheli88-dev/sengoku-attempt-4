package com.shioh.sengoku.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * Large cyan wave-splash particle for dragon breath visuals.
 */
@Environment(EnvType.CLIENT)
public class DragonSplashParticle extends TextureSheetParticle {

    private static final float MAX_QUAD_SIZE = 3.0F;
    private final SpriteSet sprites;

    protected DragonSplashParticle(ClientLevel level, double x, double y, double z,
                                   double xSpeed, double ySpeed, double zSpeed,
                                   SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;

        this.lifetime = 60;
        this.quadSize = 1.5F;
        this.hasPhysics = true;
        this.gravity = 0.48F;

        this.rCol = 0.15F;
        this.gCol = 0.9F;
        this.bCol = 1.0F;

        this.setAlpha(1.0F);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();

        float progress = (float) this.age / (float) this.lifetime;

        // Fast growth in first half, slow growth in second half, capped
        float growth = progress < 0.5F ? 0.15F : 0.03F;
        this.quadSize = Math.min(this.quadSize + growth, MAX_QUAD_SIZE);

        // Spread momentum decays outward
        this.xd *= 0.98;
        this.yd *= 0.98;
        this.zd *= 0.98;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new DragonSplashParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}