package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;

/**
 * @author LatvianModder
 */
public class DimensionCondition implements RankCondition
{
	public final DimensionType dimension;

	public DimensionCondition(JsonObject json)
	{
		dimension = DimensionType.byName(new ResourceLocation(json.get("dimension").getAsString()));
	}

	@Override
	public String getType()
	{
		return "dimension";
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		return player.dimension == dimension;
	}

	@Override
	public void save(JsonObject json)
	{
		json.addProperty("dimension", DimensionType.getKey(dimension).toString());
	}
}