package dev.ftb.mods.ftbranks.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FTBRanks.MOD_ID)
public class FTBRanksForge {

	public FTBRanksForge() {
		EventBuses.registerModEventBus(FTBRanks.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

		MinecraftForge.EVENT_BUS.addListener(this::playerNameFormatting);

		new FTBRanks();

		// Nope.
		// PermissionAPI.setPermissionHandler(new PermissionAPIWrapper(PermissionAPI.getPermissionHandler()));

//		ModLoadingContext.get().registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));

		ModArgumentTypeInfo.ARGUMENT_TYPE_INFO.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	private void playerNameFormatting(PlayerEvent.NameFormat event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			event.setDisplayname(PlayerNameFormatting.formatPlayerName(player, event.getDisplayname()));
		}
	}
}
