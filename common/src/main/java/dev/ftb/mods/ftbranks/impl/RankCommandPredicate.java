package dev.ftb.mods.ftbranks.impl;

import com.mojang.brigadier.tree.CommandNode;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class RankCommandPredicate implements Predicate<CommandSourceStack> {
	private final Predicate<CommandSourceStack> original;
	private final String nodeName;

	private Supplier<RankCommandPredicate> redirect;

	public RankCommandPredicate(CommandNode<CommandSourceStack> commandNode, String nodeName) {
		this.original = commandNode.getRequirement();
		this.nodeName = nodeName;
		this.redirect = null;
	}

	public String getNodeName() {
		return redirect == null || redirect.get() == null ? nodeName : redirect.get().getNodeName();
	}

	public void setRedirect(Supplier<RankCommandPredicate> redirect) {
		this.redirect = redirect;
	}

	@Override
	public boolean test(CommandSourceStack source) {
		if (source.getEntity() instanceof ServerPlayer sp && FTBRanksAPI.manager() != null) {
			return FTBRanksAPI.getPermissionValue(sp, getNodeName()).asBoolean().orElseGet(() -> original.test(source));
		}

		return original.test(source);
	}
}