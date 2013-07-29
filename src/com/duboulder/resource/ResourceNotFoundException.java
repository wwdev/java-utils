/**
 * 
 */
package com.duboulder.resource;

/**
 * A checked exception indicating a resource lookup failed.
 */
public class ResourceNotFoundException extends Exception {
	private static final long serialVersionUID = 1;

	public ResourceNotFoundException () { super (); }
	public ResourceNotFoundException (String msg) {
		super (msg);
	}
	public ResourceNotFoundException (Throwable t) {
		super (t);
	}
	public ResourceNotFoundException (String msg, Throwable t) {
		super (msg, t);
	}
}
