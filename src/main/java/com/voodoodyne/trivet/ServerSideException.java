package com.voodoodyne.trivet;



/**
 * This exception class is used as a replacement for sever-side exceptions that do not exist on the client.
 * Unfortunately this destroys information (the actual exception type) and there is no reasonable way to recover
 * it. It gets logged on the client.
 */
public class ServerSideException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/** You cannot instantiate these; they are deserialized by the ExceptionSafeObjectInputStream */
	private ServerSideException() {}
}
