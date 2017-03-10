# Trivial Java HTTP-RPC

This is the simplest possible Java RPC protocol, utilizing serialization and HTTP to make calls on remote objects. It
uses dependency injection to bind the interface to a concrete implementation on the server; you can use Guice,
CDI, or even just wire it up by hand.

The implementation is just 9 source files and 250 lines of code, with no dependency jars. A dynamic proxy serializes
your call into a Request object and posts that to a servlet which sends back a serialized Response. Easy!

## Source code

The official repository is (https://github.com/stickfigure/trivet)

## Download

This plugin is available in Maven Central:

```xml
<dependency>
	<groupId>com.voodoodyne.trivet</groupId>
	<artifactId>trivet</artifactId>
	<version>1.0</version>
</dependency>
```

It can be downloaded directly from [http://search.maven.org/]

## Usage

Create an interface class:

```java
public interface Hello {
	String hi(String name);
}
```

Create an implementation class, adding @Remote so that we know it's ok to invoke remotely:

```java
@Remote    // or @Remote(Hello.class) if there are other interfaces to exclude
public class HelloImpl implements Hello {
	@Override
	public String hi(String name) {
		return "Hello, " + name;
	}
}
```

That's really all you need to create for each service. The rest is boilerplate setup. Derive your own invoker servlet that hooks up your dependency injection system; this example uses Guice:

```java
@Singleton
public class GuiceTrivetServlet extends TrivetServlet {
	@Inject Injector injector;

	@Override
	public Object getInstance(Class<?> clazz) {
		return injector.getInstance(clazz);
	}
}
```

Here's the Guice way of binding the servlet and the interface:

```java
public class GuiceConfig extends GuiceServletContextListener {
	static class MyServletModule extends ServletModule {
		@Override
		protected void configureServlets() {
			serve("/rpc").with(GuiceTrivetServlet.class);
		}
	}

	static class MyModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(Hello.class).to(HelloImpl.class);
		}
	}

	protected Injector getInjector() {
		return Guice.createInjector(new MyServletModule(), new MyModule());
	}
}
```

Finally, package up Hello.class into your client jar and in your client call this:

```java
Hello hello = Client.create("http://example.com/rpc", Hello.class);
hello.hi();
```

The proxy is thread-safe and uses HttpURLConnection.

## Exceptions

Exceptions cause a small headache for serialization-based RPC systems like Trivet and Java RMI. Your server-side
code may throw an exception class which is not present on the client classpath, or such an exception may be nested
within an exception cause chain. This cannot be deserialized on the client.

Trivet crudely works around this problem by modifying the deserialization process slightly. When a class whose name
ends with "Exception" is missing from the client classpath, Trivet replaces the exception class with ServerSideException
and deserializes that normally. The stacktrace, cause chain, and message are preserved but the name of the original exception
class and any custom fields are lost. This is not ideal but you can usually figure out what's going on from the information
provided, and it's better than getting an opaque ClassNotFoundException.

## Yet another RPC protocol?

Serialization is convenient; it's well-understood, flexible, integrated with the language (respects transient and final),
and "fast enough". RMI is complicated, invasive (RemoteException), doesn't work with dependency injection frameworks,
and doesn't work on Google App Engine. I just have some Java code that needs to call some other Java code via HTTP!

## Author

* Jeff Schnitzer (jeff@infohazard.org)

## License

This software is provided under the [MIT license](http://opensource.org/licenses/MIT)
