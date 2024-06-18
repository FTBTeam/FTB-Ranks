package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankException;
import net.minecraft.server.level.ServerPlayer;

public class NotCondition implements RankCondition {
	private final RankCondition condition;

	public NotCondition(Rank rank, SNBTCompoundTag tag) throws RankException {
		condition = rank.getManager().createCondition(rank, tag.get("condition"));
	}

	@Override
	public String getType() {
		return "not";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return !condition.isRankActive(player);
	}

	@Override
	public void save(SNBTCompoundTag tag) {
		SNBTCompoundTag c = new SNBTCompoundTag();
		c.putString("type", condition.getType());
		condition.save(c);
		tag.put("condition", c);
	}
}