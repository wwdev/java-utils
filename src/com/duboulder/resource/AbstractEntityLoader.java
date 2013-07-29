/**
 * 
 */
package com.duboulder.resource;

import java.util.Date;

/**
 * An abstract implementation of EntityLoader that uses a ResourceLoader
 * and provides for fixed path prefixes and suffixes. If both the entity
 * loader and resource loader transform paths, the effective path is:<br/>
 * &nbsp;&nbsp;&nbsp;  the resource loader transformation which is
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; applied to the entity loader transformation of the path
 */
public abstract class AbstractEntityLoader<T> implements EntityLoader<T> {
	private String			_name;
	private String			_prefix;
	private String			_suffix;
	private ResourceLoader	_resourceLoader;

	protected AbstractEntityLoader (String name) {
		zzSetName (name);
		_prefix = "";
		_suffix = "";
		_resourceLoader = null;
	}

	protected AbstractEntityLoader (String name, ResourceLoader resourceLoader) {
		zzSetName (name);
		zzSetRL (resourceLoader);
		_prefix = "";
		_suffix = "";
	}
	
	protected AbstractEntityLoader (String name, ResourceLoader resourceLoader, String prefix, String suffix) {
		zzSetName (name);
		zzSetRL (resourceLoader);
		_prefix = (prefix == null ? "" : prefix);
		_suffix = (suffix == null ? "" : suffix);
	}

	@Override
	public String getEffectivePath (String path) {
		return _resourceLoader.getEffectivePath (zEffPath (path));
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public Date getLastModified(String resourcePath) {
		zzCheck ("resourcePath", resourcePath);
		return _resourceLoader.getLastModified (resourcePath);
	}

	/**
	 * The resource loader the entity loader uses to get the resource content
	 * @return the resource loader (may be null)
	 */
	public ResourceLoader getResourceLoader () {
		return _resourceLoader;
	}

	/**
	 * Helper function for checking a path and adding the prefix/suffix
	 * if present (does not include resource loader path transformations)
	 * @param path the entity path (not null, not empty)
	 * @return the effective path with prefixes/suffixes applied
	 * @throws NullPointerException if the path is null
	 * @throws IllegalArgumentException if the path is an empty string
	 */
	protected String zEffPath (String path) {
		if (path == null)
			throw new NullPointerException ("path is null");
		if (path.isEmpty ())
			throw new IllegalArgumentException ("path is empty");

		if (_prefix.isEmpty () && _suffix.isEmpty ())
			return path;

		StringBuilder sb = new StringBuilder ();
		sb.append (_prefix);

		// Prevent adding a double slash (//) at the boundary
		// between the prefix and the path
		if (!path.isEmpty ()) {
			if (path.charAt (0) == '/' && _prefix.endsWith ("/"))
				sb.append (path.subSequence (1, path.length ()));
			else
				sb.append (path);
		}

		if (!_suffix.isEmpty () && !path.endsWith (_suffix))
			sb.append (_suffix);

		return sb.toString ();
	}

	protected String zErrMsg (String entityIdent, String streamIdent, String message, Throwable t) {
		String msg = "Error reading";
		if (entityIdent != null)
			msg +=  " " + entityIdent;
		if (streamIdent != null)
			msg += " for '" + streamIdent + "'";
		else
			msg += " from resource";
		if (message != null) {
			msg += ":\n   " + message;
		}
		if (t != null && t.getMessage() != null) {
			msg += ":\n   " + t.getMessage ();
		}
		return msg;
	}

	protected static void zzCheck (String ident, String name) {
		if (name == null)
			throw new NullPointerException (ident + " is null");
		if (name.isEmpty ())
			throw new IllegalArgumentException (ident + " is empty");		
	}

	protected static void zzCheck (String ident, Object value) {
		if (value == null)
			throw new NullPointerException (ident + " is null");
	}

	private void zzSetName (String name) {
		zzCheck ("name", name);
		_name = name;
	}
	
	private void zzSetRL (ResourceLoader resourceLoader) {
		if (resourceLoader == null)
			throw new NullPointerException ("resource loader is null");
		_resourceLoader = resourceLoader;
	}
}
