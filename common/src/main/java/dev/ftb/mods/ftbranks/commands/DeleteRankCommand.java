package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.Rank;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class DeleteRankCommand {
    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("delete")
                .then(Commands.argument("rank", StringArgumentType.word())
                        .suggests((_, builder) -> FTBRanksCommands.suggestRanks(builder, false))
                        .executes(context -> deleteRank(context.getSource(), StringArgumentType.getString(context, "rank")))
                );
    }

    private static int deleteRank(CommandSourceStack source, String rankName) throws CommandSyntaxException {
        Rank rank = FTBRanksCommands.getRank(rankName);
        if (FTBRanksAPI.manager().deleteRank(rank.getId()) != null) {
            source.sendSuccess(() -> Component.literal("Rank '" + rank.getDisplayName() + "' deleted!"), false);

            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }
}
