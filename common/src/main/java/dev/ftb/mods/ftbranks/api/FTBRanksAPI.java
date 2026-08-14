package dev.ftb.mods.ftbranks.api;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/// Top-level API object
@ApiStatus.NonExtendable
public abstract class FTBRanksAPI {
	@Nullable
	private static FTBRanksAPI instance;

	/// Get the API instance
	/// @return the API
	public static FTBRanksAPI getInstance() {
		return Objects.requireNonNull(instance);
	}

	/// Convenience method to get the Ranks Manager instance
	/// @return the manager
	/// @throws NullPointerException if called before the Minecraft server has started
	public static RankManager manager() {
		return getInstance().getManager();
	}

	/// Convenience method: get the given player's value for the given permission node. This just calls
	/// [RankManager#getPermissionValue(ServerPlayer, String)].
	///
	/// The `checkParentNodes` parameter operates as follows: if the queried node name is
	/// "one.two.three", and `checkParentNodes` is true, then the nodes "one.two.three", "one.two" and
	/// "one" are checked, in that order. If `parentNodes` is false, then _only_ "one.two.three" is checked.
	///
	/// @param player the player to check
	/// @param node the node to check
	/// @param checkParentNodes see above for an explanation of this parameter
	/// @return the permission value, or [PermissionValue#MISSING] if the node is not found
	/// @throws NullPointerException if called before the Minecraft server has started
	public static PermissionValue getPermissionValue(ServerPlayer player, String node, boolean checkParentNodes) {
		return manager().getPermissionValue(player, node, checkParentNodes);
	}

	/// Calls [#getPermissionValue(net.minecraft.server.level.ServerPlayer, java.lang.String, boolean)]
	/// with `checkParentNodes` = true
	/// @throws NullPointerException if called before the Minecraft server has started
	public static PermissionValue getPermissionValue(ServerPlayer player, String node) {
		return manager().getPermissionValue(player, node, true);
	}

	/// Create a permission value by parsing the string input. This method will make a best guess as to what type to use;
	/// a string permission can be forced by enclosing the text in double quotes. Otherwise, the texts "true" and "false"
	/// are treated as boolean, and any text which can be parsed as a number will be treated as a numeric value.
	///
	/// @param str the string to parse
	/// @return the permission value, which may be null if the input was null
	@Nullable
	public abstract PermissionValue parsePermissionValue(@Nullable String str);

	/// Do not call this yourself! For internal use only.
	@ApiStatus.Internal
	public static void setup(FTBRanksAPI theInstance) {
		if (instance != null || !theInstance.getClass().getPackageName().equals("dev.ftb.mods.ftbranks.impl")) {
			throw new IllegalStateException("don't do this");
		}
		instance = theInstance;
	}

	@ApiStatus.Internal
	protected abstract RankManager getManager();
}