package dev.ftb.mods.ftbranks;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.event.RankEvent;
import dev.ftb.mods.ftbranks.impl.FTBRanksAPIImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author LatvianModder
 */
public class FTBRanks {
	public static final String MOD_ID = "ftbranks";
	public static final Logger LOGGER = LogManager.getLogger("FTB Ranks");

	public static void init() {
		FTBRanksAPI.setup(new FTBRanksAPIImpl());

		LifecycleEvent.SERVER_STARTING.register(FTBRanksAPIImpl::serverStarting);
		LifecycleEvent.SERVER_STARTED.register(FTBRanksAPIImpl::serverStarted);
		LifecycleEvent.SERVER_STOPPED.register(FTBRanksAPIImpl::serverStopped);
		LifecycleEvent.SERVER_LEVEL_SAVE.register(FTBRanksAPIImpl::worldSaved);

		RankEvent.REGISTER_CONDITIONS.register(FTBRanksAPIImpl::registerConditions);

		CommandRegistrationEvent.EVENT.register(FTBRanksCommands::register);
	}
}
