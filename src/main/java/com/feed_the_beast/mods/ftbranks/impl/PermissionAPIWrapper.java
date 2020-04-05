package com.feed_the_beast.mods.ftbranks.impl;

import com.feed_the_beast.mods.ftbranks.api.FTBRanksAPI;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.IPermissionHandler;
import net.minecraftforge.server.permission.context.IContext;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * @author LatvianModder
 */
public class PermissionAPIWrapper implements IPermissionHandler
{
	private final IPermissionHandler original;

	public PermissionAPIWrapper(IPermissionHandler h)
	{
		original = h;
	}

	@Override
	public void registerNode(String node, DefaultPermissionLevel level, String desc)
	{
		original.registerNode(node, level, desc);
	}

	@Override
	public Collection<String> getRegisteredNodes()
	{
		return original.getRegisteredNodes();
	}

	@Override
	public boolean hasPermission(GameProfile profile, String node, @Nullable IContext context)
	{
		if (context != null && context.getPlayer() instanceof ServerPlayerEntity)
		{
			return FTBRanksAPI.getPermissionValue((ServerPlayerEntity) context.getPlayer(), node).asBoolean().orElseGet(() -> original.hasPermission(profile, node, context));
		}
		else if (context != null && context.getWorld() != null && !context.getWorld().isRemote())
		{
			ServerPlayerEntity player = context.getWorld().getServer().getPlayerList().getPlayerByUUID(profile.getId());

			if (player != null)
			{
				return FTBRanksAPI.getPermissionValue(player, node).asBoolean().orElseGet(() -> original.hasPermission(profile, node, context));
			}
		}

		return original.hasPermission(profile, node, context);
	}

	@Override
	public String getNodeDescription(String node)
	{
		return original.getNodeDescription(node);
	}
}
