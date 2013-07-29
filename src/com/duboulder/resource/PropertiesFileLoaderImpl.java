package com.duboulder.resource;

import java.io.*;
import java.util.*;
/**
 * A resource-loader based implementation of PropertiesFileLoader
 */
public class PropertiesFileLoaderImpl
	extends AbstractEntityLoader<Properties>
	implements PropertiesFileLoader
{
	public PropertiesFileLoaderImpl (String name, ResourceLoader resourceLoader) {
		super (name, resourceLoader);
	}

	@Override
	public Properties loadProperties (String resourcePath)
		throws ResourceNotFoundException, IOException
	{
		String effPath = zEffPath (resourcePath);

		InputStream is = getResourceLoader ().getInputStream (resourcePath);
		if (is == null)
			throw new ResourceNotFoundException (
				"Error getting properties data for '" + effPath + "'"
			);

		Properties props = new Properties ();
		try {
			props.load (is);
		}
		catch (IOException e) {
			throw new IOException (
				zErrMsg ("properties data", effPath, null, e),
				e
			);
		}
		catch (IllegalArgumentException e2) {
			throw new IOException (
				zErrMsg ("properties data", effPath, "invalid character encoding", e2)
			);
		}

		return props;
	}

	@Override
	public Properties loadEntity (String path) 
		throws IOException, ResourceNotFoundException 
	{
		return loadProperties (path);
	}
}
