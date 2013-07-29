package com.duboulder.reflection;

/**
 * Class for holding a reference to String[] to 
 * preserve runtime type information.
 */
public class StringArrayHolder {
	/**
	 * The array reference.
	 */
	public String[] array;
	public StringArrayHolder (String[] array) {
		this.array = array;
	}
}
