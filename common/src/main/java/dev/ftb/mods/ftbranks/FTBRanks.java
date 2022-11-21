package dev.ftb.mods.ftbranks;

import dev.architectury.event.events.common.ChatEvent;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.impl.FTBRanksAPIImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author LatvianModder
 */
public class FTBRanks {
	public static final String MOD_ID = "ftbranks";
	public static final Logger LOGGER = LogManager.getLogger("FTB Ranks");

	public FTBRanks() {
		FTBRanksAPI.INSTANCE = new FTBRanksAPIImpl();
		LifecycleEvent.SERVER_BEFORE_START.register(FTBRanksAPIImpl::serverAboutToStart);
		LifecycleEvent.SERVER_STARTED.register(FTBRanksAPIImpl::serverStarted);
		LifecycleEvent.SERVER_STOPPED.register(FTBRanksAPIImpl::serverStopped);
		LifecycleEvent.SERVER_LEVEL_SAVE.register(FTBRanksAPIImpl::worldSaved);
		LifecycleEvent.SERVER_STARTING.register(FTBRanksAPIImpl::serverStarting);
		CommandRegistrationEvent.EVENT.register(FTBRanksCommands::register);
		// TODO: Register with LOWEST priority on forge
		// FIXME: add back by syncing the ranks.snbt file to clients so they can format the text on the client.
		ChatEvent.RECEIVED.register(FTBRanksAPIImpl::serverChat);
	}
}
