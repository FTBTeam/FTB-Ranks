package dev.ftb.mods.ftbranks.api;

import com.google.gson.JsonObject;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public interface RankCondition {
	String getType();

	default boolean isDefaultCondition() {
		return false;
	}

	boolean isRankActive(ServerPlayer player);

	default void save(JsonObject json) {
	}
}