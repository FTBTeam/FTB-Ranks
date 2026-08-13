package dev.ftb.mods.ftbranks.impl;

import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.config.ConfigUtil;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import dev.ftb.mods.ftbranks.api.*;
import dev.ftb.mods.ftbranks.api.event.RankCreatedEvent;
import dev.ftb.mods.ftbranks.api.event.RankDeletedEvent;
import dev.ftb.mods.ftbranks.api.event.RanksReloadedEvent;
import dev.ftb.mods.ftbranks.impl.condition.AlwaysActiveCondition;
import dev.ftb.mods.ftbranks.impl.condition.OPCondition;
import dev.ftb.mods.ftbranks.impl.permission.StringPermissionValue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static dev.ftb.mods.ftbranks.FTBRanks.LOGGER;
import static dev.ftb.mods.ftbranks.FTBRanks.MOD_ID;

public class RankManagerImpl implements RankManager {
	public static final LevelResource FOLDER_NAME = new LevelResource("serverconfig/ftbranks");
	private static final Path DEFAULT_RANK_FILE = ConfigUtil.DEFAULT_CONFIG_DIR.resolve(MOD_ID).resolve("ranks.json5");

	private final MinecraftServer server;
	private final Path directory;
	private final Path rankFile;
	private final Path playerFile;

	private boolean shouldSaveRanks;
	private boolean shouldSavePlayers;

	private Map<NamespacedRankId, RankImpl> ranks = new HashMap<>();
	private final List<RankImpl> sortedRanks = new ArrayList<>();
	private final List<RankImpl> sortedServerRanks = new ArrayList<>();
	private final Map<String, RankConditionFactory> conditions = new ConcurrentHashMap<>();
	private Map<UUID, PlayerRankData> playerData = new HashMap<>();

	public RankManagerImpl(MinecraftServer server) {
		this.server = server;

		directory = server.getWorldPath(FOLDER_NAME);
		rankFile = directory.resolve("ranks.json5");
		playerFile = directory.resolve("players.json5");
	}

	public void markRanksDirty() {
		shouldSaveRanks = true;
	}

	public void markPlayerDataDirty() {
		shouldSavePlayers = true;
	}

	@Override
	public Collection<? extends Rank> getAllRanks() {
		return sortedRanks;
	}

	@Override
	public Collection<? extends Rank> getAllServerRanks() {
		return sortedServerRanks;
	}

	@Override
	public Optional<Rank> getRank(String id) {
		return NamespacedRankId.fromString(id).map(nsId -> ranks.get(nsId));
	}

	@Override
	public RankImpl createRank(String name, int power, boolean forceCreate) {
		String id = normalizeRankId(name);

		if (forceCreate) {
			if (deleteRank(id) != null) {
				LOGGER.warn("forcibly overwriting existing rank {}", name);
			}
		} else if (ranks.containsKey(NamespacedRankId.serverRank(id))) {
			throw new RankException("Rank '" + id + "' already exists");
		}

		RankImpl rank = RankImpl.create(this, id, name, power, RankFileSource.SERVER);
		ranks.put(rank.getNamespacedId(), rank);
		rebuildSortedRanks();
		markRanksDirty();
		NativeEventPosting.get().postEvent(new RankCreatedEvent.Data(this, rank));
		return rank;
	}

	@Override
	@Nullable
	public RankImpl deleteRank(String id) {
		NamespacedRankId nsId = NamespacedRankId.serverRank(id);
		RankImpl rank = ranks.get(nsId);

		if (rank != null) {
			if (rank.getSource() == RankFileSource.MODPACK) {
				// shouldn't ever happen
				throw new RankException("cannot delete a modpack-loaded rank");
			}

			for (PlayerRankData rankData : playerData.values()) {
				if (rankData.removeRank(rank)) {
					markPlayerDataDirty();
				}
			}

			ranks.remove(nsId);

			rebuildSortedRanks();

			NativeEventPosting.get().postEvent(new RankDeletedEvent.Data(this, rank));
			markRanksDirty();
		}

		return rank;
	}

	@Override
	public Set<Rank> getAddedRanks(NameAndId nameAndId) {
		return getOrCreatePlayerData(nameAndId).addedRanks();
	}

	@Override
	public RankCondition createCondition(Rank rank, Json5Element element) throws RankException {
		Json5Object json = new Json5Object();
        if (element.isJson5Primitive()) {
            json.addProperty("type", element.getAsString());
        } else if (element.isJson5Object()) {
            json = element.getAsJson5Object();
        }
        String key = Json5Util.getString(json, "type").orElse("");
		if (!conditions.containsKey(key)) {
			throw new IllegalArgumentException("Can't create condition from tag: '" + element + "'");
		}
		return conditions.get(key).create(rank, json);
	}

