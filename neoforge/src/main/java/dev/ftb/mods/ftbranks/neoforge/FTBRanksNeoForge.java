package dev.ftb.mods.ftbranks.neoforge;

import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import dev.ftb.mods.ftbranks.impl.decorate.MessageDecorator;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@Mod(FTBRanks.MOD_ID)
public class FTBRanksNeoForge {
	public FTBRanksNeoForge() {
		NeoForge.EVENT_BUS.addListener(this::playerNameFormatting);
		NeoForge.EVENT_BUS.addListener(this::serverChat);

		FTBRanks.init();

		// Nope.
		// PermissionAPI.setPermissionHandler(new PermissionAPIWrapper(PermissionAPI.getPermissionHandler()));
	}

	private void playerNameFormatting(PlayerEvent.NameFormat event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			event.setDisplayname(PlayerNameFormatting.formatPlayerName(player, event.getDisplayname()));
		}
	}

	private void serverChat(ServerChatEvent event) {
		MutableComponent text = event.getMessage().copy();
		if (MessageDecorator.decorateMessage(event.getPlayer(), text)) {
			event.setMessage(text);
		}
	}
}
