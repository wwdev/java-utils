package com.duboulder.anttask;

import java.io.File;
import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Execute a jar once by converting all of the file names in the task&apos;s 
 * implicit file set to command line arguments. A nested file name mapper can
 * transform the file names before they are used in the command line. The
 * optional child &lt;apparg&gt; elements can provide command line arguments
 * that occur before the file name arguments.<br/> 
 * <br/>
 * If specified, the file name mapping is applied to all of the
 * file names in the implied file set, before the names are
 * appended to the argument list.<br/>
 * <br/>
 * <pre> 
 * Attributes:
 *   jar - File (required)
 *   	The path to the jar file to run.
 *   verbose - boolean (Optional, default false)
 *      When true, the command line args are displayed before
 *      execution of the jar
 *   showerr - boolean (Optional, default false)
 *      When true the command line args are displayed when
 *      there is an execution error. When this is false
 *      and verbose is false, only the source file names are shown
 *   output - File (Optional)
 *   	default is for output to go to the build output
 *   append - boolean (Optional)
 *      Whether the output file should be appended to.
 *      If output has been specified the default is true,
 *      otherwise the default is false.
 *
 * Contained Elements:
 *   <mapper .../> or its variants (Optional)
 *      Specifies a mapper converting source file names
 *      to argument file names.
 *   <apparg value="xxxx"/> (Can be used multiple times)
 *      The corresponding jar execution argument will
 *      will have the specified text.
 *   #### not implemented
 *   <apparg> <mapper ..../> </apparg> (Can be used multiple times)
 *      The corresponding jar execution arguments will
 *      be the transformed versions of the source files.
 *      
 * Ant build.xml declarations required:
 *    <typedef name="apparg" classname="com.duboulder.anttask.AppArg"
 *             description="application argument element"/>
 *	  <taskdef name="dojavafor" classname="com.duboulder.anttask.DoJar"
 *             description="execute a java jar, using the file names in a set as arguments"/>
 * </pre>
 */
public class DoJar extends JarBase {
	public DoJar () {}

	@Override
	public void execute() throws BuildException {
		zCheckSrcDir ();
		String[] files = zGetSrcFiles ();

		// Output file setting
		boolean appendMode = _append;
		String oFile = null;

		if (_outputFile != null) {
			// output attribute is set
			oFile = _outputFile.getAbsolutePath ();
		}

		// Argument list setup
		ArrayList<String> execArgs = new ArrayList<String> ();
		for (AppArg appArg : _args) {
			// Skip empty values when ifNotEmpty is set
			String argText = appArg.getValue ();
			if (argText == null || argText.length () < 1) {
				if (appArg.getIfNotEmpty ())
					continue;
			}

			// Add the option part of the arg
			if (appArg.getOption () != null)
				execArgs.add (appArg.getOption ());

			// Add the value part of the arg 
			if (argText != null)
				execArgs.add (argText);
		}

		String srcAbsPath = _srcDir.getAbsolutePath ();
		String fSep = Character.toString (File.separatorChar);
		int skipCount = 0;
		for (String relPath : files) {
			// Construct source file path
			String srcFile = srcAbsPath + fSep + relPath;

			// Transform the file name as needed
			if (_mapper != null) {
				String[] f = _mapper.mapFileName (srcFile); // abs path
				if (f == null) {
					if (_verbose)
						log ("    **** Skipping " + srcFile + " ****", Project.MSG_WARN);
					skipCount++;
					continue;
				}
				srcFile = f[0];
			}

			// Add the file path to the argument list
			execArgs.add (srcFile);
		}
		if (skipCount != 0)
			log (skipCount + " files skipped");

		if (_verbose) {
			log (zLogLine (execArgs, oFile, appendMode));
		}

		// Execute the operation
		if (!zExecJar ("DoJar", execArgs, oFile, appendMode)) {
			if (_showErr && !_verbose) // args already shown for verbose mode
				log (zLogLine (execArgs, oFile, appendMode));
			log ("        **** FAILED *****", Project.MSG_ERR);
		}
	}
}
