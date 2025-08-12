package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.server.level.ServerPlayer;

public class OPCondition implements RankCondition.Simple {
	@Override
	public String getType() {
		return "op";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return player.getServer().getPlayerList().isOp(player.getGameProfile());
	}
}