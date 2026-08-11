package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.impl.FTBRanksAPIImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

public class ListRanksOfCommand {
    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("list_ranks_of")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> listRanksOf(context.getSource(), EntityArgument.getPlayer(context, "player")))
                );
    }

    private static int listRanksOf(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.literal(String.format("Ranks added to player '%s':", player.getGameProfile().name())), false);

        for (Rank rank : Objects.requireNonNull(FTBRanksAPIImpl.manager).getAllRanks()) {
            if (rank.isActive(player)) {
                source.sendSuccess(() -> Component.literal("- ").append(FTBRanksCommands.makeRankNameClicky(rank)), false);
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
