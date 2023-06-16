package dev.ftb.mods.ftbranks.impl.condition;

import dev.architectury.hooks.level.entity.PlayerHooks;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class FakePlayerCondition implements RankCondition.Simple {
	@Override
	public String getType() {
		return "fake_player";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return PlayerHooks.isFake(player);
	}
}