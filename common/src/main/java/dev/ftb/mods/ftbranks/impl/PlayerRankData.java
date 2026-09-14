package dev.ftb.mods.ftbranks.impl;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankException;
import org.jetbrains.annotations.NotNull;

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
	@NotNull
	public PermissionValue getPermission(String node) {
		return PermissionValue.MISSING;
	}

	SNBTCompoundTag writeSNBT() {
		SNBTCompoundTag res = new SNBTCompoundTag();

		res.putString("name", name);

		SNBTCompoundTag ranksTag = new SNBTCompoundTag();
		added.forEach((rank, when) -> {
			if (rank.getCondition().isDefaultCondition()) {
				ranksTag.putString(rank.getId(), when.toString());
			}
		});
		if (!ranksTag.isEmpty()) {
			res.put("ranks", ranksTag);
		}

		return res;
	}

	static PlayerRankData fromSNBT(RankManagerImpl manager, UUID playerId, SNBTCompoundTag tag, Map<String,RankImpl> tempRanks) {
		PlayerRankData data = new PlayerRankData(manager, playerId, tag.getString("name"));

		SNBTCompoundTag ranksTag = tag.getCompound("ranks");
		for (String rankKey : ranksTag.getAllKeys()) {
			RankImpl rank = tempRanks.get(rankKey);
			if (rank != null) {
				try {
					data.added.put(rank, Instant.parse(ranksTag.getString(rankKey)));
				} catch (DateTimeParseException e) {
					throw new RankException(e.getMessage());
				}
			}
		}

		return data;
	}
}