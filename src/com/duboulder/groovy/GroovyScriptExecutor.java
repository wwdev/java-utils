package com.duboulder.groovy;

import java.io.*;
import com.duboulder.resource.*;
import com.duboulder.util.*;

/**
 * An execution engine for Groovy code fragments. The fragments are
 * wrapped in a class implementation that is then compiled. The
 * execute method of the created class instance is called.  
 */
public class GroovyScriptExecutor {
	private String						_srcRootDir;
	private String						_rootPackage;
	private GroovyScriptConverter		_scriptConverter;
	private GroovyInstanceLoaderImpl<GroovyScriptBlock> _scriptLoader;

	/**
	 * Initialize with the specified destination for the source files, the
	 * source package, script-to-class converter. The destination directory
	 * must be an absolute path.
	 * @param srcRootDir the root directory for generated class source files 
	 * 		(not null, absolute path) 
	 * @param rootPackage the root package for all of the generated classes 
	 * 		(not null, not empty)
	 * @param scriptConverter the converter from script code to a the wrapped
	 * 		class source (not null)
	 * @throws IOException if there is an error checking the destination directory
	 */
	public GroovyScriptExecutor (
		String srcRootDir,
		String rootPackage,
		GroovyScriptConverter scriptConverter
	) throws IOException {
		zzCheck ("srcRootDir", srcRootDir);
		zzCheck ("rootPackage", rootPackage);
		zzCheck ("scriptConverter", scriptConverter);
		_rootPackage		= rootPackage;
		_scriptConverter	= scriptConverter;

		File srcDir = new File (srcRootDir);
		if (!srcDir.isAbsolute ())
			throw new IllegalArgumentException (srcRootDir + " is not an absolute path");
		if (!srcDir.isDirectory ())
			throw new IllegalArgumentException (srcRootDir + " is not a directory");
		if (!srcDir.canWrite ())
			throw new IllegalArgumentException (srcRootDir + " is not writeable");
		_srcRootDir = srcDir.getCanonicalPath ();

		// Setup the class loader to use the source path
		// NOTE: This is a fundamental part of the class behavior
		//       so it isn't setup via dependency injection
		_scriptLoader = new GroovyInstanceLoaderImpl<GroovyScriptBlock> (
			"GroovyScriptExecutor",
			GroovyScriptBlock.class,
			new GroovyClassLoader (
				false, srcDir,
				new FileSystemResourceLoader (
					_srcRootDir + "/", ".groovy"
				)
			)
		);
	}

	/**
	 * The root directory where generated source files are written to.
	 * @return the root directory for the source files.
	 */
	public String getSrcRootDir () { return _srcRootDir; }

	/**
	 * Execute the script fragment in the script holder using the
	 * given context. If necessary, a wrapper class is generated
	 * for the script fragment and the compiled code is used
	 * for execution. The script holder is updated with the
	 * created class instance.
	 * @param path the path associated with the script fragment (not null, not empty)
	 * @param scriptHolder the script holder (not null)
	 * @param context the execution context (not null)
	 * @param the script block return value (may be null)
	 * @throws IOException for any read/write errors
	 * @throws ResourceNotFoundException if the class loader cannot find
	 * 		the created class
	 */
	public Object execute (
		String path, GroovyScriptBlockHolder scriptHolder, Object context
	) throws IOException, ResourceNotFoundException  {
		zzCheck ("path", path);
		zzCheck ("scriptHolder", scriptHolder);
		zzCheck ("context", context);

		// If the script block already exists, use it for the execution
		GroovyScriptBlock scriptBlock =  scriptHolder.getScriptBlock ();
		if (scriptBlock != null) {
			return scriptBlock.execute (context);
		}

		// Need to convert the script text to a wrapper class
		// and use an instance for execution
		synchronized (scriptHolder) {
			// Test again since the first null check wasn't protected
			scriptBlock =  scriptHolder.getScriptBlock ();
			if (scriptBlock == null) {
				// The script source text
				StringReader scriptSource = new StringReader (scriptHolder.getScriptText ());
	
				/* The source file path and the parent directories */
				File srcPath = new File (zSourcePath (path));
				File srcDir = srcPath.getParentFile ();
				if (!srcDir.exists ()) {
					if (!srcDir.mkdirs ())
						throw new IllegalArgumentException (
							"error creating directory chain for '" +
								srcDir.getAbsolutePath () + "'"
						);
				}
	
				// The output destination for the generated class
				PrintWriter destWriter = new PrintWriter (
					new BufferedWriter (new FileWriter (srcPath))
				);
	
				// Generate the wrapper class
				_scriptConverter.generateClass (
					context,
					zClassPackage (path),
					zClassName (path),
					new LineReader (scriptSource), 
					destWriter
				);
				destWriter.close ();

				// Get the class instance and save it in the script holder
				scriptBlock = _scriptLoader.loadEntity (
					zResourcePath (path)
				);
				scriptHolder.setScriptBlock (scriptBlock);
			}
		}

		// Execute the newly loaded script wrapper
		return scriptBlock.execute (context);
	}

	// The effective package for the generated class -- this includes the
	// directory parts of the path
	private String zClassPackage (String path) {
		String classPackage = _rootPackage;
		
		// Add the path prefix parts to the package
		int i = path.lastIndexOf ('/');
		if (i >= 0)
			classPackage += "." + path.substring (0, i);

		// Convert path to qualified package name
		return classPackage.replace ('/', '.').replace ("..", ".");
	}

	// The class name
	private String zClassName (String path) {
		path = path.replaceAll ("[^a-zA-Z0-9_/]", "_");
		int i = path.lastIndexOf ('/');
		return (i < 0) ? path : path.substring (i + 1);
	}

	// The resource path to use with the script loader
	private String zResourcePath (String path) {
		return _rootPackage.replace ('.', '/') + "/" + path;
	}

	// The file system path for the generated source file
	private String zSourcePath (String path) {
		return _srcRootDir + "/" + zResourcePath (path) + ".groovy";
	}

	private static void zzCheck (String ident, String name) {
		if (name == null)
			throw new NullPointerException (ident + " is null");
		if (name.isEmpty ())
			throw new IllegalArgumentException (ident + " is empty");
	}
	
	private static void zzCheck (String ident, Object value) {
		if (value == null)
			throw new NullPointerException (ident + " is null");
	}
}
