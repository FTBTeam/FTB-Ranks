package dev.ftb.mods.ftbranks.impl;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class RankCommandPredicate implements Predicate<CommandSourceStack> {
	public final CommandNode<CommandSourceStack> command;
	public final Predicate<CommandSourceStack> original;
	public final String node;
	public final boolean literal;
	public Supplier<RankCommandPredicate> redirect;

	public RankCommandPredicate(CommandNode<CommandSourceStack> c, String n) {
		command = c;
		original = command.getRequirement();
		node = n;
		literal = c instanceof LiteralCommandNode;
		redirect = null;
	}

	public String getNode() {
		if (redirect == null || redirect.get() == null) {
			return node;
		}

		return redirect.get().getNode();
	}

	@Override
	public boolean test(CommandSourceStack source) {
		if (source.getEntity() instanceof ServerPlayer) {
			return FTBRanksAPI.getPermissionValue((ServerPlayer) source.getEntity(), getNode()).asBoolean().orElseGet(() -> original.test(source));
		}

		return original.test(source);
	}
}