package dev.ftb.mods.ftbranks.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import dev.ftb.mods.ftbranks.FTBRanks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FTBRanksCommandManager {
	@Nullable
	private static FTBRanksCommandManager INSTANCE;

	public final Map<String, RankCommandPredicate> commandMap;
	public final Map<CommandNode<CommandSourceStack>, RankCommandPredicate> commandNodes;

	private FTBRanksCommandManager(Commands commands) {
		commandMap = new HashMap<>();
		commandNodes = new HashMap<>();

		FTBRanks.LOGGER.info("Loading command nodes...");

		try {
			// Absolute cancer but ATs don't work here //
			Field field = CommandNode.class.getDeclaredField("requirement");
			field.setAccessible(true);
			getCommandNodes(commands.getDispatcher(), "command", field, commands.getDispatcher().getRoot());
		} catch (Throwable ex) {
			ex.printStackTrace();
			FTBRanks.LOGGER.error("Reflection failed! Downgrading Java version to 8 might help");
		}

        FTBRanks.LOGGER.info("Loaded {} command nodes", this.commandMap.size());
	}

	public static void init(Commands commands) {
		INSTANCE = new FTBRanksCommandManager(commands);
	}

	public static List<String> allNodes() {
		if (INSTANCE == null) {
			return List.of("ERROR: command manager not initialised?)");
		}
		return INSTANCE.commandMap.values().stream()
				.map(RankCommandPredicate::getNodeName)
				.distinct()
				.sorted()
				.toList();
	}

	private void getCommandNodes(CommandDispatcher<CommandSourceStack> dispatcher, String perm, Field field, CommandNode<CommandSourceStack> node) throws Exception {
		for (CommandNode<CommandSourceStack> childNode : node.getChildren()) {
			if (childNode.isFork()) {
				continue;
			}

			String nodeName = perm + "." + childNode.getName().replace("*", "all");
			FTBRanks.LOGGER.debug(nodeName);
			RankCommandPredicate predicate = new RankCommandPredicate(childNode, nodeName);
			field.set(childNode, predicate);
			commandMap.put(nodeName, predicate);
			commandNodes.put(childNode, predicate);
			getCommandNodes(dispatcher, nodeName, field, childNode);

			if (childNode.getRedirect() != null && childNode.getRedirect() != dispatcher.getRoot()) {
				predicate.setRedirect(() -> commandNodes.get(childNode.getRedirect()));
			}
		}
	}
}
