package dev.ftb.mods.ftbranks.api;

import de.marhali.json5.Json5Element;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/// Top-level manager object.
public interface RankManager {
	/// Get all the known ranks, ordered by rank power, from highest to lowest. The rank's power determines which rank
	/// will apply to a player if there is more than one possibility; the rank with the highest power wins any contest.
	///
	/// @return all ranks
	Collection<? extends Rank> getAllRanks();

	/// Get the rank with the given ID, if it exists.
	///
	/// @param id the unique rank ID
	/// @return the optional rank
	Optional<Rank> getRank(String id);

	/// Create a new rank with the given name &amp; power. A canonical rank ID is derived from the name, by
	/// converting to lower case, then substituting the "+" symbol with "_plus" and all other non-alphanumeric characters
	/// with underscores. Finally, runs of multiple consecutive underscores are replaced with a single underscore.
	/// E.g. "Alice + Bob's rank" will be converted to "alice_plus_bob_s_rank"
	///
	/// @param displayName rank display name
	/// @param power rank power
	/// @param forceCreate if true, any existing rank is replaced (and a warning is logged); if false, an exception is thrown if a rank exists
	/// @return the newly-created rank
	/// @throws RankException if `forceCreate` is false and a rank with the same canonical ID already exists
	Rank createRank(String displayName, int power, boolean forceCreate);

	/// Delete the rank with the given ID.
	///
	/// @param id the unique rank ID
	/// @return the rank that was deleted, or null if the ID didn't exist
	@Nullable
	Rank deleteRank(String id);

	/// Get all the ranks to which the given player has been specifically added.
	///
	/// @param nameAndId the player's name and ID to check
	/// @return the ranks to which the player has been added
	Set<Rank> getAddedRanks(NameAndId nameAndId);

	/// Get a list of the ranks which currently apply to the given player. Note this is distinct from the result of
	/// [#getAddedRanks(NameAndId)], since it can include any ranks which implicitly apply to the player.
	///
	/// @param player the player
	/// @return a list of ranks
	default List<? extends Rank> getRanks(ServerPlayer player) {
		return getAllRanks().stream().filter(rank -> rank.isActive(player)).toList();
	}

	/// Create a condition from a Json5 element. This is typically used when registering nested condition types; see the
	/// built-in "and" / "or" / "not" conditions for examples.
	///
	/// The Json5 element must be either a primitive string element of just the condition type, or a Json5 object with
	/// a string "type" field holding the condition type (plus other fields relevant to the condition). The value of the
	/// element or "type" field must be a known condition ID, previously registered via
	/// [dev.ftb.mods.ftbranks.api.event.RegisterConditionsEvent]
	///
	/// @param rank the rank to which this condition is to be applied
	/// @param element the Json5 element
	/// @return a new condition
	/// @throws RankException if the rank could not be created for any reason
	RankCondition createCondition(Rank rank, Json5Element element) throws RankException;

	/// Retrieve the value of the given node for the given player. Querying for an unknown node will result in the
	/// return of the default permission value, which is effectively an empty return.
	///
	/// @param player the player
	/// @param node the node name
	/// @return the permission value
	PermissionValue getPermissionValue(ServerPlayer player, String node);

	/// Get the Minecraft server instance.
	///
	/// @return the server
	MinecraftServer getServer();
}