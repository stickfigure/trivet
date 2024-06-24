package com.voodoodyne.trivet;

import java.io.Serializable;
import java.util.Arrays;

/**
 * The serialized request that is posted to the servlet.
 *
 * @param method Which method to call. Must be on an interface.
 * @param args Arguments to the method; must match the method def parameter types
 */
public record Request(
		MethodDef method,
		Object[] args
) implements Serializable {
	@Override
	public String toString() {
		return "Request[method=" + method + ", args=" + Arrays.toString(args) + ']';
	}
}
