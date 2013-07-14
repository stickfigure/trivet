package com.voodoodyne.trivet;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * A serializable definition of a Method; the java Method itself is not serializable.
 */
public class MethodDef implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Class<?> clazz;
	private final String name;
	private final Class<?>[] parameterTypes;

	/** */
	public MethodDef(Class<?> clazz, String name, Class<?>[] parameterTypes) {
		this.clazz = clazz;
		this.name = name;
		this.parameterTypes = parameterTypes;
	}

	/** Class on which the method is found */
	public Class<?> getClazz() {
		return clazz;
	}

	/** Name of the method */
	public String getName() {
		return name;
	}

	/** Type params to the method */
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	/**
	 */
	public Method getMethod() throws SecurityException, NoSuchMethodException {
		return clazz.getMethod(name, parameterTypes);
	}
}
