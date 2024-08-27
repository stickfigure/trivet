package com.voodoodyne.trivet;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * <p>Some utilites for wrapping and unwrapping Optional so we can use it as
 * parameters or return values. Optional isn't serializable so we can't
 * put it on the wire.</p>
 *
 * <p>We use `null` as the empty value on the wire. This means that for
 * Optional fields, the protocol can't distinguish between null and
 * Optional.empty().</p>
 */
public class OptionalHack {

	/**
	 * Strip off the Optional if it is an optional
	 */
	public static Object strip(final Object maybeOptional, final Class<?> maybeOptionalClass) {
		if (maybeOptionalClass == Optional.class) {
			if (maybeOptional == null)
				throw new NullPointerException("Can't serialize null as an Optional<?> value; use Optional.empty()");

			return ((Optional<?>)maybeOptional).orElse(null);
		} else {
			return maybeOptional;
		}
	}

	/**
	 * Put the Optional back if appropriate
	 */
	public static Object restore(final Object object, final Class<?> maybeOptionalClass) {
		if (maybeOptionalClass == Optional.class) {
			return Optional.ofNullable(object);
		} else {
			return object;
		}
	}

	/** @return true if any of the array objects are Optional */
	public static boolean hasOptionals(final Class<?>[] array) {
		for (final Class<?> c : array) {
			if (c == Optional.class) {
				return true;
			}
		}

		return false;
	}

	/** Remove optionals from all the parameters */
	public static Object[] strip(final Object[] params, final Class<?>[] paramClasses) {
		assert params.length == paramClasses.length;

		final Object[] next = new Object[params.length];

		for (int i = 0; i < params.length; i++) {
			next[i] = strip(params[i], paramClasses[i]);
		}

		return next;
	}

	/** Restore optionals to all the parameters */
	public static Object[] restore(final Object[] params, final Class<?>[] paramClasses) {
		assert params.length == paramClasses.length;

		final Object[] next = new Object[params.length];

		for (int i = 0; i < params.length; i++) {
			next[i] = restore(params[i], paramClasses[i]);
		}

		return next;
	}

	/** Remove optionals from all the parameters */
	public static Request strip(final Request original) {
		final Class<?>[] parameterTypes = original.method().parameterTypes();

		if (hasOptionals(parameterTypes)) {
			final Object[] params = OptionalHack.strip(original.args(), parameterTypes);
			return original.withArgs(params);
		} else {
			return original;
		}
	}

	/** Restore optionals to all the parameters */
	public static Request restore(final Request original) {
		final Class<?>[] parameterTypes = original.method().parameterTypes();

		if (hasOptionals(parameterTypes)) {
			final Object[] params = OptionalHack.restore(original.args(), parameterTypes);
			return original.withArgs(params);
		} else {
			return original;
		}
	}

	/** Remove optional from the result if appropriate */
	public static Response strip(final Response original, final Method method) {
		if (original.isThrown())
			return original;

		try {
			final Object result = strip(original.result(), method.getReturnType());
			return original.withResult(result);
		} catch (final Exception e) {
			return new Response(null, e);
		}
	}

	/** Restore optional from the result if appropriate */
	public static Response restore(final Response original, final Method method) {
		if (original.isThrown())
			return original;

		final Object result = restore(original.result(), method.getReturnType());
		return original.withResult(result);
	}
}
