package com.shioh.sengoku.util;

import com.shioh.sengoku.registry.ParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ParticleUtils {

    public static void spawnBlockParticle(Player player) {
        if (!player.level().isClientSide) return;

        // Use camera position instead of player torso
        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 look = player.getLookAngle();

        // Right vector (perpendicular to look)
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = look.cross(up).normalize();

        // Side offset depends on main arm
        boolean leftHand = player.getMainArm() == HumanoidArm.LEFT;
        double sideOffset = leftHand ? -0.4 : 0.4;

        // Position relative to camera
        double forwardDist = 1.8D; // put it clearly in front
        double x = camPos.x + look.x * forwardDist + right.x * sideOffset;
        double y = camPos.y + look.y * forwardDist;
        double z = camPos.z + look.z * forwardDist + right.z * sideOffset;

        // Spawn particle clientside
        player.level().addParticle(
                ParticleRegistry.BLOCK_PARTICLE,
                x, y, z,
                0.0D, 0.0D, 0.0D
        );
    }
}
