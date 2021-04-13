package dev.ftb.mods.ftbranks.api;

import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public abstract class FTBRanksAPI {
	public static FTBRanksAPI INSTANCE;

	public abstract RankManager getManager();

	public static PermissionValue getPermissionValue(ServerPlayer player, String node) {
		return INSTANCE.getManager().getPermissionValue(player, node);
	}
}