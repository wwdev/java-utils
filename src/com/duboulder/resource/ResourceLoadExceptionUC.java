package com.duboulder.resource;

/**
 * A unchecked exception indicating a resource load or read failed.
 */
public class ResourceLoadExceptionUC extends RuntimeException {
	private static final long serialVersionUID = 1;

	public ResourceLoadExceptionUC () { super (); }
	public ResourceLoadExceptionUC (String msg) {
		super (msg);
	}
	public ResourceLoadExceptionUC (Throwable t) {
		super (t);
	}
	public ResourceLoadExceptionUC (String msg, Throwable t) {
		super (msg, t);
	}
}
