package com.duboulder.resource;

import java.io.*;
import java.util.*;

/**
 * Properties data loader. The stream content must be in standard properties
 * file format. Implementations determine how the resource stream is obtained.
 */
public interface PropertiesFileLoader extends EntityLoader<Properties> {
	/**
	 * Load the properties from the specified path. Implementations define
	 * how the path is interpreted.
	 * @param resourcePath the properties resource path (not null, not empty)
	 * @return the properties instance for the resource (never null) 
	 * @throws ResourceNotFoundException if the resource cannot be read
	 * @throws IOException if there is a format or character set error in the resource
	 */
	Properties loadProperties (String resourcePath)
		throws ResourceNotFoundException, IOException;
}
