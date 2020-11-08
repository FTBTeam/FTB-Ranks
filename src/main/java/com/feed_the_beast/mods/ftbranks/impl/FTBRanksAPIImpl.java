package com.feed_the_beast.mods.ftbranks.impl;

import com.feed_the_beast.mods.ftbranks.FTBRanks;
import com.feed_the_beast.mods.ftbranks.api.FTBRanksAPI;
import com.feed_the_beast.mods.ftbranks.api.RankManager;
import com.feed_the_beast.mods.ftbranks.impl.condition.AlwaysActiveCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.AndCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.CreativeModeCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.DimensionCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.FakePlayerCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.NotCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.OPCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.OrCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.PlaytimeCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.RankAddedCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.SpawnCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.StatCondition;
import com.feed_the_beast.mods.ftbranks.impl.condition.XorCondition;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = "ftbranks")
public class FTBRanksAPIImpl extends FTBRanksAPI
{
	public static RankManagerImpl manager;

	@Override
	public RankManager getManager()
	{
		return manager;
	}

	@SubscribeEvent
	public static void serverAboutToStart(FMLServerAboutToStartEvent event)
	{
		manager = new RankManagerImpl(event.getServer());
	}

	@SubscribeEvent
	public static void serverStarted(FMLServerStartedEvent event)
	{
		manager.initCommands();

		try
		{
			manager.load();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@SubscribeEvent
	public static void serverStopped(FMLServerStoppedEvent event)
	{
		manager = null;
	}

	@SubscribeEvent
	public static void worldSaved(WorldEvent.Save event)
	{
		if (manager != null)
		{
			manager.saveRanksNow();
			manager.savePlayersNow();
		}
	}

	@SubscribeEvent
	public static void serverStarting(FMLServerStartingEvent event)
	{
		manager.registerCondition("always_active", (rank, json) -> AlwaysActiveCondition.INSTANCE);
		manager.registerCondition("rank_added", RankAddedCondition::new);

		manager.registerCondition("not", NotCondition::new);
		manager.registerCondition("or", OrCondition::new);
		manager.registerCondition("and", AndCondition::new);
		manager.registerCondition("xor", XorCondition::new);

		manager.registerCondition("op", (rank, json) -> new OPCondition());
		manager.registerCondition("spawn", (rank, json) -> new SpawnCondition());
		manager.registerCondition("dimension", (rank, json) -> new DimensionCondition(json));
		manager.registerCondition("playtime", (rank, json) -> new PlaytimeCondition(json));
		manager.registerCondition("stat", (rank, json) -> new StatCondition(json));
		manager.registerCondition("fake_player", (rank, json) -> new FakePlayerCondition());
		manager.registerCondition("creative_mode", (rank, json) -> new CreativeModeCondition());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void serverChat(ServerChatEvent event)
	{
		String format = FTBRanksAPI.getPermissionValue(event.getPlayer(), "ftbranks.name_format").asString().orElse("");

		if (format.isEmpty())
		{
			return;
		}

		StringTextComponent main = new StringTextComponent("");
		StringTextComponent cachedNameForChat;

		try
		{
			cachedNameForChat = TextComponentParser.parse(format, s -> {
				if (s.equals("name"))
				{
					return (IFormattableTextComponent) event.getPlayer().getDisplayName();
				}

				return null;
			});
		}
		catch (Exception ex)
		{
			String s = "Error parsing " + format + ": " + ex;
			FTBRanks.LOGGER.error(s);
			cachedNameForChat = new StringTextComponent("BrokenFormatting");
			cachedNameForChat.mergeStyle(TextFormatting.RED);
			cachedNameForChat.setStyle(cachedNameForChat.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(s))));
		}

		main.append(cachedNameForChat);
		main.appendString(" ");
		//main.appendText("<").appendSibling(event.getPlayer().getDisplayName()).appendText(">").appendText(" ");

		String message = event.getMessage().trim();

		ITextComponent textWithLinks = ForgeHooks.newChatWithLinks(message);
		StringTextComponent text = textWithLinks instanceof StringTextComponent ? (StringTextComponent) textWithLinks : new StringTextComponent(message);

		TextFormatting color = TextFormatting.getValueByName(FTBRanksAPI.getPermissionValue(event.getPlayer(), "ftbranks.chat_text.color").asString().orElse(null));

		if (color != null)
		{
			text.setStyle(text.getStyle().setColor(Color.fromTextFormatting(color)));
		}

		if (FTBRanksAPI.getPermissionValue(event.getPlayer(), "ftbranks.chat_text.bold").asBooleanOrFalse())
		{
			text.setStyle(text.getStyle().setBold(true));
		}

		if (FTBRanksAPI.getPermissionValue(event.getPlayer(), "ftbranks.chat_text.italic").asBooleanOrFalse())
		{
			text.setStyle(text.getStyle().setItalic(true));
		}

		if (FTBRanksAPI.getPermissionValue(event.getPlayer(), "ftbranks.chat_text.underlined").asBooleanOrFalse())
		{
			text.setStyle(text.getStyle().setUnderlined(true));
		}

		if (FTBRanksAPI.getPermissionValue(event.getPlayer(), "ftbranks.chat_text.strikethrough").asBooleanOrFalse())
		{
			text.setStyle(text.getStyle().setStrikethrough(true));
		}

		if (FTBRanksAPI.getPermissionValue(event.getPlayer(), "ftbranks.chat_text.obfuscated").asBooleanOrFalse())
		{
			text.setStyle(text.getStyle().setObfuscated(true));
		}

		main.append(text);
		event.setComponent(main);
	}
}
