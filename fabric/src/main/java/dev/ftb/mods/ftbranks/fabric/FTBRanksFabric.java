package dev.ftb.mods.ftbranks.fabric;

import dev.ftb.mods.ftblibrary.fabric.PlayerDisplayNameCallback;
import dev.ftb.mods.ftblibrary.platform.event.EventPostingHandler;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.FTBRanksCommands;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import dev.ftb.mods.ftbranks.api.event.*;
import dev.ftb.mods.ftbranks.impl.FTBRanksAPIImpl;
import dev.ftb.mods.ftbranks.impl.decorate.MessageDecorator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.minecraft.network.chat.MutableComponent;

public class FTBRanksFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		FTBRanks.init();

		ServerLifecycleEvents.SERVER_STARTING.register(FTBRanksAPIImpl::serverStarting);
		ServerLifecycleEvents.SERVER_STARTED.register(FTBRanksAPIImpl::serverStarted);
		ServerLifecycleEvents.SERVER_STOPPED.register(FTBRanksAPIImpl::serverStopped);
		ServerLifecycleEvents.AFTER_SAVE.register((ignored, ignored2, ignored3) -> FTBRanksAPIImpl.worldSaved());

		CommandRegistrationCallback.EVENT.register(FTBRanksCommands::register);

		ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.STYLING_PHASE, (sender, message) -> {
			if (sender != null) {
				MutableComponent mutableComponent = message.copy();
				if (MessageDecorator.decorateMessage(sender, mutableComponent)) {
					return mutableComponent;
				}
			}
			return message;
		});

		PlayerDisplayNameCallback.EVENT.register(PlayerNameFormatting::formatPlayerName);

		EventPostingHandler.INSTANCE.registerEvent(RegisterConditionsEvent.Data.class, data -> FTBRanksFabricEvents.REGISTER_CONDITIONS.invoker().registerConditions(data));
		EventPostingHandler.INSTANCE.registerEvent(RanksReloadedEvent.Data.class, data -> FTBRanksFabricEvents.RANK_RELOADED.invoker().onReload(data));
		EventPostingHandler.INSTANCE.registerEvent(RankDeletedEvent.Data.class, data -> FTBRanksFabricEvents.RANK_DELETED.invoker().onRankDeleted(data));
		EventPostingHandler.INSTANCE.registerEvent(RankCreatedEvent.Data.class, data -> FTBRanksFabricEvents.RANK_CREATED.invoker().onRankCreated(data));
		EventPostingHandler.INSTANCE.registerEvent(PlayerRemovedFromRankEvent.Data.class, data -> FTBRanksFabricEvents.PLAYER_REMOVED_FROM_RANK.invoker().onPlayerRemovedFromRank(data));
		EventPostingHandler.INSTANCE.registerEvent(PlayerAddedToRankEvent.Data.class, data -> FTBRanksFabricEvents.PLAYER_ADDED_TO_RANK.invoker().onPlayerAddedToRank(data));
		EventPostingHandler.INSTANCE.registerEvent(PermissionNodeChangedEvent.Data.class, data -> FTBRanksFabricEvents.PERMISSION_NODE_CHANGED.invoker().onPermissionNodeChanged(data));
		EventPostingHandler.INSTANCE.registerEvent(ConditionChangedEvent.Data.class, data -> FTBRanksFabricEvents.CONDITION_CHANGED.invoker().onConditionChange(data));

		FTBRanksFabricEvents.REGISTER_CONDITIONS.register(FTBRanksAPIImpl::registerConditions);
	}
}
