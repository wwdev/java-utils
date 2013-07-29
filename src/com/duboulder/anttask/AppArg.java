package com.duboulder.anttask;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.types.Mapper;

/**
 * Application argument value that can include a file path mapper.
 * The argument can have an option that precedes it. The argument value
 * and file name mapper are not required if the option attribute
 * has been specified.<br/>
 * <br/>
 * An argument can have either a static value, or a file name mapper.<br/>
 * <br/>
 * When the value attribute is used, the text used in the argument
 * list is the value attribute except for the special case where the
 * text is %. In this case the source file name is used as the
 * argument text (absolute path).<br/>
 * <div style="padding-left: 24pt">
 *   &lt;apparg option="--in" value="%"/&gt;<br/>
 *   &lt;apparg option="--append-mode"/&gt;<br/>
 *   &lt;apparg value="fixed${property}"/&gt;
 * </div>
 * <br/>
 * When the value is not set, the element must contain
 * a file name mapper element.<br/>
 * <div style="padding-left: 24pt"> 
 *    &lt;apparg option="--out"&gt;
 *        <div style="padding-left: 24pt"> 
 *    &nbsp;&nbsp;&lt;globmapper from="*.txt" to="*.html"/&gt;
 *        </div>
 *    &lt;/apparg&gt;<br/>
 *    &lt;apparg&gt;&lt;flattenmapper/&gt;&lt;/apparg&gt;
 * </div>
 * <br/>
 * In all cases, the static text or mapped file name have
 * text substitution performed if the replace and with
 * attributes are set. All occurrences of the replace
 * text in the computed argument value are replaced with
 * the text of the with attribute.<br/>
 * <br/>
 * Attributes:<br/>
 * <div style="padding-left:24pt">
 *    option - String (optional)<br/>
 *        &nbsp;&nbsp;The option string that precedes the argument value<br/>
 * <br/>
 *    value - String (required if there is no mapper element)<br/>
 *        &nbsp;&nbsp;The static value for the argument<br/>
 * <br/>
 *    ifEnabled - boolean (optional, defaults to true)<br/>
 *        &nbsp;&nbsp;When false, the option is not  processed<br/>
 * <br/>
 *    ifNotEmpty - boolean (optional)<br/>
 *        &nbsp;&nbsp;When true, the option is not added if the effective value<br/>
 *        &nbsp;&nbsp;is the empty string (checked after white space trimming).<br/>
 * <br/>    
 *    replace - String (optional)<br/>
 *        &nbsp;&nbsp;The substring to replace (all occurrences are replaced)<br/>
 * <br/>
 *    with - String (optional)<br/>
 *        &nbsp;&nbsp;The replacement text<br/>
 * </div>
 * <br/>
 * Contained Elements:<br/>
 * <div style="padding-left:24pt;">
 *   &lt;mapper .../&gt; or its variants (Optional)<br/>
 *   <div style="padding-left: 24pt;">
 *      Specifies a mapper converting the source file name
 *      to the argument value. If used, value attribute 
 *      cannot be set.<br/>
 *      NOTE: The source file name is the absolute path
 *      to the file.
 *   </div>
 * </div>
 */
public class AppArg extends DataType {
	protected String					_option;
	protected String					_value; // a (mostly) fixed value
	protected FileNameMapper			_mapper;
	protected boolean					_ifEnabled;
	protected boolean					_ifNotEmpty;
	protected String					_replace;
	protected String					_with;

	public AppArg () {
        _ifEnabled  = true;
        _ifNotEmpty = false;
		_mapper     = null;
	}

	/**
	 * Specify the argument option that precedes the argument value.
	 * @return the argument option or null
	 */
	public String getOption () { return _option; }
	public void setOption (String s) {
		_option = getProject ().replaceProperties (s); 
	}

	/**
	 * Static argument value (e.g. source file independent switch).
	 * The value or mapper attribute can be set but not both.
	 * @return the static value or null.
	 */
	public String getValue () { return _value; }
	public void setValue (String v)
		throws BuildException
	{
		if (_mapper != null && v != null)
			throw new BuildException (
				"Cannot set a static value and a file name mapper"
			); 
		_value = getProject ().replaceProperties (v); 
	}

	/**
	 * A file name mapper that transforms the task source file name
	 * into the argument value. The value attribute or mapper element
	 * can be set but not both.
	 * @return the file name mapper or null
	 */
	public FileNameMapper getMapper () { return _mapper; }
	public void addConfiguredMapper (Mapper mapper) {
		if (_value != null && mapper != null)
			throw new BuildException (
				"Cannot set a static value and a file name mapper"
			);  
		_mapper = mapper.getImplementation (); 
	}

	/**
	 * Control for whether the argument is processed at all. When
	 * false, the option is completely skipped.
	 * @return true if the option should be included in the arg list
	 */
	public boolean getIfEnabled () { return _ifEnabled; }
	public void setIfEnabled (boolean f) { _ifEnabled = f; }

	/**
	 * Control for whether the argument and its option are included
	 * if the option value is an empty string (e.g. property used
	 * in the value attribute is the empty string).
	 * @return true if empty arguments are propagated to the jar execution
	 */
	public boolean getIfNotEmpty () { return _ifNotEmpty; }
	public void setIfNotEmpty (boolean f) { _ifNotEmpty = f; }

	/**
	 * Define the the match string for the post-mapping
	 * text substitution phase. 
	 * @return the replacement string
	 */
	public String getReplace () { return _replace; }
	public void setReplace (String txt) {
		_replace = getProject ().replaceProperties (txt); 
	}

	/**
	 * Define the substitution string for the post-mapping
	 * text substitution phase. 
	 * @return the substitution string
	 */
	public String getWith () { return _with; }
	public void setWidth (String txt) {
		_with = getProject ().replaceProperties (txt); 
	}

	/**
	 * Get the argument text. If a mapper has been specified
	 * it is applied to filePath. If neither the value nor a
	 * mapper have set, the return value is null.
	 * @param filePath the source file path for mapping
	 * @return the text for the arg or null
	 * @throws BuildException
	 */
	public String getArgText (String filePath)
		throws BuildException
	{
		if (_value != null) {
			String vStr = (_value.equals ("%") ? filePath : _value);
			return zzDoTextSubst (vStr);
		}
		if (_mapper == null) {
			if (_option == null)
				throw new BuildException (
						"Either the option, or a value or a file name mapper must be set"
				);
			return null;
		}

		String[] answer = _mapper.mapFileName (filePath);
		if (answer == null || answer.length == 0) {
			return zzDoTextSubst (filePath);
		}

		return zzDoTextSubst (answer[0]);
	}

	private String zzDoTextSubst (String text) {
		if (text == null) return "";
        text = text.trim ();

		if (_replace == null) return text;

		if (_with == null) _with = "";
		return text.replace (_replace, _with);
	}
}
