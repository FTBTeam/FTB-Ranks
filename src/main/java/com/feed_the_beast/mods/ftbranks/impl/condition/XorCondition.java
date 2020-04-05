package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.Rank;
import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class XorCondition implements RankCondition
{
	public final List<RankCondition> conditions;

	public XorCondition(Rank rank, JsonObject json) throws Exception
	{
		conditions = new ArrayList<>();

		for (JsonElement e : json.get("conditions").getAsJsonArray())
		{
			conditions.add(rank.getManager().createCondition(rank, e.getAsJsonObject()));
		}
	}

	@Override
	public String getType()
	{
		return "xor";
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		return conditions.get(0).isRankActive(player) != conditions.get(1).isRankActive(player);
	}

	@Override
	public void save(JsonObject json)
	{
		JsonArray a = new JsonArray();

		for (RankCondition condition : conditions)
		{
			JsonObject c = new JsonObject();
			c.addProperty("type", condition.getType());
			condition.save(c);
			a.add(c);
		}

		json.add("conditions", a);
	}
}