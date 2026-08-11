package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbranks.api.Rank;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;

import java.util.Collection;

public class AddPlayerToRankCommand {
    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("add")
                .then(Commands.argument("players", GameProfileArgument.gameProfile())
                        .then(Commands.argument("rank", StringArgumentType.word())
                                .suggests((context, builder) -> FTBRanksCommands.suggestRanks(builder))
                                .executes(context -> addRank(context.getSource(), GameProfileArgument.getGameProfiles(context, "players"), StringArgumentType.getString(context, "rank")))
                        )
                );
    }

    private static int addRank(CommandSourceStack source, Collection<NameAndId> players, String rankName) throws CommandSyntaxException {
        Rank rank = FTBRanksCommands.getRank(rankName);
        for (NameAndId profile : players) {
            if (rank.add(profile)) {
                source.sendSuccess(() -> Component.literal(String.format("Player %s added to rank '%s'!", profile.name(), rank.getName())), false);
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
