# Release Notes

## v3.0.2
2025-09-15
* Address memory leaks from unclosed streams.

## v3.0.1
2025-01-28
* Throw an exception if you try to pass a non-interface class to `Client`.
* Don't proxy Object methods like `toString()` across the wire.

## v3.0
2024-12-02
* No changes, just declaring it stable. Note that v3 is wire-compatible with v2.

## v3.0b2
2024-11-26
* Separated the `TrivetServer` from the `TrivetServlet` so that trivet can be used in spring web controllers
or other contexts where an old-school java servlet is undesirable.
* Moved the constant for the java serialized object mime type to `Client`.
* Removed the `AbstractTrivetServlet`.

## v3.0b1
2024-11-25
* New way of creating clients that allows configurable transports (java.net.http, okhttp, apache, etc). 

Most of the `Client.create()` methods have been preserved for convenience, but the preferred API looks like:

```java
// Uses the JavaHttpEndpoint by default
final ClientFactory factory = new ClientFactory("https://example.com/rpc");
final MyInterface my = factory.create(MyInterface.class);
```

You can also create `ClientFactory` with alternative implementations of `Endpoint`, which may (for example) use
different transport mechanisms. See the javadocs or look at the code, it's simple.

## v2.3
2024-11-12
* Separate TrivetServlet and AbstractTrivetServlet. Allows us to create a TrivetServlet without subclassing.

## v2.2
2024-11-05
* Wrap server side exceptions in `RemoteException`. Otherwise you miss out on the entire client side stacktrace.
* Rename `ServerSideException` to `MysteryException`

## v2.1.2
2024-08-27
* Provide a hook to allow Spring to unwrap proxies when checking remote permission.

## v2.1
2024-08-26
* Enabled `Optional<?>` as return values and parameters
* Fixed a bug when transferring exceptions

## v2.0
2024-06-23
* Changed the serialization format, using java `record`
* Client is based on `java.net.http` instead of `URLConnection`

## v1.1
2024-05-04
* Switched from `javax.*` to `jakarta.*`
