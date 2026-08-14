package dev.ftb.mods.ftbranks.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/// Represents a rank. A rank consists of a collection of permission nodes, as well as a condition that determines which
/// players the rank applies to. A rank also has a "power" level; the highest-powered rank will apply to a player if
/// more than one rank is applicable.
@ApiStatus.NonExtendable
public interface Rank {
	/// Convenience method to get the rank manager.
	///
	/// @return the rank manager
	RankManager getManager();

	/// Get the source that this rank was loaded from. See [RankFileSource] for more information.
	///
	/// @return the source of the rank
	RankFileSource getSource();

	/// Get the unique rank ID.
	///
	/// @return the rank ID
	String getId();

	NamespacedRankId getNamespacedId();

	/// @deprecated Use [#getDisplayName()]
	@Deprecated
	default String getName() {
		return getDisplayName();
	}

	/// Get the rank's displayable name. This is purely cosmetic and has no functional effect.
	///
	/// @return the display name
	String getDisplayName();

	/// Set the rank's displayable name.
	///
	/// @param displayName the new display name
	/// @throws RankException if this is called on a rank whose source is [RankFileSource#MODPACK]
	void setDisplayName(String displayName);

	/// Get the rank's power, aka the rank priority.
	///
	/// @return the rank power
	int getPower();

	/// Set the rank's power level, aka the rank priority.
	///
	/// @param power the new power level
	/// @throws RankException if this is called on a rank whose source is [RankFileSource#MODPACK]
	void setPower(int power);

	/// Get the permission value for the given node name. The default (empty) permission value will be returned if the
	/// node name is not known.
	///
	/// @param node the node name
	/// @return the permission value
	PermissionValue getPermission(String node);

	/// Set the given permission node for the rank. Note that permission values may be obtained via
	/// [FTBRanksAPI#parsePermissionValue(String)].
	///
	/// @param node the node name
	/// @param value the permission value
	/// @throws RankException if this is called on a rank whose source is [RankFileSource#MODPACK]
	void setPermission(String node, @Nullable PermissionValue value);

	/// Get the membership condition for this rank. The condition is used to determine whether the rank is applicable
	/// to a given player.
	///
	/// @return the rank's condition
	RankCondition getCondition();

	/// Set the membership condition for this rank.
	///
	/// @param condition the new condition to use
	/// @throws RankException if this is called on a rank whose source is [RankFileSource#MODPACK]
	void setCondition(RankCondition condition);

	/// Check if this rank is applicable to the given player.
	///
	/// @param player the player
	/// @return true if the rank is applicable, false otherwise
	default boolean isActive(ServerPlayer player) {
		return getCondition().isRankActive(player);
	}

	/// Check if the given player has been specifically added to this rank.
	///
	/// @param player the player
	/// @return true if the player has been added, false otherwise
	default boolean isAdded(ServerPlayer player) {
		return getManager().getAddedRanks(player.nameAndId()).contains(this);
	}

	/// Add the given player to this rank. The rank must not have an explicit condition set.
	///
	/// @param nameAndId the player's name and ID
	/// @return true if the player was added, false it was already present
	/// @throws RankException if the rank has an explicit condition
	boolean add(NameAndId nameAndId);

	/// Remove the given player from this rank.
	///
	/// @param nameAndId the player's name and ID
	/// @return true if the player was removed, false if it was not present
	boolean remove(NameAndId nameAndId);

	/// Get all the permission node names which have been defined for this rank.
	///
	/// @return all known node names
	Collection<String> getPermissions();
}