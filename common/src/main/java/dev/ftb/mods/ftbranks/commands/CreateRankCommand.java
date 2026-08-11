package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CreateRankCommand {
    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("create")
                .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("power", IntegerArgumentType.integer(1))
                                .executes(context -> createRank(context.getSource(), StringArgumentType.getString(context, "name"), IntegerArgumentType.getInteger(context, "power"))))
                        .executes(context -> createRank(context.getSource(), StringArgumentType.getString(context, "name"), 1))
                );
    }

    private static int createRank(CommandSourceStack source, String name, int power) {
        try {
            Rank rank = FTBRanksAPI.manager().createRank(name, power, false);
            source.sendSuccess(() -> Component.literal("Rank '" + rank.getId() + "' created!"), false);
            return Command.SINGLE_SUCCESS;
        } catch (RankException e) {
            source.sendFailure(Component.literal("Could not create rank '" + name + "': " + e.getMessage()));
            return 0;
        }
    }
}
