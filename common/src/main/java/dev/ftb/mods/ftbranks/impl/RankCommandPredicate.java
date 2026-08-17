package dev.ftb.mods.ftbranks.impl;

import com.mojang.brigadier.tree.CommandNode;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class RankCommandPredicate implements Predicate<CommandSourceStack> {
	private final Predicate<CommandSourceStack> original;
	private final String nodeName;

	@Nullable
	private Supplier<@Nullable RankCommandPredicate> redirect;

	public RankCommandPredicate(CommandNode<CommandSourceStack> commandNode, String nodeName) {
		this.original = commandNode.getRequirement();
		this.nodeName = nodeName;
		this.redirect = null;
	}

	public String getNodeName() {
		Set<RankCommandPredicate> visited = new HashSet<>();
		RankCommandPredicate currentNode = this;
		while (visited.add(currentNode)) {
			if (currentNode.redirect == null) {
				return currentNode.nodeName;
			}
			RankCommandPredicate nextNode = currentNode.redirect.get();
			if (nextNode == null) {
				return currentNode.nodeName;
			}
			currentNode = nextNode;
		}
		return currentNode.nodeName;
	}

	public void setRedirect(Supplier<RankCommandPredicate> redirect) {
		this.redirect = redirect;
	}

	@Override
	public boolean test(CommandSourceStack source) {
		if (source.getEntity() instanceof ServerPlayer sp) {
			return FTBRanksAPI.getPermissionValue(sp, getNodeName()).asBoolean().orElseGet(() -> original.test(source));
		}

		return original.test(source);
	}
}