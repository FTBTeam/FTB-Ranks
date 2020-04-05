package com.feed_the_beast.mods.ftbranks.impl;

import com.feed_the_beast.mods.ftbranks.api.PermissionValue;
import com.feed_the_beast.mods.ftbranks.api.Rank;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class PlayerRankData
{
	public final RankManagerImpl manager;
	public final UUID uuid;
	public String name;
	public final Map<Rank, Instant> added;
	public final Map<String, PermissionValue> permissions;

	public PlayerRankData(RankManagerImpl m, UUID id)
	{
		manager = m;
		uuid = id;
		name = "";
		added = new LinkedHashMap<>();
		permissions = new LinkedHashMap<>();
	}
}