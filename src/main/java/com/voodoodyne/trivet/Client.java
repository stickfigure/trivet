package com.voodoodyne.trivet;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Create a proxy to the server.
 */
public class Client<T> implements InvocationHandler {

	/** */
	public static <T> T create(final String endpoint, final Class<T> iface) {
		try {
			return create(new URL(endpoint), iface);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/** */
	public static <T> T create(final URL endpoint, final Class<T> iface) {
		return create(new Endpoint<>(endpoint, iface));
	}

	/** */
	@SuppressWarnings("unchecked")
	public static <T> T create(final Endpoint<T> endpoint) {
		return (T)Proxy.newProxyInstance(endpoint.getIface().getClassLoader(), new Class[] { endpoint.getIface() }, new Client<>(endpoint));
	}

	private final Endpoint<T> endpoint;

	/** */
	public Client(final Endpoint<T> url) {
		endpoint = url;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

		final MethodDef def = new MethodDef(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
		final Request request = new Request(def, args);

		final HttpURLConnection conn = (HttpURLConnection)endpoint.getUrl().openConnection();
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		try {
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", TrivetServlet.APPLICATION_JAVA_SERIALIZED_OBJECT);

			// Give client a shot at setup, eg add auth header
			endpoint.setup(conn);

			out = new ObjectOutputStream(conn.getOutputStream());
			out.writeObject(request);

			if (conn.getResponseCode() != 200)
				throw new IllegalStateException("HTTP response code " + conn.getResponseCode() + " from server at " + endpoint);

			in = new ExceptionalObjectInputStream(conn.getInputStream());
			final Response response = (Response)in.readObject();

			if (response.isThrown())
				throw response.throwable();
			else
				return response.result();
		} finally {
			if (out != null)
				out.close();

			if (in != null)
				in.close();
		}
	}
}
