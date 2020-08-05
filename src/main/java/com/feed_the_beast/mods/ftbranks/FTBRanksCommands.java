package com.feed_the_beast.mods.ftbranks;

import com.feed_the_beast.mods.ftbranks.api.Rank;
import com.feed_the_beast.mods.ftbranks.impl.FTBRanksAPIImpl;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.Collection;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber
public class FTBRanksCommands
{
	@SubscribeEvent
	public static void register(RegisterCommandsEvent event)
	{
		event.getDispatcher().register(Commands.literal("ftbranks")
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

	private static String normalizeRankName(String name)
	{
		return name.toLowerCase().replace("+", "_plus").replaceAll("[^a-z0-9_]", "_").replaceAll("_{2,}", "_");
	}

	private static int reloadRanks(CommandSource source)
	{
		try
		{
			FTBRanksAPIImpl.manager.reload();
			source.sendFeedback(new StringTextComponent("Ranks reloaded!"), true);
			return 1;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			source.sendFeedback(new StringTextComponent(ex.getLocalizedMessage()).mergeStyle(TextFormatting.RED), true);
			return 0;
		}
	}

	private static int refreshReadme(CommandSource source)
	{
		try
		{
			FTBRanksAPIImpl.manager.refreshReadme();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

		source.sendFeedback(new StringTextComponent("Done!"), false);
		return 1;
	}

	private static int listAllRanks(CommandSource source)
	{
		source.sendFeedback(new StringTextComponent("Ranks:"), false);

		for (Rank rank : FTBRanksAPIImpl.manager.getAllRanks())
		{
			source.sendFeedback(new StringTextComponent("- " + rank.getName()).mergeStyle(rank.getCondition().isDefaultCondition() ? TextFormatting.AQUA : TextFormatting.YELLOW), false);
		}

		return 1;
	}

	private static int createRank(CommandSource source, String name) throws CommandSyntaxException
	{
		String id = normalizeRankName(name);

		if (FTBRanksAPIImpl.manager.getRank(id).isPresent())
		{
			source.sendFeedback(new StringTextComponent("Rank ID already taken!"), false);
			return 0;
		}

		FTBRanksAPIImpl.manager.createRank(id, name);
		source.sendFeedback(new StringTextComponent("Rank created with id '" + id + "'!"), false);
		return 1;
	}

	private static int deleteRank(CommandSource source, String name) throws CommandSyntaxException
	{
		if (FTBRanksAPIImpl.manager.deleteRank(normalizeRankName(name)) == null)
		{
			source.sendFeedback(new StringTextComponent("Rank not found!"), false);
			return 0;
		}

		source.sendFeedback(new StringTextComponent("Rank deleted!"), false);
		return 1;
	}

	private static int addRank(CommandSource source, Collection<GameProfile> players, String name) throws CommandSyntaxException
	{
		Rank r = FTBRanksAPIImpl.manager.getRank(normalizeRankName(name)).orElseThrow(NullPointerException::new);

		for (GameProfile profile : players)
		{
			if (r.add(profile))
			{
				source.sendFeedback(new StringTextComponent("Added '" + r.getName() + "' to " + profile.getName()), false);
			}
		}

		return 1;
	}

	private static int removeRank(CommandSource source, Collection<GameProfile> players, String name) throws CommandSyntaxException
	{
		Rank r = FTBRanksAPIImpl.manager.getRank(normalizeRankName(name)).orElseThrow(NullPointerException::new);

		for (GameProfile profile : players)
		{
			if (r.remove(profile))
			{
				source.sendFeedback(new StringTextComponent("Removed '" + r.getName() + "' from " + profile.getName()), false);
			}
		}

		return 1;
	}

	private static int listRanksOf(CommandSource source, ServerPlayerEntity player)
	{
		source.sendFeedback(new StringTextComponent("Ranks added to " + player.getGameProfile().getName() + ":"), false);

		for (Rank rank : FTBRanksAPIImpl.manager.getAllRanks())
		{
			if (rank.isActive(player))
			{
				source.sendFeedback(new StringTextComponent("- " + rank.getName()).mergeStyle(rank.getCondition().isDefaultCondition() ? TextFormatting.AQUA : TextFormatting.YELLOW), false);
			}
		}

		return 1;
	}

	private static int listPlayersWith(CommandSource source, String name)
	{
		Rank r = FTBRanksAPIImpl.manager.getRank(normalizeRankName(name)).orElseThrow(NullPointerException::new);

		source.sendFeedback(new StringTextComponent("Players with " + name + " added to them:"), false);

		for (ServerPlayerEntity player : source.getServer().getPlayerList().getPlayers())
		{
			if (r.isActive(player))
			{
				source.sendFeedback(new StringTextComponent("- ").mergeStyle(TextFormatting.YELLOW).append(player.getDisplayName()), false);
			}
		}

		return 1;
	}
}