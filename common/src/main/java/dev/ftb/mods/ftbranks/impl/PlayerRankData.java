package dev.ftb.mods.ftbranks.impl;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankException;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * @author LatvianModder
 */
public class PlayerRankData {
	private final RankManagerImpl manager;
	private final UUID playerId;
	private final String name;
	private final Map<Rank, Instant> added;
	private final Map<String, PermissionValue> permissions;

	public PlayerRankData(RankManagerImpl manager, UUID playerId, String name) {
		this.manager = manager;
		this.playerId = playerId;
		this.name = name;
		this.added = new LinkedHashMap<>();
		this.permissions = new LinkedHashMap<>();
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

	@Nonnull
	public PermissionValue getPermission(String node) {
		return permissions.getOrDefault(node, PermissionValue.MISSING);
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

		SNBTCompoundTag permTag = RankManagerImpl.writePermissions(permissions, new SNBTCompoundTag());
		if (!permTag.isEmpty()) {
			res.put("permissions", permTag);
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

		SNBTCompoundTag permTag = tag.getCompound("permissions");
		for (String permKey : permTag.getAllKeys()) {
			while (permKey.endsWith(".*")) {
				permKey = permKey.substring(0, permKey.length() - 2);
				manager.markPlayerDataDirty();
			}
			if (!permKey.isEmpty()) {
				data.permissions.put(playerId.toString(), RankManagerImpl.ofTag(permTag, permKey));
			}
		}

		return data;
	}
}