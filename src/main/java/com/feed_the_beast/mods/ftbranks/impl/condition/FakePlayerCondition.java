package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.FakePlayer;

/**
 * @author LatvianModder
 */
public class FakePlayerCondition implements RankCondition
{
	@Override
	public String getType()
	{
		return "fake_player";
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		return player instanceof FakePlayer;
	}
}