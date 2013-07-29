package com.duboulder.resource;

import java.io.*;
import java.util.Date;

/**
 * Load a resource from the class path using these class loaders:<br/>
 * <pre>
 *    1 - the class loader associated with the resource loader instance
 *    		this.getClass ().getClassLoader ()
 *    2 - the current thread's context class loader
 *    		Thread.currentThread ().getContextClassLoader ()
 *    3 - the system class loader
 *    		ClassLoader.getSystemClassLoader ()
 * </pre><br/>
 * The first class loader that returns an input stream for the resource.
 */
public class ClassPathResourceLoader implements ResourceLoader {
	private boolean			_devMode;
	private String			_name;
	private String			_prefix;
	private String			_suffix;
	private MultiSrcClassLoader _classLoader;

	public ClassPathResourceLoader (String name) {
		if (name == null || name.isEmpty())
			_name = this.getClass ().getName ();
		else
			_name = name;
		_devMode = false;
		_prefix = "";
		_suffix = "";
		_classLoader = new MultiSrcClassLoader (false, this.getClass ().getClassLoader ());
	}

	public ClassPathResourceLoader (String name, String prefix, String suffix) {
		this (name);
		setPrefix (prefix);
		setSuffix (suffix);
	}

	/**
	 * Indicator of whether development messages should be output.
	 * @return true when development messages should be produced
	 */
	public boolean getDevMode () { return _devMode; }
	public void setDevMode (boolean f) { _devMode = f; }

	/**
	 * The prefix prepended to resource paths before looking for the resource
	 * in the class path.
	 * @return the path prefix - defaults to the empty string (never null)
	 */
	public String getPrefix () { return _prefix; }
	public void setPrefix (String prefix) {
		if (prefix == null)
			_prefix = "";
		else
			_prefix = prefix;
	}

	/**
	 * The suffix append to resource paths before looking for the resource
	 * in the class path.
	 * @return the path suffix - defaults to the empty string (never null)
	 */
	public String getSuffix () { return _suffix; }
	public void setSuffix (String suffix) {
		if (suffix == null)
			_suffix = "";
		else
			_suffix = suffix;
	}

	@Override
	public String getEffectivePath (String resourcePath) {
		return zFullPath (resourcePath);
	}

	@Override
	public Date getLastModified (String resourcePath) {
		return null;
	}

	@Override
	public InputStream getInputStream (String resourcePath) 
		throws IOException, ResourceNotFoundException 
	{
		String resPath = zFullPath (resourcePath);

		if (_devMode) {
			String msg = "ClassPath getting resource for " + resPath;
			System.err.println (msg);
			System.err.flush ();
		}

		InputStream is = _classLoader.getResourceAsStream (resPath);
		if (is != null) return is;

		throw new ResourceNotFoundException (
			"Resource '" + resPath + "' not found"
		);
	}

	@Override
	public String getName() {
		return _name;
	}

	private String zFullPath (String resourcePath) {
		if (resourcePath == null)
			throw new NullPointerException ("resource path is null");
		if (resourcePath.isEmpty ())
			throw new IllegalArgumentException ("resource path is empty");

		StringBuilder sb = new StringBuilder ();
		sb.append (_prefix);

		// Prevent adding a double slash (//) at the boundary
		// between the prefix and the resource path
		if (resourcePath.charAt (0) == '/' && _prefix.endsWith ("/"))
			sb.append (resourcePath.subSequence (1, resourcePath.length ()));
		else
			sb.append (resourcePath);

		if (!_suffix.isEmpty () && !resourcePath.endsWith (_suffix))
			sb.append (_suffix);

		// ClassLoaders don't work with leading / - treating it as a
		// component separator
		if (sb.charAt(0) == '/')
			return sb.substring (1);
		return sb.toString ();
	}
}
