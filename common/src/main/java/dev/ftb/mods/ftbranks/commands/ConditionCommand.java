package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.marhali.json5.Json5;
import de.marhali.json5.Json5Primitive;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.impl.condition.DefaultCondition;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ConditionCommand {
    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("condition")
                .then(Commands.argument("rank", StringArgumentType.word())
                        .suggests((_, builder) -> FTBRanksCommands.suggestRanks(builder, false))
                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                .executes(context -> setCondition(context.getSource(), StringArgumentType.getString(context, "rank"), StringArgumentType.getString(context, "value")))
                        )
                );
    }

    static int setCondition(CommandSourceStack source, String rankName, String value) throws CommandSyntaxException {
        Rank rank = FTBRanksCommands.getRank(rankName);

        try {
            RankCondition condition;
            if (value.equals("default") || value.equals("\"\"")) {
                condition = new DefaultCondition(rank);
            } else if (value.startsWith("{") || value.contains(" ")) {
                condition = FTBRanksAPI.manager().createCondition(rank, new Json5().parse(value));
            } else {
                condition = FTBRanksAPI.manager().createCondition(rank, Json5Primitive.fromString(value));
            }
            rank.setCondition(condition);
            source.sendSuccess(() -> Component.literal(String.format("Condition '%s' added to rank '%s'",  value, rank)), false);
        } catch (Exception e) {
            throw new SimpleCommandExceptionType(Component.literal(e.getMessage())).create();
        }

        return Command.SINGLE_SUCCESS;
    }
}
