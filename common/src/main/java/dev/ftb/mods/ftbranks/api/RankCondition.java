package dev.ftb.mods.ftbranks.api;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public interface RankCondition {
	String getType();

	default boolean isDefaultCondition() {
		return false;
	}

	default boolean isSimple() {
		return false;
	}

	boolean isRankActive(ServerPlayer player);

	default void save(SNBTCompoundTag tag) {
	}
}