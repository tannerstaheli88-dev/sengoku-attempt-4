package com.shioh.sengoku.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class ResetYukiOnnaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("resetyuki")
                .requires(src -> src.hasPermission(2))
                .executes(ResetYukiOnnaCommand::execute)
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command must be run by a player"));
            return 0;
        }

        ServerLevel level = player.serverLevel();
        com.shioh.sengoku.entity.ai.YukiOnnaPatrolSpawner.INSTANCE.resetNextAttempt(level);
        source.sendSuccess(() -> Component.literal("Yuki Onna spawn timer reset; next attempt allowed immediately."), true);
        return 1;
    }
}
