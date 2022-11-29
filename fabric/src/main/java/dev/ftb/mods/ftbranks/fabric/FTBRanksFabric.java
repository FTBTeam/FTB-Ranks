package dev.ftb.mods.ftbranks.fabric;

import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.RankArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.minecraft.resources.ResourceLocation;

public class FTBRanksFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		new FTBRanks();

		ArgumentTypeRegistry.registerArgumentType(new ResourceLocation(FTBRanks.MOD_ID, "rank"),
				RankArgumentType.class, new RankArgumentType.Info());
	}
}
