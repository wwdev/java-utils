package com.duboulder.anttask;

import java.util.*;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

/**
 * Display a project banner in the build log.<br/>
 * <br/>
 * Attributes:<br/>
 * <div style="padding-left:24pt">
 *    name - String (required)<br/>
 *        &nbsp;&nbsp;The project name
 * </div>
 */
public class ProjectBanner extends Task {
	protected String		_projectName;
	
	public ProjectBanner () {}
	
	public String getName () { return _projectName; }
	public void setName (String name) {
		if (name == null)
			throw new BuildException (
				"name attribute must not be null"
			);
		_projectName = name;
	}

	@Override
	public void execute() throws BuildException {
		if (_projectName == null)
			throw new BuildException ("name attribute is rquired");

		Date curDate = new Date ();
		System.out.println ("");
		log ("================================================");
		log (_projectName + "[" + curDate.toString() + "]");
		log ("================================================");
	}
}