	@Override
	public PermissionValue getPermissionValue(ServerPlayer player, String node) {
		if (node.isEmpty() || sortedRanks.isEmpty()) {
			return PermissionValue.MISSING;
		}

		try {
			List<Rank> list = sortedRanks.stream().filter(rank -> rank.isActive(player)).collect(Collectors.toList());
			return getPermissionValue(list, node);
		} catch (Exception ex) {
			FTBRanks.LOGGER.error("Error getting permission value for node {}! {} / {}", node, ex.getClass().getName(), ex.getMessage());
		}

		return PermissionValue.MISSING;
	}

	private PermissionValue getPermissionValue(List<Rank> ranks, String node) {
		for (Rank rank : ranks) {
			PermissionValue value = rank.getPermission(node);
			if (!value.isEmpty()) {
				return value;
			}
		}

		int i = node.lastIndexOf('.');
		return i == -1 ? PermissionValue.MISSING : getPermissionValue(ranks, node.substring(0, i));
	}

	@Override
	public MinecraftServer getServer() {
		return server;
	}

	public void reload() throws IOException {
		shouldSaveRanks = false;

		if (Files.notExists(rankFile)) {
			if (Files.exists(DEFAULT_RANK_FILE)) {
				Files.copy(DEFAULT_RANK_FILE, rankFile);
			} else {
				createDefaultRanks();
			}
		}

		if (Files.notExists(playerFile)) {
			playerData = new HashMap<>();
			markPlayerDataDirty();
			savePlayersNow();
		}

		Map<NamespacedRankId, RankImpl> tempRanks = new LinkedHashMap<>();
		readRankFile(RankFileSource.SERVER, tempRanks);
		if (Files.exists(RankFileSource.MODPACK.getPath(server))) {
			readRankFile(RankFileSource.MODPACK, tempRanks);
		} else {
			createDefaultModpackRanks(RankFileSource.MODPACK.getPath(server));
		}

		Map<UUID, PlayerRankData> tempPlayerData = new LinkedHashMap<>();
		var playerFileTag = Json5Util.load(playerFile);
		for (String key : playerFileTag.keySet()) {
			var el = playerFileTag.get(key);
			if (el.isJson5Object()) {
				UUID id = UUID.fromString(key);
				PlayerRankData data = PlayerRankData.fromJson(this, id, el.getAsJson5Object(), tempRanks);
				tempPlayerData.put(id, data);
			}
		}

		ranks = new LinkedHashMap<>(tempRanks);
		playerData = new LinkedHashMap<>(tempPlayerData);

		rebuildSortedRanks();

		NativeEventPosting.get().postEvent(new RanksReloadedEvent.Data(FTBRanksAPI.manager()));

		PlayerNameFormatting.refreshPlayerNames(this.server);

		FTBRanks.LOGGER.info("Loaded {} ranks", ranks.size());
	}

	private void readRankFile(RankFileSource source, Map<NamespacedRankId, RankImpl> rankMap) throws IOException {
		Path inputFile = source.getPath(server);
		Json5Object json = Json5Util.load(inputFile);
		int size = rankMap.size();
		for (String key : json.keySet()) {
			String normalizedId = normalizeRankId(key);
			if (!key.equals(normalizedId)) {
				LOGGER.warn("Normalized rank id '{}.{}' -> '{}'", source.getId(), key, normalizedId);
				markRanksDirty();
			}
			try {
				RankImpl rank = RankImpl.fromJson(this, normalizedId, json.getAsJson5Object(key), source);
				if (rankMap.containsKey(rank.getNamespacedId())) {
					// should never happen, but normalization could conceivably cause it
					FTBRanks.LOGGER.warn("Conflicting rank ID '{}' detected while reading {}, overwriting existing rank", rank.getId(), inputFile);
				}
				rankMap.put(rank.getNamespacedId(), rank);
			} catch (RankException e) {
				FTBRanks.LOGGER.error("Failed to read rank ID '{}' from {}: {}", normalizedId, inputFile, e.getMessage());
				throw new IOException(e);  // re-throw: any failure to read a rank should stop the whole file being read
			}
		}
		if (rankMap.size() == size) {
			FTBRanks.LOGGER.warn("No ranks found in {}!", inputFile);
		}
	}

	private void createDefaultRanks() {
		ranks = new LinkedHashMap<>();

		RankImpl memberRank = RankImpl.create(this, "member", "Member", 1, AlwaysActiveCondition.INSTANCE, RankFileSource.SERVER);
		ranks.put(memberRank.getNamespacedId(), memberRank);

		RankImpl vipRank = RankImpl.create(this, "vip", "VIP", 50, RankFileSource.SERVER);
		vipRank.setPermission("ftbranks.name_format", StringPermissionValue.of("&bVIP {name}"));
		ranks.put(vipRank.getNamespacedId(), vipRank);

		RankImpl adminRank = RankImpl.create(this, "admin", "Admin", 1000, new OPCondition(), RankFileSource.SERVER);
		adminRank.setPermission("ftbranks.name_format", StringPermissionValue.of("&2{name}"));
		ranks.put(adminRank.getNamespacedId(), adminRank);

		markRanksDirty();
		saveRanksNow();
	}

