package com.duboulder.groovy;

import java.io.*;

/**
 * An interface for groovy code fragment generator.
 */
public interface GroovyCodeFragment {
	/**
	 * Answer whether the generator generates output for the scope.
	 * @param scope the scope name (same as the scope argument to execute)
	 * @return true if the generator would produce output for the scope
	 */
	boolean handlesScope (String scope);
	/**
	 * Append the code fragment to the output.
	 * @param context the generation context (may be null)
	 * @param scope the name of the scope: package-start, class-start, execute-start,
	 * 		execute-end, class-end (not null, not empty)
	 * @param output the output destination (not null)
	 */
	void addText (Object context, String scope, PrintWriter output);
}
