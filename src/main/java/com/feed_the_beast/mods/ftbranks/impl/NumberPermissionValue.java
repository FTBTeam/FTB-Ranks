package com.feed_the_beast.mods.ftbranks.impl;

import com.feed_the_beast.mods.ftbranks.api.PermissionValue;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author LatvianModder
 */
public class NumberPermissionValue implements PermissionValue
{
	public final Number value;
	private final Optional<Number> cachedValue;
	private final OptionalInt cachedInteger;
	private final OptionalLong cachedLong;
	private final OptionalDouble cachedDouble;

	public NumberPermissionValue(Number n)
	{
		value = n;
		cachedValue = Optional.of(value);
		cachedInteger = OptionalInt.of(value.intValue());
		cachedLong = OptionalLong.of(value.longValue());
		cachedDouble = OptionalDouble.of(value.doubleValue());
	}

	@Override
	public Optional<Number> asNumber()
	{
		return cachedValue;
	}

	@Override
	public OptionalInt asInteger()
	{
		return cachedInteger;
	}

	@Override
	public OptionalLong asLong()
	{
		return cachedLong;
	}

	@Override
	public OptionalDouble asDouble()
	{
		return cachedDouble;
	}

	@Override
	public Optional<String> asString()
	{
		return Optional.of(value.toString());
	}
}