	private void createDefaultModpackRanks(Path path) throws IOException {
		Json5Object json = new Json5Object();
		json.setComment("""
                DO NOT EDIT THIS FILE UNLESS YOU ARE DEVELOPING A MODPACK!
                
                This file is used to hold modpack-specific ranks important to the correct
                 functioning of the pack and may be overwritten whenever the modpack is updated.
                
                If you are a server admin who needs to add or edit ranks, then either:
                 * carefully edit <world>/serverconfig/ftbranks/ranks.json5
                 * or use "/ftbranks ..." commands to make your changes
                
                Note to pack developers: rank ID's you add here will not clash with ranks added locally
                 in <world>/serverconfig/ftbranks/ranks.json5, since they are separately namespaced."""
		);
		Json5Util.save(path, json);
	}

	public void refreshReadme() throws IOException {
		List<String> lines = new ArrayList<>(List.of(
				"=== FTB Ranks ===",
				"",
				"Last README file update: " + new Date(),
				"Wiki: https://docs.feed-the-beast.com/mod-docs/mods/suite/Ranks/",
				"To refresh this file, run /ftbranks refresh_readme",
				"",
				"= All available command nodes =",
				"command"
		));
		lines.addAll(FTBRanksCommandManager.allNodes());

		Files.write(directory.resolve("README.txt"), lines);
	}

	void rebuildSortedRanks() {
		sortedRanks.clear();
		sortedRanks.addAll(ranks.values().stream().sorted().toList());
		sortedServerRanks.clear();
		sortedServerRanks.addAll(sortedRanks.stream().filter(r -> r.getSource() == RankFileSource.SERVER).toList());
	}

	PlayerRankData getOrCreatePlayerData(NameAndId profile) {
		PlayerRankData data = playerData.get(profile.id());

		if (data == null) {
			data = new PlayerRankData(this, profile.id(), profile.name());
			playerData.put(profile.id(), data);
			markPlayerDataDirty();
		}

		return data;
	}

	void registerCondition(String id, RankConditionFactory conditionFactory) {
		if (conditions.putIfAbsent(id, conditionFactory) != null) {
			FTBRanks.LOGGER.warn("condition {} already registered - ignoring attempt to overwrite", id);
		}
	}

	void load() throws IOException {
		if (Files.notExists(directory)) {
			Files.createDirectories(directory);
		}

		if (Files.notExists(directory.resolve("README.txt"))) {
			refreshReadme();
		}

		reload();
	}

	void saveRanksNow() {
		if (shouldSaveRanks) {
			Map<RankFileSource, Json5Object> map = new EnumMap<>(RankFileSource.class);
			for (RankImpl rank : ranks.values()) {
				map.computeIfAbsent(rank.getSource(), _ -> new Json5Object())
						.add(rank.getId(), rank.toJson());
			}
			map.forEach((source, json) -> {
				try {
					Json5Util.save(source.getPath(server), (Json5Element) json);
					shouldSaveRanks = false;
				} catch (IOException e) {
					FTBRanks.LOGGER.error("Failed to save {}}! {} / {}", source.getPath(server), e.getClass().getName(), e.getMessage());
				}
			});
		}
	}

	void savePlayersNow() {
		if (shouldSavePlayers) {
			Json5Object playerTag = new Json5Object();
			for (PlayerRankData data : playerData.values()) {
				playerTag.add(data.getPlayerId().toString(), data.toJson());
			}

			try {
				Json5Util.save(playerFile, (Json5Element) playerTag);
				shouldSavePlayers = false;
			} catch (IOException e) {
				FTBRanks.LOGGER.error("Failed to save players.json5! {} / {}", e.getClass().getName(), e.getMessage());
			}
		}
	}

	/// Normalize a rank display name (or rank ID loaded from file):
	/// * convert to lower case
	/// * replace "+" with "\_plus"
	/// * replace all non-alphanumerics with "\_"
	/// * contract consecutive "\_" occurrences into a single "\_".
	///
	/// This normalized ID is used for the canonical unique rank ID.
	///
	/// @param in the input ID or name
	/// @return the normalized ID
	private static String normalizeRankId(String in) {
		return in.toLowerCase(Locale.ROOT)
				.replace("+", "_plus")
				.replaceAll("[^a-z0-9_]", "_")
				.replaceAll("_{2,}", "_");
	}

}
