package com.duboulder.reflection;

import java.util.*;

/**
 * Class for holding a reference to ArrayList<String> to 
 * preserve runtime type information.
 */
public class StringArrayListHolder {
	/**
	 * The list reference.
	 */
	public ArrayList<String> list;
	
	public StringArrayListHolder (ArrayList<String> list) {
		this.list = list;
	}
}
