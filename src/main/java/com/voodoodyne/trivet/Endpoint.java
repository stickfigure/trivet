package com.voodoodyne.trivet;

import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Information about the endpoint we connect to
 */
public class Endpoint<T> {

	private final URL url;
	private final Class<T> iface;

	public Endpoint(final URL url, final Class<T> iface) {
		this.url = url;
		this.iface = iface;
	}

	public URL getUrl() { return url; }
	public Class<T> getIface() { return iface; }

	/**
	 * Override this to set http headers like basic auth. This is called for each
	 * method invocation.
	 */
	public void setup(HttpURLConnection conn) {}
}
