package dev.ftb.mods.ftbranks.neoforge;

import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.FTBRanksCommands;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import dev.ftb.mods.ftbranks.api.event.*;
import dev.ftb.mods.ftbranks.api.neoforge.FTBRanksEvent;
import dev.ftb.mods.ftbranks.impl.decorate.MessageDecorator;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import static dev.ftb.mods.ftblibrary.util.neoforge.NeoEventHelper.registerNeoEventPoster;

@Mod(FTBRanks.MOD_ID)
public class FTBRanksNeoForge {
	private final FTBRanks ranks;

	public FTBRanksNeoForge(IEventBus modBus) {
		ranks = new FTBRanks();
		var impl = ranks.getImplementation();

		IEventBus bus = NeoForge.EVENT_BUS;

		bus.addListener(ServerStartingEvent.class, event -> impl.serverStarting(event.getServer()));
		bus.addListener(ServerStartedEvent.class, event -> impl.serverStarted(event.getServer()));
		bus.addListener(ServerStoppedEvent.class, event -> impl.serverStopped(event.getServer()));
		bus.addListener(LevelEvent.Save.class, ignored -> impl.worldSaved());
		bus.addListener(this::playerNameFormatting);
		bus.addListener(this::serverChat);
		bus.addListener(this::registerCommands);

		bus.addListener(FTBRanksEvent.RegisterConditions.class, event -> impl.registerConditions(event.getEventData()));

		registerNativeEventPosters(bus);
	}

	private static void registerNativeEventPosters(IEventBus bus) {
		registerNeoEventPoster(bus, RegisterConditionsEvent.Data.class, FTBRanksEvent.RegisterConditions::new);
		registerNeoEventPoster(bus, RanksReloadedEvent.Data.class, FTBRanksEvent.Reloaded::new);
		registerNeoEventPoster(bus, RankCreatedEvent.Data.class, FTBRanksEvent.Created::new);
		registerNeoEventPoster(bus, RankDeletedEvent.Data.class, FTBRanksEvent.Deleted::new);
		registerNeoEventPoster(bus, PlayerAddedToRankEvent.Data.class, FTBRanksEvent.PlayerAdded::new);
		registerNeoEventPoster(bus, PlayerRemovedFromRankEvent.Data.class, FTBRanksEvent.PlayerRemoved::new);
		registerNeoEventPoster(bus, PermissionNodeChangedEvent.Data.class, FTBRanksEvent.PermissionNodeChanged::new);
		registerNeoEventPoster(bus, ConditionChangedEvent.Data.class, FTBRanksEvent.ConditionChanged::new);
	}

	private void registerCommands(RegisterCommandsEvent event) {
		FTBRanksCommands.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
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
