package dev.ftb.mods.ftbranks.api;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Represents a rank. A rank consists of a collection of permission nodes, as well as a condition that determines which
 * players the rank applies to. A rank also has a "power" level; the highest-powered rank will apply to a player if
 * more than one rank is applicable.
 */
public interface Rank {
	/**
	 * Convenience method to get the rank manager.
	 *
	 * @return the rank manager
	 */
	RankManager getManager();

	/**
	 * Get the unique rank ID.
	 *
	 * @return the rank ID
	 */
	String getId();

	/**
	 * Get the rank's displayable name.
	 *
	 * @return the display name
	 */
	String getName();

	/**
	 * Get the rank's power.
	 *
	 * @return the rank power
	 */
	int getPower();

	/**
	 * Set the given permission node for the rank. Note that permission values may be obtained via
	 * {@link FTBRanksAPI#parsePermissionValue(String)}.
	 *
	 * @param node the node name
	 * @param value the permission value
	 */
	void setPermission(String node, @Nullable PermissionValue value);

	/**
	 * Get the permission value for the given node name. The default (empty) permission value will be returned if the
	 * node name is not known.
	 *
	 * @param node the node name
	 * @return the permission value
	 */
	@Nonnull
	PermissionValue getPermission(String node);

	/**
	 * Get the condition for this rank. The condition is used to determine whether the rank is applicable to a
	 * given player.
	 *
	 * @return the rank's condition
	 */
	RankCondition getCondition();

	/**
	 * Set the condition for this rank.
	 *
	 * @param condition the new condition to use
	 */
	void setCondition(RankCondition condition);

	/**
	 * Check if this rank is applicable to the given player.
	 *
	 * @param player the player
	 * @return true if the rank is applicable, false otherwise
	 */
	default boolean isActive(ServerPlayer player) {
		return getCondition().isRankActive(player);
	}

	/**
	 * Check if the given player has been specifically added to this rank.
	 *
	 * @param player the player
	 * @return true if the player has been added, false otherwise
	 */
	default boolean isAdded(ServerPlayer player) {
		return getManager().getAddedRanks(player.getGameProfile()).contains(this);
	}

	/**
	 * Add the given player game profile to this rank.
	 *
	 * @param profile the game profile
	 * @return true if the profile was added, false it was already present
	 */
	boolean add(GameProfile profile);

	/**
	 * Remove the given player game profile from this rank.
	 *
	 * @param profile the game profile
	 * @return true if the profile was removed, false if it was not present
	 */
	boolean remove(GameProfile profile);

	/**
	 * Get all the permission node names which have been defined for this rank.
	 *
	 * @return all known node names
	 */
	Collection<String> getPermissions();
}