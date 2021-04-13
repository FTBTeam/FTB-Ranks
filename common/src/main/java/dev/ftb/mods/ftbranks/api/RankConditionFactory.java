package dev.ftb.mods.ftbranks.api;

import com.google.gson.JsonObject;

/**
 * @author LatvianModder
 */
public interface RankConditionFactory {
	RankCondition create(Rank rank, JsonObject json) throws Exception;
}