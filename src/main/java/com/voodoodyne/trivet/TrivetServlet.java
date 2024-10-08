package com.voodoodyne.trivet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Subclasses must implement the ability to get instances of the specified interface.
 */
abstract public class TrivetServlet extends HttpServlet {
	private static final Logger log = LoggerFactory.getLogger(TrivetServlet.class);

	@Serial
	private static final long serialVersionUID = 1L;

	/** The mime type expected and sent back */
    public static final String APPLICATION_JAVA_SERIALIZED_OBJECT = "application/x-java-serialized-object";

    /** */
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

		if (!APPLICATION_JAVA_SERIALIZED_OBJECT.equals(req.getContentType()))
			throw new ServletException("Content-Type must be " + APPLICATION_JAVA_SERIALIZED_OBJECT);

		try {
			final Request requestWithoutOptionals = (Request)new ObjectInputStream(req.getInputStream()).readObject();
			final Request request = OptionalHack.restore(requestWithoutOptionals);
			log.debug("Invoking request: {}", request);

			final Response responseWithOptionals = invoke(request);
			log.debug("Returning response: {}", responseWithOptionals);
			final Response response = OptionalHack.strip(responseWithOptionals, request.method().method());

			resp.setContentType(APPLICATION_JAVA_SERIALIZED_OBJECT);
			final ObjectOutputStream out = new ObjectOutputStream(resp.getOutputStream());
			out.writeObject(response);
			out.close();

		} catch (final ClassNotFoundException | NoSuchMethodException ex) {
			throw new ServletException(ex);
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

		final Object service = getInstance(iface);

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

	/** Subclasses should implement this to get an instance of the interface, likely through injection */
	abstract public Object getInstance(Class<?> iface);
}
