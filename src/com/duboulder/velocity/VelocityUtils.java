/**
 * 
 */
package com.duboulder.velocity;

import java.util.*;
import org.apache.velocity.app.*;
import org.apache.velocity.runtime.log.*;
//import org.apache.velocity.runtime.resource.loader.*;

/**
 * Utility operations related to Velocity.
 */
public class VelocityUtils {
	/**
	 * Create and initialize a velocity engine. The config properties are
	 * used as a baseline. If the logger or resource loader arguments are
	 * non-null, the configuration is adjusted to use them. The resource
	 * loader is added to the end to the end of the resource loader list
	 * if present.<br/><br/>
	 * The resource loader is also stored as a property of the engine
	 * under the name BASE_RESOURCE_LOADER. This can be used when
	 * determining effective paths.
	 * @param configProps the base-line properties (may be null)
	 * @param logger the LogChute instance for logging (may be null)
	 * @param resLoader the velocity template resource loader (may be null)
	 * @return the initialized velocity engine
	 */
	public static VelocityEngine CreateEngine (
		Properties configProps, LogChute logger, 
		org.apache.velocity.runtime.resource.loader.ResourceLoader resLoader
	) {
		VelocityEngine ve = new VelocityEngine ();

		// Set the engine's base properties 
		if (configProps != null) {
			Set<Map.Entry<Object,Object>> eSet = configProps.entrySet ();
			for (Map.Entry<Object,Object> kv : eSet) {
				String keyString = (String) kv.getKey ();
				ve.setProperty (keyString, kv.getValue ());
			}
		}

		// Allow null assignments so that the left hand sides of assignments
		// are always updated
		ve.setProperty ("directive.set.null.allowed", "true");

		// Set the logging instance if a logger has been provided
		if (logger != null)
			ve.setProperty ("runtime.log.logsystem", logger);

		// Add the resource loader to the list of loaders
		if (resLoader != null) {
			String loaderPrefix = "VUtilsLoader";

			// Add the loader to the end of the already configured loaders
			Object loaderList = ve.getProperty ("resource.loader");
			if (loaderList == null)
				loaderList = loaderPrefix;
			else
				loaderList = ((String) loaderList) + ", " + loaderPrefix;
			ve.setProperty ("resource.loader", loaderList);

			// NOTE: This key isn't documented, it is checked for
			// in org.apache.velocity.runtime.resource.ResourceManagerImpl
			//		initialize (final RuntimeServices rsvc)
			// for velocity 1.6.1 & velocity 1.6.2
			// Looks like it should be documented just like
			// webctx.resource.loader.class is, it is used in the same place
			ve.setProperty (
				loaderPrefix + ".resource.loader.instance", resLoader
			);

			ve.setProperty (
				loaderPrefix + ".resource.loader.description", 
				"VelocityUtils: " + resLoader.getClass ().getName ()
			);
			
			/* Disable caching as we want it handled at a higher level */ 
			ve.setProperty (
				loaderPrefix + ".resource.loader.cache", false
			);
		}

		// Initialize the instance
		try {
			ve.init ();
		}
		catch (Exception e) {
			// Convert the checked exception to an unchecked error
			throw new Error (
				"VelocityUtils.CreateEngine failed " +
					"in thread " + Thread.currentThread ().getId (), e
			);
		}
		return ve;
	}
}
