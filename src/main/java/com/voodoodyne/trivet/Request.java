package com.voodoodyne.trivet;

import java.io.Serializable;

/**
 * The serialized request that is posted to the servlet.
 */
public class Request implements Serializable {

	private static final long serialVersionUID = 1L;

	private final MethodDef method;
	private final Object[] args;

	/** Which method to call. Must be on an interface. */
	public MethodDef getMethod() {
		return method;
	}

	/** Arguments to the method; must match the method def parameter types */
	public Object[] getArgs() {
		return args;
	}

	/** */
	public Request(MethodDef method, Object[] args) {
		this.method = method;
		this.args = args;
	}
}
