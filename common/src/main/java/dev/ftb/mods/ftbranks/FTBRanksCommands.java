package dev.ftb.mods.ftbranks;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.impl.FTBRanksAPIImpl;
import dev.ftb.mods.ftbranks.impl.condition.DefaultCondition;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * @author LatvianModder
 */
public class FTBRanksCommands {
	public static final DynamicCommandExceptionType ERROR_UNKNOWN_RANK = new DynamicCommandExceptionType(
			(object) -> Component.literal("Unknown rank: " + object.toString())
	);

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection selection) {
		// source.getServer() *can* return null: https://github.com/FTBTeam/FTB-Mods-Issues/issues/766
		//noinspection ConstantValue
		dispatcher.register(Commands.literal("ftbranks")
				.requires(source -> source.getServer() != null && source.getServer().isSingleplayer() || source.hasPermission(2))
				.then(Commands.literal("reload")
						.executes(context -> reloadRanks(context.getSource()))
				)
				.then(Commands.literal("refresh_readme")
						.executes(context -> refreshReadme(context.getSource()))
				)
				.then(Commands.literal("list_all_ranks")
						.executes(context -> listAllRanks(context.getSource()))
				)
				.then(Commands.literal("create")
						.then(Commands.argument("name", StringArgumentType.word())
								.then(Commands.argument("power", IntegerArgumentType.integer(1))
										.executes(context -> createRank(context.getSource(), StringArgumentType.getString(context, "name"), IntegerArgumentType.getInteger(context,"power"))))
								.executes(context -> createRank(context.getSource(), StringArgumentType.getString(context, "name"), 1))
						)
				)
				.then(Commands.literal("delete")
						.then(Commands.argument("rank", StringArgumentType.word())
								.suggests((context, builder) -> suggestRanks(builder))
								.executes(context -> deleteRank(context.getSource(), StringArgumentType.getString(context, "rank")))
						)
				)
				.then(Commands.literal("add")
						.then(Commands.argument("players", GameProfileArgument.gameProfile())
								.then(Commands.argument("rank", StringArgumentType.word())
										.suggests((context, builder) -> suggestRanks(builder))
										.executes(context -> addRank(context.getSource(), GameProfileArgument.getGameProfiles(context, "players"), StringArgumentType.getString(context, "rank")))
								)
						)
				)
				.then(Commands.literal("remove")
						.then(Commands.argument("players", GameProfileArgument.gameProfile())
								.then(Commands.argument("rank", StringArgumentType.word())
										.suggests((context, builder) -> suggestRanks(builder))
										.executes(context -> removeRank(context.getSource(), GameProfileArgument.getGameProfiles(context, "players"), StringArgumentType.getString(context, "rank")))
								)
						)
				)
				.then(Commands.literal("list_ranks_of")
						.then(Commands.argument("player", EntityArgument.player())
								.executes(context -> listRanksOf(context.getSource(), EntityArgument.getPlayer(context, "player")))
						)
				)
				.then(Commands.literal("list_players_with")
						.then(Commands.argument("rank", StringArgumentType.word())
								.suggests((context, builder) -> suggestRanks(builder))
								.executes(context -> listPlayersWith(context.getSource(), StringArgumentType.getString(context, "rank")))
						)
				)
				.then(Commands.literal("node")
						.then(Commands.literal("add")
								.then(Commands.argument("rank", StringArgumentType.word())
										.suggests((context, builder) -> suggestRanks(builder))
										.then(Commands.argument("node", StringArgumentType.word())
												.then(Commands.argument("value", StringArgumentType.greedyString())
														.executes(context -> setNode(context.getSource(), StringArgumentType.getString(context, "rank"), StringArgumentType.getString(context, "node"), StringArgumentType.getString(context, "value")))
												)
										)
								)
						)
						.then(Commands.literal("remove")
								.then(Commands.argument("rank", StringArgumentType.word())
										.suggests((context, builder) -> suggestRanks(builder))
										.then(Commands.argument("node", StringArgumentType.word())
												.executes(context -> setNode(context.getSource(), StringArgumentType.getString(context, "rank"), StringArgumentType.getString(context, "node"), null))
										)
								)
						)
						.then(Commands.literal("list")
								.then(Commands.argument("rank", StringArgumentType.word())
										.suggests((context, builder) -> suggestRanks(builder))
										.executes(context -> listNodes(context.getSource(), StringArgumentType.getString(context, "rank")))
								)
						)
				)
				.then(Commands.literal("condition")
						.then(Commands.argument("rank", StringArgumentType.word())
								.suggests((context, builder) -> suggestRanks(builder))
								.then(Commands.argument("value", StringArgumentType.greedyString())
										.executes(context -> setCondition(context.getSource(), StringArgumentType.getString(context, "rank"), StringArgumentType.getString(context, "value")))
								)
						)
				)
				.then(Commands.literal("show_rank")
						.then(Commands.argument("rank", StringArgumentType.word())
								.suggests((context, builder) -> suggestRanks(builder))
								.executes(context -> showRank(context.getSource(), StringArgumentType.getString(context, "rank")))
						)
				)
		);
	}

	private static CompletableFuture<Suggestions> suggestRanks(SuggestionsBuilder builder) {
		return SharedSuggestionProvider.suggest(FTBRanksAPI.manager().getAllRanks().stream().map(Rank::getId), builder);
	}

	private static String normalizeRankName(String name) {
		return name.toLowerCase()
				.replace("+", "_plus")
				.replaceAll("[^a-z0-9_]", "_")
				.replaceAll("_{2,}", "_");
	}

	private static int reloadRanks(CommandSourceStack source) {
		try {
			FTBRanksAPIImpl.manager.reload();
			source.sendSuccess(Component.literal("Ranks reloaded from disk!"), true);

			for (ServerPlayer p : source.getServer().getPlayerList().getPlayers()) {
				source.getServer().getPlayerList().sendPlayerPermissionLevel(p);
			}

			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
			source.sendFailure(Component.literal(ex.getLocalizedMessage()));
			return 0;
		}
	}

	private static int refreshReadme(CommandSourceStack source) {
		try {
			FTBRanksAPIImpl.manager.refreshReadme();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		source.sendSuccess(Component.literal("README file refreshed!"), false);
		return 1;
	}

	private static int listAllRanks(CommandSourceStack source) {
		source.sendSuccess(Component.literal("Ranks:"), false);

		for (Rank rank : FTBRanksAPIImpl.manager.getAllRanks()) {
			source.sendSuccess(Component.literal("- " + rank.getName()).withStyle(rank.getCondition().isDefaultCondition() ? ChatFormatting.AQUA : ChatFormatting.YELLOW), false);
		}

		return 1;
	}

	private static int createRank(CommandSourceStack source, String name, int power) {
		String id = normalizeRankName(name);

		if (FTBRanksAPIImpl.manager.getRank(id).isPresent()) {
			source.sendFailure(Component.literal("Rank '" + name + "' is already taken!"));
			return 0;
		}

		FTBRanksAPIImpl.manager.createRank(id, name, power);
		source.sendSuccess(Component.literal("Rank '" + id + "' created!"), false);
		return 1;
	}

	private static int deleteRank(CommandSourceStack source, String rankName) throws CommandSyntaxException {
		Rank rank = getRank(rankName);
		FTBRanksAPI.manager().deleteRank(rank.getId());
		source.sendSuccess(Component.literal("Rank '" + rank.getName() + "' deleted!"), false);

		return 1;
	}

	private static int addRank(CommandSourceStack source, Collection<GameProfile> players, String rankName) throws CommandSyntaxException {
		Rank rank = getRank(rankName);
		for (GameProfile profile : players) {
			if (rank.add(profile)) {
				source.sendSuccess(Component.literal(String.format("Player %s added to rank '%s'!", profile.getName(), rank.getName())), false);
			}
		}

		return 1;
	}

	private static int removeRank(CommandSourceStack source, Collection<GameProfile> players, String rankName) throws CommandSyntaxException {
		Rank rank = getRank(rankName);
		for (GameProfile profile : players) {
			if (rank.remove(profile)) {
				source.sendSuccess(Component.literal(String.format("Player %s removed from rank '%s'!", profile.getName(), rank.getName())), false);
			}
		}

		return 1;
	}

	private static int listRanksOf(CommandSourceStack source, ServerPlayer player) {
		source.sendSuccess(Component.literal(String.format("Ranks added to player '%s':", player.getGameProfile().getName())), false);

		for (Rank rank : FTBRanksAPIImpl.manager.getAllRanks()) {
			if (rank.isActive(player)) {
				source.sendSuccess(Component.literal("- " + rank.getName()).withStyle(rank.getCondition().isDefaultCondition() ? ChatFormatting.AQUA : ChatFormatting.YELLOW), false);
			}
		}

		return 1;
	}

	private static int listPlayersWith(CommandSourceStack source, String rankName) throws CommandSyntaxException {
		Rank rank = getRank(rankName);

		source.sendSuccess(Component.literal(String.format("Players with rank '%s':", rank.getName())), false);

		for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
			if (rank.isActive(player)) {
				source.sendSuccess(Component.literal("- ").withStyle(ChatFormatting.YELLOW).append(player.getDisplayName()), false);
			}
		}

		return 1;
	}

	private static int listNodes(CommandSourceStack source, String rankName) throws CommandSyntaxException {
		Rank rank = getRank(rankName);

		Collection<String> nodes = rank.getPermissions();
		if (nodes.isEmpty()) {
			source.sendSuccess(Component.literal(String.format("No permission nodes in rank '%s'", rankName)).withStyle(ChatFormatting.GOLD), false);
		} else {
			source.sendSuccess(Component.literal(String.format("%d permission node(s) in rank '%s':", nodes.size(), rankName)).withStyle(ChatFormatting.GREEN), false);
			source.sendSuccess(Component.literal("-".repeat(20)).withStyle(ChatFormatting.GREEN), false);
			nodes.forEach(node -> {
				source.sendSuccess(Component.literal(String.format("%s = %s", node, rank.getPermission(node))).withStyle(ChatFormatting.YELLOW), false);
			});
			source.sendSuccess(Component.literal("-".repeat(20)).withStyle(ChatFormatting.GREEN), false);
		}

		return 1;
	}

	private static int setNode(CommandSourceStack source, String rankName, String node, String value) throws CommandSyntaxException {
		Rank rank = getRank(rankName);

		try {
			rank.setPermission(node, PermissionValue.parse(value));
			if (value != null) {
				source.sendSuccess(Component.literal(String.format("Permission node '%s'='%s' added to rank '%s'", node, rank.getPermission(node), rank)), false);
			} else {
				source.sendSuccess(Component.literal(String.format("Permission node '%s' removed from rank '%s'", node, rank)), false);
			}
		} catch (IllegalArgumentException e) {
			throw new SimpleCommandExceptionType(Component.literal(e.getMessage())).create();
		}

		return 1;
	}

	private static int setCondition(CommandSourceStack source, String rankName, String value) throws CommandSyntaxException {
		Rank rank = getRank(rankName);

		try {
			RankCondition condition;
			if (value.equals("default") || value.equals("\"\"")) {
				condition = new DefaultCondition(rank);
			} else if (value.startsWith("{") || value.contains(" ")) {
				condition = FTBRanksAPI.manager().createCondition(rank, SNBT.readLines(Collections.singletonList(value)));
			} else {
				condition = FTBRanksAPI.manager().createCondition(rank, StringTag.valueOf(value));
			}
			rank.setCondition(condition);
			source.sendSuccess(Component.literal(String.format("Condition '%s' added to rank '%s'",  value, rank)), false);
		} catch (Exception e) {
			throw new SimpleCommandExceptionType(Component.literal(e.getMessage())).create();
		}

		return 1;
	}

	private static int showRank(CommandSourceStack source, String rankName) throws CommandSyntaxException {
		Rank rank = getRank(rankName);

		source.sendSuccess(Component.literal("=".repeat(50)).withStyle(ChatFormatting.GREEN), false);

		source.sendSuccess(Component.literal(String.format("Rank ID: %s, Rank Name: %s, Power: %d", rank.getId(), rank.getName(), rank.getPower())).withStyle(ChatFormatting.YELLOW), false);

		String condStr = rank.getCondition().asString();
		Component c = condStr.isEmpty() ?
				Component.literal("(none: players must be added)").withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC) : Component.literal(condStr);
		source.sendSuccess(Component.literal("Condition: ").append(c).withStyle(ChatFormatting.YELLOW), false);

		source.sendSuccess(Component.literal("Permission nodes:").withStyle(ChatFormatting.YELLOW), false);
		rank.getPermissions().stream().sorted().forEach(node ->
				source.sendSuccess(Component.literal(" - " + node + ": " + rank.getPermission(node)), false)
		);

		return 0;
	}

	private static Rank getRank(String rankName) throws CommandSyntaxException {
		return FTBRanksAPI.manager().getRank(rankName).orElseThrow(() -> ERROR_UNKNOWN_RANK.create(rankName));
	}
}
