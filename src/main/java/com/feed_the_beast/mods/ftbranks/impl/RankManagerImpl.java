package com.feed_the_beast.mods.ftbranks.impl;

import com.feed_the_beast.mods.ftbranks.FTBRanks;
import com.feed_the_beast.mods.ftbranks.api.PermissionValue;
import com.feed_the_beast.mods.ftbranks.api.Rank;
import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import com.feed_the_beast.mods.ftbranks.api.RankConditionFactory;
import com.feed_the_beast.mods.ftbranks.api.RankManager;
import com.feed_the_beast.mods.ftbranks.impl.condition.AlwaysActiveCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.OPCondition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.FolderName;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class RankManagerImpl implements RankManager
{
	public static final FolderName FOLDER_NAME = new FolderName("serverconfig/ftbranks");
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().serializeNulls().disableHtmlEscaping().create();

	public final MinecraftServer server;
	private Map<String, RankCommandPredicate> commands;
	private Map<CommandNode<CommandSource>, RankCommandPredicate> commandNodes;
	private Path directory;
	private Path rankFile;
	private Path playerFile;
	boolean shouldSaveRanks;
	boolean shouldSavePlayers;

	private Map<String, RankImpl> ranks;
	private List<RankImpl> sortedRanks;
	private final Map<String, RankConditionFactory> conditions;
	Map<UUID, PlayerRankData> playerData;

	public RankManagerImpl(MinecraftServer s)
	{
		server = s;
		conditions = new HashMap<>();
	}

	void initCommands()
	{
		commands = new HashMap<>();
		commandNodes = new HashMap<>();

		FTBRanks.LOGGER.info("Loading command nodes...");

		try
		{
			// Absolute cancer but ATs don't work here //
			Field field = CommandNode.class.getDeclaredField("requirement");
			field.setAccessible(true);
			getCommandNodes(server.getCommandManager().getDispatcher(), "command", field, server.getCommandManager().getDispatcher().getRoot());
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
			FTBRanks.LOGGER.error("Reflection failed! Downgrading Java version to 8 might help");
		}

		FTBRanks.LOGGER.info("Loaded " + commands.size() + " command nodes");
	}

	void load() throws Exception
	{
		directory = server.func_240776_a_(FOLDER_NAME);

		if (Files.notExists(directory))
		{
			Files.createDirectories(directory);
		}

		rankFile = directory.resolve("ranks.json");
		playerFile = directory.resolve("players.json");

		if (Files.notExists(directory.resolve("README.txt")))
		{
			refreshReadme();
		}

		reload();
	}

	public void refreshReadme() throws IOException
	{
		List<String> lines = new ArrayList<>();
		lines.add("=== FTB Ranks ===");
		lines.add("");
		lines.add("Last README file update: " + new Date());
		lines.add("Wiki: https://faq.ftb.world/books/ftb-ranks");
		lines.add("To refresh this file, run /ftbranks refresh_readme");
		lines.add("");
		lines.add("= All available command nodes =");
		lines.add("command");

		HashSet<String> set = new HashSet<>();

		for (RankCommandPredicate predicate : commands.values())
		{
			set.add(predicate.getNode());
		}

		List<String> commandList = new ArrayList<>(set);
		commandList.sort(null);
		lines.addAll(commandList);

		Files.write(directory.resolve("README.txt"), lines);
	}

	@Override
	public void saveRanks()
	{
		shouldSaveRanks = true;
	}

	@Override
	public void savePlayers()
	{
		shouldSavePlayers = true;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public List<Rank> getAllRanks()
	{
		return (List<Rank>) (List) sortedRanks;
	}

	@Override
	public Optional<Rank> getRank(String id)
	{
		return Optional.ofNullable(ranks.get(id));
	}

	@Override
	public RankImpl createRank(String id)
	{
		deleteRank(id);
		RankImpl r = new RankImpl(this, id);
		ranks.put(id, r);
		saveRanks();
		return r;
	}

	public RankImpl createRank(String id, String name)
	{
		RankImpl rank = createRank(id);
		rank.name = name;
		saveRanks();
		return rank;
	}

	@Override
	@Nullable
	public RankImpl deleteRank(String id)
	{
		RankImpl r = ranks.get(id);

		if (r != null)
		{
			for (PlayerRankData rankData : playerData.values())
			{
				if (rankData.added.remove(r) != null)
				{
					savePlayers();
				}
			}

			ranks.remove(id);
			saveRanks();
		}

		return r;
	}

	public PlayerRankData getPlayerData(GameProfile profile)
	{
		PlayerRankData data = playerData.get(profile.getId());

		if (data == null)
		{
			data = new PlayerRankData(this, profile.getId());
			data.name = profile.getName();
			playerData.put(data.uuid, data);
			saveRanks();
		}

		return data;
	}

	@Override
	public Set<Rank> getAddedRanks(GameProfile profile)
	{
		return getPlayerData(profile).added.keySet();
	}

	@Override
	public void registerCondition(String predicate, RankConditionFactory conditionFactory)
	{
		conditions.put(predicate, conditionFactory);
	}

	@Override
	public RankCondition createCondition(Rank rank, JsonObject json) throws Exception
	{
		return conditions.get(json.get("type").getAsString()).create(rank, json);
	}

	@Override
	public PermissionValue getPermissionValue(ServerPlayerEntity player, String node)
	{
		if (node.isEmpty())
		{
			return PermissionValue.DEFAULT;
		}

		List<RankImpl> list = new ArrayList<>();

		for (RankImpl rank : sortedRanks)
		{
			if (rank.isActive(player))
			{
				list.add(rank);
			}
		}

		return getPermissionValue(getPlayerData(player.getGameProfile()), list, node);
	}

	private PermissionValue getPermissionValue(PlayerRankData data, List<RankImpl> ranks, String node)
	{
		if (node.isEmpty())
		{
			return PermissionValue.DEFAULT;
		}

		PermissionValue pvalue = data.permissions.get(node);

		if (pvalue != null)
		{
			return pvalue;
		}

		for (RankImpl rank : ranks)
		{
			PermissionValue value = rank.permissions.get(node);

			if (value != null)
			{
				return value;
			}
		}

		int i = node.lastIndexOf('.');
		return i == -1 ? PermissionValue.DEFAULT : getPermissionValue(data, ranks, node.substring(0, i));
	}

	public void reload() throws Exception
	{
		shouldSaveRanks = false;

		if (Files.notExists(rankFile))
		{
			ranks = new LinkedHashMap<>();

			RankImpl memberRank = new RankImpl(this, "member");
			memberRank.setPermission("name", StringPermissionValue.of("Member"));
			memberRank.setPermission("power", NumberPermissionValue.of(1));
			memberRank.setPermission("ftbranks.name_format", StringPermissionValue.of("<{name}>"));
			memberRank.condition = AlwaysActiveCondition.INSTANCE;
			ranks.put("member", memberRank);

			RankImpl vipRank = new RankImpl(this, "vip");
			vipRank.setPermission("name", StringPermissionValue.of("VIP"));
			vipRank.setPermission("power", NumberPermissionValue.of(50));
			vipRank.setPermission("ftbranks.name_format", StringPermissionValue.of("<&bVIP {name}&r>"));
			ranks.put("vip", vipRank);

			RankImpl adminRank = new RankImpl(this, "admin");
			adminRank.setPermission("name", StringPermissionValue.of("Admin"));
			adminRank.setPermission("power", NumberPermissionValue.of(1000));
			adminRank.setPermission("ftbranks.name_format", StringPermissionValue.of("<&2{name}&r>"));
			adminRank.condition = new OPCondition();
			ranks.put("admin", adminRank);

			saveRanks();
			saveRanksNow();
		}

		if (Files.notExists(playerFile))
		{
			playerData = new HashMap<>();

			savePlayers();
			savePlayersNow();
		}

		LinkedHashMap<String, RankImpl> tempRanks = new LinkedHashMap<>();
		LinkedHashMap<UUID, PlayerRankData> tempPlayerData = new LinkedHashMap<>();

		try (Reader reader = Files.newBufferedReader(rankFile))
		{
			try
			{
				JsonObject json = GSON.fromJson(reader, JsonObject.class);

				for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet())
				{
					RankImpl rank = new RankImpl(this, entry.getKey());

					JsonObject o = entry.getValue().getAsJsonObject();
					rank.name = o.remove("name").getAsString();
					rank.power = o.remove("power").getAsInt();

					if (o.has("condition"))
					{
						try
						{
							rank.condition = createCondition(rank, o.remove("condition").getAsJsonObject());
						}
						catch (Exception ex)
						{
							FTBRanks.LOGGER.error("Failed to parse condition for " + rank.id + ": " + ex);
						}
					}

					for (Map.Entry<String, JsonElement> pEntry : o.entrySet())
					{
						String key = pEntry.getKey();

						while (key.endsWith(".*"))
						{
							key = key.substring(0, key.length() - 2);
							saveRanks();
						}

						if (!key.isEmpty())
						{
							rank.permissions.put(key, ofJson(pEntry.getValue()));
						}
					}

					if (rank.name.isEmpty())
					{
						rank.name = rank.id;
						saveRanks();
					}

					tempRanks.put(rank.id, rank);
				}

				if (tempRanks.isEmpty())
				{
					FTBRanks.LOGGER.warn("No ranks found!");
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				throw new JsonSyntaxException("Couldn't parse serverconfig/ftbranks/ranks.json file! Error: " + ex.getLocalizedMessage());
			}
		}

		try (Reader reader = Files.newBufferedReader(playerFile))
		{
			try
			{
				JsonObject json = GSON.fromJson(reader, JsonObject.class);

				for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet())
				{
					JsonObject o = entry.getValue().getAsJsonObject();
					PlayerRankData data = new PlayerRankData(this, UUID.fromString(entry.getKey()));
					data.name = o.get("name").getAsString();

					for (Map.Entry<String, JsonElement> r : o.get("ranks").getAsJsonObject().entrySet())
					{
						RankImpl rank = tempRanks.get(r.getKey());

						if (rank != null)
						{
							data.added.put(rank, Instant.parse(r.getValue().getAsString()));
						}
					}

					if (o.has("permissions"))
					{
						for (Map.Entry<String, JsonElement> pEntry : o.get("permissions").getAsJsonObject().entrySet())
						{
							String key = pEntry.getKey();

							while (key.endsWith(".*"))
							{
								key = key.substring(0, key.length() - 2);
								savePlayers();
							}

							if (!key.isEmpty())
							{
								data.permissions.put(key, ofJson(pEntry.getValue()));
							}
						}
					}

					tempPlayerData.put(data.uuid, data);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				throw new JsonSyntaxException("Couldn't parse serverconfig/ftbranks/players.json file! Error: " + ex.getLocalizedMessage());
			}
		}

		ranks = new LinkedHashMap<>(tempRanks);
		playerData = new LinkedHashMap<>(tempPlayerData);

		sortedRanks = new ArrayList<>(ranks.values());
		sortedRanks.sort(null);

		FTBRanks.LOGGER.info("Loaded " + ranks.size() + " ranks");
	}

	public void saveRanksNow()
	{
		if (!shouldSaveRanks)
		{
			return;
		}

		shouldSaveRanks = false;

		JsonObject json = new JsonObject();

		for (RankImpl rank : ranks.values())
		{
			JsonObject o = new JsonObject();
			o.addProperty("name", rank.getName());
			o.addProperty("power", rank.getPower());

			if (!rank.condition.isDefaultCondition())
			{
				JsonObject c = new JsonObject();
				c.addProperty("type", rank.condition.getType());
				rank.condition.save(c);
				o.add("condition", c);
			}

			for (Map.Entry<String, PermissionValue> entry : rank.permissions.entrySet())
			{
				PermissionValue v = entry.getValue();

				if (v.isDefaultValue())
				{
					o.add(entry.getKey(), JsonNull.INSTANCE);
				}
				else if (v instanceof BooleanPermissionValue)
				{
					o.addProperty(entry.getKey(), ((BooleanPermissionValue) entry.getValue()).value);
				}
				else if (v instanceof StringPermissionValue)
				{
					o.addProperty(entry.getKey(), ((StringPermissionValue) entry.getValue()).value);
				}
				else if (v instanceof NumberPermissionValue)
				{
					o.addProperty(entry.getKey(), ((NumberPermissionValue) entry.getValue()).value);
				}
				else
				{
					o.addProperty(entry.getKey(), entry.getValue().asString().orElse(""));
				}
			}

			json.add(rank.id, o);
		}

		try (Writer writer = Files.newBufferedWriter(rankFile))
		{
			GSON.toJson(json, writer);
		}
		catch (Exception ex)
		{
			FTBRanks.LOGGER.warn("Failed to save ranks.json! Error: " + ex);
		}
	}

	public void savePlayersNow()
	{
		if (!shouldSavePlayers)
		{
			return;
		}

		shouldSavePlayers = false;

		JsonObject playerJson = new JsonObject();

		for (PlayerRankData data : playerData.values())
		{
			JsonObject o = new JsonObject();
			o.addProperty("name", data.name);

			JsonObject r = new JsonObject();

			for (Map.Entry<Rank, Instant> entry : data.added.entrySet())
			{
				if (entry.getKey().getCondition().isDefaultCondition())
				{
					r.addProperty(entry.getKey().getId(), entry.getValue().toString());
				}
			}

			o.add("ranks", r);

			if (!data.permissions.isEmpty())
			{
				JsonObject p = new JsonObject();

				for (Map.Entry<String, PermissionValue> entry : data.permissions.entrySet())
				{
					PermissionValue v = entry.getValue();

					if (v.isDefaultValue())
					{
						p.add(entry.getKey(), JsonNull.INSTANCE);
					}
					else if (v instanceof BooleanPermissionValue)
					{
						p.addProperty(entry.getKey(), ((BooleanPermissionValue) entry.getValue()).value);
					}
					else if (v instanceof StringPermissionValue)
					{
						p.addProperty(entry.getKey(), ((StringPermissionValue) entry.getValue()).value);
					}
					else if (v instanceof NumberPermissionValue)
					{
						p.addProperty(entry.getKey(), ((NumberPermissionValue) entry.getValue()).value);
					}
					else
					{
						p.addProperty(entry.getKey(), entry.getValue().asString().orElse(""));
					}
				}

				o.add("permissions", p);
			}

			playerJson.add(data.uuid.toString(), o);
		}

		try (Writer writer = Files.newBufferedWriter(playerFile))
		{
			GSON.toJson(playerJson, writer);
		}
		catch (Exception ex)
		{
			FTBRanks.LOGGER.warn("Failed to save players.json! Error: " + ex);
		}
	}

	private static PermissionValue ofJson(@Nullable JsonElement v)
	{
		if (v == null || v instanceof JsonNull)
		{
			return PermissionValue.DEFAULT;
		}
		else if (v instanceof JsonPrimitive)
		{
			if (((JsonPrimitive) v).isBoolean())
			{
				return BooleanPermissionValue.of(v.getAsBoolean());
			}
			else if (((JsonPrimitive) v).isNumber())
			{
				return NumberPermissionValue.of(v.getAsNumber());
			}

			return StringPermissionValue.of(v.getAsString());
		}

		return StringPermissionValue.of(v.toString());
	}

	private void getCommandNodes(CommandDispatcher<CommandSource> dispatcher, String perm, Field field, CommandNode<CommandSource> node) throws Exception
	{
		for (CommandNode<CommandSource> c : node.getChildren())
		{
			if (c.isFork())
			{
				continue;
			}

			String n = perm + "." + c.getName().replace("*", "all");
			FTBRanks.LOGGER.debug(n);
			RankCommandPredicate predicate = new RankCommandPredicate(c, n);
			field.set(c, predicate);
			commands.put(n, predicate);
			commandNodes.put(c, predicate);
			getCommandNodes(dispatcher, n, field, c);

			if (c.getRedirect() != null && c.getRedirect() != dispatcher.getRoot())
			{
				predicate.redirect = () -> commandNodes.get(c.getRedirect());
			}
		}
	}
}