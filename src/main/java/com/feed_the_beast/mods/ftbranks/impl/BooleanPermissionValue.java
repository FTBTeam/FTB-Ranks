package com.feed_the_beast.mods.ftbranks.impl;

import com.feed_the_beast.mods.ftbranks.api.OptionalBoolean;
import com.feed_the_beast.mods.ftbranks.api.PermissionValue;

/**
 * @author LatvianModder
 */
public class BooleanPermissionValue implements PermissionValue
{
	public static final BooleanPermissionValue TRUE = new BooleanPermissionValue(true);
	public static final BooleanPermissionValue FALSE = new BooleanPermissionValue(false);

	public static BooleanPermissionValue of(boolean value)
	{
		return value ? TRUE : FALSE;
	}

	public final boolean value;
	private final OptionalBoolean cachedValue;

	private BooleanPermissionValue(boolean v)
	{
		value = v;
		cachedValue = OptionalBoolean.of(value);
	}

	@Override
	public OptionalBoolean asBoolean()
	{
		return cachedValue;
	}

	@Override
	public boolean asBooleanOrTrue()
	{
		return value;
	}

	@Override
	public boolean asBooleanOrFalse()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return value ? "true" : "false";
	}

	@Override
	public boolean equals(Object o)
	{
		return this == o;
	}

	@Override
	public int hashCode()
	{
		return Boolean.hashCode(value);
	}
}