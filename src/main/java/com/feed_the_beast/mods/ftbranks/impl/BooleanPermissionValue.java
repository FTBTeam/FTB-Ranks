package com.feed_the_beast.mods.ftbranks.impl;

import com.feed_the_beast.mods.ftbranks.api.PermissionValue;

import java.util.Optional;

/**
 * @author LatvianModder
 */
public class BooleanPermissionValue implements PermissionValue
{
	public static final BooleanPermissionValue TRUE = new BooleanPermissionValue(true);
	public static final BooleanPermissionValue FALSE = new BooleanPermissionValue(false);

	public final boolean value;
	private final Optional<Boolean> cachedValue;

	private BooleanPermissionValue(boolean v)
	{
		value = v;
		cachedValue = Optional.of(value);
	}

	@Override
	public Optional<Boolean> asBoolean()
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
}