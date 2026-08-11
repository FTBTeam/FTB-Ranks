package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

public class NodeCommand {
    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("node")
                .then(Commands.literal("add")
                        .then(Commands.argument("rank", StringArgumentType.word())
                                .suggests((_, builder) -> FTBRanksCommands.suggestRanks(builder))
                                .then(Commands.argument("node", StringArgumentType.word())
                                        .then(Commands.argument("value", StringArgumentType.greedyString())
                                                .executes(context -> setNode(context.getSource(), StringArgumentType.getString(context, "rank"), StringArgumentType.getString(context, "node"), StringArgumentType.getString(context, "value")))
                                        )
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("rank", StringArgumentType.word())
                                .suggests((_, builder) -> FTBRanksCommands.suggestRanks(builder))
                                .then(Commands.argument("node", StringArgumentType.word())
                                        .executes(context -> setNode(context.getSource(), StringArgumentType.getString(context, "rank"), StringArgumentType.getString(context, "node"), null))
                                )
                        )
                )
                .then(Commands.literal("list")
                        .then(Commands.argument("rank", StringArgumentType.word())
                                .suggests((_, builder) -> FTBRanksCommands.suggestRanks(builder))
                                .executes(context -> listNodes(context.getSource(), StringArgumentType.getString(context, "rank")))
                        )
                );
    }

     private static int setNode(CommandSourceStack source, String rankName, String node, @Nullable String value) throws CommandSyntaxException {
        Rank rank = FTBRanksCommands.getRank(rankName);

        try {
            rank.setPermission(node, PermissionValue.parse(value));
            if (value != null) {
                source.sendSuccess(() -> Component.literal(String.format("Permission node '%s'='%s' added to rank '%s'", node, rank.getPermission(node), rank)), false);
            } else {
                source.sendSuccess(() -> Component.literal(String.format("Permission node '%s' removed from rank '%s'", node, rank)), false);
            }
        } catch (IllegalArgumentException e) {
            throw new SimpleCommandExceptionType(Component.literal(e.getMessage())).create();
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int listNodes(CommandSourceStack source, String rankName) throws CommandSyntaxException {
        Rank rank = FTBRanksCommands.getRank(rankName);

        Collection<String> nodes = rank.getPermissions();
        if (nodes.isEmpty()) {
            source.sendSuccess(() -> Component.literal(String.format("No permission nodes in rank '%s'", rankName)).withStyle(ChatFormatting.GOLD), false);
        } else {
            source.sendSuccess(() -> Component.literal(String.format("%d permission node(s) in rank '%s':", nodes.size(), rankName)).withStyle(ChatFormatting.GREEN), false);
            source.sendSuccess(() -> Component.literal("-".repeat(20)).withStyle(ChatFormatting.GREEN), false);
            nodes.forEach(node ->
                    source.sendSuccess(() -> Component.literal(String.format("%s = %s", node, rank.getPermission(node))).withStyle(ChatFormatting.YELLOW), false)
            );
            source.sendSuccess(() -> Component.literal("-".repeat(20)).withStyle(ChatFormatting.GREEN), false);
        }

        return Command.SINGLE_SUCCESS;
    }
}
