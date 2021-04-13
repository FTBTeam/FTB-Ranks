package dev.ftb.mods.ftbranks.api;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public interface Rank {
	RankManager getManager();

	String getId();

	String getName();

	int getPower();

	void setPermission(String node, PermissionValue value);

	PermissionValue getPermission(String node);

	RankCondition getCondition();

	default boolean isActive(ServerPlayer player) {
		return getCondition().isRankActive(player);
	}

	default boolean isAdded(ServerPlayer player) {
		return getManager().getAddedRanks(player.getGameProfile()).contains(this);
	}

	boolean add(GameProfile profile);

	boolean remove(GameProfile profile);
}