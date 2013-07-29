package com.duboulder.groovy;

import java.io.*;
import org.codehaus.groovy.control.*;
import com.duboulder.resource.*;

/**
 * A class loader that works with groovy source text. The configured
 * resource loader specifies where source text comes from. The binary
 * .class files are written to a temporary directory. There is no
 * caching of class definitions, the source text will be compiled
 * each time one of the load methods is used.<br/><br/>
 * 
 * TBD whether this implementation is thread-safe.
 */
public class GroovyClassLoader 
	extends AbstractEntityLoader<Class<?>>
	implements EntityLoader<Class<?>>
{
	private boolean							_verbose;
	private File							_classDir;
	private groovy.lang.GroovyClassLoader	_gcl;

	/**
	 * Initialize the class loader to find source files using the supplied
	 * resource loader and to write .class files to the specified directory
	 * @param classDir the absolute path to a readable/writable directory (not null, not empty)
	 * @param resLoader the source file resource loader (not null)
	 */
	public GroovyClassLoader (String classDir, ResourceLoader resLoader) {
		this (false, new File (classDir), resLoader);
	}

	/**
	 * Initialize the class loader to find source files using the supplied
	 * resource loader and to write .class files to the specified directory
	 * @param verbose flag indicating whether messages are sent to System.err
	 * 		during operations
	 * @param classDir the absolute path to a readable/writable directory (not null, not empty)
	 * @param resLoader the source file resource loader (not null)
	 */
	public GroovyClassLoader (boolean verbose, String classDir, ResourceLoader resLoader) {
		this (verbose, new File (classDir), resLoader);
	}

	/**
	 * Initialize the class loader to find source files using the supplied
	 * resource loader and to write .class files to the specified directory
	 * @param verbose flag indicating whether messages are sent to System.err
	 * 		during operations
	 * @param classDir the absolute path to a writable directory (not null, not empty)
	 * @param resLoader the source file resource loader
	 */
	public GroovyClassLoader (boolean verbose, File classDir, ResourceLoader resLoader) {
		super ("GroovyClassLoader", resLoader);
		if (classDir == null)
			throw new NullPointerException ("class dir is null");
		_verbose = verbose;

		if (_verbose) {
			System.err.println ("GroovyClassLoader: classDir=" + classDir.getAbsolutePath());
			System.err.flush();
		}

		// Temporary class file directory
		_classDir = classDir;
		if (!_classDir.isAbsolute ())
			throw new Error (
				"'" + classDir + "' is not an absolute path"
			);
		if (!_classDir.isDirectory ())
			throw new Error (
				"'" + _classDir + "' is not a driectory"
			);

		// Compiler configuration and parent class loader for _gcl
		CompilerConfiguration cCfg = new CompilerConfiguration ();
		cCfg.setTargetDirectory (_classDir);

		ClassLoader parent = new MultiSrcClassLoader (
			_verbose, this.getClass().getClassLoader ()
		);

		_gcl = new groovy.lang.GroovyClassLoader (parent, cCfg);
	}

	/**
	 * Indicator of whether messages are written to System.err during
	 * operations.
	 * @return true if output should be written to System.err
	 */
	public boolean getVerbose () { return _verbose; }
	public void setVerbose (boolean verbose) { _verbose = verbose; }

	/**
	 * Retrieve the root directory where compiled class files get written.
	 * The directory must exist or else the load methods will throw
	 * exceptions. The packages the Groovy source files declare define
	 * the directory structure created under the root.
	 * @return the directory where class files are written to
	 */
	public File getClassDir () { return _classDir; }

	/**
	 * Load the groovy class defined in the specified path. The
	 * return value on success is the class object.
	 * @param path the source path
	 * @return the groovy class instance
	 * @throws IOException if there is an error reading the source or there
	 * 	is a syntax error.
	 */
	public Class<?> loadClass (String path)
		throws ResourceNotFoundException, IOException
	{
		if (path == null)
			throw new NullPointerException ("path is null");
		if (path.isEmpty ())
			throw new IllegalArgumentException ("path is empty");
		
		InputStream is = null;
		String effPath = getResourceLoader ().getEffectivePath (path);
		try {
			is = getResourceLoader ().getInputStream (path);
			if (is == null)
				throw new ResourceNotFoundException (
					"'" + effPath + " not found "
				);

			// Load the groovy class
			Class<?> gClass = _gcl.parseClass (is, effPath);
			zzClose (is);

			if (_verbose) {
				System.err.println ("loaded groovy source '" + effPath + "'");
				System.err.flush ();
			}

			return gClass;
		}
		catch (CompilationFailedException e) {
			zzClose (is);
			throw new IOException (
				"Error compiling '" + effPath + "'" +
				(e.getMessage() == null ? "" :
					": " + e.getMessage ()), e
			);
		}
		catch (ResourceNotFoundException e2) {
			zzClose (is);
			throw e2;
		}
		catch (IOException e3) {
			zzClose (is);
			throw new IOException (
				"Error reading '" + effPath + "' " +
				(e3.getMessage() == null ? "" :
					":\n " + e3.getMessage ()), e3
				);
		}
	}

	@Override
	public Class<?> loadEntity(String path) 
		throws IOException,	ResourceNotFoundException 
	{
		return loadClass (path);
	}

	private void zzClose (InputStream is) {
		if (is == null) return;
		try { is.close (); } catch (Exception e) {};
	}
}
