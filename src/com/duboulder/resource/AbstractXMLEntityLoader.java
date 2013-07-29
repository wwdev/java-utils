/**
 * 
 */
package com.duboulder.resource;

import java.io.*;
import org.w3c.dom.*;
import com.duboulder.xml.*;

/**
 * An abstract entity loader that loads XML files using a DOM tree. The
 * rootElementTag specifies the required element for the XML document&apos;s
 * root element.
 * @param <T> the type for the entities the loader returns.
 */
public abstract class AbstractXMLEntityLoader<T> extends AbstractEntityLoader<T> {
	private String			_rootElementTag;
	private XMLDomOps		_xmlOps;

	protected AbstractXMLEntityLoader (
		String name, ResourceLoader resourceLoader, String rootElementTag
	) {
		super (name, resourceLoader);
		zzInit (rootElementTag);
	}
	protected AbstractXMLEntityLoader (
		String name, ResourceLoader resourceLoader, String rootElementTag, String prefix, String suffix
	) {
		super (name, resourceLoader, prefix, suffix);
		zzInit (rootElementTag);
	}

	/**
	 * The required tag for the document root element
	 * @return the required tag for the document's root element (never null, never empty)
	 */
	public String getRootElementTag () {
		return _rootElementTag;
	}

	/**
	 * @return a helper for working with XML DOM elements
	 */
	protected XMLDomOps zGetXmlOps () { return _xmlOps; }

	/**
	 * Load the resource for the path and create an XML DOM tree from its
	 * contents.
	 * @param resourcePath the resource path w/o prefix/suffix (not null, not empty)
	 * @return the root element of the DOM tree
	 * @throws IOException on a load or parse error
	 */
	protected Element zGetDom (String resourcePath) 
		throws IOException, ResourceNotFoundException
	{
		// Stream ident includes path transformations applied by
		// the resource loader so it is not usable as the path
		// to pass to the resource loader. We have to use zEffPath
		// which just applies our prefix/suffix.
		String streamIdent = getEffectivePath (resourcePath);
		InputStream is = getResourceLoader ().getInputStream (zEffPath (resourcePath));
		if (is == null)
			throw new ResourceNotFoundException (
				zErrMsg (streamIdent, "resource not found", null)
			);

		// Need to use a new instance for loading documents since there
		// is a side-effect (modifying the error message)
		XMLDomOps xmlLoader = new XMLDomOpsImpl ();
		Element docRoot = xmlLoader.loadXMLStream (streamIdent, is, _rootElementTag);
		if (docRoot == null)
			throw new IOException (zErrMsg (streamIdent, xmlLoader.getErrMsg (), null));
		
		return docRoot;
	}

	protected String zErrMsg (String streamIdent, String message, Throwable t) {
		return zErrMsg (_rootElementTag, streamIdent, message, t);
	}

	private void zzInit (String rootElementTag) {
		if (rootElementTag == null)
			throw new NullPointerException ("root element tag is null");
		if (rootElementTag.isEmpty ())
			throw new IllegalArgumentException ("root element tag is empty");
		_rootElementTag = rootElementTag;
		_xmlOps = new XMLDomOpsImpl ();
	}
}
