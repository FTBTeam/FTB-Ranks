package com.feed_the_beast.mods.ftbranks.api;

import net.minecraft.entity.player.ServerPlayerEntity;

/**
 * @author LatvianModder
 */
public abstract class FTBRanksAPI
{
	public static FTBRanksAPI INSTANCE;

	public abstract RankManager getManager();

	public static PermissionValue getPermissionValue(ServerPlayerEntity player, String node)
	{
		return INSTANCE.getManager().getPermissionValue(player, node);
	}
}