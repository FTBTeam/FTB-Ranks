package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * @author LatvianModder
 */
public class CreativeModeCondition implements RankCondition
{
	@Override
	public String getType()
	{
		return "creative_mode";
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		return player.isCreative();
	}
}