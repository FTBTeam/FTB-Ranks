package dev.ftb.mods.ftbranks;

import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.impl.FTBRanksAPIImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FTBRanks {
	public static final String MOD_ID = "ftbranks";
	public static final Logger LOGGER = LogManager.getLogger("FTB Ranks");

	public FTBRanks() {
		FTBRanksAPI.setup(new FTBRanksAPIImpl());
	}
}
