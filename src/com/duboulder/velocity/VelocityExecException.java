package com.duboulder.velocity;

/**
 * Unchecked exception thrown for template execution errors
 */
public class VelocityExecException extends RuntimeException {
	private static final long serialVersionUID = 1;

	public VelocityExecException (String msg) {
		super (msg);
	}
	public VelocityExecException (Throwable t) {
		super (t);
	}
	public VelocityExecException (String msg, Throwable t) {
		super (msg, t);
	}
}
