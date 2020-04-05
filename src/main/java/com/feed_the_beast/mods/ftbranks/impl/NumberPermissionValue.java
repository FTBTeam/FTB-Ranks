package com.feed_the_beast.mods.ftbranks.impl;

import com.feed_the_beast.mods.ftbranks.api.PermissionValue;

import java.util.Optional;

/**
 * @author LatvianModder
 */
public class NumberPermissionValue implements PermissionValue
{
	public final Number value;
	private final Optional<Number> cachedValue;

	public NumberPermissionValue(Number n)
	{
		value = n;
		cachedValue = Optional.of(value);
	}

	@Override
	public Optional<Number> asNumber()
	{
		return cachedValue;
	}

	@Override
	public Optional<String> asString()
	{
		return Optional.of(value.toString());
	}
}