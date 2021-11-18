package dev.ftb.mods.ftbranks.impl;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftblibrary.snbt.config.ConfigUtil;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankConditionFactory;
import dev.ftb.mods.ftbranks.api.RankManager;
import dev.ftb.mods.ftbranks.impl.condition.AlwaysActiveCondition;
import dev.ftb.mods.ftbranks.impl.condition.OPCondition;
import me.shedaniel.architectury.hooks.LevelResourceHooks;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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

import static dev.ftb.mods.ftbranks.FTBRanks.MOD_ID;

/**
 * @author LatvianModder
 */
public class RankManagerImpl implements RankManager {
	public static final LevelResource FOLDER_NAME = LevelResourceHooks.create("serverconfig/ftbranks");

	public final MinecraftServer server;

	private Path directory;
	private Path rankFile;
	private Path playerFile;

	private final Path defaultRankFile = ConfigUtil.DEFAULT_CONFIG_DIR.resolve(MOD_ID).resolve("ranks.snbt");

	boolean shouldSaveRanks;
	boolean shouldSavePlayers;

	private Map<String, RankImpl> ranks;
	private List<RankImpl> sortedRanks;
	private final Map<String, RankConditionFactory> conditions;
	Map<UUID, PlayerRankData> playerData;

	public RankManagerImpl(MinecraftServer s) {
		server = s;
		conditions = new HashMap<>();
	}

	void load() throws Exception {
		directory = server.getWorldPath(FOLDER_NAME);

		if (Files.notExists(directory)) {
			Files.createDirectories(directory);
		}

		rankFile = directory.resolve("ranks.snbt");
		playerFile = directory.resolve("players.snbt");

		Path oldRankFile = directory.resolve("ranks.json");
		Path oldPlayerFile = directory.resolve("players.json");
		boolean oldRankFileLoaded = false;
		boolean oldPlayerFileLoaded = false;

		if (Files.exists(oldRankFile)) {
			Files.move(oldRankFile, rankFile);
			oldRankFileLoaded = true;
		}

		if (Files.exists(oldPlayerFile)) {
			Files.move(oldPlayerFile, playerFile);
			oldPlayerFileLoaded = true;
		}

		if (oldRankFileLoaded || oldPlayerFileLoaded || Files.notExists(directory.resolve("README.txt"))) {
			refreshReadme();
		}

		reload();

		if (oldRankFileLoaded) {
			saveRanks();
			saveRanksNow();
		}

		if (oldPlayerFileLoaded) {
			savePlayers();
			savePlayersNow();
		}
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

		HashSet<String> set = new HashSet<>();

		for (RankCommandPredicate predicate : FTBRanksCommandManager.INSTANCE.commands.values()) {
			set.add(predicate.getNode());
		}

		List<String> commandList = new ArrayList<>(set);
		commandList.sort(null);
		lines.addAll(commandList);

		Files.write(directory.resolve("README.txt"), lines);
	}

	@Override
	public void saveRanks() {
		shouldSaveRanks = true;
	}

