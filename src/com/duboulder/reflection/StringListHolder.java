package com.duboulder.reflection;

import java.util.*;

/**
 * Class for holding a reference to List<String> to 
 * preserve runtime type information.
 */
public class StringListHolder {
	/**
	 * The list reference.
	 */
	public List<String> list;
	
	public StringListHolder (List<String> list) {
		this.list = list;
	}
}
