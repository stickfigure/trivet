package com.voodoodyne.trivet;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientTest {
	@Test
	void clientCanBeStringified() throws Exception {
		final Runnable client = Client.create("http://localhost:7778/hello", Runnable.class);

		final String str = client.toString();
		assertThat(str).isEqualTo("Client(JavaHttpEndpoint(http://localhost:7778/hello), interface java.lang.Runnable)");
	}
}
