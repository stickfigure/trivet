package com.voodoodyne.trivet;


import java.io.Serial;

/**
 * This exception class is used as a replacement for sever-side exceptions that do not exist on the client.
 * To make the serialization magic work, this is subclassed at runtime for every missing class. The subclass
 * has the same name as the missing class so the stacktrace looks magically correct.
 */
public class MysteryException extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 1L;

	public MysteryException() {}
}
