package com.duboulder.velocity;

import java.io.*;
import java.util.Date;

import org.apache.velocity.*;
import org.apache.velocity.app.*;
//import com.duboulder.logger.*;
import com.duboulder.resource.*;

/**
 * A velocity template loader implementation that uses a preconfigured
 * velocity engine, and optional prefix/suffix strings. If the prefix
 * or suffix is specified, they are prepended/appended to the template
 * path before being passed to the velocity engine.<br/>
 * <br/>
 * This loader does no caching.
 */
public class TemplateLoaderImpl
	implements TemplateLoader 
{
	private String				_name;
	private String				_prefix;
	private ResourceLoader		_resourceLoader;
	private String				_suffix;
	private VelocityEngine		_engine;

	public TemplateLoaderImpl (
		String name, String prefix, String suffix, VelocityEngine engine
	) {
		if (engine == null)
			throw new NullPointerException ("engine is null");
		_name = (name == null ? this.getClass ().getName () : name);
		setPrefix (prefix);
		setSuffix (suffix);

		_engine = engine;
		_resourceLoader = (ResourceLoader) engine.getProperty ("BASE_RESOURCE_LOADER");
	}

	/**
	 * @return the prefix string prepended to template paths before lookup (never null, may be empty)
	 */
	public String getPrefix () { return _prefix; }
	public void setPrefix (String prefix) {
		_prefix = (prefix == null ? "" : prefix);
	}

	/**
	 * @return the suffix appended to template paths before lookup (never null, may be empty)
	 */
	public String getSuffix () { return _suffix; }
	public void setSuffix (String suffix) {
		_suffix = (suffix == null ? "" : suffix);
	}

	/**
	 * @return the velocity engine used for template loading (never null)
	 */
	public VelocityEngine getEngine () { return _engine; }

	@Override
	public String getEffectivePath(String templatePath) {
		String ourPath = zPath (templatePath);
		return _resourceLoader == null ? ourPath :
			_resourceLoader.getEffectivePath (ourPath);
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public Date getLastModified(String resourcePath) {
		// TODO Auto-generated method stub
		return _resourceLoader.getLastModified (resourcePath);
	}

	@Override
	public Template loadTemplate(String templatePath)
		throws ResourceNotFoundException, IOException 
	{
		String path = zPath (templatePath);
		try {
			return _engine.getTemplate (path, "UTF-8");
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
				"Template '" + path + "' not found", e
			);
		}
		catch (org.apache.velocity.exception.ParseErrorException e2) {
			throw new IOException (
				"Error parsing template '" + path + "'",
				e2
			);
		}
		catch (Exception e3) {
			throw new IOException (
				"Error loading template '" + path + "':\n  " +
					(e3.getMessage () == null ? 
						e3.getClass ().getName () : e3.getMessage ()), 
				e3
			);
		}
	}

	@Override
	public Template loadEntity(String path) 
		throws IOException, ResourceNotFoundException 
	{
		return loadTemplate (path);
	}

	private String zPath (String templatePath) {
		if (templatePath == null)
			throw new NullPointerException ("template path is null");

		if (_prefix.isEmpty () && _suffix.isEmpty ())
			return templatePath;

		StringBuilder sb = new StringBuilder ();
		sb.append (_prefix);

		// Prevent adding a double slash (//) at the boundary
		// between the prefix and the template path
		if (!templatePath.isEmpty ()) {
			if (templatePath.charAt (0) == '/' && _prefix.endsWith ("/"))
				sb.append (templatePath.subSequence (1, templatePath.length ()));
			else
				sb.append (templatePath);
		}

		if (!_suffix.isEmpty () && !templatePath.endsWith (_suffix))
			sb.append (_suffix);

		return sb.toString ();
	}
}
