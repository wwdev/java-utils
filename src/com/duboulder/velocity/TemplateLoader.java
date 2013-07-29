/**
 * 
 */
package com.duboulder.velocity;

import java.io.*;
import org.apache.velocity.*;
import com.duboulder.resource.*;

/**
 * A pre-configured velocity template loader. Implementations
 * must be thread safe. A loader implementation _does not_
 * cache templates unless the implementation explicitly
 * specifies caching behavior.<br/>
 * <br/>
 * NOTE: With velocity 1.6.1 the VelocityTemplate implementation
 * looks to be thread safe - it only contains configuration data.
 * There is no template state that gets modified during a
 * template merge.
 */
public interface TemplateLoader extends EntityLoader<Template> {
	/**
	 * @return the loader's name (never null)
	 */
	String getName ();

	/**
	 * Answer the effective path used to locate the template. This
	 * would include prefixes/suffixes for example.
	 * @param templatePath the plain path (not null, not empty)
	 * @return the effective path
	 */
	String getEffectivePath (String templatePath);

	/**
	 * Load a template using the implementations resolution rules.
	 * @param templatePath the template path (not null, not empty)
	 * @return the template instance
	 */
	Template loadTemplate (String templatePath)
		throws ResourceNotFoundException, IOException;
}
