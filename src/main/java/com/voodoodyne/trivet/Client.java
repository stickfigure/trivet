package com.voodoodyne.trivet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;


/**
 * Create a proxy to the server.
 */
public class Client<T> implements InvocationHandler {

	/** */
	public static <T> T create(final String endpoint, final Class<T> iface) {
		try {
			return create(new URI(endpoint), iface);
		} catch (final URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/** */
	public static <T> T create(final URI endpoint, final Class<T> iface) {
		return create(new Endpoint<>(endpoint, iface));
	}

	/** */
	@SuppressWarnings("unchecked")
	public static <T> T create(final Endpoint<T> endpoint) {
		return (T)Proxy.newProxyInstance(endpoint.iface().getClassLoader(), new Class[] { endpoint.iface() }, new Client<>(endpoint));
	}

	private final Endpoint<T> endpoint;

	/** */
	public Client(final Endpoint<T> endpoint) {
		this.endpoint = endpoint;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

		final MethodDef def = new MethodDef(method);

		final Request reqWithOptionals = new Request(def, args);
		final Request req = OptionalHack.strip(reqWithOptionals);

		final byte[] reqBytes = serializeRequest(req);

		final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(endpoint.uri())
				.header("Content-Type", AbstractTrivetServlet.APPLICATION_JAVA_SERIALIZED_OBJECT)
				.POST(BodyPublishers.ofByteArray(reqBytes));
		endpoint.requestMunger().accept(httpRequestBuilder);
		final HttpRequest httpRequest = httpRequestBuilder.build();

		final HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
		endpoint.clientMunger().accept(httpClientBuilder);
		final HttpClient httpClient = httpClientBuilder.build();

		final HttpResponse<InputStream> httpResponse = httpClient.send(httpRequest, BodyHandlers.ofInputStream());

		if (httpResponse.statusCode() != 200) {
			throw new IllegalStateException("HTTP response code " + httpResponse.statusCode() + " from server at " + endpoint);
		}

		final ExceptionalObjectInputStream inStream = new ExceptionalObjectInputStream(httpResponse.body());
		final Response responseWithoutOptionals = (Response)inStream.readObject();
		final Response response = OptionalHack.restore(responseWithoutOptionals, method);

		if (response.isThrown()) {
			throw new RemoteException(response.throwable());
		} else {
			return response.result();
		}
	}

	private byte[] serializeRequest(final Request req) throws IOException {
		final ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();

		final ObjectOutputStream objectStream = new ObjectOutputStream(byteArrayStream);
		objectStream.writeObject(req);

		return byteArrayStream.toByteArray();
	}
}
