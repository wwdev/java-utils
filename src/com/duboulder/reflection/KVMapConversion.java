package com.duboulder.reflection;

import java.util.*;

/**
 * Interface with methods that convert an object value to/from
 * a map<String,Object>. Classes should implement this interface
 * when generic access is required but it is not desired to use
 * reflection or generic bean operations.
 */
public interface KVMapConversion {
	/**
	 * Create a map with the object property values.
	 * @return the map with the public values (never null)
	 */
	Map<String,Object> toMap ();

	/**
	 * Populate the object using the values from the map. The
	 * implementation decides what values it will accept for
	 * updating state.
	 * @param map the map with the update values (not null)
	 */
	void fromMap (Map<String,Object> map);
}
