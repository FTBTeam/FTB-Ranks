package dev.ftb.mods.ftbranks.impl.condition;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;

public class PlaytimeCondition implements RankCondition {
	private final int time;
	private final TimeUnit timeUnit;
	private final Stat<Identifier> stat;

	public PlaytimeCondition(Json5Object json) {
		time = Json5Util.getInt(json,"time").orElse(1);
		if (!json.has("time_unit")) {
			FTBRanks.LOGGER.warn("missing 'time_unit' field in playtime condition - assuming 'seconds'");
		}
		timeUnit = TimeUnit.NAME_MAP.get(Json5Util.getString(json,"time_unit").orElse(TimeUnit.SECONDS.name));
		stat = Stats.CUSTOM.get(Stats.PLAY_TIME);
	}

	@Override
	public String getType() {
		return "playtime";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return player.getStats().getValue(stat) >= time * timeUnit.ticks;
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

		public static final NameMap<TimeUnit> NAME_MAP = NameMap.of(SECONDS, TimeUnit.values()).create();

		private final String name;
        private final int ticks;

        TimeUnit(String name, int ticks) {
            this.name = name;
            this.ticks = ticks;
        }
	}
}
