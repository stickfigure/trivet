package com.voodoodyne.trivet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Serial;
import java.util.function.Function;

/**
 * Simple contract that takes the instance mapper function as a constructor parameter.
 * Generally the most convenient way to create the invoker servlet. You can also use
 * the TrivetServer directly in something like Spring.
 */
public class TrivetServlet extends HttpServlet {
	@Serial
	private static final long serialVersionUID = 1L;

	private final TrivetServer trivetServer;

	/**
	 * @param instanceMapper should be something like guice's Injector::getInstance or
	 *                       spring's ApplicationContext::getBean. Maps a remote interface
	 *                       to a local implementation via whatever mechanism you choose.
	 *                       Write it by hand if you like.
	 */
	public TrivetServlet(final Function<Class<?>, Object> instanceMapper) {
		this(new TrivetServer(instanceMapper));
	}

	/**
	 * In case you want to customize the TrivetServer.
	 */
	public TrivetServlet(final TrivetServer trivetServer) {
		this.trivetServer = trivetServer;
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
		if (!Client.APPLICATION_JAVA_SERIALIZED_OBJECT.equals(req.getContentType()))
			throw new ServletException("Content-Type must be " + Client.APPLICATION_JAVA_SERIALIZED_OBJECT);

		resp.setContentType(Client.APPLICATION_JAVA_SERIALIZED_OBJECT);

		trivetServer.execute(req.getInputStream(), resp.getOutputStream());
	}
}
