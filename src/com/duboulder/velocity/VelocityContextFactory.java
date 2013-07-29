package com.duboulder.velocity;

import org.apache.velocity.*;

/**
 * A factory that creates a VelocityContext instance from
 * run-time values and possibly a parent context.
 */
public interface VelocityContextFactory {
	/**
	 * @return the identifying name for the factory (never null, not empty)
	 */
	String getName ();
	
	/**
	 * @return the parent context to be used for all contexts created by
	 * this factory (may be null)
	 */
	VelocityContext getParentConext ();

	/**
	 * Create a new velocity context using the run-time values, also uses
	 * the parent context during the creation if it is not null.
	 * @param values arbitrary run-time value used for populating the context 
	 * 		(implementation specifies whether this can be null)
	 * @return the new initialized velocity-context
	 */
	VelocityContext createContext (Object values);
}
