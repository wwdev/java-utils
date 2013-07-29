package com.duboulder.velocity;

import java.io.*;
import java.util.Date;

import org.apache.velocity.*;
import org.apache.velocity.app.*;
import com.duboulder.resource.*;

public class TemplateHolder implements TemplateLoader {
	private String				_name;
	private long				_lifetime;
	private String				_path;
	private VelocityEngine		_engine;
	private ResourceLoader		_resourceLoader;
	private long				_expires;
	private Template			_template;

	/**
	 * Initialize to load the specified template using the given engine.The
	 * lifetime argument controls how long a loaded instance is kept before
	 * being reloaded. A lifetime of zero means no reloading.
	 * @param name the loader name (may be null, defaults to the class name)
	 * @param lifetime the template instance lifetime in milliseconds 
	 * @param path the template path (not null, not empty)
	 * @param engine the velocity engine used to load the template (not null)
	 */
	public TemplateHolder (
		String name, long lifetime, String path, VelocityEngine engine
	) {
		if (path == null)
			throw new NullPointerException ("path is null");
		if (path.isEmpty ())
			throw new IllegalArgumentException ("path is empty");
		if (engine == null)
			throw new NullPointerException ("engine is null");

		_name 			= (name == null ? this.getClass ().getName () : name);
		_lifetime 		= lifetime;
		_path			= path;
		_engine			= engine;
		_resourceLoader = (ResourceLoader) engine.getProperty ("BASE_RESOURCE_LOADER");
		_expires		= -1;
		_template		= null;
	}

	@Override
	public String getEffectivePath(String templatePath) {
		return _resourceLoader.getEffectivePath (_path);
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public Date getLastModified (String resourcePath) {
		return _resourceLoader.getLastModified (resourcePath);
	}

	@Override
	public Template loadTemplate (String templatePath)
		throws ResourceNotFoundException, IOException 
	{
		if (
			_template != null &&
			(_lifetime <= 0 || _expires <= System.currentTimeMillis ())
		)
			return _template;

		synchronized (this) {
			// Test again since the test / update is not atomic
			if (
					_template != null &&
					(_lifetime <= 0 || _expires <= System.currentTimeMillis ())
				)
					return _template;
			try {
				_template = _engine.getTemplate (_path, "UTF-8");
			}
			catch (NullPointerException e4) {
				throw new IOException (
					"Null pointer _engine=" +
						(_engine == null ? "NULL" : _engine.getClass ().getName ()),
					e4
				);
			}
			catch (org.apache.velocity.exception.ResourceNotFoundException e) {
				throw new ResourceNotFoundException (
					"Template '" + _path + "' not found", e
				);
			}
			catch (org.apache.velocity.exception.ParseErrorException e2) {
				throw new IOException (
					"Error parsing template '" + _path + "'",
					e2
				);
			}
			catch (Exception e3) {
				throw new IOException (
					"Error loading template '" + _path + "':\n  " +
						(e3.getMessage () == null ? 
							e3.getClass ().getName () : e3.getMessage ()), 
					e3
				);
			}
			
			_expires = System.currentTimeMillis () + _lifetime;
			return _template;
		}
	}

	@Override
	public Template loadEntity(String path) 
		throws IOException,	ResourceNotFoundException 
	{
		return loadTemplate (path); 
	}

}
