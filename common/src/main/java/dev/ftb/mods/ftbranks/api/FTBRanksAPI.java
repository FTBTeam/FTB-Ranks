package dev.ftb.mods.ftbranks.api;

import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public abstract class FTBRanksAPI {
	private static FTBRanksAPI instance;

	/**
	 * Get the Ranks Manager instance
	 * @return the manager
	 */
	public static RankManager manager() {
		return instance.getManager();
	}

	public static FTBRanksAPI getInstance() {
		return instance;
	}

	/**
	 * Convenience method: get the given player's value for the given permission node. This just calls
	 * {@link RankManager#getPermissionValue(ServerPlayer, String)}.
	 *
	 * @param player the player to check
	 * @param node the node to check
	 * @return the permission value
	 */
	public static PermissionValue getPermissionValue(ServerPlayer player, String node) {
		return instance.getManager().getPermissionValue(player, node);
	}

	/**
	 * Create a permission value by parsing the string input. This method will make a best guess as to what type to use;
	 * a string permission can be forced by enclosing the text in double quotes. Otherwise, the texts "true" and "false"
	 * are treated as boolean, and any text which can be parsed as a number will be treated as a numeric value.
	 *
	 * @param str the string to parse
	 * @return the permission value, which may be null if the input was null
	 */
	public abstract PermissionValue parsePermissionValue(String str);

	/**
	 * Do not call this yourself! For internal use only.
	 */
	public static void setup(FTBRanksAPI theInstance) {
		if (instance != null || !theInstance.getClass().getName().startsWith("dev.ftb.mods.ftbranks")) {
			throw new IllegalStateException("don't do this");
		}
		instance = theInstance;
	}

	protected abstract RankManager getManager();
}