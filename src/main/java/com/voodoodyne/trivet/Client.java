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
public class Client implements InvocationHandler {

	/** */
	public static <T> T create(String endpoint, Class<T> iface) {
		try {
			return create(new URL(endpoint), iface);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/** */
	@SuppressWarnings("unchecked")
	public static <T> T create(URL endpoint, Class<T> iface) {
		return (T)Proxy.newProxyInstance(iface.getClassLoader(), new Class[] { iface }, new Client(endpoint));
	}

	final URL endpoint;

	/** */
	public Client(URL url) {
		endpoint = url;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		MethodDef def = new MethodDef(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
		Request request = new Request(def, args);

		HttpURLConnection conn = (HttpURLConnection)endpoint.openConnection();
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		try {
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", TrivetServlet.APPLICATION_JAVA_SERIALIZED_OBJECT);

			out = new ObjectOutputStream(conn.getOutputStream());
			out.writeObject(request);

			if (conn.getResponseCode() != 200)
				throw new IllegalStateException("HTTP response code " + conn.getResponseCode() + " from server at " + endpoint);

			in = new ObjectInputStream(conn.getInputStream());
			Response response = (Response)in.readObject();

			if (response.isThrown())
				throw response.getThrowable();
			else
				return response.getResult();
		} finally {
			if (out != null)
				out.close();

			if (in != null)
				in.close();
		}
	}
}
