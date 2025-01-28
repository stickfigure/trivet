package com.voodoodyne.trivet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URI;


/**
 * A proxy to the server. There are convenience methods here to create a client,
 * but you should probably use the {@code ClientFactory} instead.
 */
public class Client<T> implements InvocationHandler {

	/** The mime type expected for both directions of client/server communication */
    public static final String APPLICATION_JAVA_SERIALIZED_OBJECT = "application/x-java-serialized-object";

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

	private final Endpoint endpoint;
	private final Class<T> iface;

	/** */
	Client(final Endpoint endpoint, final Class<T> iface) {
		this.endpoint = endpoint;
		this.iface = iface;

		if (!iface.isInterface()) {
			throw new IllegalArgumentException("iface must be an interface, not a concrete class");
		}
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(" + endpoint + ", " + iface + ")";
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

		// Don't proxy method calls on Object methods
		if (method.getDeclaringClass() == Object.class) {
			return method.invoke(this, args);
		}

		final MethodDef def = new MethodDef(method);

		final Request reqWithOptionals = new Request(def, args);
		final Request req = OptionalHack.strip(reqWithOptionals);

		final byte[] reqBytes = serializeRequest(req);

		final InputStream responseBody = endpoint.post(APPLICATION_JAVA_SERIALIZED_OBJECT, reqBytes, iface);

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
