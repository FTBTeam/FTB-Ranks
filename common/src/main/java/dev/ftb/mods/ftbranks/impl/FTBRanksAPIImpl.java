package dev.ftb.mods.ftbranks.impl;

import com.mojang.datafixers.types.Type;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.ChatEvent;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.RankManager;
import dev.ftb.mods.ftbranks.impl.condition.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * @author LatvianModder
 */
public class FTBRanksAPIImpl extends FTBRanksAPI {
	public static RankManagerImpl manager;

	@Override
	public RankManager getManager() {
		return manager;
	}

	public static void serverAboutToStart(MinecraftServer server) {
		manager = new RankManagerImpl(server);
	}

	public static void serverStarted(MinecraftServer server) {
		// manager.initCommands();

		try {
			manager.load();
		} catch (Exception ex) {
			ex.printStackTrace();
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

	public static void serverStarting(MinecraftServer server) {
		manager.registerCondition("always_active", (rank, json) -> AlwaysActiveCondition.INSTANCE);
		manager.registerCondition("rank_added", RankAddedCondition::new);

		manager.registerCondition("not", NotCondition::new);
		manager.registerCondition("or", OrCondition::new);
		manager.registerCondition("and", AndCondition::new);
		manager.registerCondition("xor", XorCondition::new);

		manager.registerCondition("op", (rank, tag) -> new OPCondition());
		manager.registerCondition("spawn", (rank, tag) -> new SpawnCondition());
		manager.registerCondition("dimension", (rank, tag) -> new DimensionCondition(tag));
		manager.registerCondition("playtime", (rank, tag) -> new PlaytimeCondition(tag));
		manager.registerCondition("stat", (rank, tag) -> new StatCondition(tag));
		manager.registerCondition("fake_player", (rank, tag) -> new FakePlayerCondition());
		manager.registerCondition("creative_mode", (rank, tag) -> new CreativeModeCondition());
	}

	public static EventResult chatReceived(@Nullable ServerPlayer player, Component component) {
		if (component instanceof MutableComponent text) {
			// NOTE: only message text is decorated here; see PlayerNameFormatting for decoration of sender names
			ChatFormatting color = ChatFormatting.getByName(FTBRanksAPI.getPermissionValue(player, "ftbranks.chat_text.color").asString().orElse(null));
			if (color != null) {
				text.setStyle(text.getStyle().applyFormat(color));
			}

			modifyText(player, text, "ftbranks.chat_text.bold", ChatFormatting.BOLD);
			modifyText(player, text, "ftbranks.chat_text.italic", ChatFormatting.ITALIC);
			modifyText(player, text, "ftbranks.chat_text.underlined", ChatFormatting.UNDERLINE);
			modifyText(player, text, "ftbranks.chat_text.strikethrough", ChatFormatting.STRIKETHROUGH);
			modifyText(player, text, "ftbranks.chat_text.obfuscated", ChatFormatting.OBFUSCATED);
			return EventResult.interruptTrue();
		}
		return EventResult.pass();
	}

	private static void modifyText(ServerPlayer player, MutableComponent component, String node, ChatFormatting modifier) {
		if (FTBRanksAPI.getPermissionValue(player, node).asBooleanOrFalse()) {
			component.setStyle(component.getStyle().applyFormat(modifier));
		}
	}
}
