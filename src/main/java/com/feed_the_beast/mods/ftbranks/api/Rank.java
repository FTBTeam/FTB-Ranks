package com.feed_the_beast.mods.ftbranks.api;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * @author LatvianModder
 */
public interface Rank
{
	RankManager getManager();

	String getId();

	String getName();

	int getPower();

	void setPermission(String node, PermissionValue value);

	PermissionValue getPermission(String node);

	RankCondition getCondition();

	default boolean isActive(ServerPlayerEntity player)
	{
		return getCondition().isRankActive(player);
	}

	default boolean isAdded(ServerPlayerEntity player)
	{
		return getManager().getAddedRanks(player.getGameProfile()).contains(this);
	}

	boolean add(GameProfile profile);

	boolean remove(GameProfile profile);
}