package com.duboulder.resource;

import java.io.*;
import java.util.Date;

/**
 * A general entity loader interface. An entity loader must specify
 * its caching policy, e.g. none, fixed size, ....
 * @param <T> the type for the entities the loader returns.
 */
public interface EntityLoader<T> {
	/**
	 * The loader name.
	 * @return the loader name (never null, never empty)
	 */
	String  getName ();
	/**
	 * Answer the effective path the loader uses. This might
	 * include prefixes or suffixes.
	 * @param path the entity path (never null, never empty)
	 * @return the effective path
	 */
	String getEffectivePath (String path);
	/**
	 * Answer the last modified time for the resource if known. When not
	 * known, the answer is null.
	 * @param resourcePath the resource path specification (not null, not empty)
	 * @return null or the last known modification time
	 */
	Date getLastModified (String resourcePath);
	/**
	 * Load the entity for the specified path.
	 * @param path the entity path (never null, never empty)
	 * @return an entity that implements interface T or is an
	 * 		instance of class T.
	 * @throws ResourceNotFoundException if the resource does not exist
	 * @throws IOException if there is an error creating the stream
	 */
	T loadEntity (String path) throws IOException, ResourceNotFoundException;
}
