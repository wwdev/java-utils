package com.duboulder.velocity;

import java.io.*;
import org.apache.velocity.*;
import org.apache.velocity.context.*;

/**
 * A base implementation of TemplateExec
 */
public class TemplateExecImpl implements TemplateExec {
	private String				_name;
	private String				_prefix;
	private String				_suffix;
	private TemplateLoader		_templateLoader;

	public TemplateExecImpl (
		String name, String prefix, String suffix, TemplateLoader templateLoader
	) {
		zzCheck ("name", name);
		zzCheck ("templateLoader", templateLoader);
		_name = name;
		setPrefix (prefix);
		setSuffix (suffix);
		_templateLoader	= templateLoader;
	}

	@Override
	public String getName () { return _name; }

	@Override
	public String getPrefix () { return _prefix; }
	@Override
	public void setPrefix (String prefix) {
		_prefix = prefix;
		if (_prefix != null)
			_prefix = _prefix.trim ();
		else
			_prefix = "";
	}

	@Override
	public String getSuffix () { return _suffix; }
	@Override
	public void setSuffix (String suffix) {
		_suffix = suffix;
		if (_suffix != null)
			_suffix = _suffix.trim ();
		else
			_suffix = ".vm";
	}

	@Override
	public void execute (String templatePath, Context context, PrintWriter output) {
		zzCheck ("templatePath", templatePath);
		zzCheck ("context", context);
		zzCheck ("output", output);

		try {
//	if (getDevMode () && getLogger () != null && getDebugLevel () > 0) {
//		getLogger ().logDebug (
//			"Template path: " + templatePath, null,
//				_templateLoader.getEffectivePath (templatePath)
//		);
//	}

			Template template = _templateLoader.loadTemplate (templatePath);

//	if (getDevMode () && getLogger () != null)
//		getLogger ().logDebug (
//			"Template " + 
//				_templateLoader.getEffectivePath (templatePath) + 
//				" found", 
//			null, template
//		);

			template.merge (context, output);
		} catch (Exception e) {
			String effPath = _templateLoader.getEffectivePath (templatePath);
			throw new VelocityExecException (
				"Error executing template '" + effPath + "'" +
				(e.getMessage () == null ? "" : ":\n    " + e.getMessage ()),
				e
			);
		}
	}

	private static void zzCheck (String ident, String value) {
		if (value == null)
			throw new NullPointerException (ident + " is null");
		if (value.isEmpty ())
			throw new NullPointerException (ident + " is empty");
	}

	private static void zzCheck (String ident, Object value) {
		if (value == null)
			throw new NullPointerException (ident + " is null");		
	}
}
