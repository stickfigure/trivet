package com.voodoodyne.trivet;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;


/**
 * <p>The one massive problem with using Java serialization for RPC is that the server might throw an
 * exception which does not exist on the client. This problem extends to exceptions that may be deep
 * in a nested cause chain; they will trigger a ClassNotFoundException on the client. This ObjectInputStream
 * works around that issue.</p>
 *
 * <p>This is not an exact science. We look for classes which end with "Exception" and explicitly
 * check to see if they are on the classpath; if not, we replace the ObjectStreamClass with the
 * version for ServerSideException. We can't use the normal resolveObject() mechanism because that
 * still checks the serialVersionUID; replacing the ObjectStreamClass solves that problem.</p>
 *
 * <p>This only works if your exceptions are suffixed Exception, of course. And it has the massive
 * downside of destroying the name of the original exception class. We log this, but hopefully
 * you can figure out the exception from the stacktrace and the message.</p>
 */
public class ExceptionSafeObjectInputStream extends ObjectInputStream {

	private static final ObjectStreamClass SUBSTITUTE_EXCEPTION = ObjectStreamClass.lookup(ServerSideException.class);

	/** */
	public ExceptionSafeObjectInputStream(InputStream in) throws IOException {
		super(in);
	}

	/* (non-Javadoc)
	 * @see java.io.ObjectInputStream#readClassDescriptor()
	 */
	@Override
	protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
		ObjectStreamClass read = super.readClassDescriptor();

		if (read.forClass() == null && read.getName().endsWith("Exception"))
			return SUBSTITUTE_EXCEPTION;
		else
			return read;
	}
}
