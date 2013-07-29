package com.duboulder.groovy;

import java.io.*;
import com.duboulder.util.*;

/**
 * Interface used by GroovyScriptExecutor to access
 * services for generating the wrapper class source text.
 * Implementations should be thread safe.
 */
public interface GroovyScriptConverter {
	/**
	 * The list of code generators to to use. 
	 * @return the code generators list (not null, may be empty)
	 */
	GroovyCodeFragment[] getFragmentGenerators ();

	/**
	 * Generate the Groovy class source from the script input text. The generated
	 * class should implement the GroovyScriptBlock interface.
	 * @param context the code generation context (may be null)
	 * @param classPackage the package the generated class should be in (not null, not empty)
	 * @param className the class name for the generated class (not null, not empty)
	 * @param scriptInput the input service for reading the script source (not null)
	 * @param classOutput the output stream for the generated class source (not null)
	 */
	void generateClass (
		Object context, String classPackage, String className, 
		LineReader scriptInput, PrintWriter classOutput
	) throws IOException;
}
