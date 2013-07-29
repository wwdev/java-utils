package com.duboulder.anttask;

import java.io.*;
import java.util.*;
import org.apache.tools.ant.*;

/**
 * Execute the specified java jar once for each file in the
 * task&apos;s implicit file set. The file names may be
 * transformed before being used in the jar&apos;s command
 * line. If neither the output file nor an output file
 * name mapper is specified, the execution output appears
 * in the ant build output.<br/>
 * <br/>
 * <pre>
 * The task executes as follows:
 *   For each file in the implicit file set:
 *     1) Construct an argument list for the jar file
 *        using the contained apparg elements. The order
 *        of the arguments is the same as they appear
 *        in the build file.
 *     2) Determine the output file name from the output
 *        attribute or from the output file name mapper.
 *        If an output file name mapper was specified and
 *        the source file is not mapped, the file is skipped.
 *        NOTE: The source file mapping is applied to the 
 *        absolute path for the source file.
 *     3) Process the apparg items. They are passed the
 *        absolute path of the source file, for those args
 *        that generate their value from the source file.
 *     4) Log the arguments if verbose is true
 *     5) Execute the jar using the java task (forked vm)
 *        with the output file name from step 2.
 *
 * Attributes:
 *   jarFile - File (required)
 *   	The path to the jar file to run.
 *   srcdir - File (Required)
 *      The root directory to scan
 *   verbose - boolean (Optional, default false)
 *      When true, the command line args are displayed for each
 *      execution of the jar
 *   showerr - boolean (Optional, default false)
 *      When true the command line args are displayed for
 *      execution when there is an error. When this is false
 *      and verbose is false, only the source file name is shown
 *   output - File (Optional)
 *   	default is for output to go to the build output
 *   append - boolean (Optional)
 *      Whether the output file(s) should be appended to.
 *      If output has been specified the default is true,
 *      otherwise the default is false.
 *
 * Contained Elements:
 *   <mapper .../> or its variants (Optional)
 *      Specifies a mapper converting source file names
 *      to output file names. If used, the output file
 *      attribute cannot be set.
 *   <apparg value="xxxx"/> (Can be used multiple times)
 *      The corresponding jar execution argument will
 *      will have the specified text.
 *   <apparg> <mapper ..../> </apparg> (Can be used multiple times)
 *      The corresponding jar execution argument will
 *      be the transformed version of the source file.
 *      
 * Ant build.xml declarations required:
 *    <typedef name="apparg" classname="com.duboulder.anttask.AppArg"
 *             description="application argument element"/>
 *	  <taskdef name="dojavafor" classname="com.duboulder.anttask.DoJavaFor"
 *             description="execute a java jar, once for each file in a set"/>
 * </pre>
 */
public class DoJavaFor extends JarBase {

	public DoJavaFor () {}

	@Override
	public void execute() throws BuildException {
		// Check the attributes
		zCheckSrcDir ();
		if (_outputFile != null && _mapper != null)
			throw new BuildException (
				"Only one of the output file name or output file name mapper can be used"
			);

		String[] files = zGetSrcFiles ();

		int count = 0; 		// source file count
		int skipCount = 0;	// skipped files
		int errCount = 0;	// failed executions

		String srcAbsPath = _srcDir.getAbsolutePath ();
		String fSep = File.separator;

		for (String relPath : files) {
			// Construct source file path
			String srcFile = srcAbsPath + fSep + relPath;

			// Output file setup
			count++;
			boolean appendMode = _append;
			String oFile = null;
			if (_outputFile != null) {
				// output attribute is set - overwrite on the
				// first execution, append for later ones
				// (only when append attribute has not been explicitly set)
				oFile = _outputFile.getAbsolutePath ();
				if (!_appendSet)
					appendMode = (count > 1);
			}
			else if (_mapper != null) {
				String[] f = _mapper.mapFileName (srcFile); // abs path
				if (f == null) {
					if (_verbose)
						log ("    **** Skipping " + srcFile + " ****", Project.MSG_WARN);
					skipCount++;
					continue;
				}
				oFile = f[0];
			}

			// Argument list setup
			ArrayList<String> execArgs = new ArrayList<String> ();
			for (AppArg appArg : _args) {
                // Only enabled args
				if (!appArg.getIfEnabled ()) continue;

				// Get the effective value text and trim
				String text = appArg.getArgText (srcFile); // abs path
				if (text != null)
					text = text.trim ();
				// Skip empty values when ifNotEmpty is set
				if (appArg.getIfNotEmpty () &&
					(text == null || text.length () < 1))
					continue;
				// Add the option part of the arg
				if (appArg.getOption () != null)
					execArgs.add (appArg.getOption ());
				// Add the value part of the arg 
				if (text != null)
					execArgs.add (text);
			}

			if (_verbose) {
				log (zLogLine (execArgs, oFile, appendMode));
			}

            if (_pretend) continue;

			// Execute the operation
			if (!zExecJar ("DoJavaFor", execArgs, oFile, appendMode)) {
				errCount++;
				if (_showErr && !_verbose) // args already shown for verbose mode
					log (zLogLine (execArgs, oFile, appendMode));
				if (_verbose || _showErr)
					log ("        **** FAILED *****", Project.MSG_ERR);
				else 
					log (
						"        **** " + srcFile + 
							" FAILED *****", Project.MSG_ERR
					);
			}
		}
		
		if (skipCount != 0)
			log (skipCount + " files skipped");
		if (errCount != 0)
			log (errCount + " files with errors");
	}
}

