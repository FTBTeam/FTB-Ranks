package dev.ftb.mods.ftbranks.fabric;

import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.MessageDecorator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.concurrent.CompletableFuture;

public class FTBRanksFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		new FTBRanks();

		ServerMessageDecoratorEvent.EVENT.register(ServerMessageDecoratorEvent.STYLING_PHASE, (sender, message) -> {
			if (sender != null) {
				MutableComponent mutableComponent = message.copy();
				if (MessageDecorator.decorateMessage(sender, mutableComponent)) {
					return CompletableFuture.completedFuture(mutableComponent);
				}
			}
			return CompletableFuture.completedFuture(message);
		});
	}
}
