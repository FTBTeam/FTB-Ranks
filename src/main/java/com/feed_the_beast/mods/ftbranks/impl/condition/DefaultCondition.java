package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.Rank;
import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * @author LatvianModder
 */
public class DefaultCondition implements RankCondition
{
	public final Rank original;

	public DefaultCondition(Rank r)
	{
		original = r;
	}

	@Override
	public String getType()
	{
		return "default";
	}

	@Override
	public boolean isDefaultCondition()
	{
		return true;
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		return original.isAdded(player);
	}
}