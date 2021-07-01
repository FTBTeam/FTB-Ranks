package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.SimpleRankCondition;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class DefaultCondition implements SimpleRankCondition {
	public final Rank original;

	public DefaultCondition(Rank r) {
		original = r;
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