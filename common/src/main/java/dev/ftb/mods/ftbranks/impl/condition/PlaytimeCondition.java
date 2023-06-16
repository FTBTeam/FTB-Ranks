package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.FTBRanks;
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

	private final int time;
	private final int timeUnit;
	private final Stat<ResourceLocation> stat;

	public PlaytimeCondition(SNBTCompoundTag tag) {
		time = tag.getInt("time");

		if (!tag.contains("time_unit")) {
			FTBRanks.LOGGER.warn("missing 'time_unit' field in playtime condition - assuming 'ticks'");
		}
		switch (tag.getString("time_unit")) {
			case "seconds" -> timeUnit = SECONDS;
			case "minutes" -> timeUnit = MINUTES;
			case "hours" -> timeUnit = HOURS;
			case "days" -> timeUnit = DAYS;
			case "weeks" -> timeUnit = WEEKS;
			default -> timeUnit = TICKS;
		}

		stat = Stats.CUSTOM.get(Stats.PLAY_TIME);
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
			case SECONDS -> tag.putString("time_unit", "seconds");
			case MINUTES -> tag.putString("time_unit", "minutes");
			case HOURS -> tag.putString("time_unit", "hours");
			case DAYS -> tag.putString("time_unit", "days");
			case WEEKS -> tag.putString("time_unit", "weeks");
			default -> tag.putString("time_unit", "ticks");
		}
	}
}