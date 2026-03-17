package dev.ftb.mods.ftbranks.impl.condition;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.util.Json5Util;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;

import java.util.NoSuchElementException;

public class StatCondition implements RankCondition {
	public static final int EQUALS = 1;
	public static final int NOT_EQUALS = 2;
	public static final int GREATER = 3;
	public static final int GREATER_OR_EQUAL = 4;
	public static final int LESSER = 5;
	public static final int LESSER_OR_EQUAL = 6;

	private final Identifier statId;
	private final int value;
	private final int valueCheck;
	private final Stat<?> stat;

	public StatCondition(Json5Object json) {
		statId = Identifier.parse(Json5Util.getString(json, "stat").orElse(""));
		stat = BuiltInRegistries.CUSTOM_STAT.getOptional(statId)
				.map(Stats.CUSTOM::get)
				.orElseThrow(() ->
						new NoSuchElementException(String.format("%s does not match any known stat", statId))
				);
		value = Json5Util.getInt(json, "value").orElse(0);

		switch (Json5Util.getString(json, "value_check").orElse("")) {
			case "not_equals", "not", "!=" -> valueCheck = NOT_EQUALS;
			case "greater", ">" -> valueCheck = GREATER;
			case "greater_or_equal", ">=" -> valueCheck = GREATER_OR_EQUAL;
			case "lesser", "<" -> valueCheck = LESSER;
			case "lesser_or_equal", "<=" -> valueCheck = LESSER_OR_EQUAL;
			default -> valueCheck = EQUALS;
		}
	}

	@Override
	public String getType() {
		return "stat";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		int v = player.getStats().getValue(stat);

		return switch (valueCheck) {
			case NOT_EQUALS -> v != value;
			case GREATER -> v > value;
			case GREATER_OR_EQUAL -> v >= value;
			case LESSER -> v < value;
			case LESSER_OR_EQUAL -> v <= value;
			default -> v == value;
		};
	}

	@Override
	public Json5Object save(Json5Object json) {
		json.addProperty("stat", statId.toString());
		json.addProperty("value", value);

		switch (valueCheck) {
			case NOT_EQUALS -> json.addProperty("value_check", "!=");
			case GREATER -> json.addProperty("value_check", ">");
			case GREATER_OR_EQUAL -> json.addProperty("value_check", ">=");
			case LESSER -> json.addProperty("value_check", "<");
			case LESSER_OR_EQUAL -> json.addProperty("value_check", "<=");
			default -> json.addProperty("value_check", "==");
		}

		return json;
	}
}
