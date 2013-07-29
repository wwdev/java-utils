package com.duboulder.groovy;

import java.io.*;
import java.util.*;
import com.duboulder.util.*;

/**
 * Implementation helper for the the GroovyScriptConverter interface.
 */
public class AbstractGroovyScriptConverter implements GroovyScriptConverter {
	private List<GroovyCodeFragment>		_generators;

	protected AbstractGroovyScriptConverter () {
		_generators = null;
	}

	protected AbstractGroovyScriptConverter (GroovyCodeFragment[] generators) {
		setFragmentGenerators (generators);
	}

	@Override
	public GroovyCodeFragment[] getFragmentGenerators () {
		if (_generators == null)
			return new GroovyCodeFragment[0];

		return _generators.toArray (new GroovyCodeFragment[_generators.size ()]);
	}

	/**
	 * Set the fragment generators to the given list. If the argument is null,
	 * is empty or has no non-null elements, the list is reset to be empty
	 * @param generators the new list (may be null, may be empty)
	 */
	public void setFragmentGenerators (GroovyCodeFragment[] generators) {
		_generators = null;
		addFragmentGenerators (generators);
	}

	public void addFragmentGenerators (GroovyCodeFragment[] generators) {
		if (generators == null || generators.length < 1)
			return;

		if (_generators == null)
			_generators = new ArrayList<GroovyCodeFragment> ();
		for (GroovyCodeFragment generator : generators) {
			if (generator != null)
				_generators.add (generator);
		}
		if (_generators.isEmpty ())
			_generators = null;
	}

	@Override
	public void generateClass (
		Object context, String classPackage, String className, 
		LineReader scriptSource, PrintWriter classOutput
	) throws IOException {
		// Package start and some imports
		StringBuilder line = startPackage (classPackage, scriptSource, classOutput);
		zzDoFragments (context, "package-start", classOutput);

		classOutput.println ("\npublic class " + className + " implements GroovyScriptBlock {");
		zzDoFragments (context, "class-start", classOutput);

		// Required methods
		classOutput.println ("    public String getPackage () { return '" + classPackage + "'; }");
		classOutput.println ("    public String getName () { return '" + className + "'; }\n");

		// The execute method
		classOutput.println ("    public Object execute (Object context) {");
			zzDoFragments (context, "execute-start", classOutput);
			scriptBody (context, line, scriptSource, classOutput);
			zzDoFragments (context, "execute-end", classOutput);
			classOutput.println (finalReturnStatement ());
		classOutput.println ("\n    }");

		// The end of the wrapper class
		zzDoFragments (context, "class-end", classOutput);
		classOutput.println ("\n}\n");
	}

	/**
	 * A subclass hook for producing the script body. The default is to
	 * just copy the input to the output. 
	 * @param context the generation context (may be null)
	 * @param currLine the current line (null if there is no more input)
	 * @param scriptInput the script source (not null)
	 * @param output the output destination (not null)
	 */
	protected void scriptBody (
		Object context, StringBuilder currLine, LineReader scriptInput, PrintWriter output
	) throws IOException {
		while (currLine != null) {
			output.print ("        ");
			output.println (currLine);
			currLine = scriptInput.nextLine (currLine);
		}
	}

	/**
	 * Subclass hook for defining the final return statement of the 
	 * execute method. The default is 'return null;'.
	 * @return the final return statement (not null, not empty)
	 */
	protected String finalReturnStatement () {
		return "        return null;";
	}

	/**
	 * Start the package and import part of the class file. Any input lines
	 * starting with 'import ' are copied to the output after the
	 * package declaration. The scan stops on the first non-empty line
	 * that does not start with 'import '.
	 * @param classPackage the package name (not null)
	 * @param scriptInput the input line reader (not null)
	 * @param classOutput the output stream (not null)
	 * @return null for end of input, or the first line that does
	 * 		not start with 'import ' 
	 * @throws IOException
	 */
	protected StringBuilder startPackage (
		String classPackage, LineReader scriptInput, PrintWriter classOutput
	) throws IOException {
		classOutput.println ("// " + this.getClass ().getName ());
		classOutput.println ("package " + classPackage + ";\n");

		StringBuilder sb = new StringBuilder (300);
		while (scriptInput.nextLine (sb) != null) {
			if (sb.length() < 1) continue;
			
			// Check for an import line
			if (sb.length () < 8) break;
			if (
				!sb.substring (0, 6).equals ("import") ||
				!Character.isWhitespace (sb.charAt (6))
			) break;

			// Output the import line
			classOutput.append (sb);
			classOutput.append ('\n');
		}

		return sb;
	}

	protected static void zzCheck (String ident, String value) {
		if (value == null)
			throw new NullPointerException (ident + " is null");
		if (value.isEmpty ())
			throw new IllegalArgumentException (ident + " is empty");
	}

	protected void zzDoFragments (Object context, String scope, PrintWriter output) {
		if (_generators == null) return;
		for (GroovyCodeFragment generator : _generators) {
			if (generator.handlesScope (scope))
				generator.addText (context, scope, output);
		}
	}
}
