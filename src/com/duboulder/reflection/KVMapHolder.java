package com.duboulder.reflection;

import java.util.*;

/**
 * Class for holding a reference to a Map<String,Object> to 
 * preserve runtime type information.
 */
public class KVMapHolder {
	/**
	 * The map reference.
	 */
	public Map<String,Object> map;

	public KVMapHolder (Map<String,Object> map) {
		this.map = map;
	}
}
