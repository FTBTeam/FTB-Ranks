package com.feed_the_beast.mods.ftbranks.impl;

import com.feed_the_beast.mods.ftbranks.api.FTBRanksAPI;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class RankCommandPredicate implements Predicate<CommandSource>
{
	public final CommandNode<CommandSource> command;
	public final Predicate<CommandSource> original;
	public final String node;
	public final boolean literal;
	public Supplier<RankCommandPredicate> redirect;

	public RankCommandPredicate(CommandNode<CommandSource> c, String n)
	{
		command = c;
		original = command.getRequirement();
		node = n;
		literal = c instanceof LiteralCommandNode;
		redirect = null;
	}

	public String getNode()
	{
		if (redirect == null)
		{
			return node;
		}

		return redirect.get().getNode();
	}

	@Override
	public boolean test(CommandSource source)
	{
		if (source.getEntity() instanceof ServerPlayerEntity)
		{
			return FTBRanksAPI.getPermissionValue((ServerPlayerEntity) source.getEntity(), getNode()).asBoolean().orElseGet(() -> original.test(source));
		}

		return original.test(source);
	}
}