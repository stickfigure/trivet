package com.voodoodyne.trivet;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Create clients.
 */
public class ClientFactory {

	/**
	 * Create a factory which uses the JavaHttpEndpoint
	 */
	public ClientFactory(final String endpoint) {
		this(makeUri(endpoint));
	}

	/**
	 * Create a factory which uses the JavaHttpEndpoint
	 */
	public ClientFactory(final URI endpoint) {
		this(new JavaHttpEndpoint(endpoint));
	}

	/**
	 * Gives you full control of the underlying transport.
	 * @see Endpoint for the complete contract.
	 */
	public ClientFactory(final Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	private final Endpoint endpoint;

	/**
	 * Build a client interface.
	 */
	@SuppressWarnings("unchecked")
	public <T> T create(final Class<T> iface) {
		return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class[] { iface }, new Client<>(endpoint, iface));
	}

	private static URI makeUri(final String endpoint) {
		try {
			return new URI(endpoint);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
