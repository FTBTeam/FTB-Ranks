package com.feed_the_beast.mods.ftbranks.api;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * @author LatvianModder
 */
public interface RankCondition
{
	String getType();

	default boolean isDefaultCondition()
	{
		return false;
	}

	boolean isRankActive(ServerPlayerEntity player);

	default void save(JsonObject json)
	{
	}
}