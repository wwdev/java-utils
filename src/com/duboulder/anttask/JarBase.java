/**
 * 
 */
package com.duboulder.anttask;

import java.io.*;
import java.util.*;
import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.util.*;

/**
 * Base class for jar-based tasks. The appendSet property is true when
 * when the append attribute has been specified in the task element.
 */
public class JarBase extends MatchingTask {
    protected boolean               _pretend;
	protected File					_jarFile;
	protected File					_srcDir;
	protected FileNameMapper		_mapper;		// the real mapper
	protected File					_outputFile;
	protected boolean				_verbose;
	protected boolean				_showErr;
	protected boolean				_appendSet;		// flag for explicit setting of _append
	protected boolean				_append;
	protected ArrayList<AppArg> 	_args;

	protected JarBase () {
		_jarFile	= null;
		_srcDir		= null;
		_mapper		= null;
		_outputFile	= null;
		_verbose	= false;
		_showErr	= false;
		_appendSet	= false;
		_append		= false;
		_args 		= new ArrayList<AppArg> ();
	}

    /**
     * Execution control flag. When true, the arguments
     * are displayed instead of executing the jar
     */
	public boolean isPretend () { return _pretend; }
    public boolean getPretend () { return _pretend; }
    public void setPretend (boolean f) { _pretend = f; }

	/**
	 * The path to the jar file to run (required attribute)
	 * @return the path to a jar file
	 */
	public File getJarFile () { return _jarFile; }
	public void setJarFile (File f) { _jarFile = f; }
	
	/**
	 * The base directory for the file set.
	 * @return the path to the directory.
	 */
	public File getSrcDir () { return _srcDir; }
	public void setSrcDir (File baseDir) {
		if (baseDir == null)
			throw new BuildException (
				"SrcDir must not be null"
			);
		_srcDir = baseDir;
	}

	/**
	 * Add a file name mapper that transforms input file names to
	 * output file names. Only one of the mapper or output
	 * settings can be used at a time.
	 */
	public void addConfiguredMapper (Mapper mapper)	{
		// it appears jdk 6 is pickier about overload matching - so use the redundant
		// versions to try and guarantee a match
		/**
		 * This adds a <mapper type="xxx" ..../> element
		 */
		addConfiguredMapper (mapper.getImplementation ());
	}
	public void addConfiguredMapper (FileNameMapper mapper)	{
		if (_mapper != null)
			throw new BuildException (
				"Can only have one output file name mapper"
			);
		if (_outputFile != null && mapper != null)
			throw new BuildException (
				"The output file name mapper and output file name are mutually exclusive, cannot set both"
			);

		// Default the append mode to false unless the defining task
		// has set the append attribute.
		if (!_appendSet) _append = false;
		
		// Set the output mapper
		_mapper = mapper; 
	}

	/**
	 * The path to the output file for the execution
	 * @return the path to the output file
	 */
	public File getOutput () { return _outputFile; }
	public void setOutput (File f) {
		if (_mapper != null)
			throw new BuildException (
				"The output file name mapper and output file name are mutually exclusive, cannot set both"
			);
		// Default the append mode to true unless the defining task
		// has set the append attribute.
		if (!_appendSet) _append = true;
		// Set the output file.
		_outputFile = f; 
	}

	/**
	 * Control whether the argument list is logged for each file
	 * @return the verbose attribute setting
	 */
	public boolean getVerbose () { return _verbose; }
	public void setVerbose (boolean f) { 
		_verbose = f;
		if (_verbose) _showErr = true;
	}

	/**
	 * Control whether the command line args are shown when
	 * the jar returns with an error (non-zero exit). This
	 * is set if verbose is set.
	 * @return true when the command line arguments should be shown for execution errors
	 */
	public boolean getShowErr () { return _showErr; }
	public void setShowErr (boolean f) { _showErr = f; }

	/**
	 * Control whether the output file(s) are appended to.
	 * @return true when output is appended to existing files
	 */
	public boolean getAppend () { return _append; }
	public void setAppend (boolean f) { _append = f; _appendSet = true; }

