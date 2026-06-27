package com.shioh.sengoku.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.shioh.sengoku.entity.ai.ClanPatrolSpawner;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Command to spawn a clan patrol for testing.
 */
public class SpawnClanPatrolCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("spawnclanpatrol")
            .requires(source -> source.hasPermission(2)) // Requires OP level 2
            .executes(SpawnClanPatrolCommand::execute));
    }
    
    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        
        if (!(source.getLevel() instanceof ServerLevel level)) {
            source.sendFailure(Component.literal("This command can only be used in a server world"));
            return 0;
        }
        
        // Get position in front of the command executor
        BlockPos executorPos = BlockPos.containing(source.getPosition());
        BlockPos spawnPos = executorPos.offset(10, 0, 0);
        
        // Adjust to surface
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, spawnPos.getX(), spawnPos.getZ());
        spawnPos = new BlockPos(spawnPos.getX(), surfaceY, spawnPos.getZ());
        
        // Force spawn a clan patrol
        int spawned = ClanPatrolSpawner.INSTANCE.forceSpawnPatrol(level, spawnPos, level.random);
        
        if (spawned > 0) {
            source.sendSuccess(() -> Component.literal("Spawned a clan patrol with " + spawned + " members!"), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Failed to spawn patrol - invalid location"));
            return 0;
        }
    }
}
