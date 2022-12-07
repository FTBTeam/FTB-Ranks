package dev.ftb.mods.ftbranks.fabric;

import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.MessageDecorator;
import dev.ftb.mods.ftbranks.RankArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class FTBRanksFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		new FTBRanks();

		ArgumentTypeRegistry.registerArgumentType(new ResourceLocation(FTBRanks.MOD_ID, "rank"),
				RankArgumentType.class, new RankArgumentType.Info());

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
