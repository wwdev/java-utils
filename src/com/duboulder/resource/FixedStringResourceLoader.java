package com.duboulder.resource;

import java.io.*;
import java.util.Date;

/**
 * A resource loader that generates an InputStream for a string value.
 * A request only succeeds when the resource path matches the
 * the loader&apos;s path.
 */
public class FixedStringResourceLoader implements ResourceLoader {
	private String			_name;
	private String			_resPath;
	private String			_text;

	/**
	 * Initialize with the specified resource path and resource text.
	 * @param name the loader name
	 * @param resourcePath the resource path for the text (not null, not empty)
	 * @param resourceText the resource text (not null, may be empty)
	 */
	public FixedStringResourceLoader (String name, String resourcePath, String resourceText) {
		zzCheck ("name", name);
		zzCheck ("resourcePath", resourcePath);
		if (resourceText == null)
			throw new NullPointerException ("resourceText is null");
		_name 		= name;
		_resPath 	= resourcePath;
		_text 		= resourceText;
	}

	/**
	 * Answer the resource path for the text
	 * @return the resource path (not null, not empty)
	 */
	public String getResourcePath () { return _resPath; }

	/**
	 * Answer the resource text as a string.
	 * @return the resource text string (not null, may be empty)
	 */
	public String getResourceText () { return _text; }

	@Override
	public String getEffectivePath (String resourcePath) {
		zzCheck ("resourcePath", resourcePath);
		return resourcePath;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public Date getLastModified(String resourcePath) {
		zzCheck ("resourcePath", resourcePath);
		return _resPath.equals (resourcePath) ? new Date (0) : null;
	}

	@Override
	public InputStream getInputStream (String resourcePath) 
		throws IOException, ResourceNotFoundException 
	{
		zzCheck ("resourcePath", resourcePath);
		if (!_resPath.equals (resourcePath))
			throw new ResourceNotFoundException (
				"No resource available for '" + resourcePath + "'"
			);

		return new ByteArrayInputStream (_text.getBytes ());
	}

	private static void zzCheck (String ident, String value) {
		if (value == null)
			throw new NullPointerException (ident + " is null");
		if (value.isEmpty ())
			throw new NullPointerException (ident + " is empty");
	}
}
