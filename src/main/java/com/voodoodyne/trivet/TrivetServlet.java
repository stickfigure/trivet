package com.voodoodyne.trivet;

import java.io.Serial;
import java.util.function.Function;

/**
 * Simple contract that takes the instance mapper function as a constructor parameter.
 * Generally the most convenient way to create the invoker servlet.
 */
public class TrivetServlet extends AbstractTrivetServlet {
	@Serial
	private static final long serialVersionUID = 1L;

	private final Function<Class<?>, Object> instanceMapper;

	/**
	 * @param instanceMapper should be something like guice's Injector::getInstance or
	 *                       spring's ApplicationContext::getBean. Maps a remote interface
	 *                       to a local implementation via whatever mechanism you choose.
	 *                       Write it by hand if you like.
	 */
	public TrivetServlet(final Function<Class<?>, Object> instanceMapper) {
		this.instanceMapper = instanceMapper;
	}

	@Override
	public Object getInstance(Class<?> iface) {
		return instanceMapper.apply(iface);
	}
}
