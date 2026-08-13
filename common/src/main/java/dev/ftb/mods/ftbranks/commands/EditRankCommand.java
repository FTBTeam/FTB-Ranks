package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbranks.api.Rank;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class EditRankCommand {
    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("edit")
                .then(Commands.argument("rank", StringArgumentType.word())
                        .suggests((context, builder) -> FTBRanksCommands.suggestRanks(builder, false))
                        .then(Commands.literal("name")
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .executes(context -> modifyDisplayName(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "rank"),
                                                StringArgumentType.getString(context, "name"))
                                        )
                                )
                        )
                        .then(Commands.literal("power")
                                .then(Commands.argument("power", IntegerArgumentType.integer(1))
                                        .executes(context -> modifyPower(
                                                context.getSource(),
                                                StringArgumentType.getString(context, "rank"),
                                                IntegerArgumentType.getInteger(context, "power"))
                                        )
                                )
                        )
                        .then(Commands.literal("condition")
                                .then(Commands.argument("value", StringArgumentType.greedyString())
                                        .executes(context -> ConditionCommand.setCondition(context.getSource(), StringArgumentType.getString(context, "rank"), StringArgumentType.getString(context, "value")))
                                )
                        )
                );
    }

    private static int modifyDisplayName(CommandSourceStack source, String rankName, String name) throws CommandSyntaxException {
        Rank rank = FTBRanksCommands.getRank(rankName);

        rank.setDisplayName(name);

        source.sendSuccess(() -> Component.literal("Display name for '" + rank.getNamespacedId() + "' = '" + name + "'"), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int modifyPower(CommandSourceStack source, String rankName, int power) throws CommandSyntaxException {
        Rank rank = FTBRanksCommands.getRank(rankName);

        rank.setPower(power);

        source.sendSuccess(() -> Component.literal("Power for '" + rank.getNamespacedId() + "' = '" + power + "'"), false);

        return Command.SINGLE_SUCCESS;
    }
}
