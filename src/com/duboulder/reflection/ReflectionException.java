package com.duboulder.reflection;

/**
 * Error loading/creating an object/class using reflection
 */
public class ReflectionException
	extends RuntimeException 
{
	private static final long serialVersionUID = 1;

	public ReflectionException () {}
	public ReflectionException (String msg) { super (msg); }
	public ReflectionException (Throwable t) { super (t); }
	public ReflectionException (String msg, Throwable t) { super (msg, t); }
}
