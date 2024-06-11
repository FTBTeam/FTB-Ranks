package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;

import java.util.NoSuchElementException;

/**
 * @author LatvianModder
 */
public class StatCondition implements RankCondition {
	public static final int EQUALS = 1;
	public static final int NOT_EQUALS = 2;
	public static final int GREATER = 3;
	public static final int GREATER_OR_EQUAL = 4;
	public static final int LESSER = 5;
	public static final int LESSER_OR_EQUAL = 6;

	private final ResourceLocation statId;
	public final int value;
	public final int valueCheck;
	private final Stat<?> stat;

	public StatCondition(SNBTCompoundTag tag) {
		statId = new ResourceLocation(tag.getString("stat"));
		stat = Registry.CUSTOM_STAT.getOptional(statId)
			.map(Stats.CUSTOM::get)
			.orElseThrow(
				() -> new NoSuchElementException(
					String.format("%s does not match any known stat", statId)
				)
			);
		value = tag.getInt("value");

		switch (tag.getString("value_check")) {
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
	public void save(SNBTCompoundTag tag) {
		tag.putString("stat", statId.toString());
		tag.putInt("value", value);

		switch (valueCheck) {
			case NOT_EQUALS -> tag.putString("value_check", "!=");
			case GREATER -> tag.putString("value_check", ">");
			case GREATER_OR_EQUAL -> tag.putString("value_check", ">=");
			case LESSER -> tag.putString("value_check", "<");
			case LESSER_OR_EQUAL -> tag.putString("value_check", "<=");
			default -> tag.putString("value_check", "==");
		}
	}
}