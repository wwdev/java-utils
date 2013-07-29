package com.duboulder.resource;

import java.io.*;
import java.util.*;

/**
 * A resource loader that uses a sequence of child loaders to locate a resource.<br/>
 * <br/>
 * Since each child loader may have a different response to getEffectivePath for
 * the same resource, this loader&apos;s getEffectivePath returns all of the effective
 * paths from each of the child loaders.
 */
public class ResourceLoaderSequence implements ResourceLoader {
	private String						_name;
	private ResourceLoader[]			_children;

	/**
	 * Initialize with the optional name, and the list of children. There must be
	 * at least one child.
	 * @param name optional (may be null, not empty)
	 * @param children the child loaders (not null, not empty)
	 */
	public ResourceLoaderSequence (String name, ResourceLoader... children) {
		if (children == null)
			throw new NullPointerException ("children ref is null");

		_name = (_name == null ? this.getClass ().getName () : name);
		
		// Count the non-null child loaders
		int n = 0;
		for (ResourceLoader resLoader : children) {
			if (resLoader != null) n++;
		}
		if (n <= 0)
			throw new IllegalArgumentException ("children list is empty");

		// Populate the list
		_children = new ResourceLoader[n];
		n = 0;
		for (ResourceLoader resLoader : children) {
			if (resLoader != null)
				_children[n++] = resLoader;
		}
	}

	public ResourceLoaderSequence (String name, List<ResourceLoader> children) {
		if (children == null)
			throw new NullPointerException ("children list is null");

		_name = (_name == null ? this.getClass ().getName () : name);
		
		// Count the non-null child loaders
		int n = 0;
		for (ResourceLoader resLoader : children) {
			if (resLoader != null) n++;
		}
		if (n <= 0)
			throw new IllegalArgumentException ("children list is empty");

		// Populate the list
		_children = new ResourceLoader[n];
		n = 0;
		for (ResourceLoader resLoader : children) {
			if (resLoader != null)
				_children[n++] = resLoader;
		}
	}

	@Override
	public String getEffectivePath (String resourcePath) {
		zzCheckName ("resource path", resourcePath);
		StringBuilder effPaths = new StringBuilder ();
		for (ResourceLoader resLoader : _children) {
			if (effPaths.length() > 0)
				effPaths.append (", ");
			effPaths.append (resLoader.getEffectivePath (resourcePath));
		}
		return effPaths.toString ();
	}

	@Override
	public Date getLastModified (String resourcePath) {
		zzCheckName ("resource path", resourcePath);
		for (ResourceLoader resLoader : _children) {
			Date lastModified = resLoader.getLastModified (resourcePath);
			if (lastModified != null) return lastModified;
		}
		return null;
	}

	@Override
	public InputStream getInputStream (String resourcePath) 
		throws IOException, ResourceNotFoundException 
	{
		zzCheckName ("resource path", resourcePath);
		for (ResourceLoader resLoader : _children) {
			try {
				InputStream is = resLoader.getInputStream (resourcePath);
				if (is != null) return is;
			}
			catch (ResourceNotFoundException e) {}
		}

		throw new ResourceNotFoundException (
			"Resource not found, tried:\n" + getEffectivePath (resourcePath) 
		);
	}

	@Override
	public String getName() {
		return _name;
	}

	private static void zzCheckName (String ident, String name) {
		if (name == null)
			throw new NullPointerException (ident + " is null");
		if (name.isEmpty ())
			throw new IllegalArgumentException (ident + " is empty");
	}
}
