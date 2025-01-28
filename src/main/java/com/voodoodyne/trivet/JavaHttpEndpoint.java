package com.voodoodyne.trivet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

/**
 * An endpoint implementation that uses the java.net.http facility introduced
 * with Java 11. You can subclass this if you want to munge the request
 * or client builders (add proxies, timeouts, etc).
 */
public class JavaHttpEndpoint implements Endpoint {
	private final URI endpoint;

	public JavaHttpEndpoint(final URI endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + endpoint + ")";
	}

	@Override
	public InputStream post(final String contentType, final byte[] body, final Class<?> iface) throws IOException {

		final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(endpoint)
			.header("Content-Type", contentType)
			.POST(BodyPublishers.ofByteArray(body));
		this.munge(httpRequestBuilder, iface);
		final HttpRequest httpRequest = httpRequestBuilder.build();

		final HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
		this.munge(httpClientBuilder, iface);
		final HttpClient httpClient = httpClientBuilder.build();

		final HttpResponse<InputStream> httpResponse;
		try {
			httpResponse = httpClient.send(httpRequest, BodyHandlers.ofInputStream());
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}

		if (httpResponse.statusCode() != 200) {
			throw new IllegalStateException("HTTP response code " + httpResponse.statusCode() + " from server at " + endpoint);
		}

		return httpResponse.body();
	}

	/**
	 * Override this if you wish to customize the request builder
	 */
	protected void munge(final HttpRequest.Builder builder, final Class<?> iface) {
	}

	/**
	 * Override this if you wish to customize the client builder
	 */
	protected void munge(final HttpClient.Builder builder, final Class<?> iface) {
	}
}
