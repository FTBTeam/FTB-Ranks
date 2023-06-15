package dev.ftb.mods.ftbranks.impl;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil;
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
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static dev.ftb.mods.ftbranks.FTBRanks.MOD_ID;

/**
 * @author LatvianModder
 */
public class RankManagerImpl implements RankManager {
	public static final LevelResource FOLDER_NAME = new LevelResource("serverconfig/ftbranks");
	private static final Path DEFAULT_RANK_FILE = ConfigUtil.DEFAULT_CONFIG_DIR.resolve(MOD_ID).resolve("ranks.snbt");

	private final MinecraftServer server;
	private final Path directory;
	private final Path rankFile;
	private final Path playerFile;

	private boolean shouldSaveRanks;
	private boolean shouldSavePlayers;

	private Map<String, RankImpl> ranks;
	private final List<Rank> sortedRanks = new ArrayList<>();
	private final Map<String, RankConditionFactory> conditions = new ConcurrentHashMap<>();
	private Map<UUID, PlayerRankData> playerData;

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
	public RankImpl createRank(String id, String name, int power) {
		deleteRank(id);
		RankImpl rank = RankImpl.create(this, id, name, power);
		ranks.put(id, rank);
		rebuildSortedRanks();
		markRanksDirty();
		RankEvent.CREATED.invoker().accept(new RankCreatedEvent(this, rank));
		return rank;
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
	public Set<Rank> getAddedRanks(GameProfile profile) {
		return getOrCreatePlayerData(profile).addedRanks();
	}

	@Override
	public RankCondition createCondition(Rank rank, @Nullable Tag tag) throws RankException {
		SNBTCompoundTag compoundTag = new SNBTCompoundTag();
		if (tag instanceof StringTag) {
			compoundTag.putString("type", tag.getAsString());
		} else if (tag instanceof SNBTCompoundTag c) {
			compoundTag = c;
		}
		String key = compoundTag.getString("type");
		if (!conditions.containsKey(key)) {
			throw new IllegalArgumentException("Can't create condition from tag: '" + tag + "'");
		}
		return conditions.get(key).create(rank, compoundTag);
	}

	@Override
	@NotNull
	public PermissionValue getPermissionValue(ServerPlayer player, String node) {
		if (node.isEmpty() || sortedRanks.isEmpty()) {
			return PermissionValue.MISSING;
		}

		try {
			List<Rank> list = sortedRanks.stream().filter(rank -> rank.isActive(player)).collect(Collectors.toList());
			return getPermissionValue(getOrCreatePlayerData(player.getGameProfile()), list, node);
		} catch (Exception ex) {
			FTBRanks.LOGGER.error("Error getting permission value for node " + node + "!");
			ex.printStackTrace();
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

	public void reload() throws Exception {
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
		SNBTCompoundTag rankFileTag = SNBT.read(rankFile);
		if (rankFileTag != null) {
			for (String rankId : rankFileTag.getAllKeys()) {
				try {
					RankImpl rank = RankImpl.readSNBT(this, rankId, rankFileTag.getCompound(rankId));
					tempRanks.put(rank.getId(), rank);
				} catch (RankException e) {
					FTBRanks.LOGGER.error("Failed to read rank {} from SNBT: {}", rankId, e.getMessage());
				}
			}
			if (tempRanks.isEmpty()) {
				FTBRanks.LOGGER.warn("No ranks found!");
			}
		} else {
			throw new RuntimeException("ranks.snbt failed to load! check your server log for errors");
		}

		Map<UUID, PlayerRankData> tempPlayerData = new LinkedHashMap<>();
		SNBTCompoundTag playerFileTag = SNBT.read(playerFile);
		if (playerFileTag != null) {
			for (String key : playerFileTag.getAllKeys()) {
				SNBTCompoundTag o = playerFileTag.getCompound(key);
				UUID id = UUID.fromString(key);
				PlayerRankData data = PlayerRankData.fromSNBT(this, id, o, tempRanks);
				tempPlayerData.put(id, data);
			}
		} else {
			throw new RuntimeException("players.snbt failed to load! check your server log for errors");
		}

		ranks = new LinkedHashMap<>(tempRanks);
		playerData = new LinkedHashMap<>(tempPlayerData);

		rebuildSortedRanks();

		RankEvent.RELOADED.invoker().accept(new RanksReloadedEvent(FTBRanksAPI.manager()));

		PlayerNameFormatting.refreshPlayerNames();

		FTBRanks.LOGGER.info("Loaded " + ranks.size() + " ranks");
	}

	private void createDefaultRanks() {
		ranks = new LinkedHashMap<>();

		RankImpl memberRank = RankImpl.create(this, "member", "Member", 1, AlwaysActiveCondition.INSTANCE);
		ranks.put("member", memberRank);

		RankImpl vipRank = RankImpl.create(this, "vip", "VIP", 50);
		vipRank.setPermission("ftbranks.name_format", StringPermissionValue.of("&bVIP {name}"));
		ranks.put("vip", vipRank);

		RankImpl adminRank = RankImpl.create(this, "admin", "Admin", 1000, new OPCondition());
		adminRank.setPermission("ftbranks.name_format", StringPermissionValue.of("&2{name}"));
		ranks.put("admin", adminRank);

		markRanksDirty();
		saveRanksNow();
	}

	public void refreshReadme() throws IOException {
		List<String> lines = new ArrayList<>();
		lines.add("=== FTB Ranks ===");
		lines.add("");
		lines.add("Last README file update: " + new Date());
		lines.add("Wiki: https://www.notion.so/feedthebeast/FTB-Mod-Documentation-da2e359bad2449459d58d787edda3168");
		lines.add("To refresh this file, run /ftbranks refresh_readme");
		lines.add("");
		lines.add("= All available command nodes =");
		lines.add("command");

		List<String> commandList = FTBRanksCommandManager.INSTANCE.commandMap.values().stream()
				.map(RankCommandPredicate::getNodeName)
				.distinct()
				.sorted()
				.toList();
		lines.addAll(commandList);

		Files.write(directory.resolve("README.txt"), lines);
	}

	private void rebuildSortedRanks() {
		sortedRanks.clear();
		sortedRanks.addAll(ranks.values().stream().sorted().toList());
	}

	PlayerRankData getOrCreatePlayerData(GameProfile profile) {
		PlayerRankData data = playerData.get(profile.getId());

		if (data == null) {
			data = new PlayerRankData(this, profile.getId(), profile.getName());
			playerData.put(profile.getId(), data);
			markRanksDirty();
		}

		return data;
	}

	void registerCondition(String id, RankConditionFactory conditionFactory) {
		if (conditions.putIfAbsent(id, conditionFactory) != null) {
			FTBRanks.LOGGER.warn("condition {} already registered - ignoring attempt to overwrite", id);
		}
	}

	void load() throws Exception {
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
			SNBTCompoundTag tag = new SNBTCompoundTag();
			for (RankImpl rank : ranks.values()) {
				tag.put(rank.getId(), rank.writeSNBT());
			}
			if (!SNBT.write(rankFile, tag)) {
				FTBRanks.LOGGER.warn("Failed to save ranks.snbt!");
			}
			shouldSaveRanks = false;
		}
	}

	void savePlayersNow() {
		if (shouldSavePlayers) {
			SNBTCompoundTag playerTag = new SNBTCompoundTag();
			for (PlayerRankData data : playerData.values()) {
				playerTag.put(data.getPlayerId().toString(), data.writeSNBT());
			}

			if (!SNBT.write(playerFile, playerTag)) {
				FTBRanks.LOGGER.warn("Failed to save players.snbt!");
			}
			shouldSavePlayers = false;
		}
	}

	static PermissionValue ofTag(SNBTCompoundTag tag, String key) {
		if (tag.isBoolean(key)) {
			return BooleanPermissionValue.of(tag.getBoolean(key));
		}

		Tag v = tag.get(key);

		if (v == null || v instanceof EndTag) {
			return PermissionValue.MISSING;
		} else if (v instanceof NumericTag) {
			return NumberPermissionValue.of(((NumericTag) v).getAsNumber());
		} else if (v instanceof StringTag) {
			return StringPermissionValue.of(v.getAsString());
		}

		return StringPermissionValue.of(v.toString());
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