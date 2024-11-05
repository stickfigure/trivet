package com.voodoodyne.trivet;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serial;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchException;

public class FullIntegrationTest {
	private Server server;

	public interface Hello {
		String hi(String name);
		Optional<String> hiMaybe(final Optional<String> name);
		void throwup();
		Optional<String> badReturnsNull();
	}

	@Remote
	private static class HelloImpl implements Hello {
		@Override
		public String hi(final String name) {
			return "Hi, " + name;
		}

		@Override
		public Optional<String> hiMaybe(final Optional<String> name) {
			return name.map(this::hi);
		}

		@Override
		public void throwup() {
			throw new NullPointerException("Hey this is annoying");
		}

		@Override
		public Optional<String> badReturnsNull() {
			return null;
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

	@BeforeEach
	void startServer() throws Exception {
		server = new Server(7778);

		final ServletContextHandler sch = new ServletContextHandler("/");
		sch.addServlet(ServerServlet.class, "/hello");
		server.setHandler(sch);

		server.start();
	}

	@AfterEach
	void stopServer() throws Exception {
		server.stop();
	}

	@Test
	void runIntegrationTest() throws Exception {
		final Hello client = Client.create("http://localhost:7778/hello", Hello.class);

		final String greeting = client.hi("Bob");
		assertThat(greeting).isEqualTo("Hi, Bob");

		final Optional<String> greetingYes = client.hiMaybe(Optional.of("Bob"));
		assertThat(greetingYes).contains("Hi, Bob");

		final Optional<String> greetingNo = client.hiMaybe(Optional.empty());
		assertThat(greetingNo).isEmpty();

		// Good question about whether this should just be a NPE.
		assertThatThrownBy(() -> client.badReturnsNull())
			.isInstanceOf(RemoteException.class)
			.hasCauseInstanceOf(NullPointerException.class);

		assertThatThrownBy(() -> client.hiMaybe(null))
			.isInstanceOf(NullPointerException.class);

		assertThatThrownBy(() -> client.throwup())
			.isInstanceOf(RemoteException.class)
			.hasCauseInstanceOf(NullPointerException.class);
	}
}
