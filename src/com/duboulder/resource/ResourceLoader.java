package com.duboulder.resource;

import java.io.*;
import java.util.*;

/**
 * A resource loader - returns input streams for specified
 * resource paths (not necessarily file system paths).<br/>
 * <br/>
 * A resource loader is not required to be thread-safe.
 */
public interface ResourceLoader {
	/**
	 * The resource loader's name
	 * @return the resource loader's name (never null or empty)
	 */
	String getName ();

	/**
	 * Answer the effective path name used for locating the resource
	 * @param resourcePath the resource path specification (not null, not empty)
	 * @return the effective name used (e.g. includes prefixes/suffixes etc)
	 */
	String getEffectivePath (String resourcePath);

	/**
	 * Answer the last modified time for the resource if known. When not
	 * known, the answer is null.
	 * @param resourcePath the resource path specification (not null, not empty)
	 * @return null or the last known modification time
	 */
	Date getLastModified (String resourcePath);

	/**
	 * Open a resource for read-only access and return the stream. The
	 * loader determines how the resource path is interpreted. It is the
	 * caller's responsibility to close the stream.
	 * @param resourcePath the resource path specification (not null, not empty)
	 * @return the input stream for the path (not null)
	 * @throws IOException if there is an error creating the stream
	 * @throws ResourceNotFoundException if there is no such resource
	 */
	InputStream getInputStream (String resourcePath)
		throws IOException, ResourceNotFoundException;
}
