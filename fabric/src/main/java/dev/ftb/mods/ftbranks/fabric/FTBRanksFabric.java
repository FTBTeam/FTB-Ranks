package dev.ftb.mods.ftbranks.fabric;

import dev.ftb.mods.ftblibrary.fabric.PlayerDisplayNameCallback;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.FTBRanksCommands;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import dev.ftb.mods.ftbranks.api.event.*;
import dev.ftb.mods.ftbranks.api.fabric.FTBRanksEvents;
import dev.ftb.mods.ftbranks.impl.decorate.MessageDecorator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import static dev.ftb.mods.ftblibrary.util.fabric.FabricEventHelper.registerFabricEventPoster;

public class FTBRanksFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		var ranks = new FTBRanks();
		var impl = ranks.getImplementation();

		ServerLifecycleEvents.SERVER_STARTING.register(impl::serverStarting);
		ServerLifecycleEvents.SERVER_STARTED.register(impl::serverStarted);
		ServerLifecycleEvents.SERVER_STOPPED.register(impl::serverStopped);
		ServerLifecycleEvents.AFTER_SAVE.register((ignored, ignored2, ignored3) -> impl.worldSaved());
		CommandRegistrationCallback.EVENT.register(FTBRanksCommands::register);
		ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.STYLING_PHASE, FTBRanksFabric::decorateMessage);
		PlayerDisplayNameCallback.EVENT.register(PlayerNameFormatting::formatPlayerName);

		FTBRanksEvents.REGISTER_CONDITIONS.register(impl::registerConditions);

		registerNativeEventPosters();
	}

	private static void registerNativeEventPosters() {
		registerFabricEventPoster(RegisterConditionsEvent.Data.class, FTBRanksEvents.REGISTER_CONDITIONS);
		registerFabricEventPoster(RanksReloadedEvent.Data.class, FTBRanksEvents.RANK_RELOADED);
		registerFabricEventPoster(RankDeletedEvent.Data.class, FTBRanksEvents.RANK_DELETED);
		registerFabricEventPoster(RankCreatedEvent.Data.class, FTBRanksEvents.RANK_CREATED);
		registerFabricEventPoster(PlayerAddedToRankEvent.Data.class, FTBRanksEvents.PLAYER_ADDED_TO_RANK);
		registerFabricEventPoster(PlayerRemovedFromRankEvent.Data.class, FTBRanksEvents.PLAYER_REMOVED_FROM_RANK);
		registerFabricEventPoster(PermissionNodeChangedEvent.Data.class, FTBRanksEvents.PERMISSION_NODE_CHANGED);
		registerFabricEventPoster(ConditionChangedEvent.Data.class, FTBRanksEvents.CONDITION_CHANGED);
	}

	private static Component decorateMessage(@Nullable ServerPlayer sender, Component message) {
		if (sender != null) {
			MutableComponent mutableComponent = message.copy();
			if (MessageDecorator.decorateMessage(sender, mutableComponent)) {
				return mutableComponent;
			}
		}
		return message;
	}
}
