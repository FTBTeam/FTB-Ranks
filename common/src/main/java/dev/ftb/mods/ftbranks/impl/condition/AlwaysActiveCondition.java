package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.server.level.ServerPlayer;

public enum AlwaysActiveCondition implements RankCondition.Simple {
	INSTANCE;

	@Override
	public String getType() {
		return "always_active";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return true;
	}
}