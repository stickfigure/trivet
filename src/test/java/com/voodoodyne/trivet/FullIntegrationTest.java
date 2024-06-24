package com.voodoodyne.trivet;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.Test;

import java.io.Serial;

import static org.assertj.core.api.Assertions.assertThat;

public class FullIntegrationTest {

	private interface Hello {
		String hi(String name);
	}

	@Remote
	private static class HelloImpl implements Hello {
		@Override
		public String hi(final String name) {
			return "Hi, " + name;
		}
	}

	public static class ServerServlet extends TrivetServlet {
		@Serial
		private static final long serialVersionUID = 1L;

		@Override
		public Object getInstance(final Class<?> iface) {
			assert iface == Hello.class;
			return new HelloImpl();
		}
	}

	@Test
	void runIntegrationTest() throws Exception {
		final Server server = new Server(7778);

		final ServletContextHandler sch = new ServletContextHandler("/");
		sch.addServlet(ServerServlet.class, "/hello");
		server.setHandler(sch);

		server.start();

		final Hello client = Client.create("http://localhost:7778/hello", Hello.class);
		final String greeting = client.hi("Bob");
		assertThat(greeting).isEqualTo("Hi, Bob");
	}
}
