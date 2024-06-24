package com.voodoodyne.trivet;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.function.Consumer;


/**
 * Information about the endpoint we connect to
 */
public record Endpoint<T>(
		URI uri,
		Class<T> iface,
		Consumer<HttpRequest.Builder> requestMunger,
		Consumer<HttpClient.Builder> clientMunger
) {
	/** Convenience method */
	public Endpoint(final URI uri, final Class<T> iface) {
		this(uri, iface, req -> {}, cli -> {});
	}

	/** Convenience method */
	public Endpoint(final URI uri, final Class<T> iface, final Consumer<HttpRequest.Builder> requestMunger) {
		this(uri, iface, requestMunger, cli -> {});
	}
}
