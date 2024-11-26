package com.voodoodyne.trivet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * The main engine for processing trivet requests. Suitable for use in a servlet
 * or web controller. You can subclass this if you want special permission behavior.
 */
public class TrivetServer {
	private static final Logger log = LoggerFactory.getLogger(TrivetServer.class);

	/** This is how we get an instance of the interface, likely through injection */
	private final Function<Class<?>, Object> instanceMapper;

	/**
	 * @param instanceMapper should be something like guice's Injector::getInstance or
	 *                       spring's ApplicationContext::getBean. Maps a remote interface
	 *                       to a local implementation via whatever mechanism you choose.
	 *                       Write it by hand if you like.
	 */
	public TrivetServer(final Function<Class<?>, Object> instanceMapper) {
		this.instanceMapper = instanceMapper;
	}

	/**
	 * Execute, reading a request off the input and writing the response to output.
	 * @param input will contain a serialized Request object
	 * @param output will have a serialized Response object written to it
	 */
	public void execute(final InputStream input, final OutputStream output) throws IOException {
		try {
			final Request requestWithoutOptionals = (Request)new ObjectInputStream(input).readObject();
			final Request request = OptionalHack.restore(requestWithoutOptionals);
			log.debug("Invoking request: {}", request);

			final Response responseWithOptionals = invoke(request);
			log.debug("Returning response: {}", responseWithOptionals);
			final Response response = OptionalHack.strip(responseWithOptionals, request.method().method());

			final ObjectOutputStream out = new ObjectOutputStream(output);
			out.writeObject(response);
			out.close();

		} catch (final ClassNotFoundException | NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Execute a request and turn it into a response. If you would like, for example, to hide exceptions
	 * from the client (say, in a production environment and your clients are untrusted Androids), you can
	 * override this method and replace the Response with a generic exception.
	 */
	protected Response invoke(final Request request) {
		try {
			final Object result = invokeDirect(request);
			return new Response(result, null);
		} catch (final InvocationTargetException ex) {
			return new Response(null, ex.getCause());
		} catch (final Throwable ex) {
			return new Response(null, ex);
		}
	}

	/**
	 * Execute a request and return the result, possibly throwing nasty exceptions
	 */
	private Object invokeDirect(final Request request) throws SecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// This is a quick sanity check; we don't want to let remotes instantiate any random class on the server
		final Class<?> iface = request.method().clazz();
		if (!iface.isInterface())
			throw new IllegalArgumentException("Requests must be methods on interfaces; '" + iface.getName() + "' is not an interface");

		final Object service = instanceMapper.apply(iface);

		checkAllowed(service, request.method());

		final Method method = request.method().method();

		return method.invoke(service, request.args());
	}

	/**
	 * This is overridable to allow customization of the rules. For example, the service might be a Spring
	 * proxy that needs to be unwrapped in a Spring-specific manner. The default behavior is to call
	 * {@code checkAllowedClass(service.getClass(), method)}.
	 *
	 * @throws IllegalArgumentException if the service is not properly annotated to allow the interface to be called remotely
	 */
	protected void checkAllowed(final Object service, final MethodDef method) {
		checkAllowedClass(service.getClass(), method);
	}

	/**
	 * This is overridable to allow customization of the rules.
	 *
	 * @throws IllegalArgumentException if the service is not properly annotated to allow the interface to be called remotely
	 */
	protected void checkAllowedClass(final Class<?> serviceClass, final MethodDef method) {
		final Remote remote = serviceClass.getAnnotation(Remote.class);
		if (remote == null)
			throw new IllegalArgumentException("Cannot invoke " + method + " because " + serviceClass.getName() + " does not have @" + Remote.class.getName());

		if (remote.value().length > 0 && !contains(remote.value(), method.clazz()))
			throw new IllegalArgumentException("@Remote annotation on " + serviceClass.getName() + " does not allow " + method);
	}

	/** */
	private boolean contains(final Class<?>[] array, final Class<?> instance) {
		for (final Class<?> clazz: array)
			if (clazz == instance)
				return true;

		return false;
	}
}
