package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbranks.api.Rank;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ListPlayersWithCommand {
    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("list_players_with")
                .then(Commands.argument("rank", StringArgumentType.string())
                        .suggests((context, builder) -> FTBRanksCommands.suggestRanks(builder))
                        .executes(context -> listPlayersWith(context.getSource(), StringArgumentType.getString(context, "rank")))
                );
    }

    private static int listPlayersWith(CommandSourceStack source, String rankName) throws CommandSyntaxException {
        Rank rank = FTBRanksCommands.getRank(rankName);

        source.sendSuccess(() -> Component.literal(String.format("Players with rank '%s':", rank.getName())), false);

        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            if (rank.isActive(player)) {
                source.sendSuccess(() -> Component.literal("- ").withStyle(ChatFormatting.YELLOW).append(player.getDisplayName()), false);
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
