package dev.ftb.mods.ftbranks.api;

import de.marhali.json5.Json5;
import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import de.marhali.json5.config.Json5Options;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
	 * Save this condition to the given Json5 object.
	 *
	 * @param json the Json5 object to write to
	 * @return the passed Json5 object with data written to it
	 */
	default Json5Object save(Json5Object json) {
		return json;
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
            try {
				Json5Object json = new Json5Object();
				json.addProperty("type", getType());
				save(json);
                return new Json5(Json5Options.builder().indentFactor(0).build()).serialize(json);
            } catch (IOException e) {
				return "<can't serialize rank>";
            }
		}
	}

	@Override
	default boolean test(ServerPlayer player) {
		return isRankActive(player);
	}

	default List<RankCondition> getConditionList(Json5Object json, String field, Rank rank) {
		return Util.make(new ArrayList<>(), l -> {
			Json5Element el = json.get(field);
			if (el.isJson5Array()) {
				for (Json5Element member : el.getAsJson5Array()) {
					l.add(rank.getManager().createCondition(rank, member));
				}
			}
		});
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