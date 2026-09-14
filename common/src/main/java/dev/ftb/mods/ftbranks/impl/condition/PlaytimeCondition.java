package dev.ftb.mods.ftbranks.impl.condition;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankException;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PlaytimeCondition implements RankCondition {
	private final int time;
	private final TimeUnit timeUnit;
	private final Stat<Identifier> stat;

	public PlaytimeCondition(Json5Object json) {
		time = Json5Util.getInt(json,"time").orElse(1);
		timeUnit = readTimeUnit(json);
		if ((long) time * timeUnit.ticks >= Integer.MAX_VALUE) {
			FTBRanks.LOGGER.warn("playtime condition of {} {} will never be met due to integer limit on vanilla play_time stat",
					time, TimeUnit.NAME_MAP.getName(timeUnit));
		}
		stat = Stats.CUSTOM.get(Stats.PLAY_TIME);
	}

	private TimeUnit readTimeUnit(Json5Object json) {
		if (!json.has("time_unit")) {
			FTBRanks.LOGGER.warn("missing 'time_unit' field in playtime condition - assuming 'seconds'");
		}
		String unitField = Json5Util.getString(json, "time_unit").orElse(TimeUnit.SECONDS.name);
		var unit = TimeUnit.NAME_MAP.getNullable(unitField);
		if (unit == null) {
			String accepted = Arrays.stream(TimeUnit.values()).map(u -> u.name).collect(Collectors.joining(", "));
			throw new RankException("invalid time unit '" + unitField + "' in playtime condition - accepted values: " + accepted);
		}
		return unit;
	}

	@Override
	public String getType() {
		return "playtime";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return player.getStats().getValue(stat) >= (long)time * timeUnit.ticks;
	}

	@Override
	public Json5Object save(Json5Object json) {
		json.addProperty("time", time);
		json.addProperty("time_unit", timeUnit.name);
		return json;
	}

	public enum TimeUnit {
		TICKS("ticks", 1),
		SECONDS("seconds", 20),
		MINUTES("minutes", 20 * 60),
		HOURS("hours", 20 * 60 * 60),
		DAYS("days", 20 * 60 * 60 * 24),
		WEEKS("weeks", 20 * 60 * 60 * 24 * 7)
		;

		public static final NameMap<TimeUnit> NAME_MAP = NameMap.of(SECONDS, TimeUnit.values())
				.id(u -> u.name)
				.create();

		private final String name;
        private final int ticks;

        TimeUnit(String name, int ticks) {
            this.name = name;
            this.ticks = ticks;
        }
	}
}
