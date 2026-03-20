package dev.ftb.mods.ftbranks.impl;

import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.event.RegisterConditionsEvent;
import dev.ftb.mods.ftbranks.impl.condition.*;
import dev.ftb.mods.ftbranks.impl.permission.BooleanPermissionValue;
import dev.ftb.mods.ftbranks.impl.permission.NumberPermissionValue;
import dev.ftb.mods.ftbranks.impl.permission.StringPermissionValue;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Objects;

public class FTBRanksAPIImpl extends FTBRanksAPI {
	@Nullable
	public static RankManagerImpl manager;

	@Override
	public RankManagerImpl getManager() {
		return Objects.requireNonNull(manager);
	}

	@Override
	@Nullable
	public PermissionValue parsePermissionValue(@Nullable String str) {
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

	public void serverStarting(MinecraftServer server) {
		manager = new RankManagerImpl(server);

		NativeEventPosting.get().postEvent(new RegisterConditionsEvent.Data((id, factory) -> manager.registerCondition(id, factory)));
	}

	public void serverStarted(MinecraftServer ignoredServer) {
		if (manager != null) {
			try {
				manager.load();
			} catch (IOException ex) {
				FTBRanks.LOGGER.error("failed to load ranks data: {} / {}", ex.getClass().getName(), ex.getMessage());
			}
		}
	}

	public void serverStopped(MinecraftServer ignoredServer) {
		manager = null;
	}

	public void worldSaved() {
		if (manager != null) {
			manager.saveRanksNow();
			manager.savePlayersNow();
		}
	}

	public void registerConditions(RegisterConditionsEvent.Data data) {
		data.registerSimple("always_active", () -> AlwaysActiveCondition.INSTANCE);
		data.registerSimple("op", OPCondition::new);
		data.registerSimple("spawn",SpawnCondition::new);
		data.registerSimple("fake_player", FakePlayerCondition::new);
		data.registerSimple("creative_mode",CreativeModeCondition::new);

		data.register("dimension", (ignored, json) -> new DimensionCondition(json));
		data.register("playtime", (ignored, json) -> new PlaytimeCondition(json));
		data.register("stat", (ignored, json) -> new StatCondition(json));
		data.register("rank_added", RankAddedCondition::new);
		data.register("rank_applies", RankAppliesCondition::new);

		data.register("not", NotCondition::new);
		data.register("or", OrCondition::new);
		data.register("and", AndCondition::new);
		data.register("xor", XorCondition::new);
	}
}
