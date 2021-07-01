package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftbranks.api.SimpleRankCondition;
import me.shedaniel.architectury.hooks.PlayerHooks;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class FakePlayerCondition implements SimpleRankCondition {
	@Override
	public String getType() {
		return "fake_player";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return PlayerHooks.isFake(player);
	}
}