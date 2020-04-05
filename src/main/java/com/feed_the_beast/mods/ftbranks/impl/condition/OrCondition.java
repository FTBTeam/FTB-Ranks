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
public class OrCondition implements RankCondition
{
	public final List<RankCondition> conditions;

	public OrCondition(Rank rank, JsonObject json) throws Exception
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
		return "or";
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		for (RankCondition condition : conditions)
		{
			if (condition.isRankActive(player))
			{
				return true;
			}
		}

		return false;
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