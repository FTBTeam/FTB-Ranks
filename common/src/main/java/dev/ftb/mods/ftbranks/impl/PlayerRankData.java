package dev.ftb.mods.ftbranks.impl;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.api.*;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

public class PlayerRankData {
	private final RankManagerImpl manager;
	private final UUID playerId;
	private String playerName;
	private final Map<Rank, Instant> added;

	public PlayerRankData(RankManagerImpl manager, UUID playerId, String playerName) {
		this.manager = manager;
		this.playerId = playerId;
		this.playerName = playerName;
		this.added = new LinkedHashMap<>();
	}

	public UUID getPlayerId() {
		return playerId;
	}

	public Set<Rank> addedRanks() {
		return added.keySet();
	}

	public boolean addRank(Rank rank) {
		if (!added.containsKey(rank)) {
			added.put(rank, Instant.now());
			manager.markPlayerDataDirty();
			return true;
		}
		return false;
	}

	public boolean removeRank(Rank rank) {
		if (added.remove(rank) != null) {
			manager.markPlayerDataDirty();
			return true;
		}
		return false;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PlayerRankData that = (PlayerRankData) o;
		return Objects.equals(playerId, that.playerId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(playerId);
	}

	/**
	 * Player-specific permission nodes have never worked correctly and will be removed.
	 * @param node the node
	 * @return always returns MISSING
	 */
	@Deprecated(forRemoval = true)
	public PermissionValue getPermission(String node) {
		return PermissionValue.MISSING;
	}

	Json5Object toJson() {
		Json5Object res = new Json5Object();

		res.addProperty("name", playerName);

		Json5Object ranksJson = new Json5Object();
		added.forEach((rank, when) -> {
			if (rank.getCondition().isDefaultCondition()) {
				ranksJson.addProperty(rank.getNamespacedId().toString(), when.toString());
			}
		});
		if (!ranksJson.isEmpty()) {
			res.add("ranks", ranksJson);
		}

		return res;
	}

	static PlayerRankData fromJson(RankManagerImpl manager, UUID playerId, Json5Object json, Map<NamespacedRankId,RankImpl> tempRanks) {
		PlayerRankData data = new PlayerRankData(manager, playerId, Json5Util.getString(json, "name").orElse(""));

		Json5Util.getJson5Object(json, "ranks").ifPresent(ranks -> {
			for (String rankKey : ranks.keySet()) {
				RankImpl rank = NamespacedRankId.fromString(rankKey).map(tempRanks::get).orElse(null);
				if (rank == null) {
					// legacy import
					rank = findUnprefixedRank(rankKey, tempRanks);
				}
				if (rank != null) {
					try {
						data.added.put(rank, Instant.parse(Json5Util.getString(ranks, rankKey).orElse("")));
					} catch (DateTimeParseException e) {
						throw new RankException(e.getMessage());
					}
				} else {
					FTBRanks.LOGGER.warn("unknown rank {} found in player data for {}, ignoring", rankKey, playerId);
				}
			}
		});

		return data;
	}

	@Nullable
	private static RankImpl findUnprefixedRank(String id, Map<NamespacedRankId,RankImpl> map) {
		for (RankFileSource source : RankFileSource.values()) {
			RankImpl rank = map.get(new NamespacedRankId(source, id));
			if (rank != null) {
				return rank;
			}
		}
		return null;
	}

    public String getPlayerName() {
        return playerName;
    }
}
