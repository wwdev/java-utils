package com.duboulder.groovy;

import java.io.IOException;

import com.duboulder.resource.*;

/**
 * A Groovy instance loader that converts paths to Groovy class instances.
 */
public interface GroovyInstanceLoader<T>
	extends EntityLoader<T>
{
	/**
	 * Load the groovy instance associated with the path. The path will be converted
	 * into a package name as follows: any slashes (/) are converted to periods (.),
	 * then the last component is removed.
	 * @param path the source file path (not null, not empty)
	 * @return the newly created instance (not null)
	 * @throws IOException if there is an error parsing the Groovy source file
	 * @throws ResourceNotFoundException if the Groovy source file is not found
	 */
	T loadInstance (String path) 
		throws IOException,	ResourceNotFoundException; 
}
