package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.server.level.ServerPlayer;

public class RankAddedCondition implements RankCondition {
	protected final Rank original;
	protected final String id;

	public RankAddedCondition(Rank r, SNBTCompoundTag tag) {
		original = r;
		id = tag.getStringOr("rank", "");
	}

	@Override
	public String getType() {
		return "rank_added";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return original.getManager().getRank(id)
				.map(rank -> rank != original && rank.isAdded(player))
				.orElse(false);
	}

	@Override
	public void save(SNBTCompoundTag tag) {
		tag.putString("rank", id);
	}
}
