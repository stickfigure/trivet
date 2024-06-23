package com.voodoodyne.trivet;

import java.io.Serializable;

/**
 * The serialized response that is returned to the client.
 *
 * @param result If a normal return value, this will be the result. If return type is void, this will be null.
 * @param throwable If there was an exception, this will be the result. Null means no exception.
 */
public record Response(
		Object result,
		Throwable throwable
) implements Serializable {
	/**
	 * We are in the exception case when throwable is not null; a result of null is perfectly legal.
	 */
	public boolean isThrown() {
		return throwable != null;
	}
}
