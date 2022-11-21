package dev.ftb.mods.ftbranks.api;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * @author LatvianModder
 */
public interface Rank {
	RankManager getManager();

	String getId();

	String getName();

	int getPower();

	void setPermission(String node, @Nullable PermissionValue value);

	PermissionValue getPermission(String node);

	RankCondition getCondition();

	void setCondition(RankCondition condition);

	default boolean isActive(ServerPlayer player) {
		return getCondition().isRankActive(player);
	}

	default boolean isAdded(ServerPlayer player) {
		return getManager().getAddedRanks(player.getGameProfile()).contains(this);
	}

	boolean add(GameProfile profile);

	boolean remove(GameProfile profile);

	Collection<String> getPermissions();
}