package dev.ftb.mods.ftbranks.fabric;

import dev.ftb.mods.ftbranks.FTBRanks;
import net.fabricmc.api.ModInitializer;

public class FTBRanksFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		new FTBRanks();
	}
}
