package com.duboulder.velocity;

import java.io.*;
import org.apache.velocity.*;
import com.duboulder.resource.*;

/**
 * Caching implementation for Velocity template loaders.<br/>
 * <br/>
 * The lifetime property specifies the time in milliseconds that a
 * loaded template will be considered good. A value of 0, means
 * indefinite validity.<br/>
 * <br/>
 * The base loader is used to load fresh templates and to reload
 * expired ones.
 */
public class TemplateCache
	extends EntityCachingLoader<Template>
	implements TemplateLoader 
{
	/**
	 * Initialize the cache with the specified lifetime and base loader.
	 * @param lifetime the template lifetime in milliseconds (less than or equal to 0 means no expiration)
	 * @param baseLoader the base template loader (not null)
	 */
	public TemplateCache (long lifetime, TemplateLoader baseLoader) {
		super (lifetime, baseLoader);
	}

	/**
	 * Initialize the cache with the specified name, lifetime and base loader.
	 * @param name the loader's name
	 * @param lifetime the template lifetime in milliseconds (less than or equal to 0 means no expiration)
	 * @param baseLoader the base template loader (not null)
	 */
	public TemplateCache (String name, long lifetime, TemplateLoader baseLoader) {
		super (name, lifetime, baseLoader);
	}

	@Override
	public Template loadTemplate(String templatePath)
			throws ResourceNotFoundException, IOException 
	{
		return loadEntity (templatePath);
	}
}
