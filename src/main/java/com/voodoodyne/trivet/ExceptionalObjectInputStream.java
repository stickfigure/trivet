package com.voodoodyne.trivet;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * <p>The one massive problem with using Java serialization for RPC is that the server might throw an
 * exception which does not exist on the client. This problem extends to exceptions that may be deep
 * in a nested cause chain; they will trigger a ClassNotFoundException on the client. This ObjectInputStream
 * works around that issue.</p>
 */
public class ExceptionalObjectInputStream extends ObjectInputStream {

	private static final Logger log = Logger.getLogger(ExceptionalObjectInputStream.class.getName());

	/** */
	public ExceptionalObjectInputStream(final InputStream in) throws IOException {
		super(in);
	}

	@Override
	protected Class<?> resolveClass(final ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		try {
			return super.resolveClass(desc);
		} catch (final ClassNotFoundException ex) {
			if (isThrowable(desc)) {
				// Return a unique MysteryException subclass for this specific name and serialversionuid
                return MysteryExceptionClassFactory.get(desc);
			} else {
				throw ex;
			}
		}
	}

	private boolean isThrowable(final ObjectStreamClass desc) {
		return desc.getName().endsWith("Exception") || desc.getName().endsWith("Error") || desc.getName().endsWith("Throwable");
	}

}
