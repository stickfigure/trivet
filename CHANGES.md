# Release Notes

## v2.1.1
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
