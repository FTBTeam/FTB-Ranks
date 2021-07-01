package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftbranks.api.SimpleRankCondition;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class OPCondition implements SimpleRankCondition {
	@Override
	public String getType() {
		return "op";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return player.server.getPlayerList().isOp(player.getGameProfile());
	}
}