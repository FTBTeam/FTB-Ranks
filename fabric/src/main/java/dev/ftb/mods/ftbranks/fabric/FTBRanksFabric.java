package dev.ftb.mods.ftbranks.fabric;

import dev.ftb.mods.ftblibrary.fabric.PlayerDisplayNameCallback;
import dev.ftb.mods.ftblibrary.platform.event.EventPostingHandler;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.FTBRanksCommands;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import dev.ftb.mods.ftbranks.api.event.*;
import dev.ftb.mods.ftbranks.api.fabric.FTBRanksEvents;
import dev.ftb.mods.ftbranks.impl.FTBRanksAPIImpl;
import dev.ftb.mods.ftbranks.impl.decorate.MessageDecorator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public class FTBRanksFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		var ranks = new FTBRanks();

		ServerLifecycleEvents.SERVER_STARTING.register(FTBRanksAPIImpl::serverStarting);
		ServerLifecycleEvents.SERVER_STARTED.register(FTBRanksAPIImpl::serverStarted);
		ServerLifecycleEvents.SERVER_STOPPED.register(FTBRanksAPIImpl::serverStopped);
		ServerLifecycleEvents.AFTER_SAVE.register((ignored, ignored2, ignored3) -> FTBRanksAPIImpl.worldSaved());
		CommandRegistrationCallback.EVENT.register(FTBRanksCommands::register);
		ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.STYLING_PHASE, FTBRanksFabric::decorateMessage);
		PlayerDisplayNameCallback.EVENT.register(PlayerNameFormatting::formatPlayerName);

		FTBRanksEvents.REGISTER_CONDITIONS.register(FTBRanksAPIImpl::registerConditions);

		registerNativeEventPosting();
	}

	private static void registerNativeEventPosting() {
		EventPostingHandler.INSTANCE.registerEvent(RegisterConditionsEvent.Data.class,
				data -> FTBRanksEvents.REGISTER_CONDITIONS.invoker().registerConditions(data));
		EventPostingHandler.INSTANCE.registerEvent(RanksReloadedEvent.Data.class,
				data -> FTBRanksEvents.RANK_RELOADED.invoker().onReload(data));
		EventPostingHandler.INSTANCE.registerEvent(RankDeletedEvent.Data.class,
				data -> FTBRanksEvents.RANK_DELETED.invoker().onRankDeleted(data));
		EventPostingHandler.INSTANCE.registerEvent(RankCreatedEvent.Data.class,
				data -> FTBRanksEvents.RANK_CREATED.invoker().onRankCreated(data));
		EventPostingHandler.INSTANCE.registerEvent(PlayerRemovedFromRankEvent.Data.class,
				data -> FTBRanksEvents.PLAYER_REMOVED_FROM_RANK.invoker().onPlayerRemovedFromRank(data));
		EventPostingHandler.INSTANCE.registerEvent(PlayerAddedToRankEvent.Data.class,
				data -> FTBRanksEvents.PLAYER_ADDED_TO_RANK.invoker().onPlayerAddedToRank(data));
		EventPostingHandler.INSTANCE.registerEvent(PermissionNodeChangedEvent.Data.class,
				data -> FTBRanksEvents.PERMISSION_NODE_CHANGED.invoker().onPermissionNodeChanged(data));
		EventPostingHandler.INSTANCE.registerEvent(ConditionChangedEvent.Data.class,
				data -> FTBRanksEvents.CONDITION_CHANGED.invoker().onConditionChange(data));
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
