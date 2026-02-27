package dev.ftb.mods.ftbranks.impl;

import dev.ftb.mods.ftblibrary.config.ConfigUtil;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import dev.ftb.mods.ftbranks.api.*;
import dev.ftb.mods.ftbranks.api.event.RankCreatedEvent;
import dev.ftb.mods.ftbranks.api.event.RankDeletedEvent;
import dev.ftb.mods.ftbranks.api.event.RankEvent;
import dev.ftb.mods.ftbranks.api.event.RanksReloadedEvent;
import dev.ftb.mods.ftbranks.impl.condition.AlwaysActiveCondition;
import dev.ftb.mods.ftbranks.impl.condition.OPCondition;
import dev.ftb.mods.ftbranks.impl.permission.BooleanPermissionValue;
import dev.ftb.mods.ftbranks.impl.permission.NumberPermissionValue;
import dev.ftb.mods.ftbranks.impl.permission.StringPermissionValue;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
	private static final Path DEFAULT_RANK_FILE = ConfigUtil.DEFAULT_CONFIG_DIR.resolve(MOD_ID).resolve("ranks.snbt");

	private final MinecraftServer server;
	private final Path directory;
	private final Path rankFile;
	private final Path playerFile;

	private boolean shouldSaveRanks;
	private boolean shouldSavePlayers;

	private Map<String, RankImpl> ranks = new HashMap<>();
	private final List<Rank> sortedRanks = new ArrayList<>();
	private final Map<String, RankConditionFactory> conditions = new ConcurrentHashMap<>();
	private Map<UUID, PlayerRankData> playerData = new HashMap<>();

	public RankManagerImpl(MinecraftServer server) {
		this.server = server;

		directory = server.getWorldPath(FOLDER_NAME);
		rankFile = directory.resolve("ranks.snbt");
		playerFile = directory.resolve("players.snbt");
	}

	public void markRanksDirty() {
		shouldSaveRanks = true;
	}

	public void markPlayerDataDirty() {
		shouldSavePlayers = true;
	}

	@Override
	public Collection<Rank> getAllRanks() {
		return sortedRanks;
	}

	@Override
	public Optional<Rank> getRank(String id) {
		return Optional.ofNullable(ranks.get(id));
	}

	@Override
	public RankImpl createRank(String name, int power, boolean forceCreate) {
		String id = normalizeRankName(name);

		if (forceCreate) {
			if (deleteRank(id) != null) {
				LOGGER.warn("forcibly overwriting existing rank {}", name);
			}
		} else if (ranks.containsKey(id)) {
			throw new RankException("Rank '" + id + "' already exists");
		}

		RankImpl rank = RankImpl.create(this, id, name, power, RankFileSource.SERVER);
		ranks.put(id, rank);
		rebuildSortedRanks();
		markRanksDirty();
		RankEvent.CREATED.invoker().accept(new RankCreatedEvent(this, rank));
		return rank;
	}

	private static String normalizeRankName(String name) {
		return name.toLowerCase()
				.replace("+", "_plus")
				.replaceAll("[^a-z0-9_]", "_")
				.replaceAll("_{2,}", "_");
	}

	@Override
	@Nullable
	public RankImpl deleteRank(String id) {
		RankImpl rank = ranks.get(id);

		if (rank != null) {
			for (PlayerRankData rankData : playerData.values()) {
				if (rankData.removeRank(rank)) {
					markPlayerDataDirty();
				}
			}

			ranks.remove(id);

			rebuildSortedRanks();

			RankEvent.DELETED.invoker().accept(new RankDeletedEvent(this, rank));
			markRanksDirty();
		}

		return rank;
	}

	@Override
	public Set<Rank> getAddedRanks(NameAndId nameAndId) {
		return getOrCreatePlayerData(nameAndId).addedRanks();
	}

	@Override
	public RankCondition createCondition(Rank rank, @Nullable Tag tag) throws RankException {
		SNBTCompoundTag compoundTag = new SNBTCompoundTag();
		if (tag instanceof StringTag) {
			compoundTag.putString("type", tag.asString().orElseThrow());
		} else if (tag instanceof SNBTCompoundTag c) {
			compoundTag = c;
		}
		String key = compoundTag.getStringOr("type", "");
		if (!conditions.containsKey(key)) {
			throw new IllegalArgumentException("Can't create condition from tag: '" + tag + "'");
		}
		return conditions.get(key).create(rank, compoundTag);
	}

	@Override
	public PermissionValue getPermissionValue(ServerPlayer player, String node) {
		if (node.isEmpty() || sortedRanks.isEmpty()) {
			return PermissionValue.MISSING;
		}

		try {
			List<Rank> list = sortedRanks.stream().filter(rank -> rank.isActive(player)).collect(Collectors.toList());
			return getPermissionValue(getOrCreatePlayerData(player.nameAndId()), list, node);
		} catch (Exception ex) {
			FTBRanks.LOGGER.error("Error getting permission value for node {}! {} / {}", node, ex.getClass().getName(), ex.getMessage());
		}

		return PermissionValue.MISSING;
	}

	@Override
	public MinecraftServer getServer() {
		return server;
	}

	private PermissionValue getPermissionValue(PlayerRankData data, List<Rank> ranks, String node) {
		if (node.isEmpty()) {
			return PermissionValue.MISSING;
		}

		PermissionValue value = data.getPermission(node);
		if (!value.isEmpty()) {
			return value;
		}

		for (Rank rank : ranks) {
			PermissionValue value1 = rank.getPermission(node);
			if (!value1.isEmpty()) {
				return value1;
			}
		}

		int i = node.lastIndexOf('.');
		return i == -1 ? PermissionValue.MISSING : getPermissionValue(data, ranks, node.substring(0, i));
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

		Map<String, RankImpl> tempRanks = new LinkedHashMap<>();
		readRankFile(RankFileSource.SERVER, tempRanks);
		if (Files.exists(RankFileSource.MODPACK.getPath(server))) {
			readRankFile(RankFileSource.MODPACK, tempRanks);
		}

		Map<UUID, PlayerRankData> tempPlayerData = new LinkedHashMap<>();
		SNBTCompoundTag playerFileTag = SNBT.tryRead(playerFile);
		for (String key : playerFileTag.keySet()) {
			SNBTCompoundTag o = playerFileTag.getAsSnbtComponent(key);
			UUID id = UUID.fromString(key);
			PlayerRankData data = PlayerRankData.fromSNBT(this, id, o, tempRanks);
			tempPlayerData.put(id, data);
		}

		ranks = new LinkedHashMap<>(tempRanks);
		playerData = new LinkedHashMap<>(tempPlayerData);

		rebuildSortedRanks();

		RankEvent.RELOADED.invoker().accept(new RanksReloadedEvent(FTBRanksAPI.manager()));

		PlayerNameFormatting.refreshPlayerNames();

		FTBRanks.LOGGER.info("Loaded {} ranks", ranks.size());
	}

	private void readRankFile(RankFileSource source, Map<String, RankImpl> rankMap) throws IOException {
		Path inputFile = source.getPath(server);
		SNBTCompoundTag rankFileTag = SNBT.tryRead(inputFile);
		int size = rankMap.size();
		for (String rankId : rankFileTag.keySet()) {
			try {
				RankImpl rank = RankImpl.readSNBT(this, rankId, rankFileTag.getAsSnbtComponent(rankId), source);
				if (rankMap.putIfAbsent(rank.getId(), rank) != null) {
					FTBRanks.LOGGER.warn("Conflicting rank ID '{}' detected while reading {}, ignoring", rank.getId(), inputFile);
				}
			} catch (RankException e) {
				FTBRanks.LOGGER.error("Failed to read rank ID '{}' from {}: {}", rankId, inputFile, e.getMessage());
			}
		}
		if (rankMap.size() == size) {
			FTBRanks.LOGGER.warn("No ranks found in {}!", inputFile);
		}
	}

	private void createDefaultRanks() {
		ranks = new LinkedHashMap<>();

		RankImpl memberRank = RankImpl.create(this, "member", "Member", 1, AlwaysActiveCondition.INSTANCE, RankFileSource.SERVER);
		ranks.put("member", memberRank);

		RankImpl vipRank = RankImpl.create(this, "vip", "VIP", 50, RankFileSource.SERVER);
		vipRank.setPermission("ftbranks.name_format", StringPermissionValue.of("&bVIP {name}"));
		ranks.put("vip", vipRank);

		RankImpl adminRank = RankImpl.create(this, "admin", "Admin", 1000, new OPCondition(), RankFileSource.SERVER);
		adminRank.setPermission("ftbranks.name_format", StringPermissionValue.of("&2{name}"));
		ranks.put("admin", adminRank);

		markRanksDirty();
		saveRanksNow();
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

	private void rebuildSortedRanks() {
		sortedRanks.clear();
		sortedRanks.addAll(ranks.values().stream().sorted().toList());
	}

	PlayerRankData getOrCreatePlayerData(NameAndId profile) {
		PlayerRankData data = playerData.get(profile.id());

		if (data == null) {
			data = new PlayerRankData(this, profile.id(), profile.name());
			playerData.put(profile.id(), data);
			markRanksDirty();
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
			Map<RankFileSource,SNBTCompoundTag> map = new EnumMap<>(RankFileSource.class);
			for (RankImpl rank : ranks.values()) {
				map.computeIfAbsent(rank.getSource(), k -> new SNBTCompoundTag())
						.put(rank.getId(), rank.writeSNBT());
			}
			map.forEach((source, tag) -> {
				try {
					SNBT.tryWrite(source.getPath(server), tag);
				} catch (IOException e) {
					FTBRanks.LOGGER.warn("Failed to save {}}! {} / {}", source.getPath(server), e.getClass().getName(), e.getMessage());
				}
			});
			shouldSaveRanks = false;
		}
	}

	void savePlayersNow() {
		if (shouldSavePlayers) {
			SNBTCompoundTag playerTag = new SNBTCompoundTag();
			for (PlayerRankData data : playerData.values()) {
				playerTag.put(data.getPlayerId().toString(), data.writeSNBT());
			}

			try {
				SNBT.tryWrite(playerFile, playerTag);
			} catch (IOException e) {
				FTBRanks.LOGGER.warn("Failed to save players.snbt! {} / {}", e.getClass().getName(), e.getMessage());
			}
			shouldSavePlayers = false;
		}
	}

	static PermissionValue ofTag(SNBTCompoundTag tag, String key) {
		if (tag.isBoolean(key)) {
			return BooleanPermissionValue.of(tag.getBooleanOr(key, false)); // TODO: False default might not be ideal
		}

		Tag v = tag.get(key);

		return switch (v) {
			case null -> PermissionValue.MISSING;
			case EndTag ignored -> PermissionValue.MISSING;
			case NumericTag n -> NumberPermissionValue.of(n.asNumber().orElseThrow());
			case StringTag s -> StringPermissionValue.of(s.asString().orElseThrow());
			default -> StringPermissionValue.of(v.toString());
		};

	}

	static SNBTCompoundTag writePermissions(Map<String, PermissionValue> map, SNBTCompoundTag res) {
		map.forEach((key, value) -> {
			if (value.isEmpty()) {
				res.putNull(key);
			} else if (value instanceof BooleanPermissionValue b) {
				res.putBoolean(key, b.value);
			} else if (value instanceof StringPermissionValue s) {
				res.putString(key, s.value);
			} else if (value instanceof NumberPermissionValue n) {
				res.putNumber(key, n.value);
			} else {
				res.putString(key, value.asString().orElse(""));
			}
		});
		return res;
	}
}
