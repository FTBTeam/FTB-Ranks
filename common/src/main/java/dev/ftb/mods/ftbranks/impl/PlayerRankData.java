package dev.ftb.mods.ftbranks.impl;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankException;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

public class PlayerRankData {
	private final RankManagerImpl manager;
	private final UUID playerId;
	private final String name;
	private final Map<Rank, Instant> added;

	public PlayerRankData(RankManagerImpl manager, UUID playerId, String name) {
		this.manager = manager;
		this.playerId = playerId;
		this.name = name;
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

		res.addProperty("name", name);

		Json5Object ranksJson = new Json5Object();
		added.forEach((rank, when) -> {
			if (rank.getCondition().isDefaultCondition()) {
				ranksJson.addProperty(rank.getId(), when.toString());
			}
		});
		if (!ranksJson.isEmpty()) {
			res.add("ranks", ranksJson);
		}

		return res;
	}

	static PlayerRankData fromJson(RankManagerImpl manager, UUID playerId, Json5Object json, Map<String,RankImpl> tempRanks) {
		PlayerRankData data = new PlayerRankData(manager, playerId, Json5Util.getString(json, "name").orElse(""));

		Json5Util.getJson5Object(json, "ranks").ifPresent(ranks -> {
			for (String rankKey : ranks.keySet()) {
				RankImpl rank = tempRanks.get(rankKey);
				if (rank != null) {
					try {
						data.added.put(rank, Instant.parse(Json5Util.getString(ranks, rankKey).orElse("")));
					} catch (DateTimeParseException e) {
						throw new RankException(e.getMessage());
					}
				}
			}
		});

		return data;
	}
}
