package com.voodoodyne.trivet;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * A serializable definition of a Method; the java Method itself is not serializable.
 *
 * @param clazz Class on which the method is found
 * @param name Name of the method
 * @param parameterTypes Type params to the method
 */
public record MethodDef(
		Class<?> clazz,
		String name,
		Class<?>[] parameterTypes
) implements Serializable {

	public MethodDef(final Method method) {
		this(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
	}

	/**
	 * Get the Method to invoke
	 */
	public Method method() throws SecurityException, NoSuchMethodException {
		return clazz.getMethod(name, parameterTypes);
	}

	@Override
	public String toString() {
		return clazz.getName() + "#" + name;
	}
}
