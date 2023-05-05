package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class CreativeModeCondition implements RankCondition.Simple {
	@Override
	public String getType() {
		return "creative_mode";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return player.isCreative();
	}
}