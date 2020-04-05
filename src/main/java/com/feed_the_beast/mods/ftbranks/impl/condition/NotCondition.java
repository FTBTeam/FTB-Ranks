package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.Rank;
import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * @author LatvianModder
 */
public class NotCondition implements RankCondition
{
	public final RankCondition condition;

	public NotCondition(Rank rank, JsonObject json) throws Exception
	{
		condition = rank.getManager().createCondition(rank, json.get("condition").getAsJsonObject());
	}

	@Override
	public String getType()
	{
		return "not";
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		return !condition.isRankActive(player);
	}

	@Override
	public void save(JsonObject json)
	{
		JsonObject c = new JsonObject();
		c.addProperty("type", condition.getType());
		condition.save(c);
		json.add("condition", c);
	}
}