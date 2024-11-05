package com.voodoodyne.trivet;


import java.io.Serial;

/**
 * An exception was thrown on the remote end. That exception is wrapped in this
 * exception and rethrown. Note that since we can't guarantee that the cause
 * can be deserialized here, you *might* wrap a MysteryException.
 */
public class RemoteException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;

	public RemoteException(final Throwable cause) {
		super(cause);
	}
}
