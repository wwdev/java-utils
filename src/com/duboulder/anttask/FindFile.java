package com.duboulder.anttask;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * Find the file name in the implicit fileset. The
 * property whose name is specified in the property
 * attribute is set to the absolute path of the
 * first file found.<br/>
 * <br/>
 * Attributes:<br/>
 * <div style="padding-left:24pt;">
 *    basedir - String<br/>
 *        &nbsp;&nbsp;The root directory for the implicit file set<br/>
 *    property - String<br/>
 *        &nbsp;&nbsp;The property to set with the found name<br/>    
 * </div>
 * <br/>
 */
public class FindFile extends MatchingTask {
	private String			_property;
	private File			_baseDir;

	public FindFile () {
	}

	public String getProperty () { return _property; }
	public void setProperty (String str) {
        if (str == null)
            throw new BuildException ("property attribute cannot be null");

		_property = str.trim ();
        if (_property.isEmpty ()) {
            _property = null;
            throw new BuildException ("property attribute cannot be an empty string");
        }
	}

	public File getBaseDir () { return _baseDir; }
	public void setBaseDir (File baseDir) {
		if (baseDir == null)
			throw new BuildException (
				"baseDir must not be null"
			);
		_baseDir = baseDir;
	}

	@Override
	public void execute() throws BuildException {
        if (_baseDir == null)
            _baseDir = new File (".");
        if (_property == null)
            throw new BuildException ("property attribute is required");

		DirectoryScanner dirScan = getDirectoryScanner (_baseDir);
		for (String relPath : dirScan.getIncludedFiles ()) {
// Log the absolute input path that most tasks
// will use when using a file set.
log (_baseDir + "/" + relPath);

            File file = new File (_baseDir + File.separator + relPath);
            if (file.exists ()) {
		        getProject ().setProperty ( _property, file.getAbsolutePath ());
                return;
            }
		}
	}
}

