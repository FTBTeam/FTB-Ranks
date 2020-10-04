package com.feed_the_beast.mods.ftbranks.api;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author LatvianModder
 */
public interface PermissionValue
{
	PermissionValue DEFAULT = new PermissionValue()
	{
		@Override
		public boolean isDefaultValue()
		{
			return true;
		}
	};

	default boolean isDefaultValue()
	{
		return false;
	}

	default Optional<String> asString()
	{
		return Optional.empty();
	}

	default Optional<Boolean> asBoolean()
	{
		return Optional.empty();
	}

	default boolean asBooleanOrTrue()
	{
		return asBoolean().orElse(Boolean.TRUE);
	}

	default boolean asBooleanOrFalse()
	{
		return asBoolean().orElse(Boolean.FALSE);
	}

	default Optional<Number> asNumber()
	{
		return Optional.empty();
	}

	default OptionalInt asInteger()
	{
		return OptionalInt.empty();
	}

	default OptionalLong asLong()
	{
		return OptionalLong.empty();
	}

	default OptionalDouble asDouble()
	{
		return OptionalDouble.empty();
	}
}