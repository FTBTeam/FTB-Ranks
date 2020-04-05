package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;

/**
 * @author LatvianModder
 */
public class PlaytimeCondition implements RankCondition
{
	public static final int TICKS = 1;
	public static final int SECONDS = TICKS * 20;
	public static final int MINUTES = SECONDS * 60;
	public static final int HOURS = MINUTES * 60;
	public static final int DAYS = HOURS * 24;
	public static final int WEEKS = DAYS * 7;

	public final int time;
	public final int timeUnit;
	private Stat<ResourceLocation> stat;

	public PlaytimeCondition(JsonObject json)
	{
		time = json.get("time").getAsInt();

		switch (json.get("time_unit").getAsString())
		{
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
	public String getType()
	{
		return "playtime";
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		return player.getStats().getValue(stat) >= time * timeUnit;
	}

	@Override
	public void save(JsonObject json)
	{
		json.addProperty("time", time);

		switch (timeUnit)
		{
			case SECONDS:
				json.addProperty("time_unit", "seconds");
				break;
			case MINUTES:
				json.addProperty("time_unit", "minutes");
				break;
			case HOURS:
				json.addProperty("time_unit", "hours");
				break;
			case DAYS:
				json.addProperty("time_unit", "days");
				break;
			case WEEKS:
				json.addProperty("time_unit", "weeks");
				break;
			default:
				json.addProperty("time_unit", "ticks");
		}
	}
}