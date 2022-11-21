package dev.ftb.mods.ftbranks;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.impl.BooleanPermissionValue;
import dev.ftb.mods.ftbranks.impl.FTBRanksAPIImpl;
import dev.ftb.mods.ftbranks.impl.NumberPermissionValue;
import dev.ftb.mods.ftbranks.impl.StringPermissionValue;
import dev.ftb.mods.ftbranks.impl.condition.DefaultCondition;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * @author LatvianModder
 */
public class FTBRanksCommands {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection selection) {
		dispatcher.register(Commands.literal("ftbranks")
				.requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2))
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
						.then(Commands.argument("name", StringArgumentType.greedyString())
								.executes(context -> createRank(context.getSource(), StringArgumentType.getString(context, "name")))
						)
				)
				.then(Commands.literal("delete")
						.then(Commands.argument("rank", RankArgumentType.rank())
								.executes(context -> deleteRank(context.getSource(), RankArgumentType.getRank(context, "rank")))
						)
				)
				.then(Commands.literal("add")
						.then(Commands.argument("players", GameProfileArgument.gameProfile())
								.then(Commands.argument("rank", RankArgumentType.rank())
										.executes(context -> addRank(context.getSource(), GameProfileArgument.getGameProfiles(context, "players"), RankArgumentType.getRank(context, "rank")))
								)
						)
				)
				.then(Commands.literal("remove")
						.then(Commands.argument("players", GameProfileArgument.gameProfile())
								.then(Commands.argument("rank", RankArgumentType.rank())
										.executes(context -> removeRank(context.getSource(), GameProfileArgument.getGameProfiles(context, "players"), RankArgumentType.getRank(context, "rank")))
								)
						)
				)
				.then(Commands.literal("list_ranks_of")
						.then(Commands.argument("player", EntityArgument.player())
								.executes(context -> listRanksOf(context.getSource(), EntityArgument.getPlayer(context, "player")))
						)
				)
				.then(Commands.literal("list_players_with")
						.then(Commands.argument("rank", RankArgumentType.rank())
								.executes(context -> listPlayersWith(context.getSource(), RankArgumentType.getRank(context, "rank")))
						)
				)
				.then(Commands.literal("node")
						.then(Commands.literal("add")
								.then(Commands.argument("rank", RankArgumentType.rank())
										.then(Commands.argument("node", StringArgumentType.word())
												.then(Commands.argument("value", StringArgumentType.greedyString())
														.executes(context -> setNode(context.getSource(), RankArgumentType.getRank(context, "rank"), StringArgumentType.getString(context, "node"), StringArgumentType.getString(context, "value")))
												)
										)
								)
						)
						.then(Commands.literal("remove")
								.then(Commands.argument("rank", RankArgumentType.rank())
										.then(Commands.argument("node", StringArgumentType.word())
												.executes(context -> setNode(context.getSource(), RankArgumentType.getRank(context, "rank"), StringArgumentType.getString(context, "node"), null))
										)
								)
						)
				)
				.then(Commands.literal("condition")
						.then(Commands.argument("rank", RankArgumentType.rank())
								.then(Commands.argument("value", StringArgumentType.greedyString())
										.executes(context -> setCondition(context.getSource(), RankArgumentType.getRank(context, "rank"), StringArgumentType.getString(context, "value")))
								)
						)
				)
		);
	}

	private static String normalizeRankName(String name) {
		return name.toLowerCase().replace("+", "_plus").replaceAll("[^a-z0-9_]", "_").replaceAll("_{2,}", "_");
	}

	private static int reloadRanks(CommandSourceStack source) {
		try {
			FTBRanksAPIImpl.manager.reload();
			source.sendSuccess(Component.translatable("ftbranks.reload"), true);

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

		source.sendSuccess(Component.translatable("ftbranks.refresh_readme"), false);
		return 1;
	}

	private static int listAllRanks(CommandSourceStack source) {
		source.sendSuccess(Component.translatable("ftbranks.ranks"), false);

		for (Rank rank : FTBRanksAPIImpl.manager.getAllRanks()) {
			source.sendSuccess(Component.literal("- " + rank.getName()).withStyle(rank.getCondition().isDefaultCondition() ? ChatFormatting.AQUA : ChatFormatting.YELLOW), false);
		}

		return 1;
	}

	private static int createRank(CommandSourceStack source, String name) {
		String id = normalizeRankName(name);

		if (FTBRanksAPIImpl.manager.getRank(id).isPresent()) {
			source.sendFailure(Component.translatable("ftbranks.rank_taken", name));
			return 0;
		}

		FTBRanksAPIImpl.manager.createRank(id, name);
		source.sendSuccess(Component.translatable("ftbranks.rank_created", id), false);
		return 1;
	}

	private static int deleteRank(CommandSourceStack source, Rank rank) {
		FTBRanksAPI.INSTANCE.getManager().deleteRank(rank.getId());
		source.sendSuccess(Component.translatable("ftbranks.rank_deleted", rank.getName()), false);

		return 1;
	}

	private static int addRank(CommandSourceStack source, Collection<GameProfile> players, Rank rank) {
		for (GameProfile profile : players) {
			if (rank.add(profile)) {
				source.sendSuccess(Component.translatable("ftbranks.player_added", profile.getName(), rank.getName()), false);
			}
		}

		return 1;
	}

	private static int removeRank(CommandSourceStack source, Collection<GameProfile> players, Rank rank) {
		for (GameProfile profile : players) {
			if (rank.remove(profile)) {
				source.sendSuccess(Component.translatable("ftbranks.player_removed", profile.getName(), rank.getName()), false);
			}
		}

		return 1;
	}

	private static int listRanksOf(CommandSourceStack source, ServerPlayer player) {
		source.sendSuccess(Component.translatable("ftbranks.list_ranks_of", player.getGameProfile().getName()), false);

		for (Rank rank : FTBRanksAPIImpl.manager.getAllRanks()) {
			if (rank.isActive(player)) {
				source.sendSuccess(Component.literal("- " + rank.getName()).withStyle(rank.getCondition().isDefaultCondition() ? ChatFormatting.AQUA : ChatFormatting.YELLOW), false);
			}
		}

		return 1;
	}
	private static int listPlayersWith(CommandSourceStack source, Rank rank) {
		source.sendSuccess(Component.translatable("ftbranks.list_players_with", rank.getName()), false);

		for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
			if (rank.isActive(player)) {
				source.sendSuccess(Component.literal("- ").withStyle(ChatFormatting.YELLOW).append(player.getDisplayName()), false);
			}
		}

		return 1;
	}

	private static int setNode(CommandSourceStack source, Rank rank, String node, String value) throws CommandSyntaxException {
		try {
			rank.setPermission(node, strToPermissionValue(value));
			if (value != null) {
				source.sendSuccess(Component.translatable("ftbranks.node_added", node, rank.getPermission(node), rank), false);
			} else {
				source.sendSuccess(Component.translatable("ftbranks.node_removed", node, rank), false);
			}
		} catch (IllegalArgumentException e) {
			throw new SimpleCommandExceptionType(Component.literal(e.getMessage())).create();
		}

		return 1;
	}

	private static int setCondition(CommandSourceStack source, Rank rank, String value) throws CommandSyntaxException {
		try {
			RankCondition condition;
			if (value.equals("default") || value.equals("\"\"")) {
				condition = new DefaultCondition(rank);
			} else if (value.startsWith("{") || value.contains(" ")) {
				condition = FTBRanksAPI.INSTANCE.getManager().createCondition(rank, SNBT.readLines(Collections.singletonList(value)));
			} else {
				condition = FTBRanksAPI.INSTANCE.getManager().createCondition(rank, StringTag.valueOf(value));
			}
			rank.setCondition(condition);
			source.sendSuccess(Component.translatable("ftbranks.node_added", "condition", value, rank), false);
		} catch (Exception e) {
			throw new SimpleCommandExceptionType(Component.literal(e.getMessage())).create();
		}

		return 1;
	}

	private static PermissionValue strToPermissionValue(String str) {
		if (str == null) {
			return null;
		} else if (str.startsWith("\"") && str.endsWith("\"")) {
			return StringPermissionValue.of(str.substring(1, str.length() - 1));
		} if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
			return BooleanPermissionValue.of(str.equalsIgnoreCase("true"));
		} else if (NumberUtils.isCreatable(str)) {
			return NumberPermissionValue.of(NumberUtils.createNumber(str));
		} else {
			return StringPermissionValue.of(str);
		}
	}
}
