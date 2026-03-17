package dev.ftb.mods.ftbranks.neoforge;

import dev.ftb.mods.ftblibrary.platform.event.EventPostingHandler;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.FTBRanksCommands;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import dev.ftb.mods.ftbranks.api.event.*;
import dev.ftb.mods.ftbranks.api.neoforge.FTBRanksEvent;
import dev.ftb.mods.ftbranks.impl.FTBRanksAPIImpl;
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

@Mod(FTBRanks.MOD_ID)
public class FTBRanksNeoForge {
	private final FTBRanks ranks;

	public FTBRanksNeoForge(IEventBus modBus) {
		ranks = new FTBRanks();

		IEventBus bus = NeoForge.EVENT_BUS;

		bus.addListener(ServerStartingEvent.class, event -> FTBRanksAPIImpl.serverStarting(event.getServer()));
		bus.addListener(ServerStartedEvent.class, event -> FTBRanksAPIImpl.serverStarted(event.getServer()));
		bus.addListener(ServerStoppedEvent.class, event -> FTBRanksAPIImpl.serverStopped(event.getServer()));
		bus.addListener(LevelEvent.Save.class, ignored -> FTBRanksAPIImpl.worldSaved());
		bus.addListener(this::playerNameFormatting);
		bus.addListener(this::serverChat);
		bus.addListener(this::registerCommands);

		bus.addListener(FTBRanksEvent.RegisterConditions.class, event -> FTBRanksAPIImpl.registerConditions(event.getData()));

		registerNativeEventPosting(bus);
	}

	private static void registerNativeEventPosting(IEventBus bus) {
		EventPostingHandler.INSTANCE.registerEvent(RegisterConditionsEvent.Data.class,
				data -> bus.post(new FTBRanksEvent.RegisterConditions(data)));
		EventPostingHandler.INSTANCE.registerEvent(RanksReloadedEvent.Data.class,
				data -> bus.post(new FTBRanksEvent.Reloaded(data)));
		EventPostingHandler.INSTANCE.registerEvent(RankDeletedEvent.Data.class,
				data -> bus.post(new FTBRanksEvent.Deleted(data)));
		EventPostingHandler.INSTANCE.registerEvent(RankCreatedEvent.Data.class,
				data -> bus.post(new FTBRanksEvent.Created(data)));
		EventPostingHandler.INSTANCE.registerEvent(PlayerRemovedFromRankEvent.Data.class,
				data -> bus.post(new FTBRanksEvent.PlayerRemoved(data)));
		EventPostingHandler.INSTANCE.registerEvent(PermissionNodeChangedEvent.Data.class,
				data -> bus.post(new FTBRanksEvent.NodeChanged(data)));
		EventPostingHandler.INSTANCE.registerEvent(ConditionChangedEvent.Data.class,
				data -> bus.post(new FTBRanksEvent.ConditionChanged(data)));
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