	@Override
	public void savePlayers() {
		shouldSavePlayers = true;
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public List<Rank> getAllRanks() {
		return (List<Rank>) (List) sortedRanks;
	}

	@Override
	public Optional<Rank> getRank(String id) {
		return Optional.ofNullable(ranks.get(id));
	}

	@Override
	public RankImpl createRank(String id) {
		deleteRank(id);
		RankImpl r = new RankImpl(this, id);
		ranks.put(id, r);
		saveRanks();
		return r;
	}

	public RankImpl createRank(String id, String name) {
		RankImpl rank = createRank(id);
		rank.name = name;
		saveRanks();
		return rank;
	}

	@Override
	@Nullable
	public RankImpl deleteRank(String id) {
		RankImpl r = ranks.get(id);

		if (r != null) {
			for (PlayerRankData rankData : playerData.values()) {
				if (rankData.added.remove(r) != null) {
					savePlayers();
				}
			}

			ranks.remove(id);
			saveRanks();
		}

		return r;
	}

	public PlayerRankData getPlayerData(GameProfile profile) {
		PlayerRankData data = playerData.get(profile.getId());

		if (data == null) {
			data = new PlayerRankData(this, profile.getId());
			data.name = profile.getName();
			playerData.put(data.uuid, data);
			saveRanks();
		}

		return data;
	}

	@Override
	public Set<Rank> getAddedRanks(GameProfile profile) {
		return getPlayerData(profile).added.keySet();
	}

	@Override
	public void registerCondition(String predicate, RankConditionFactory conditionFactory) {
		conditions.put(predicate, conditionFactory);
	}

	@Override
	public RankCondition createCondition(Rank rank, @Nullable Tag tag) throws Exception {
		if (tag instanceof StringTag) {
			SNBTCompoundTag tag1 = new SNBTCompoundTag();
			tag1.put("type", tag);
			return conditions.get(tag.getAsString()).create(rank, tag1);
		} else if (tag instanceof SNBTCompoundTag) {
			return conditions.get(((SNBTCompoundTag) tag).getString("type")).create(rank, (SNBTCompoundTag) tag);
		}

		throw new IllegalArgumentException("Can't create condition from tag " + tag);
	}

	@Override
	public PermissionValue getPermissionValue(ServerPlayer player, String node) {
		if (node.isEmpty() || sortedRanks == null || sortedRanks.isEmpty()) {
			return PermissionValue.DEFAULT;
		}

		try {
			List<RankImpl> list = new ArrayList<>();

			for (RankImpl rank : sortedRanks) {
				if (rank.isActive(player)) {
					list.add(rank);
				}
			}

			return getPermissionValue(getPlayerData(player.getGameProfile()), list, node);
		} catch (Exception ex) {
			FTBRanks.LOGGER.error("Error getting permission value for node " + node + "!");
			ex.printStackTrace();
		}

		return PermissionValue.DEFAULT;
	}

	private PermissionValue getPermissionValue(PlayerRankData data, List<RankImpl> ranks, String node) {
		if (node.isEmpty()) {
			return PermissionValue.DEFAULT;
		}

		PermissionValue pvalue = data.permissions.get(node);

		if (pvalue != null) {
			return pvalue;
		}

		for (RankImpl rank : ranks) {
			PermissionValue value = rank.permissions.get(node);

			if (value != null) {
				return value;
			}
		}

		int i = node.lastIndexOf('.');
		return i == -1 ? PermissionValue.DEFAULT : getPermissionValue(data, ranks, node.substring(0, i));
	}

	public void reload() throws Exception {
		shouldSaveRanks = false;

		if (Files.notExists(rankFile)) {
			if (Files.exists(defaultRankFile)) {
				Files.copy(defaultRankFile, rankFile);
			} else {
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
		}

		if (Files.notExists(playerFile)) {
			playerData = new HashMap<>();

			savePlayers();
			savePlayersNow();
		}

		LinkedHashMap<String, RankImpl> tempRanks = new LinkedHashMap<>();
		LinkedHashMap<UUID, PlayerRankData> tempPlayerData = new LinkedHashMap<>();

		SNBTCompoundTag rankFileTag = SNBT.read(rankFile);

		if (rankFileTag != null) {
			for (String key : rankFileTag.getAllKeys()) {
				RankImpl rank = new RankImpl(this, key);

				SNBTCompoundTag o = rankFileTag.getCompound(key);
				rank.name = o.getString("name");
				rank.power = o.getInt("power");

				if (o.contains("condition")) {
					try {
						rank.condition = createCondition(rank, o.get("condition"));
					} catch (Exception ex) {
						FTBRanks.LOGGER.error("Failed to parse condition for " + rank.id + ": " + ex);
					}
				}

				o.remove("name");
				o.remove("power");
				o.remove("condition");

				for (String pkey : o.getAllKeys()) {
					while (pkey.endsWith(".*")) {
						pkey = pkey.substring(0, pkey.length() - 2);
						saveRanks();
					}

					if (!pkey.isEmpty()) {
						rank.permissions.put(pkey, ofTag(o, pkey));
					}
				}

				if (rank.name.isEmpty()) {
					rank.name = rank.id;
					saveRanks();
				}

				tempRanks.put(rank.id, rank);
			}

			if (tempRanks.isEmpty()) {
				FTBRanks.LOGGER.warn("No ranks found!");
			}
		}

		SNBTCompoundTag playerFileTag = SNBT.read(playerFile);

		if (playerFileTag != null) {
			for (String key : playerFileTag.getAllKeys()) {
				SNBTCompoundTag o = playerFileTag.getCompound(key);
				PlayerRankData data = new PlayerRankData(this, UUID.fromString(key));
				data.name = o.getString("name");

				SNBTCompoundTag ranksTag = o.getCompound("ranks");

				for (String rkey : ranksTag.getAllKeys()) {
					RankImpl rank = tempRanks.get(rkey);

					if (rank != null) {
						data.added.put(rank, Instant.parse(ranksTag.getString(rkey)));
					}
				}

				if (o.contains("permissions")) {
					SNBTCompoundTag ptag = o.getCompound("permissions");

					for (String pkey : ptag.getAllKeys()) {
						while (pkey.endsWith(".*")) {
							pkey = pkey.substring(0, pkey.length() - 2);
							savePlayers();
						}

						if (!pkey.isEmpty()) {
							data.permissions.put(key, ofTag(ptag, pkey));
						}
					}
				}

				tempPlayerData.put(data.uuid, data);
			}
		}

		ranks = new LinkedHashMap<>(tempRanks);
		playerData = new LinkedHashMap<>(tempPlayerData);

		sortedRanks = new ArrayList<>(ranks.values());
		sortedRanks.sort(null);

		FTBRanks.LOGGER.info("Loaded " + ranks.size() + " ranks");
	}

	public void saveRanksNow() {
		if (!shouldSaveRanks) {
			return;
		}

		shouldSaveRanks = false;

		SNBTCompoundTag tag = new SNBTCompoundTag();

		for (RankImpl rank : ranks.values()) {
			SNBTCompoundTag o = new SNBTCompoundTag();
			o.putString("name", rank.getName());
			o.putInt("power", rank.getPower());

			if (!rank.condition.isDefaultCondition()) {
				if (rank.condition.isSimple()) {
					o.putString("condition", rank.condition.getType());
				} else {
					SNBTCompoundTag c = new SNBTCompoundTag();
					c.putString("type", rank.condition.getType());
					rank.condition.save(c);
					o.put("condition", c);
				}
			}

			for (Map.Entry<String, PermissionValue> entry : rank.permissions.entrySet()) {
				PermissionValue v = entry.getValue();

				if (v.isDefaultValue()) {
					o.putNull(entry.getKey());
				} else if (v instanceof BooleanPermissionValue) {
					o.putBoolean(entry.getKey(), ((BooleanPermissionValue) entry.getValue()).value);
				} else if (v instanceof StringPermissionValue) {
					o.putString(entry.getKey(), ((StringPermissionValue) entry.getValue()).value);
				} else if (v instanceof NumberPermissionValue) {
					o.putNumber(entry.getKey(), ((NumberPermissionValue) entry.getValue()).value);
				} else {
					o.putString(entry.getKey(), entry.getValue().asString().orElse(""));
				}
			}

			tag.put(rank.id, o);
		}

		if (!SNBT.write(rankFile, tag)) {
			FTBRanks.LOGGER.warn("Failed to save ranks.snbt!");
		}
	}

	public void savePlayersNow() {
		if (!shouldSavePlayers) {
			return;
		}

		shouldSavePlayers = false;

		SNBTCompoundTag playerJson = new SNBTCompoundTag();

		for (PlayerRankData data : playerData.values()) {
			SNBTCompoundTag o = new SNBTCompoundTag();
			o.putString("name", data.name);

			SNBTCompoundTag r = new SNBTCompoundTag();

			for (Map.Entry<Rank, Instant> entry : data.added.entrySet()) {
				if (entry.getKey().getCondition().isDefaultCondition()) {
					r.putString(entry.getKey().getId(), entry.getValue().toString());
				}
			}

			o.put("ranks", r);

			if (!data.permissions.isEmpty()) {
				SNBTCompoundTag p = new SNBTCompoundTag();

				for (Map.Entry<String, PermissionValue> entry : data.permissions.entrySet()) {
					PermissionValue v = entry.getValue();

					if (v.isDefaultValue()) {
						p.putNull(entry.getKey());
					} else if (v instanceof BooleanPermissionValue) {
						p.putBoolean(entry.getKey(), ((BooleanPermissionValue) entry.getValue()).value);
					} else if (v instanceof StringPermissionValue) {
						p.putString(entry.getKey(), ((StringPermissionValue) entry.getValue()).value);
					} else if (v instanceof NumberPermissionValue) {
						p.putNumber(entry.getKey(), ((NumberPermissionValue) entry.getValue()).value);
					} else {
						p.putString(entry.getKey(), entry.getValue().asString().orElse(""));
					}
				}

				o.put("permissions", p);
			}

			playerJson.put(data.uuid.toString(), o);
		}

		if (!SNBT.write(playerFile, playerJson)) {
			FTBRanks.LOGGER.warn("Failed to save players.snbt!");
		}
	}

	private static PermissionValue ofTag(SNBTCompoundTag tag, String key) {
		if (tag.isBoolean(key)) {
			return BooleanPermissionValue.of(tag.getBoolean(key));
		}

		Tag v = tag.get(key);

		if (v == null || v instanceof EndTag) {
			return PermissionValue.DEFAULT;
		} else if (v instanceof NumericTag) {
			return NumberPermissionValue.of(((NumericTag) v).getAsNumber());
		} else if (v instanceof StringTag) {
			return StringPermissionValue.of(v.getAsString());
		}

		return StringPermissionValue.of(v.toString());
	}
}