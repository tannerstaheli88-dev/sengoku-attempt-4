package com.shioh.sengoku.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shioh.sengoku.event.YukiOnnaSnowSpawnHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Debug command to manually trigger a Yuki Onna spawn event.
 */
public class SpawnYukiOnnaCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("spawnyukionna")
                .requires(source -> source.hasPermission(2))
                .executes(SpawnYukiOnnaCommand::execute)
        );
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        ServerLevel level = player.serverLevel();

        // Force spawn at player's position (surface adjusted)
        BlockPos executorPos = player.blockPosition();
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, executorPos.getX(), executorPos.getZ());
        BlockPos spawnPos = new BlockPos(executorPos.getX(), surfaceY, executorPos.getZ());

        int spawned = com.shioh.sengoku.entity.ai.YukiOnnaPatrolSpawner.INSTANCE.forceSpawnPatrol(level, spawnPos, level.random);
        if (spawned > 0) {
            source.sendSuccess(() -> Component.literal("§bYuki Onna spawn triggered successfully. Look around for her..."), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cFailed to spawn Yuki Onna. Make sure you're on the surface."));
            return 0;
        }
    }
}
