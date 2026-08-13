package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.Rank;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.*;
import net.minecraft.server.permissions.Permissions;

import java.util.concurrent.CompletableFuture;

public class FTBRanksCommands {
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_RANK = new DynamicCommandExceptionType(
			(object) -> Component.literal("Unknown rank: " + object.toString())
	);

	private static boolean isCommandSourceAllowed(CommandSourceStack source) {
		// source.getServer() *can* return null: https://github.com/FTBTeam/FTB-Mods-Issues/issues/766
		//noinspection ConstantValue
		if (source.getServer() == null) {
			return false;
		}

		// from console, or owner of SSP world (incl open to LAN), or has GM perm level or better
		return source.getPlayer() == null
				|| source.getServer().isSingleplayerOwner(source.getPlayer().nameAndId())
				|| source.getPlayer().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext ignoredContext, Commands.CommandSelection ignoredSelection) {
		dispatcher.register(Commands.literal("ftbranks")
				.requires(FTBRanksCommands::isCommandSourceAllowed)
				.then(ReloadCommand.register())
				.then(RefreshReadmeCommand.register())
				.then(ListAllRanksCommand.register())
				.then(CreateRankCommand.register())
				.then(DeleteRankCommand.register())
				.then(AddPlayersToRankCommand.register())
				.then(RemovePlayersFromRankCommand.register())
				.then(ListRanksOfCommand.register())
				.then(ListPlayersWithCommand.register())
				.then(NodeCommand.register())
				.then(ConditionCommand.register())
				.then(ShowRankCommand.register())
		);
	}

	static CompletableFuture<Suggestions> suggestRanks(SuggestionsBuilder builder) {
		return suggestRanks(builder, true);
	}

	static CompletableFuture<Suggestions> suggestRanks(SuggestionsBuilder builder, boolean allRanks) {
		var ranks = allRanks ? FTBRanksAPI.manager().getAllRanks() : FTBRanksAPI.manager().getAllServerRanks();

		return SharedSuggestionProvider.suggest(ranks.stream().map(rank -> rank.getNamespacedId().toString()), builder);
	}

	static Component makeRankNameClicky(Rank rank) {
		boolean isDef = rank.getCondition().isDefaultCondition();

		MutableComponent tooltip = Component.literal("Rank ID: ").withStyle(ChatFormatting.WHITE)
				.append(Component.literal(rank.getNamespacedId().toString()).withStyle(ChatFormatting.GRAY))
				.append("\n")
				.append(Component.literal("Rank condition: ").withStyle(ChatFormatting.WHITE)
						.append(Component.literal(rank.getCondition().getType()).withStyle(ChatFormatting.GRAY)))
				.append("\n")
				.append(Component.literal("Rank power: ").withStyle(ChatFormatting.WHITE)
						.append(Component.literal(String.valueOf(rank.getPower())).withStyle(ChatFormatting.GRAY)));
		if (isDef) {
			tooltip.append("\n").append(Component.literal("Players must be explicitly added to this rank\nwith '/ftbranks add <player> " + rank.getId() + "'").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}

		return Component.literal(rank.getName())
				.withStyle(isDef ? ChatFormatting.AQUA : ChatFormatting.YELLOW)
				.withStyle(Style.EMPTY
						.withClickEvent(new ClickEvent.RunCommand("/ftbranks show_rank " + rank.getNamespacedId()))
						.withHoverEvent(new HoverEvent.ShowText(tooltip))
				);
	}

	static Rank getRank(String rankName) throws CommandSyntaxException {
		return FTBRanksAPI.manager().getRank(rankName).orElseThrow(() -> ERROR_UNKNOWN_RANK.create(rankName));
	}
}
