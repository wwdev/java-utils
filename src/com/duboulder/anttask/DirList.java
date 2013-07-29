package com.duboulder.anttask;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 * Display the files matching the includes/excludes. They
 * are sent to the log using INFO<br/>
 * <br/>
 * Attributes:<br/>
 * <div style="padding-left:24pt;">
 *    prefix - String<br/>
 *        &nbsp;&nbsp;The text to prepend to each line before logging it<br/>
 *    basedir - String<br/>
 *        &nbsp;&nbsp;The root directory for the implicit file set
 * </div>
 * <br/>
 * Contained elements:<br/>
 * <div stlye="padding-left: 24pt">
 *    &lt;mapper ..../&gt; - File name mapper (Optional)<br/>
 *    <div style="padding-left: 24pt;">
 *      The results of the mapper's transformation of
 *      each file path is displayed when this element
 *      is present (at most one occurrence).
 *    </div>
 * </div>
 */
public class DirList extends MatchingTask {
	private String			_linePrefix;
	private File			_baseDir;
	private FileNameMapper	_mapper;

	public DirList () {
		_linePrefix = "";
	}

	public String getPrefix () { return _linePrefix; }
	public void setPrefix (String str) {
		_linePrefix = str;
		if (_linePrefix == null) _linePrefix = "";
	}

	public File getBaseDir () { return _baseDir; }
	public void setBaseDir (File baseDir) {
		if (baseDir == null)
			throw new BuildException (
				"baseDir must not be null"
			);
		_baseDir = baseDir;
	}

	// it appears jdk 6 is pickier about overload matching - so use the redundant
	// versions to try and guarantee a match
	/**
	 * This adds a <mapper type="xxx" ..../> element
	 */
	public void addConfiguredMapper (Mapper mapper)	{
		addConfiguredMapper (mapper.getImplementation ());
	}
	public void addConfiguredMapper (FileNameMapper mapper)	{
		if (_mapper != null)
			throw new BuildException (
				"Can only have one output file name mapper"
			);
		_mapper = mapper; 
	}

	@Override
	public void execute() throws BuildException {
		DirectoryScanner dirScan = getDirectoryScanner (_baseDir);
		for (String relPath : dirScan.getIncludedFiles()) {
			// Log the absolute input path that most tasks
			// will use when using a file set.
			log (_linePrefix + _baseDir + "/" + relPath);
			
			// If a mapper was defined, then show the results
			// of the mapping
			if (_mapper != null) {
				String[] mNames = _mapper.mapFileName (
						_baseDir + "/" + relPath
				);
				String mName = "<unmatched by mapper>";
				if (mNames != null && mNames.length > 0)
					mName = mNames[0];

				log (_linePrefix + "    --> " + mName);
			}
		}
	}
}
