package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * @author LatvianModder
 */
public class AlwaysActiveCondition implements RankCondition
{
	public static final AlwaysActiveCondition INSTANCE = new AlwaysActiveCondition();

	private AlwaysActiveCondition()
	{
	}

	@Override
	public String getType()
	{
		return "always_active";
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		return true;
	}
}