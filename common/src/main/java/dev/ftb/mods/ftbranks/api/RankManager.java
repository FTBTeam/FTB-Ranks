package dev.ftb.mods.ftbranks.api;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author LatvianModder
 */
public interface RankManager {
	void saveRanks();

	void savePlayers();

	List<Rank> getAllRanks();

	Optional<Rank> getRank(String id);

	Rank createRank(String id);

	@Nullable
	Rank deleteRank(String id);

	Set<Rank> getAddedRanks(GameProfile profile);

	default List<Rank> getRanks(ServerPlayer player) {
		List<Rank> list = new ArrayList<>();

		for (Rank rank : getAllRanks()) {
			if (rank.isActive(player)) {
				list.add(rank);
			}
		}

		return list;
	}

	void registerCondition(String id, RankConditionFactory conditionFactory);

	RankCondition createCondition(Rank rank, SNBTCompoundTag tag) throws Exception;

	PermissionValue getPermissionValue(ServerPlayer player, String node);
}