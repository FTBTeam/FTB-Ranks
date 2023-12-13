package dev.ftb.mods.ftbranks.neoforge;

/**
 * @author LatvianModder
 */
public class PermissionAPIWrapper /* implements IPermissionHandler */ {
	/* Nah, im not doing this lol
	private final IPermissionHandler original;

	public PermissionAPIWrapper(IPermissionHandler h) {
		original = h;
	}

	@Override
	public void registerNode(String node, DefaultPermissionLevel level, String desc) {
		original.registerNode(node, level, desc);
	}

	@Override
	public Collection<String> getRegisteredNodes() {
		return original.getRegisteredNodes();
	}

	@Override
	public boolean hasPermission(GameProfile profile, String node, @Nullable IContext context) {
		if (context != null && context.getPlayer() instanceof ServerPlayer) {
			return FTBRanksAPI.getPermissionValue((ServerPlayer) context.getPlayer(), node).asBoolean().orElseGet(() -> original.hasPermission(profile, node, context));
		} else if (context != null && context.getWorld() != null && !context.getWorld().isClientSide()) {
			ServerPlayer player = context.getWorld().getServer().getPlayerList().getPlayer(profile.getId());

			if (player != null) {
				return FTBRanksAPI.getPermissionValue(player, node).asBoolean().orElseGet(() -> original.hasPermission(profile, node, context));
			}
		}

		return original.hasPermission(profile, node, context);
	}

	@Override
	public String getNodeDescription(String node) {
		return original.getNodeDescription(node);
	}
	 */
}
