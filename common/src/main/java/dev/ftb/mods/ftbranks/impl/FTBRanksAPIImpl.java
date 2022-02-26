package dev.ftb.mods.ftbranks.impl;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.ChatEvent;
import dev.ftb.mods.ftblibrary.util.TextComponentUtils;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.RankManager;
import dev.ftb.mods.ftbranks.impl.condition.AlwaysActiveCondition;
import dev.ftb.mods.ftbranks.impl.condition.AndCondition;
import dev.ftb.mods.ftbranks.impl.condition.CreativeModeCondition;
import dev.ftb.mods.ftbranks.impl.condition.DimensionCondition;
import dev.ftb.mods.ftbranks.impl.condition.FakePlayerCondition;
import dev.ftb.mods.ftbranks.impl.condition.NotCondition;
import dev.ftb.mods.ftbranks.impl.condition.OPCondition;
import dev.ftb.mods.ftbranks.impl.condition.OrCondition;
import dev.ftb.mods.ftbranks.impl.condition.PlaytimeCondition;
import dev.ftb.mods.ftbranks.impl.condition.RankAddedCondition;
import dev.ftb.mods.ftbranks.impl.condition.SpawnCondition;
import dev.ftb.mods.ftbranks.impl.condition.StatCondition;
import dev.ftb.mods.ftbranks.impl.condition.XorCondition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.TextFilter;

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

	// TODO: rework(?)
	public static EventResult serverChat(ServerPlayer player, TextFilter.FilteredText eventMessage, ChatEvent.ChatComponent component) {
		String format = FTBRanksAPI.getPermissionValue(player, "ftbranks.name_format").asString().orElse("");

		if (format.isEmpty()) {
			return EventResult.pass();
		}

		TextComponent main = new TextComponent("");
		TextComponent cachedNameForChat;

		try {
			cachedNameForChat = TextComponentParser.parse(format, s -> {
				if (s.equals("name")) {
					return player.getDisplayName();
				}

				return null;
			});
		} catch (Exception ex) {
			String s = "Error parsing " + format + ": " + ex;
			FTBRanks.LOGGER.error(s);
			cachedNameForChat = new TextComponent("BrokenFormatting");
			cachedNameForChat.withStyle(ChatFormatting.RED);
			cachedNameForChat.setStyle(cachedNameForChat.getStyle().withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(s))));
		}

		main.append(cachedNameForChat);
		main.append(" ");
		//main.appendText("<").appendSibling(event.getPlayer().getDisplayName()).appendText(">").appendText(" ");

		String message = eventMessage.getFiltered().trim();

		Component textWithLinks = TextComponentUtils.withLinks(message);
		TextComponent text = textWithLinks instanceof TextComponent ? (TextComponent) textWithLinks : new TextComponent(message);

		ChatFormatting color = ChatFormatting.getByName(FTBRanksAPI.getPermissionValue(player, "ftbranks.chat_text.color").asString().orElse(null));

		if (color != null) {
			text.setStyle(text.getStyle().applyFormat(color));
		}

		if (FTBRanksAPI.getPermissionValue(player, "ftbranks.chat_text.bold").asBooleanOrFalse()) {
			text.setStyle(text.getStyle().applyFormat(ChatFormatting.BOLD));
		}

		if (FTBRanksAPI.getPermissionValue(player, "ftbranks.chat_text.italic").asBooleanOrFalse()) {
			text.setStyle(text.getStyle().applyFormat(ChatFormatting.ITALIC));
		}

		if (FTBRanksAPI.getPermissionValue(player, "ftbranks.chat_text.underlined").asBooleanOrFalse()) {
			text.setStyle(text.getStyle().applyFormat(ChatFormatting.UNDERLINE));
		}

		if (FTBRanksAPI.getPermissionValue(player, "ftbranks.chat_text.strikethrough").asBooleanOrFalse()) {
			text.setStyle(text.getStyle().applyFormat(ChatFormatting.STRIKETHROUGH));
		}

		if (FTBRanksAPI.getPermissionValue(player, "ftbranks.chat_text.obfuscated").asBooleanOrFalse()) {
			text.setStyle(text.getStyle().applyFormat(ChatFormatting.OBFUSCATED));
		}

		main.append(text);

		component.setFiltered(main);

		return EventResult.interruptTrue();
	}
}
