package dev.ftb.mods.ftbranks.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.MessageDecorator;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FTBRanks.MOD_ID)
public class FTBRanksForge {

	public FTBRanksForge() {
		EventBuses.registerModEventBus(FTBRanks.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

		MinecraftForge.EVENT_BUS.addListener(this::playerNameFormatting);
		MinecraftForge.EVENT_BUS.addListener(this::serverChat);

		new FTBRanks();

		// Nope.
		// PermissionAPI.setPermissionHandler(new PermissionAPIWrapper(PermissionAPI.getPermissionHandler()));

		ModArgumentTypeInfo.ARGUMENT_TYPE_INFO.register(FMLJavaModLoadingContext.get().getModEventBus());
	}

	private void playerNameFormatting(PlayerEvent.NameFormat event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			event.setDisplayname(PlayerNameFormatting.formatPlayerName(player, event.getDisplayname()));
		}
	}

	private void serverChat(ServerChatEvent event) {
		if (event.canChangeMessage()) {
			MutableComponent text = event.getMessage().copy();
			if (MessageDecorator.decorateMessage(event.getPlayer(), text)) {
				event.setMessage(text);
			}
		}

	}

}
