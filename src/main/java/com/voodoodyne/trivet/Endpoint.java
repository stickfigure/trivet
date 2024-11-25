package com.voodoodyne.trivet;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for an object that represents a Trivet endpoint.
 */
public interface Endpoint {
	/**
	 * <p>Post the request body to the appropriate endpoint. The contract is:</p>
	 * <ul>
	 *     <li>Always POST</li>
	 *     <li>Anything other than success (200 OK) must be thrown as an exception.</li>
	 * </ul>
	 *
	 * @param contentType is for the request we are submitting to the server
	 * @param body is the raw content that should be posted to the endpoint
	 * @param iface is provided here for convenience; it isn't strictly necessary, but might make logging,
	 *              debugging, and path munging easier.
	 * @return an input stream from the server
	 *
	 * Throws an unspecified exception if the server responds with anything other than 200 OK.
	 */
	InputStream post(final String contentType, final byte[] body, final Class<?> iface) throws IOException;
}
