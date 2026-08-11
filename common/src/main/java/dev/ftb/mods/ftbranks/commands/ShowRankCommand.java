package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbranks.api.Rank;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ShowRankCommand {
    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("show_rank")
                .then(Commands.argument("rank", StringArgumentType.word())
                        .suggests((context, builder) -> FTBRanksCommands.suggestRanks(builder))
                        .executes(context -> showRank(context.getSource(), StringArgumentType.getString(context, "rank")))
                );
    }

    private static int showRank(CommandSourceStack source, String rankName) throws CommandSyntaxException {
        Rank rank = FTBRanksCommands.getRank(rankName);

        source.sendSuccess(() -> Component.literal("=".repeat(50)).withStyle(ChatFormatting.GREEN), false);

        source.sendSuccess(() -> Component.literal("Rank ID: ").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(rank.getId()).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(", Rank Name: ").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(rank.getName()).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(", Power: ").withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(String.valueOf(rank.getPower())).withStyle(ChatFormatting.WHITE)),
                true);

        String condStr = rank.getCondition().asString();
        Component c = condStr.isEmpty() ?
                Component.literal("(none: players must be added)").withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC) :
                Component.literal(condStr).withStyle(ChatFormatting.WHITE);
        source.sendSuccess(() -> Component.literal("Condition: ").append(c).withStyle(ChatFormatting.YELLOW), false);

        source.sendSuccess(() -> Component.literal("Permission nodes:").withStyle(ChatFormatting.YELLOW), false);
        rank.getPermissions().stream().sorted().forEach(node ->
                source.sendSuccess(() -> Component.literal(" - " + node + ": " + rank.getPermission(node)), false)
        );

        return 0;
    }
}
