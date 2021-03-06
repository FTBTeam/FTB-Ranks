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
public class PlaytimeCondition implements RankCondition {
	public static final int TICKS = 1;
	public static final int SECONDS = TICKS * 20;
	public static final int MINUTES = SECONDS * 60;
	public static final int HOURS = MINUTES * 60;
	public static final int DAYS = HOURS * 24;
	public static final int WEEKS = DAYS * 7;

	public final int time;
	public final int timeUnit;
	private final Stat<ResourceLocation> stat;

	public PlaytimeCondition(SNBTCompoundTag tag) {
		time = tag.getInt("time");

		switch (tag.getString("time_unit")) {
			case "seconds":
				timeUnit = SECONDS;
				break;
			case "minutes":
				timeUnit = MINUTES;
				break;
			case "hours":
				timeUnit = HOURS;
				break;
			case "days":
				timeUnit = DAYS;
				break;
			case "weeks":
				timeUnit = WEEKS;
				break;
			default:
				timeUnit = TICKS;
		}

		stat = Stats.CUSTOM.get(Stats.PLAY_ONE_MINUTE);
	}

	@Override
	public String getType() {
		return "playtime";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return player.getStats().getValue(stat) >= time * timeUnit;
	}

	@Override
	public void save(SNBTCompoundTag tag) {
		tag.putInt("time", time);

		switch (timeUnit) {
			case SECONDS:
				tag.putString("time_unit", "seconds");
				break;
			case MINUTES:
				tag.putString("time_unit", "minutes");
				break;
			case HOURS:
				tag.putString("time_unit", "hours");
				break;
			case DAYS:
				tag.putString("time_unit", "days");
				break;
			case WEEKS:
				tag.putString("time_unit", "weeks");
				break;
			default:
				tag.putString("time_unit", "ticks");
		}
	}
}