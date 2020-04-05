package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * @author LatvianModder
 */
public class OPCondition implements RankCondition
{
	@Override
	public String getType()
	{
		return "op";
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		return player.server.getPlayerList().canSendCommands(player.getGameProfile());
	}
}