package com.voodoodyne.trivet;

import com.voodoodyne.trivet.FullIntegrationTest.Hello;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class OptionalHackTest {

	@Test
	void stripsObject() throws Exception {
		assertThat(OptionalHack.strip(Optional.empty(), Optional.class)).isNull();
		assertThat(OptionalHack.strip(Optional.of("foo"), Optional.class)).isEqualTo("foo");
		assertThat(OptionalHack.strip("foo", String.class)).isEqualTo("foo");
	}

	@Test
	void cantStripNullOptional() throws Exception {
		assertThatNullPointerException().isThrownBy(() -> {
			OptionalHack.strip(null, Optional.class);
		});
	}

	@Test
	void restoresObject() throws Exception {
		assertThat(OptionalHack.restore(null, Optional.class)).isEqualTo(Optional.empty());
		assertThat(OptionalHack.restore("foo", Optional.class)).isEqualTo(Optional.of("foo"));
		assertThat(OptionalHack.restore("foo", String.class)).isEqualTo("foo");
	}

	@Test
	void hasOptionals() throws Exception {
		assertThat(OptionalHack.hasOptionals(new Class<?>[]{String.class, Object.class})).isFalse();
		assertThat(OptionalHack.hasOptionals(new Class<?>[]{String.class, Optional.class})).isTrue();
	}

	@Test
	void stripsParams() throws Exception {
		assertThat(OptionalHack.strip(
				new Object[]{Optional.empty(), "foo", Optional.of("bar")},
				new Class<?>[]{Optional.class, String.class, Optional.class}
		)).containsExactly(null, "foo", "bar");
	}

	@Test
	void restoresParams() throws Exception {
		assertThat(OptionalHack.restore(
				new Object[]{null, "foo", "bar"},
				new Class<?>[]{Optional.class, String.class, Optional.class}
		)).containsExactly(Optional.empty(), "foo", Optional.of("bar"));
	}

	@Test
	void requestNotStrippedIfThereAreNoOptionals() throws Exception {
		final Method method = Hello.class.getMethod("hi", String.class);
		final MethodDef methodDef = new MethodDef(method);
		final Request request = new Request(methodDef, new Object[]{"bob"});

		final Request stripped = OptionalHack.strip(request);
		assertThat(stripped).isSameAs(request);
	}

	@Test
	void requestIsStrippedIfThereAreOptionals() throws Exception {
		final Method method = Hello.class.getMethod("hiMaybe", Optional.class);
		final MethodDef methodDef = new MethodDef(method);

		{
			final Request request = new Request(methodDef, new Object[]{Optional.of("bob")});
			final Request stripped = OptionalHack.strip(request);
			assertThat(stripped.args()).containsExactly("bob");
		}

		{
			final Request request = new Request(methodDef, new Object[]{Optional.empty()});
			final Request stripped = OptionalHack.strip(request);
			assertThat(stripped.args()).containsExactlyElementsOf(Collections.singleton(null));
		}
	}

	@Test
	void responseNotStrippedIfReturnNotOptional() throws Exception {
		final Method method = Hello.class.getMethod("hi", String.class);
		final Response response = new Response("bob", null);

		final Response stripped = OptionalHack.strip(response, method);
		assertThat(stripped).isSameAs(response);
	}

	@Test
	void responseIsStrippedIfReturnIsOptional() throws Exception {
		final Method method = Hello.class.getMethod("hiMaybe", Optional.class);

		{
			final Response response = new Response(Optional.of("bob"), null);
			final Response stripped = OptionalHack.strip(response, method);
			assertThat(stripped.result()).isEqualTo("bob");
		}

		{
			final Response response = new Response(Optional.empty(), null);
			final Response stripped = OptionalHack.strip(response, method);
			assertThat(stripped.result()).isNull();
		}
	}
}
