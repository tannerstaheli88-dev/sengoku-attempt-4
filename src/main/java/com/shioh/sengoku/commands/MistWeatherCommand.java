package com.shioh.sengoku.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * Command to start/stop mist weather for testing
 */
public class MistWeatherCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("mistweather")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("start")
                    .executes(MistWeatherCommand::startMist)
                    .then(Commands.argument("duration", IntegerArgumentType.integer(1, 24000))
                        .executes(MistWeatherCommand::startMistWithDuration)
                    )
                )
                .then(Commands.literal("stop")
                    .executes(MistWeatherCommand::stopMist)
                )
        );
    }
    
    private static int startMist(CommandContext<CommandSourceStack> context) {
        return startMistWithDuration(context, 6000); // 5 minutes default
    }
    
    private static int startMistWithDuration(CommandContext<CommandSourceStack> context) {
        int duration = IntegerArgumentType.getInteger(context, "duration");
        return startMistWithDuration(context, duration);
    }
    
    private static int startMistWithDuration(CommandContext<CommandSourceStack> context, int duration) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        
        com.shioh.sengoku.system.MistWeatherSystem.startMist(level, duration);
        
        int minutes = duration / 1200;
        int seconds = (duration % 1200) / 20;
        
        source.sendSuccess(() -> 
            Component.literal(String.format("Started mist weather for %d minutes and %d seconds", minutes, seconds)), 
            true
        );
        
        return 1;
    }
    
    private static int stopMist(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        
        com.shioh.sengoku.system.MistWeatherSystem.stopMist(level);
        
        source.sendSuccess(() -> Component.literal("Stopped mist weather"), true);
        
        return 1;
    }
}
