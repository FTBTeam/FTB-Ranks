package com.feed_the_beast.mods.ftbranks.impl;

import com.feed_the_beast.mods.ftbranks.api.PermissionValue;
import com.feed_the_beast.mods.ftbranks.api.Rank;
import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import com.feed_the_beast.mods.ftbranks.api.RankManager;
import com.feed_the_beast.mods.ftbranks.impl.condition.DefaultCondition;
import com.mojang.authlib.GameProfile;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
public class RankImpl implements Rank, Comparable<RankImpl>
{
	public final RankManagerImpl manager;
	public final String id;
	public final Map<String, PermissionValue> permissions;
	public String name;
	public int power;
	public RankCondition condition;

	public RankImpl(RankManagerImpl m, String s)
	{
		manager = m;
		id = s;
		permissions = new LinkedHashMap<>();
		name = "";
		power = 50;
		condition = new DefaultCondition(this);
	}

	@Override
	public String toString()
	{
		return id;
	}

	@Override
	public boolean equals(Object o)
	{
		return o == this || o instanceof Rank && id.equals(((Rank) o).getId());
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public RankManager getManager()
	{
		return manager;
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public int getPower()
	{
		return power;
	}

	@Override
	public void setPermission(String node, PermissionValue value)
	{
		permissions.put(node, value);
		manager.saveRanks();
	}

	public PermissionValue getPermission(String node)
	{
		return permissions.getOrDefault(node, PermissionValue.DEFAULT);
	}

	@Override
	public RankCondition getCondition()
	{
		return condition;
	}

	@Override
	public boolean add(GameProfile profile)
	{
		PlayerRankData data = manager.getPlayerData(profile);

		if (!data.added.containsKey(this))
		{
			data.added.put(this, Instant.now());
			manager.savePlayers();
			return true;
		}

		return false;
	}

	@Override
	public boolean remove(GameProfile profile)
	{
		PlayerRankData data = manager.getPlayerData(profile);

		if (data.added.remove(this) != null)
		{
			manager.savePlayers();
			return true;
		}

		return false;
	}

	@Override
	public int compareTo(RankImpl o)
	{
		return o.getPower() - getPower();
	}
}