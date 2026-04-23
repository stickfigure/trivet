package com.voodoodyne.trivet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serial;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class MysteryExceptionTest {

	private static final String SER_FILE = "src/test/resources/hidden-exception-response.ser";

	interface Whatever {
		void hi();
	}

//	static class HiddenException extends RuntimeException {
//		@Serial
//		private static final long serialVersionUID = 1L;
//
//		public final String extra;
//
//		public HiddenException(final String msg, final Throwable cause) {
//			super(msg, cause);
//			this.extra = "something extra";
//		}
//	}

	/**
	 * It's a pain in the ass setting up multiple JVMs so we can have different classpaths.
	 * A quick hack that gets the job done is to write out a static .ser file, check it in,
	 * and then comment out the class. Sorry.
	 */
//	public static void main(final String args[]) throws IOException {
//		final FileOutputStream fileOutput = new FileOutputStream(SER_FILE);
//		final ObjectOutputStream output = new ObjectOutputStream(fileOutput);
//
//		final IllegalStateException exception =
//			new IllegalStateException("I am illegal state",
//				new HiddenException("I am hidden",
//					new IllegalArgumentException("I am illegal argument")));
//
//		output.writeObject(new Response(null, exception));
//
//		output.close();
//		fileOutput.close();
//	}

	@Test
	void missingExceptionsWorkAsExpected() {
		final Endpoint endpoint = (contentType, body, iface) -> new FileInputStream(SER_FILE);

		final Whatever whatever = new ClientFactory(endpoint).create(Whatever.class);

		final Throwable remote = catchThrowable(() -> whatever.hi());
		assertThat(remote).isInstanceOf(RemoteException.class);
		assertThat(remote).hasMessage("java.lang.IllegalStateException: I am illegal state");

		final Throwable outerCause = remote.getCause();
		assertThat(outerCause).isInstanceOf(IllegalStateException.class);
		assertThat(outerCause).hasMessage("I am illegal state");

		final Throwable mysteryCause = outerCause.getCause();
		assertThat(mysteryCause).isInstanceOf(MysteryException.class);
		assertThat(mysteryCause.getClass().getName()).isEqualTo("com.voodoodyne.trivet.MysteryExceptionTest$HiddenException");
		assertThat(mysteryCause).hasMessage("I am hidden");

		final Throwable sourceCause = mysteryCause.getCause();
		assertThat(sourceCause).isInstanceOf(IllegalArgumentException.class);
		assertThat(sourceCause).hasMessage("I am illegal argument");
	}
}
