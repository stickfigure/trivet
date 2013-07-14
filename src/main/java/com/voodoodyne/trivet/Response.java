package com.voodoodyne.trivet;

import java.io.Serializable;

/**
 * The serialized response that is returned to the client.
 */
public class Response implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Null is perfectly legal */
	private final Object result;

	/** Non-null means there is an exception */
	private final Throwable throwable;

	/** */
	public Response(Object result, Throwable throwable) {
		this.result = result;
		this.throwable = throwable;
	}

	/** If a normal return value, this will be the result. If return type is void, this will be null. */
	public Object getResult() {
		return result;
	}

	/** If there was an exception, this will be the result. */
	public Throwable getThrowable() {
		return throwable;
	}

	/**
	 * We are in the exception case when throwable is not null; a result of null is perfectly legal.
	 */
	public boolean isThrown() {
		return throwable != null;
	}
}
