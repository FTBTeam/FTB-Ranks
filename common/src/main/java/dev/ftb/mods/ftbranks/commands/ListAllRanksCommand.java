package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.impl.FTBRanksAPIImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class ListAllRanksCommand {
    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("list_all_ranks")
                .executes(context -> listAllRanks(context.getSource()));
    }

    private static int listAllRanks(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Ranks:"), false);

        for (Rank rank : Objects.requireNonNull(FTBRanksAPIImpl.manager).getAllRanks()) {
            source.sendSuccess(() -> Component.literal("- ").append(FTBRanksCommands.makeRankNameClicky(rank)), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
