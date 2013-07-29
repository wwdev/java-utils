package com.duboulder.velocity;

import java.io.*;
import org.apache.commons.collections.*;
import org.apache.velocity.runtime.resource.*;

/**
 * Wrapper around com.duboulder.resource.ResourceLoader instances
 * for use with the velocity engine.<br/>
 * <br/>
 * This class always returns 0 for the last modification time and
 * always answers false for whether the source has been modified.<br/>
 * <br/>
 * To have better control over caching, use a caching resource loader
 * as the base loader for this class or wrap the velocity engine in
 * a caching TemplateLoader implementation
 */
public class TemplateResourceLoader 
	extends org.apache.velocity.runtime.resource.loader.ResourceLoader
{
	private boolean	_logInit;
	private com.duboulder.resource.ResourceLoader _resourceLoader;

	public TemplateResourceLoader (com.duboulder.resource.ResourceLoader baseLoader) {
		if (baseLoader == null)
			throw new NullPointerException ("base loader is null");
		_logInit = false;
		_resourceLoader = baseLoader;
	}

	/**
	 * Answer whether the loader initialization is logged to System.err. The
	 * default value is false.
	 * @return true if the initialization message will be output
	 */
	public boolean getLogInit () { return _logInit; }
	public void setLogInit (boolean f) { _logInit = f; }

	@Override
	public long getLastModified (Resource resource) {
		return 0;
	}

	@Override
	public InputStream getResourceStream (String resPath)
		throws org.apache.velocity.exception.ResourceNotFoundException 
	{
		try {
			InputStream is = _resourceLoader.getInputStream (resPath);
			if (is == null)
				throw new org.apache.velocity.exception.ResourceNotFoundException (
					resPath
				);
			return is;
		}
		catch (com.duboulder.resource.ResourceNotFoundException e) {
			throw new org.apache.velocity.exception.ResourceNotFoundException (
				"I/O error locating resource '" + resPath + "'", e
			);						
		}
		catch (IOException e1) {
			throw new org.apache.velocity.exception.ResourceNotFoundException (
				"I/O error loading resource '" + resPath + "'", e1
			);			
		}
	}

	@Override
	public void init (ExtendedProperties props) {
		if (!_logInit) return;
		System.out.flush ();
		System.err.println (
			"Velocity Resource Loader: " + getClass ().getName () + " initialized"
		);
		System.err.flush ();
	}

	@Override
	public boolean isSourceModified (Resource resource) {
		return false;
	}
}
