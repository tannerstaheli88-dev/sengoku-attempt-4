package com.shioh.sengoku.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shioh.sengoku.entity.ai.NightZombiePatrolSpawner;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Command to spawn a night zombie patrol for testing.
 */
public class SpawnNightZombiePatrolCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawnnightonipatrol")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            .executes(SpawnNightZombiePatrolCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("This command can only be used in a server world"));
            return 0;
        }

        // Spawn a bit in front of the executor
        BlockPos executorPos = BlockPos.containing(source.getPosition());
        BlockPos spawnPos = executorPos.offset(10, 0, 0);

        // Adjust to surface
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPos.getX(), spawnPos.getZ());
        spawnPos = new BlockPos(spawnPos.getX(), surfaceY, spawnPos.getZ());

        // Force spawn a night oni patrol
        int spawned = NightZombiePatrolSpawner.INSTANCE.forceSpawnPatrol(level, spawnPos, level.random);

        if (spawned > 0) {
            source.sendSuccess(() -> Component.literal("Spawned a night oni patrol with " + spawned + " members!"), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Failed to spawn night oni patrol - invalid location"));
            return 0;
        }
    }
}
