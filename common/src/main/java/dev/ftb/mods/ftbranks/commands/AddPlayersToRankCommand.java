package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbranks.api.Rank;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;

import java.text.MessageFormat;
import java.util.Collection;

public class AddPlayersToRankCommand {
    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("add")
                .then(Commands.argument("players", GameProfileArgument.gameProfile())
                        .then(Commands.argument("rank", StringArgumentType.string())
                                .suggests((context, builder) -> FTBRanksCommands.suggestRanks(builder))
                                .executes(context -> addRank(context.getSource(), GameProfileArgument.getGameProfiles(context, "players"), StringArgumentType.getString(context, "rank")))
                        )
                );
    }

    private static int addRank(CommandSourceStack source, Collection<NameAndId> players, String rankName) throws CommandSyntaxException {
        Rank rank = FTBRanksCommands.getRank(rankName);

        int success = 0;
        for (NameAndId profile : players) {
            try {
                if (rank.add(profile)) {
                    success++;
                    source.sendSuccess(() -> Component.literal(MessageFormat.format("Player {0} added to rank {1}!", profile.name(), rank.getName())), false);
                }
            } catch (Exception e) {
                source.sendFailure(Component.literal(MessageFormat.format("Could not add player {0} to rank {1}: {2}", profile.name(), rank.getName(), e.getMessage())));
            }
        }

        return success;
    }
}
