package com.duboulder.resource;

import java.io.*;
import java.util.Date;

/**
 * A resource loader that accesses the local file system. The
 * root path property is prepended to resource paths and then
 * the path suffix as appended to form the file system path.
 * This loader does not sanitize resource paths - that is the
 * caller's responsibility.
 */
public class FileSystemResourceLoader implements ResourceLoader {
	private String			_name;
	private String			_rootPath;
	private String			_pathSuffix;

	public FileSystemResourceLoader () {
		this (FileSystemResourceLoader.class.getName (), "", "");
	}

	public FileSystemResourceLoader (String rootPath, String pathSuffix) {
		this (
			FileSystemResourceLoader.class.getName (), 
			rootPath, pathSuffix
		);
	}

	public FileSystemResourceLoader (String name, String rootPath, String pathSuffix) {
		setName (name);
		setRootPath (rootPath);
		setPathSuffix (pathSuffix);
	}

	/**
	 * The path prefix - defaults to the empty string
	 * @return the path prepended to resource paths to form file system paths
	 */
	public String getRootPath () { return _rootPath; }
	public FileSystemResourceLoader setRootPath (String path) {
		if (path == null)
			_rootPath = "";
		else
			_rootPath = path;
		return this;
	}

	/**
	 * The path suffix - defaults to the empty string
	 * @return the suffix appended to resource paths to form file system paths
	 */
	public String getPathSuffix () { return _pathSuffix; }
	public FileSystemResourceLoader setPathSuffix (String suffix) {
		if (suffix == null)
			_pathSuffix = "";
		else
			_pathSuffix = suffix;
		return this;
	}

	@Override
	public Date getLastModified (String resourcePath) {
		String fsPath = zGetPath (resourcePath);
		File filePath = new File (fsPath);
		return new Date (filePath.lastModified ());
	}

	@Override
	public InputStream getInputStream (String resourcePath) 
		throws IOException, ResourceNotFoundException 
	{
		String fsPath = zGetPath (resourcePath);
		try {
			return new FileInputStream (fsPath);
		}
		catch (FileNotFoundException e) {
			throw new ResourceNotFoundException (
				"resource '" + fsPath + "' not found or is not a readable file" , e
			);
		}
	}

	@Override
	public String getName() {
		return _name;
	}
	public FileSystemResourceLoader setName (String name) {
		if (name == null)
			_name = this.getClass ().getName ();
		else
			_name = name;
		return this;
	}

	@Override
	public String getEffectivePath(String resourcePath) {
		return zGetPath (resourcePath);
	}

	protected String zGetPath (String resourcePath) {
		if (resourcePath == null)
			throw new NullPointerException ("resource path is null");
		if (resourcePath.isEmpty ())
			throw new IllegalArgumentException ("resource path is empty");

		StringBuilder sb = new StringBuilder ();
		sb.append (_rootPath);

		// Prevent adding a double slash (//) at the boundary
		// between the prefix and the resource path
		if (resourcePath.charAt (0) == '/' &&  _rootPath.endsWith ("/"))
			sb.append (resourcePath.subSequence (1, resourcePath.length ()));
		else
			sb.append (resourcePath);

		if (_pathSuffix.length () > 0 && !resourcePath.endsWith (_pathSuffix))
			sb.append (_pathSuffix);
		return sb.toString ();
	}
}
