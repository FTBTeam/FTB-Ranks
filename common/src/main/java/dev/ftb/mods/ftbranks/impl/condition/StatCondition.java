package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;

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
		stat = Stats.CUSTOM.get(statId);
		value = tag.getInt("value");

		switch (tag.getString("value_check")) {
			case "not_equals":
			case "not":
			case "!=":
				valueCheck = NOT_EQUALS;
				break;
			case "greater":
			case ">":
				valueCheck = GREATER;
				break;
			case "greater_or_equal":
			case ">=":
				valueCheck = GREATER_OR_EQUAL;
				break;
			case "lesser":
			case "<":
				valueCheck = LESSER;
				break;
			case "lesser_or_equal":
			case "<=":
				valueCheck = LESSER_OR_EQUAL;
				break;
			default:
				valueCheck = EQUALS;
		}
	}

	@Override
	public String getType() {
		return "stat";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		int v = player.getStats().getValue(stat);

		switch (valueCheck) {
			case NOT_EQUALS:
				return v != value;
			case GREATER:
				return v > value;
			case GREATER_OR_EQUAL:
				return v >= value;
			case LESSER:
				return v < value;
			case LESSER_OR_EQUAL:
				return v <= value;
			default:
				return v == value;
		}
	}

	@Override
	public void save(SNBTCompoundTag tag) {
		tag.putString("stat", statId.toString());
		tag.putInt("value", value);

		switch (valueCheck) {
			case NOT_EQUALS:
				tag.putString("value_check", "!=");
				break;
			case GREATER:
				tag.putString("value_check", ">");
				break;
			case GREATER_OR_EQUAL:
				tag.putString("value_check", ">=");
				break;
			case LESSER:
				tag.putString("value_check", "<");
				break;
			case LESSER_OR_EQUAL:
				tag.putString("value_check", "<=");
				break;
			default:
				tag.putString("value_check", "==");
		}
	}
}