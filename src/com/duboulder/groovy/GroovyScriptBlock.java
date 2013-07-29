package com.duboulder.groovy;

/**
 * An interface for implementations that wrap a Groovy code fragment
 * for execution. Implementations must be thread-safe.
 */
public interface GroovyScriptBlock {
	/**
	 * The package for the fragment
	 * @return the fully qualified package name (not null, not empty)
	 */
	String getPackage ();

	/**
	 * The name associated with the code fragment (should be a regular java name)
	 * @return the name (not null, not empty)
	 */
	String getName ();

	/**
	 * Execute the fragment using the values in the context.
	 * @param context the execution context with initial values and where
	 * 		the result values are stored (not null)
	 * @return an enironment defined return value (may be null)
	 */
	Object execute (Object context);
}
