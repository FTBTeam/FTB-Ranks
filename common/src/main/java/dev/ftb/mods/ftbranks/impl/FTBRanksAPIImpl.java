package dev.ftb.mods.ftbranks.impl;

import dev.ftb.mods.ftblibrary.util.TextComponentUtils;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.RankManager;
import dev.ftb.mods.ftbranks.impl.condition.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResultHolder;

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

	public static InteractionResultHolder<Component> serverChat(ServerPlayer player, String eventMessage, Component component) {
		String format = FTBRanksAPI.getPermissionValue(player, "ftbranks.name_format").asString().orElse("");

		if (format.isEmpty()) {
			return InteractionResultHolder.pass(component);
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

		String message = eventMessage.trim();
		Component textWithLinks = TextComponentUtils.withLinks(message);
		TextComponent text = null;

		TranslatableComponent fullComp = (TranslatableComponent) component;
		if (fullComp.getKey().equals("chat.type.text")) {
			for (int i = 1; i < ((TranslatableComponent) component).getArgs().length; i++) {
				text = fullComp.getArgs()[i] instanceof Component ? (TextComponent) fullComp.getArgs()[i] : new TextComponent((String) fullComp.getArgs()[i]);
			}
			System.out.print(fullComp.toString());
		} else {
			// Original Method fallback
			text = textWithLinks instanceof TextComponent ? (TextComponent) textWithLinks : new TextComponent(message);
		}

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

		return InteractionResultHolder.success(main);
	}
}
