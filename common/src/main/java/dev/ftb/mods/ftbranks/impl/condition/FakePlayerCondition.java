package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.server.level.ServerPlayer;

public class FakePlayerCondition implements RankCondition.Simple {
	@Override
	public String getType() {
		return "fake_player";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return Platform.get().misc().isFakePlayer(player);
	}
}
