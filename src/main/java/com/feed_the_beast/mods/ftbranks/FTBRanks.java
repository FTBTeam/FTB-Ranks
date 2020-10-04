package com.feed_the_beast.mods.ftbranks;

import com.feed_the_beast.mods.ftbranks.api.FTBRanksAPI;
import com.feed_the_beast.mods.ftbranks.impl.FTBRanksAPIImpl;
import com.feed_the_beast.mods.ftbranks.impl.PermissionAPIWrapper;
import com.feed_the_beast.mods.ftbranks.impl.TextComponentParser;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author LatvianModder
 */
@Mod("ftbranks")
public class FTBRanks
{
	public static FTBRanks instance;
	public static final Logger LOGGER = LogManager.getLogger("FTB Ranks");

	public FTBRanks()
	{
		instance = this;
		FTBRanksAPI.INSTANCE = new FTBRanksAPIImpl();
		MinecraftForge.EVENT_BUS.register(FTBRanks.class);
		PermissionAPI.setPermissionHandler(new PermissionAPIWrapper(PermissionAPI.getPermissionHandler()));
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
	}

	@SubscribeEvent
	public static void serverStarting(FMLServerStartingEvent event)
	{
		FTBRanksAPI.INSTANCE.getManager().registerCondition("always_active", (rank, json) -> AlwaysActiveCondition.INSTANCE);
		FTBRanksAPI.INSTANCE.getManager().registerCondition("rank_added", RankAddedCondition::new);

		FTBRanksAPI.INSTANCE.getManager().registerCondition("not", NotCondition::new);
		FTBRanksAPI.INSTANCE.getManager().registerCondition("or", OrCondition::new);
		FTBRanksAPI.INSTANCE.getManager().registerCondition("and", AndCondition::new);
		FTBRanksAPI.INSTANCE.getManager().registerCondition("xor", XorCondition::new);

		FTBRanksAPI.INSTANCE.getManager().registerCondition("op", (rank, json) -> new OPCondition());
		FTBRanksAPI.INSTANCE.getManager().registerCondition("spawn", (rank, json) -> new SpawnCondition());
		FTBRanksAPI.INSTANCE.getManager().registerCondition("dimension", (rank, json) -> new DimensionCondition(json));
		FTBRanksAPI.INSTANCE.getManager().registerCondition("playtime", (rank, json) -> new PlaytimeCondition(json));
		FTBRanksAPI.INSTANCE.getManager().registerCondition("stat", (rank, json) -> new StatCondition(json));
		FTBRanksAPI.INSTANCE.getManager().registerCondition("fake_player", (rank, json) -> new FakePlayerCondition());
		FTBRanksAPI.INSTANCE.getManager().registerCondition("creative_mode", (rank, json) -> new CreativeModeCondition());
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