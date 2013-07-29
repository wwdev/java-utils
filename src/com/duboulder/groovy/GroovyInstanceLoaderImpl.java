package com.duboulder.groovy;

import java.io.*;
import java.util.Date;

import com.duboulder.resource.*;

/**
 * A loader that creates an instance of a Groovy class from source
 * text that is compiled to a class by the supplied class loader.
 * An instance is created from the class using the newInstance
 * method. This means the class must have a no-args constructor.
 * The instance is then cast to the appropriate type using the provided
 * runtime class object.<br/><br/>
 * 
 * This class is thread-safe if the class loader is thread-safe.
 * @param <T> the interface the instance is cast to after construction
 */
public class GroovyInstanceLoaderImpl<T>
	implements EntityLoader<T>, GroovyInstanceLoader<T>
{
	private String					_name;
	private GroovyClassLoader		_gcl;
	private Class<T>				_classCast;
	private String					_prefix;
	private String					_suffix;

	/**
	 * Initialize using the runtime class object and class loader. The loader
	 * name property is set to the class name.
	 * @param classObj the runtime class object (not null)
	 * @param gcl the class loader (not null)
	 */
	public GroovyInstanceLoaderImpl (Class<T> classObj, GroovyClassLoader gcl) {
		zzInit (classObj, gcl);
		_name = classObj.getName ();
	}

	/**
	 * Initialize the loader name, interface name and class loader
	 * @param loaderName the loader name (not null, not empty)
	 * @param classObj the runtime class object (not null)
	 * @param gcl the class loader (not null)
	 */
	public GroovyInstanceLoaderImpl (String loaderName, Class<T> classObj, GroovyClassLoader gcl) {
		zzCheckName ("loaderName", loaderName);
		zzInit (classObj, gcl);
		_name = loaderName;
	}

	/**
	 * The base resource loader used to load source files.
	 * @return the source-file resource-loader (not null)
	 */
	public ResourceLoader getResourceLoader () { return _gcl.getResourceLoader(); }

	/**
	 * The Groovy class loader that compiles source files to instantiated class
	 * instances.
	 * @return the Groovy class loader that loads and compiles source
	 */
	public GroovyClassLoader getClassLoader () { return _gcl; }

	@Override
	public String getEffectivePath (String path) {
		return _gcl.getEffectivePath (path);
	}

	@Override
	public String getName() {
		return _name;
	}

	/**
	 * The prefix prepended to the path passed to loadEntity
	 * @return the path prefix (not null, may be empty)
	 */
	public String getPrefix () { return _prefix; }
	/**
	 * Set the path prefix. Null values are converted to an
	 * empty string, non-null values have leading/trailing
	 * white space removed
	 * @param prefix
	 */
	public void setPrefix (String prefix) {
		_prefix = prefix;
		if (_prefix == null)
			_prefix = "";
		else
			_prefix = _prefix.trim ();
	}

	/**
	 * The suffix appended to the path passed to loadEntity
	 * @return the path suffix (not null, may be empty)
	 */
	public String getSuffix () { return _suffix; }
	/**
	 * Set the path suffix. Null values are converted to an
	 * empty string, non-null values have leading/trailing
	 * white space removed
	 * @param prefix
	 */
	public void setSuffix (String suffix) {
		_suffix = suffix;
		if (_suffix == null)
			_suffix = "";
		else
			_suffix = _suffix.trim ();
	}

	@Override
	public Date getLastModified(String resourcePath) {
		return _gcl.getResourceLoader ().getLastModified (resourcePath);
	}

	@Override
	public T loadEntity (String path) 
		throws IOException,	ResourceNotFoundException 
	{
		Class<?> gClass = _gcl.loadClass (_prefix + path + _suffix);
		Object objInstance = null;
		try {
			objInstance = gClass.newInstance ();
			return _classCast.cast (objInstance);
		}
		catch (IllegalAccessException e2) {
			throw new IOException (
				"Error creating instance from class " +
					gClass.getName () + " (illegal access)" +
				(e2.getMessage() == null ? "" :
					": " + e2.getMessage ()), e2
			);		
		}
		catch (InstantiationException e3) {
			throw new IOException (
					"Error creating instance from class " +
						gClass.getName () + " (instantiation)" +
					(e3.getMessage() == null ? "" :
						": " + e3.getMessage ()), e3
				);
		}
		catch (ClassCastException e4) {
			throw new IOException (
				getEffectivePath (path) + " defines the class " +
					(objInstance == null ? "?" :
						objInstance.getClass ().getName ()) +
					" which does not implement the interface " +
					_classCast.getName (), e4
			);
		}
	}

	@Override
	public T loadInstance (String path) 
		throws IOException, ResourceNotFoundException 
	{
		return loadEntity (path);
	}

	private void zzInit (Class<T> classObj, GroovyClassLoader gcl) {
		zzCheck ("classObj", classObj);
		zzCheck ("gcl", gcl);
		_classCast = classObj;
		_gcl = gcl;
		_prefix = "";
		_suffix = "";
	}

	private static void zzCheckName (String ident, String name) {
		if (name == null)
			throw new NullPointerException (ident + " is null");
		if (name.isEmpty ())
			throw new IllegalArgumentException (ident + " is empty");
	}

	private static void zzCheck (String ident, Object value) {
		if (value == null)
			throw new NullPointerException (ident + " is null");
	}
}
