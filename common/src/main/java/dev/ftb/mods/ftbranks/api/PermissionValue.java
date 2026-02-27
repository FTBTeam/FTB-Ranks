package dev.ftb.mods.ftbranks.api;

import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * Represents the value of a permission node retrieved from a player by
 * {@link FTBRanksAPI#getPermissionValue(ServerPlayer, String)}.
 */
public interface PermissionValue {
	/**
	 * The fallback permission value returned by querying an unknown node name
	 */
	PermissionValue MISSING = new PermissionValue() {
		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public String toString() {
			return "<empty>";
		}
	};

	/**
	 * Check if this is the empty (missing) permission value
	 * @return true if this is the empty value, false otherwise
	 */
	default boolean isEmpty() {
		return false;
	}

	/**
	 * Get the string value of this permission, if possible. This will work for string and numeric permissions.
	 *
	 * @return the string value if the node is string-representable
	 */
	default Optional<String> asString() {
		return Optional.empty();
	}

	/**
	 * Get the boolean value of this permission, if possible.
	 *
	 * @return the optional boolean value
	 */
	default Optional<Boolean> asBoolean() {
		return Optional.empty();
	}

	/**
	 * Get the boolean value of this permission, if possible. Otherwise, return true.
	 *
	 * @return the optional boolean value, or true if this not a boolean permission value
	 */
	default boolean asBooleanOrTrue() {
		return asBoolean().orElse(true);
	}

	/**
	 * Get the boolean value of this permission, if possible. Otherwise, return false.
	 *
	 * @return the optional boolean value, or false if this not a boolean permission value
	 */
	default boolean asBooleanOrFalse() {
		return asBoolean().orElse(false);
	}

	/**
	 * Get the numeric value of this permission, if possible.
	 *
	 * @return the numeric value, if this is a numeric-valued permission
	 */
	default Optional<Number> asNumber() {
		return Optional.empty();
	}

	/**
	 * Get the integer value of this permission, if possible.
	 *
	 * @return the integer value, if this is an integer-valued permission
	 */
	default OptionalInt asInteger() {
		return OptionalInt.empty();
	}

	/**
	 * Get the long value of this permission, if possible.
	 *
	 * @return the long value, if this is a long-valued permission
	 */
	default OptionalLong asLong() {
		return OptionalLong.empty();
	}

	/**
	 * Get the double value of this permission, if possible.
	 *
	 * @return the double value, if this is a double-valued permission
	 */
	default OptionalDouble asDouble() {
		return OptionalDouble.empty();
	}

	/**
	 * See {@link FTBRanksAPI#parsePermissionValue(String)}
	 */
	@Nullable
	static PermissionValue parse(@Nullable String str) {
		return FTBRanksAPI.getInstance().parsePermissionValue(str);
	}
}