package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.server.level.ServerPlayer;

public record DefaultCondition(Rank original) implements RankCondition.Simple {
	@Override
	public String getType() {
		return "default";
	}

	@Override
	public boolean isDefaultCondition() {
		return true;
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return original.isAdded(player);
	}
}