package com.feed_the_beast.mods.ftbranks.impl;

import com.feed_the_beast.mods.ftbranks.api.PermissionValue;

import java.util.Optional;

/**
 * @author LatvianModder
 */
public class StringPermissionValue implements PermissionValue
{
	public final String value;
	private final Optional<String> cachedValue;

	public StringPermissionValue(String v)
	{
		value = v;
		cachedValue = Optional.of(value);
	}

	@Override
	public Optional<String> asString()
	{
		return cachedValue;
	}
}