	/**
	 * Create an app arg instance for ant to use.
	 * @return the new application argument instance
	 */
	public AppArg createAppArg () {
		AppArg arg = new AppArg ();
		_args.add (arg);
		return arg;
	}

	protected void zCheckSrcDir () {
		if (_srcDir == null)
			throw new BuildException (
				"Srcdir attribute is required"
			);
		if (!_srcDir.exists ())
			throw new BuildException (
				"srcdir '" + _srcDir.getAbsolutePath() + 
					"' does not exist"
			);
		if (!_srcDir.isDirectory ())
			throw new BuildException (
				"srcdir '" + _srcDir.getAbsolutePath() + 
					"' is not a directory"
			);
	}

	protected String[] zGetSrcFiles () {
		// Iterate over the files in the fileset
		DirectoryScanner dirScan = getDirectoryScanner (_srcDir);
		String[] files = dirScan.getIncludedFiles ();
		log (
			files.length + " files to process [" + _jarFile + "]", 
			Project.MSG_INFO
		);
		return files;
	}

	protected boolean zExecJar (
		String taskName,
		ArrayList<String> execArgs, 
		String oFile, 
		boolean append
	) {
		Task task = getProject ().createTask ("java");
		if (task == null) throw new BuildException ("java task not found");
		if (!(task instanceof Java))
			throw new BuildException (
				"java taskdef is wrong type: " +
					task.getClass ().getName ()
			);
		Java jTask = (Java) task;

		// Set the output file
		// NOTE: Output file creation errors are bypassing our
		// catch clause around jTask.executeJava () so we are
		// trying to detect errors before that point
		if (oFile != null) {
			try {
				File out = new File (oFile);
				if (out.isDirectory ()) {
					log (
						"Output file '" + oFile + 
							"' is a directory", 
						Project.MSG_ERR
					);
					return false;
				}

				// Open for output/append (so we can trap errors before
				// calling executeJava on jTask)
				FileOutputStream ofs = new FileOutputStream (out, append);
				ofs.close ();

				jTask.setOutput (out);
				jTask.setAppend (true);
                jTask.setLogError (true);
			}
			catch (Exception e) {
				log (
					"Output file " +
						(append ? "append" : "create") +
						" error for '" + oFile + "'",
					Project.MSG_ERR
				);
				return false;
			}
		}

		// Set command arguments
		for (String arg : execArgs) {
			Commandline.Argument cmdArg = jTask.createArg ();
			cmdArg.setValue (arg);
		}

		jTask.setJar (_jarFile);
		jTask.setFork (true); // required for jar execution
		jTask.setTaskName (taskName);

		try {
			return jTask.executeJava () == 0;
		}
		catch (Exception e) {
			// This handler is being bypassed: sun-jdk 1.6.07 and
			// ant 1.7 (FileNotFoundException not trapped here)
			log ("Java jar execution error:", Project.MSG_ERR);
			if (e.getMessage () !=  null)
				log ("    " + e.getMessage (), Project.MSG_ERR);

			if (_verbose) {
				throw new BuildException (
					"Java jar execution error", e
				);
			}
		}
		return false;
	}

	protected String zLogLine (
		ArrayList<String> execArgs, 
		String oFile,
		boolean appendMode
	) {
		StringBuilder sb = new StringBuilder ();
		sb.append ("    ");
		int len = 0; // current line length (w/o leading spaces)
		for (String execArg : execArgs) {
			if (len != 0 && len + execArg.length () >= 60) {
				len = 0;
				sb.append ("\n             ");
			}
			else {
				if (sb.length() > 0) {
					sb.append (' ');
					len++;
				}
			}
			sb.append (execArg);
			len += execArg.length ();
		}
	
		if (oFile != null) {
			sb.append ("\n              >");
			if (appendMode) sb.append ('>');
			sb.append (' ');
			sb.append (oFile);
		}
		return sb.toString ();
	}	
}
