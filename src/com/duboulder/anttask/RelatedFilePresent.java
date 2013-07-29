package com.duboulder.anttask;

import java.io.File;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.selectors.*;

/**
 * A file selector that checks for the existence of a related file in the
 * same directory as the source, but whose name is different than the source
 * file being checked.<br/>
 * <br/>
 * The custom String parameter named relatedFile specifies the related file
 * name to check. For example:<br/>
 * <br/>

&lt;fileset dir="workspace"&gt;
  <div style="padding-left: 24pt;">
    &lt;custom classname="com.duboulder.anttask.RelatedFilePresent"&gt;
    <div style="padding-left: 18pt">
     	&lt;includes="** /build.xml"/&gt;
        &lt;param name="relatedFile" value="javadoc.xml"/&gt;
    </div>
    &lt;/custom&gt;
  </div><br/>
&lt;/fileset&gt;<br/>
<br/>
 * If this fileset is included in subant task, only build.xml files 
 * that have a companion javadoc.xml file will be selected.
 */
public class RelatedFilePresent extends BaseExtendSelector {
	// NOTE: the selector is is reused
	private String			_relatedFile;

	@Override
	public void setParameters(Parameter[] parameters) {
		// Reset state
		_relatedFile = null;

		// Execution environment is setting the parameters that
		// have been specified in the ant file
		if (parameters == null) return;
		for (Parameter parameter : parameters) {
			if (parameter == null) continue;
			if (!parameter.getName ().equals ("relatedFile"))
				throw new BuildException (
					"unsupported parameter '" + parameter.getName () + "'"
				);
			String pType = parameter.getType ();
			if (pType != null && !pType.equals ("java.lang.String"))
				throw new BuildException (
					"the custom parameter relatedFile must be a String"
				);
			if (_relatedFile != null)
				throw new BuildException (
					"the parameter relatedFile has already been set"
				);
			_relatedFile = parameter.getValue ();
		}

		if (_relatedFile == null)
			throw new BuildException (
				"the String parameter 'relatedFile' is required"
			);
	}

	@Override
	public boolean isSelected(File baseDir, String filename, File file)
			throws BuildException 
	{
		if (filename == null) return false;
		if (filename.isEmpty ()) return false;

		// Get the directory part (which is relative to the containing fileset's
		// directory)
		int i = filename.lastIndexOf (File.separatorChar);
		String relatedName = baseDir.getPath() + File.separator +  
			(i < 0 ? filename : filename.substring (0, i)) +
			File.separator + _relatedFile;

		return new File (relatedName).exists ();
	}
}
