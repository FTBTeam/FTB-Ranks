package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.server.level.ServerPlayer;

public class DefaultCondition implements RankCondition.Simple {
	public final Rank original;

	public DefaultCondition(Rank original) {
		this.original = original;
	}

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