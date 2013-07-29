package com.duboulder.resource;

/**
 * A unchecked exception indicating a resource lookup failed.
 */
public class ResourceNotFoundExceptionUC extends RuntimeException {
	private static final long serialVersionUID = 1;

	public ResourceNotFoundExceptionUC () { super (); }
	public ResourceNotFoundExceptionUC (String msg) {
		super (msg);
	}
	public ResourceNotFoundExceptionUC (Throwable t) {
		super (t);
	}
	public ResourceNotFoundExceptionUC (String msg, Throwable t) {
		super (msg, t);
	}
}
