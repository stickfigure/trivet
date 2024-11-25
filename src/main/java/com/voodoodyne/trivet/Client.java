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


/**
 * A proxy to the server. There are convenience methods here to create a client,
 * but you should probably use the {@code ClientFactory} instead.
 */
public class Client<T> implements InvocationHandler {

	/**
	 * Convenience method, same as {@code new ClientFactory(endpoint).create(iface)}.
	 * This method isn't deprecated, but might be in the future.
	 */
	public static <T> T create(final String endpoint, final Class<T> iface) {
		return new ClientFactory(endpoint).create(iface);
	}

	/**
	 * Convenience method, same as {@code new ClientFactory(endpoint).create(iface)}.
	 * This method isn't deprecated, but might be in the future.
	 */
	public static <T> T create(final URI endpoint, final Class<T> iface) {
		return new ClientFactory(endpoint).create(iface);
	}

	private final Endpoint transport;
	private final Class<T> iface;

	/** */
	Client(final Endpoint transport, final Class<T> iface) {
		this.transport = transport;
		this.iface = iface;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

		final MethodDef def = new MethodDef(method);

		final Request reqWithOptionals = new Request(def, args);
		final Request req = OptionalHack.strip(reqWithOptionals);

		final byte[] reqBytes = serializeRequest(req);

		final InputStream responseBody = transport.post(AbstractTrivetServlet.APPLICATION_JAVA_SERIALIZED_OBJECT, reqBytes, iface);

		final ExceptionalObjectInputStream inStream = new ExceptionalObjectInputStream(responseBody);
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
