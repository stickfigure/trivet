package com.voodoodyne.trivet;

import java.io.Serializable;

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
}
