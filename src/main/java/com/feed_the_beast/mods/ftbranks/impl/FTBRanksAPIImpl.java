package com.feed_the_beast.mods.ftbranks.impl;

import com.feed_the_beast.mods.ftbranks.api.FTBRanksAPI;
import com.feed_the_beast.mods.ftbranks.api.RankManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
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
}
