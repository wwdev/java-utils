package com.duboulder.anttask;

import java.io.*;
import org.apache.tools.ant.*;

/**
 * Convert a string containing a qualified java name to a file system
 * path form by replacing all dots with the file path separator character.<br/>
 * <br>
 * Required attributes:<br/>
 * <div style="padding-left: 24pt">
 *   <table>
 *     <tr><td>value - String</td>
 *         <td>&nbsp;</td>
 *         <td>The text to convert from qualified name form to file system path form</td>
 *     </tr>
 *     <tr><td>property - String</td>
 *     	   <td>&nbsp;</td>
 *         <td>the property name that stores the converted text</td>
 *     </tr>
 *   <table>
 * </div>
 * <br/>
 * <pre>
 * Ant build.xml declarations required:
 *	  <taskdef name="qnametopath" classname="com.duboulder.anttask.QNameToPath"
 *             description="Convert a string from java qualified name form to file system path form."/>
 * </pre>
 */
public class QNameToPath extends Task {
	private String			_value;
	private String			_property;

	public QNameToPath () {}

	/**
	 * The value to convert from a qualified java name form  to
	 * a files system path form
	 * @return the value convert (not null)
	 */
	public String getValue () { return _value; }
	public void setValue (String value) {
		if (value == null)
			throw new BuildException ("value is null");
		_value = value;
	}

	/**
	 * The name of the property that receives the converted value.
	 * @return the property name (not null, not empty)
	 */
	public String getProperty () { return _property; }
	public void setProperty (String property) {
		if (property == null)
			throw new BuildException ("property is null");
		if (property.trim ().isEmpty ())
			throw new BuildException ("property is empty");
		_property = property.trim ();
	}

	@Override
	public void execute() throws BuildException {
		if (_value == null)
			throw new BuildException ("value attribute is required");
		if (_property == null)
			throw new BuildException ("property attribute is required");
		
		// Convert dots to file separator chars
		String pathForm = getProject ().replaceProperties (_value);
		pathForm = pathForm.replace ('.', File.separatorChar);
		
		// Store the property
		getProject ().setProperty ( _property, pathForm);
	}
}
