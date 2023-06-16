package dev.ftb.mods.ftbranks.api;

import dev.ftb.mods.ftblibrary.snbt.SNBT;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Predicate;

/**
 * A rank condition is basically a predicate that determines if a rank is applicable to a player. Each rank has a
 * condition.
 */
public interface RankCondition extends Predicate<ServerPlayer> {
	/**
	 * Get the unique type ID for this condition. This is mainly used for serialization purposes.
	 *
	 * @return the type ID
	 */
	String getType();

	/**
	 * Is this a default condition? The default conditions requires players to be explicitly added to the rank
	 * for the rank to be applicable. A rank with no explicit condition specified will use the default condition.
	 *
	 * @return true if this is the default condition
	 */
	default boolean isDefaultCondition() {
		return false;
	}

	/**
	 * Is this a simple condition? Simple conditions take no arguments, and serialize as just the condition's
	 * type ID. An example of a simple condition is the "op" condition. A non-simple condition example is the
	 * "playtime" condition, which requires a time and optional unit argument.
	 *
	 * @return true if this is a simple condition, false otherwise
	 */
	default boolean isSimple() {
		return false;
	}

	/**
	 * Is this condition's rank applicable to the given player at this time? E.g. the "op" condition will check if
	 * the player is a server operator.
	 *
	 * @param player the player to check
	 * @return true if the condition is applicable to the player
	 */
	boolean isRankActive(ServerPlayer player);

	/**
	 * Save this condition to the given SNBTCompound tag.
	 *
	 * @param tag the SNBT tag to write to
	 */
	default void save(SNBTCompoundTag tag) {
	}

	/**
	 * Dump the condition as a printable string.
	 *
	 * @return the string representation
	 */
	default String asString() {
		if (isDefaultCondition()) {
			return "";
		} else if (isSimple()) {
			return getType();
		} else {
			SNBTCompoundTag tag = new SNBTCompoundTag();
			tag.singleLine();
			tag.putString("type", getType());
			save(tag);
			return String.join(" ", SNBT.writeLines(tag)).replace("\t", "");
		}
	}

	@Override
	default boolean test(ServerPlayer player) {
		return isRankActive(player);
	}

	/**
	 * Convenience interface for simple conditions
	 */
	interface Simple extends RankCondition {
		@Override
		default boolean isSimple() {
			return true;
		}
	}
}