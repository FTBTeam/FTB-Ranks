package dev.ftb.mods.ftbranks.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.ftb.mods.ftbranks.FTBRanks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FTBRanks.MOD_ID)
public class FTBRanksForge {
	public FTBRanksForge() {
		EventBuses.registerModEventBus(FTBRanks.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
		new FTBRanks();
		// Nope.
		// PermissionAPI.setPermissionHandler(new PermissionAPIWrapper(PermissionAPI.getPermissionHandler()));
//		ModLoadingContext.get().registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
	}
}
