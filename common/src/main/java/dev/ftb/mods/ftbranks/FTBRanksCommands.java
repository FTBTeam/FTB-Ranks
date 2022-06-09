package dev.ftb.mods.ftbranks;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.impl.FTBRanksAPIImpl;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.util.Collection;

/**
 * @author LatvianModder
 */
public class FTBRanksCommands {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, Commands.CommandSelection selection) {
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
						.then(Commands.argument("rank", StringArgumentType.word())
								.executes(context -> deleteRank(context.getSource(), StringArgumentType.getString(context, "rank")))
						)
				)
				.then(Commands.literal("add")
						.then(Commands.argument("players", GameProfileArgument.gameProfile())
								.then(Commands.argument("rank", StringArgumentType.word())
										.executes(context -> addRank(context.getSource(), GameProfileArgument.getGameProfiles(context, "players"), StringArgumentType.getString(context, "rank")))
								)
						)
				)
				.then(Commands.literal("remove")
						.then(Commands.argument("players", GameProfileArgument.gameProfile())
								.then(Commands.argument("rank", StringArgumentType.word())
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
								.executes(context -> listPlayersWith(context.getSource(), StringArgumentType.getString(context, "rank")))
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
			source.sendSuccess(Component.literal("Ranks reloaded!"), true);

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

		source.sendSuccess(Component.literal("Done!"), false);
		return 1;
	}

	private static int listAllRanks(CommandSourceStack source) {
		source.sendSuccess(Component.literal("Ranks:"), false);

		for (Rank rank : FTBRanksAPIImpl.manager.getAllRanks()) {
			source.sendSuccess(Component.literal("- " + rank.getName()).withStyle(rank.getCondition().isDefaultCondition() ? ChatFormatting.AQUA : ChatFormatting.YELLOW), false);
		}

		return 1;
	}

	private static int createRank(CommandSourceStack source, String name) throws CommandSyntaxException {
		String id = normalizeRankName(name);

		if (FTBRanksAPIImpl.manager.getRank(id).isPresent()) {
			source.sendFailure(Component.literal("Rank ID already taken!"));
			return 0;
		}

		FTBRanksAPIImpl.manager.createRank(id, name);
		source.sendSuccess(Component.literal("Rank created with id '" + id + "'!"), false);
		return 1;
	}

	private static int deleteRank(CommandSourceStack source, String name) throws CommandSyntaxException {
		if (FTBRanksAPIImpl.manager.deleteRank(normalizeRankName(name)) == null) {
			source.sendFailure(Component.literal("Rank not found!"));
			return 0;
		}

		source.sendSuccess(Component.literal("Rank deleted!"), false);
		return 1;
	}

	private static int addRank(CommandSourceStack source, Collection<GameProfile> players, String name) throws CommandSyntaxException {
		Rank r = FTBRanksAPIImpl.manager.getRank(normalizeRankName(name)).orElseThrow(NullPointerException::new);

		for (GameProfile profile : players) {
			if (r.add(profile)) {
				source.sendSuccess(Component.literal("Added '" + r.getName() + "' to " + profile.getName()), false);
			}
		}

		return 1;
	}

	private static int removeRank(CommandSourceStack source, Collection<GameProfile> players, String name) throws CommandSyntaxException {
		Rank r = FTBRanksAPIImpl.manager.getRank(normalizeRankName(name)).orElseThrow(NullPointerException::new);

		for (GameProfile profile : players) {
			if (r.remove(profile)) {
				source.sendSuccess(Component.literal("Removed '" + r.getName() + "' from " + profile.getName()), false);
			}
		}

		return 1;
	}

	private static int listRanksOf(CommandSourceStack source, ServerPlayer player) {
		source.sendSuccess(Component.literal("Ranks added to " + player.getGameProfile().getName() + ":"), false);

		for (Rank rank : FTBRanksAPIImpl.manager.getAllRanks()) {
			if (rank.isActive(player)) {
				source.sendSuccess(Component.literal("- " + rank.getName()).withStyle(rank.getCondition().isDefaultCondition() ? ChatFormatting.AQUA : ChatFormatting.YELLOW), false);
			}
		}

		return 1;
	}

	private static int listPlayersWith(CommandSourceStack source, String name) {
		Rank r = FTBRanksAPIImpl.manager.getRank(normalizeRankName(name)).orElseThrow(NullPointerException::new);

		source.sendSuccess(Component.literal("Players with " + name + " added to them:"), false);

		for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
			if (r.isActive(player)) {
				source.sendSuccess(Component.literal("- ").withStyle(ChatFormatting.YELLOW).append(player.getDisplayName()), false);
			}
		}

		return 1;
	}
}
