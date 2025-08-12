package dev.ftb.mods.ftbranks.impl;

import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.RankManager;
import dev.ftb.mods.ftbranks.api.event.RankEvent;
import dev.ftb.mods.ftbranks.api.event.RegisterConditionsEvent;
import dev.ftb.mods.ftbranks.impl.condition.*;
import dev.ftb.mods.ftbranks.impl.permission.BooleanPermissionValue;
import dev.ftb.mods.ftbranks.impl.permission.NumberPermissionValue;
import dev.ftb.mods.ftbranks.impl.permission.StringPermissionValue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;

public class FTBRanksAPIImpl extends FTBRanksAPI {
	public static RankManagerImpl manager;

	@Override
	protected RankManager getManager() {
		return manager;
	}

	@Override
	public PermissionValue parsePermissionValue(String str) {
		if (str == null) {
			return null;
		} else if (str.startsWith("\"") && str.endsWith("\"")) {
			return StringPermissionValue.of(str.substring(1, str.length() - 1));
		} else if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false")) {
			return BooleanPermissionValue.of(str.equalsIgnoreCase("true"));
		} else if (NumberUtils.isCreatable(str)) {
			return NumberPermissionValue.of(NumberUtils.createNumber(str));
		} else {
			return StringPermissionValue.of(str);
		}
	}

	public static void serverStarting(MinecraftServer server) {
		manager = new RankManagerImpl(server);

		RankEvent.REGISTER_CONDITIONS.invoker().accept(new RegisterConditionsEvent((id, factory) -> manager.registerCondition(id, factory)));
	}

	public static void serverStarted(MinecraftServer server) {
		try {
			manager.load();
		} catch (IOException ex) {
			FTBRanks.LOGGER.error("failed to load ranks data: {} / {}", ex.getClass().getName(), ex.getMessage());
		}
	}

	public static void serverStopped(MinecraftServer server) {
		manager = null;
	}

	public static void worldSaved(ServerLevel event) {
		if (manager != null) {
			manager.saveRanksNow();
			manager.savePlayersNow();
		}
	}

	public static void registerConditions(RegisterConditionsEvent event) {
		event.register("always_active", (rank, json) -> AlwaysActiveCondition.INSTANCE);
		event.register("rank_added", RankAddedCondition::new);
		event.register("rank_applies", RankAppliesCondition::new);

		event.register("not", NotCondition::new);
		event.register("or", OrCondition::new);
		event.register("and", AndCondition::new);
		event.register("xor", XorCondition::new);

		event.register("op", (rank, tag) -> new OPCondition());
		event.register("spawn", (rank, tag) -> new SpawnCondition());
		event.register("dimension", (rank, tag) -> new DimensionCondition(tag));
		event.register("playtime", (rank, tag) -> new PlaytimeCondition(tag));
		event.register("stat", (rank, tag) -> new StatCondition(tag));
		event.register("fake_player", (rank, tag) -> new FakePlayerCondition());
		event.register("creative_mode", (rank, tag) -> new CreativeModeCondition());
	}
}
