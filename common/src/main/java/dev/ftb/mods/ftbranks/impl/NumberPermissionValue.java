package dev.ftb.mods.ftbranks.impl;

import dev.ftb.mods.ftbranks.api.PermissionValue;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author LatvianModder
 */
public class NumberPermissionValue implements PermissionValue {
	public static final NumberPermissionValue ZERO = new NumberPermissionValue(0);

	public static NumberPermissionValue of(Number n) {
		return (n instanceof Long || n instanceof Integer) && n.longValue() == 0L ? ZERO : new NumberPermissionValue(n);
	}

	public final Number value;
	private final Optional<Number> cachedValue;
	private final OptionalInt cachedInteger;
	private final OptionalLong cachedLong;
	private final OptionalDouble cachedDouble;

	private NumberPermissionValue(Number n) {
		value = n;
		cachedValue = Optional.of(value);
		cachedInteger = OptionalInt.of(value.intValue());
		cachedLong = OptionalLong.of(value.longValue());
		cachedDouble = OptionalDouble.of(value.doubleValue());
	}

	@Override
	public Optional<Number> asNumber() {
		return cachedValue;
	}

	@Override
	public OptionalInt asInteger() {
		return cachedInteger;
	}

	@Override
	public OptionalLong asLong() {
		return cachedLong;
	}

	@Override
	public OptionalDouble asDouble() {
		return cachedDouble;
	}

	@Override
	public Optional<String> asString() {
		return Optional.of(value.toString());
	}

	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof NumberPermissionValue && value.equals(((NumberPermissionValue) o).value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}