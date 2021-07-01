package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftbranks.api.SimpleRankCondition;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class AlwaysActiveCondition implements SimpleRankCondition {
	public static final AlwaysActiveCondition INSTANCE = new AlwaysActiveCondition();

	private AlwaysActiveCondition() {
	}

	@Override
	public String getType() {
		return "always_active";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return true;
	}
}