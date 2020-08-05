package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

/**
 * @author LatvianModder
 */
public class DimensionCondition implements RankCondition
{
	public final ResourceLocation dimension;

	public DimensionCondition(JsonObject json)
	{
		dimension = new ResourceLocation(json.get("dimension").getAsString());
	}

	@Override
	public String getType()
	{
		return "dimension";
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		return player.world.func_234923_W_().getRegistryName().equals(dimension);
	}

	@Override
	public void save(JsonObject json)
	{
		json.addProperty("dimension", dimension.toString());
	}
}