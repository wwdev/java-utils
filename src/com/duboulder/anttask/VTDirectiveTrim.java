package com.duboulder.anttask;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.LogLevel;

/**
 * Ant task for trimming velocity template directives
 * so there is less extraneous white space in template
 * output. The input files are copied to the destination
 * where the source lines are modified as follows:<br/>
 * <br/>
 * <div style="padding-left: 24pt;">
 *   <table>
 *     <tr><td>\s+##.*</td><td>skipped (line comment)</td></tr>
 *     <tr><td>\s+#\s.*</td><td>as is</td></tr>
 *     <tr><td>\s+#(\w.*)</td><td>#\1</td></tr>
 *   </table>
 * </div>
 * <br/>
 * Attributes:<br/>
 * <div style="padding-left:24pt;">
 *   srcdir - directory path<br/>
 *       &nbsp;&nbsp;The root of the directory tree with the templates.<br/>
 *   destdir - directory path<br/>
 *       &nbsp;&nbsp;The root of directory tree where the transformed versions<br/>
 *       &nbsp;&nbsp;are written. The sub directory structure of the source<br/>
 *       &nbsp;&nbsp;tree is preserved in the destination.<br/>
 *   verbose - boolean controlling verbose output<br/>
 * </div>
 * <br/>
 * Contained elements: <br/>
 *   &nbsp;&nbsp;None.
 */
public class VTDirectiveTrim extends MatchingTask {
	protected File			_srcDir;
	protected File			_destDir;
	protected boolean		_verbose;
	
	protected static final Pattern DOUBLE_HASH =
		Pattern.compile ("\\s*##");
	protected static final Pattern HASH_SPACE =
		Pattern.compile ("\\s*#\\s");
	protected static final Pattern HASH_WORD =
		Pattern.compile ("\\s*#\\w");

	public File getSrcDir () { return _srcDir; }
	public void setSrcDir (File baseDir) {
		if (baseDir == null)
			throw new BuildException (
				"baseDir must not be null"
			);
		_srcDir = baseDir;
	}

	public File getDestDir () { return _srcDir; }
	public void setDestDir (File destDir) {
		if (destDir == null)
			throw new BuildException (
				"destDir must not be null"
			);
		_destDir = destDir;
	}
	
	public boolean getVerbose () { return _verbose; }
	public void setVerbose (boolean f) { _verbose = f; }

	@Override
	public void execute() throws BuildException {
		// Check the attributes
		if (_srcDir == null)
			throw new BuildException (
				"basedir attribute is required"
			);
		if (_destDir == null)
			throw new BuildException (
				"destdir attribute is required"
			);
		if (!_destDir.exists ())
			throw new BuildException (
					"destdir '" + _destDir.getAbsolutePath () + 
						"' does not exist"
				);
		if (!_destDir.isDirectory ())
			throw new BuildException (
					"destdir '" + _destDir.getAbsolutePath () + 
						"' is not a directory"
				);

		// Iterate over the files in the fileset
		DirectoryScanner dirScan = getDirectoryScanner (_srcDir);
		String[] files = dirScan.getIncludedFiles ();
		if (!_verbose) {
			log (
				"processing " + files.length + 
					" velocity template files",
			    Project.MSG_INFO
			);
			log (
				"    from " + _srcDir,
			    Project.MSG_INFO
			);
			log (
				"    to " + _destDir, 
			    Project.MSG_INFO
			);
		}

		for (String relPath : files) {
			if (_verbose)
				log (relPath);
			
			String dest = _destDir.getAbsolutePath () + File.separator + relPath;
			makeDestDir (dest);

			zTransform (
				_srcDir.getAbsolutePath () + File.separator + relPath, dest
			);
		}
	}
	
	private final void zTransform (String src, String dest)
		throws BuildException
	{
		// Input file
		BufferedReader srcFile = null;
		try {
			srcFile = new BufferedReader (new FileReader (src));
		}
		catch (FileNotFoundException e) {
			throw new BuildException (
				"Error reading file '" + src + "'", e
			);
		}

		// Output file
		BufferedWriter destFile = null;
		try {
			destFile = new BufferedWriter (new FileWriter (dest));
		}
		catch (IOException e) {
			throw new BuildException (
				"Error creating file '" + dest + "'", e
			);
		}

		// Process the file
		try {
			while (true) {
				String line = srcFile.readLine ();
				if (line == null) break;

				// (ws) ## is a comment - skip the line
				Matcher m = DOUBLE_HASH.matcher (line);
				if (m.lookingAt ()) continue;

				// (ws) # (ws) - a literal #
				m = HASH_SPACE.matcher (line);
				if (m.lookingAt ()) {
					// Copy as is
					destFile.write (line);
					destFile.newLine ();
					continue;
				}

				// (ws) # (word) ... is a directive
				m = HASH_WORD.matcher (line);
				if (m.lookingAt ()) {
					// Trim leading/trailing space
					destFile.write (line.trim ());
					destFile.newLine ();
					continue;
				}

				// All others just get copied
				destFile.write (line);
				destFile.newLine ();
			}
		}
		catch (IOException e) {
			throw new BuildException (
				"I/O error src=" + src + 
					" dest=" + dest, e
			);
		}
		finally {
			try { srcFile.close (); } catch (Exception e1) {};
			try { destFile.close (); } catch (Exception e2) {};
		}
	}

	// Create the destination directory from the destination file's
	// absolute path
    private void makeDestDir (String absDestPath) throws BuildException {
    	// Split off the file name part and check
    	// for directory existence
    	int i = absDestPath.lastIndexOf (File.separatorChar);
    	if (i < 0) return;
   
        File destPath = new File (absDestPath.substring (0, i));
        if (destPath.exists ()) return;

        // Create the directory, log and exit
        if (destPath.mkdirs ()) {
        	log (
        		"Created directory '" + destPath.getAbsolutePath () + "'", 
        		LogLevel.INFO.getLevel ()
        	);
        	return;
        }

        // Directory create failed
		log (
			"Error creating directory '" + destPath.getAbsolutePath () + "'", 
			LogLevel.ERR.getLevel ()
		);
		throw new BuildException (
			"Error creating directory '" + destPath.getAbsolutePath () + "'"
		);
    }
}
