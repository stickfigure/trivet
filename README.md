# Trivial Java HTTP-RPC

This is the simplest possible Java RPC protocol, utilizing serialization and HTTP to make calls on remote objects. It
uses dependency injection to bind the interface to a concrete implementation on the server; you can use Guice,
CDI, or even just wire it up by hand.

The implementation is just 14 source files and 400 lines of code, with no dependency jars. A dynamic proxy serializes
your call into a Request object and posts that to a servlet which sends back a serialized Response. Easy!

 * v1.0 uses `javax.*`
 * v1.1+ uses `jakarta.*`
 * v2.0+ changes the serialization format, using java `record`. The client is now based on `java.net.http`.

## Source code

The official repository is (https://github.com/stickfigure/trivet)

## Download

This plugin is available in Maven Central:

```xml
<dependency>
    <groupId>com.voodoodyne.trivet</groupId>
    <artifactId>trivet</artifactId>
    <version>3.0</version>
</dependency>
```

It can be downloaded directly from [https://central.sonatype.com/]

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

That's really all you need to create for each service. The rest is boilerplate setup. Create an invoker servlet that
hooks up your dependency injection system; this example uses Guice. You can also look at `TrivetServer` if you want
more control.

```java
@Singleton
public class GuiceTrivetServlet extends TrivetServlet {
    @Inject
    public GuiceTrivetServlet(final Injector injector) {
        super(injector::getInstance);
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
ClientFactory factory = new ClientFactory("http://example.com/rpc");
Hello hello = factory.create(Hello.class);
hello.hi();
```

The proxy is thread-safe and by default uses `java.net.http`. The `ClientFactory` has a constructor that lets you
customize the request or use alternative transports (okhttp, apache, etc):

```java

Endpoint endpoint = new JavaHttpEndpoint("http://example.com/rpc") {
	protected void munge(HttpRequest.Builder builder) {
		builder.header("Authentication", someBearerToken);
    }
};
ClientFactory factory = new ClientFactory(endpoint);
Hello hello = factory.create(Hello.class);
hello.hi();
```

See [Endpoint.java](https://github.com/stickfigure/trivet/blob/master/src/main/java/com/voodoodyne/trivet/Endpoint.java)
for more options.

## Exceptions

Exceptions cause a small headache for serialization-based RPC systems like Trivet and Java RMI. Your server-side
code may throw an exception class which is not present on the client classpath, or such an exception may be nested
within an exception cause chain. This cannot be deserialized on the client.

Trivet crudely works around this problem by modifying the deserialization process slightly. When a class whose name
ends with "Exception" is missing from the client classpath, Trivet replaces the exception class with `MysteryException`
and deserializes that normally. The stacktrace, cause chain, and message are preserved but the name of the original exception
class and any custom fields are lost. This is not ideal but you can usually figure out what's going on from the information
provided, and it's better than getting an opaque ClassNotFoundException.

When an exception is thrown during a remote call, the exception (possibly mysterious) is serialized back to the client
stub. The client stub then throws `com.voodoodyne.trivet.RemoteException`, wrapping the exception thrown on the server side.
If you need to inspect the server side exception, inspect the cause.

## `Optional`s

`java.util.Optional` is incredibly useful as a return type and parameter type for Java methods, but isn't Serializable.
Trivet hacks around this by unwrapping/wrapping them on the wire. This example from the test suite works:

```java
public interface Hello {
    Optional<String> hiMaybe(final Optional<String> name);
}
```

Some caveats:
 * You can use `Optional<?>` as a return type or a method parameter type.
 * You can't use `Optional<?>` inside serialized classes. Only parameters and return types are special-cased.
 * You can't pass `null` for an `Optional<?>` parameter. Use `Optional.empty()`.
 * You can't return `null` for an `Optional<?>` return type. Use `Optional.empty()`.
 * The special casing is via static analysis. If you have a parameter of type `Object` and you pass in an `Optional<?>` value, you will get a `NotSerializableException`. 

## Yet another RPC protocol?

Serialization is convenient; it's well-understood, flexible, integrated with the language (respects transient and final),
and "fast enough". RMI is complicated, invasive (RemoteException), doesn't work with dependency injection frameworks,
and doesn't work on Google App Engine. I just have some Java code that needs to call some other Java code via HTTP!

## Author

* Jeff Schnitzer (jeff@infohazard.org)

## License

This software is provided under the [MIT license](http://opensource.org/licenses/MIT)
