package dev.ftb.mods.ftbranks.api;

import dev.ftb.mods.ftblibrary.snbt.SNBT;
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

	default String asString() {
		if (isDefaultCondition()) {
			return "";
		} if (isSimple()) {
			return getType();
		} else {
			SNBTCompoundTag c = new SNBTCompoundTag();
			c.singleLine();
			c.putString("type", getType());
			save(c);
			return String.join(" ", SNBT.writeLines(c)).replace("\t", "");
		}
	}
}