package com.shioh.sengoku.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class FlowingLeavesParticle extends TextureSheetParticle {

    private static final double WIND_X = 0.72D;
    private static final double WIND_Z = 0.26D;

    private final SpriteSet sprites;
    private final float baseSize;
    private final float rollSpeed;

    protected FlowingLeavesParticle(
            ClientLevel level,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed,
            SpriteSet sprites
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;

        this.baseSize = 1.1F + this.random.nextFloat() * 1.7F;
        this.quadSize = this.baseSize;
        this.lifetime = 18 + this.random.nextInt(12);
        this.hasPhysics = false;
        this.gravity = 0.018F;

        this.xd = WIND_X + (this.random.nextDouble() - 0.5D) * 0.03D;
        this.zd = WIND_Z + (this.random.nextDouble() - 0.5D) * 0.03D;
        this.yd = -0.02D - this.random.nextDouble() * 0.02D;

        this.alpha = 0.95F;
        this.roll = this.random.nextFloat() * ((float) Math.PI * 2.0F);
        this.oRoll = this.roll;
        this.rollSpeed = (this.random.nextFloat() * 0.10F + 0.04F) * (this.random.nextBoolean() ? 1.0F : -1.0F);

        this.setSprite(this.sprites.get(this.random));
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.xd = WIND_X;
        this.zd = WIND_Z;
        this.yd -= 0.004D + this.gravity * 0.05D;
        this.roll += this.rollSpeed;
        this.move(this.xd, this.yd, this.zd);

        this.alpha = 0.95F;
        this.quadSize = this.baseSize;
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
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed
        ) {
            return new FlowingLeavesParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}