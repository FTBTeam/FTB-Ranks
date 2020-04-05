package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.Rank;
import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Optional;

/**
 * @author LatvianModder
 */
public final class RankAddedCondition implements RankCondition
{
	public final Rank original;
	public final String id;

	public RankAddedCondition(Rank r, JsonObject json)
	{
		original = r;
		id = json.get("rank").getAsString();
	}

	@Override
	public String getType()
	{
		return "rank_added";
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		Optional<Rank> rank = original.getManager().getRank(id);
		Rank r = rank.orElse(null);
		return r != null && r != original && r.isAdded(player);
	}
}
