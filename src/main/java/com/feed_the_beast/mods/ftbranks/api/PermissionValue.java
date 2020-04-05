package com.feed_the_beast.mods.ftbranks.api;

import java.util.Optional;

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
}