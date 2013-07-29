package com.duboulder.anttask;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * Implementation helper for the EnvSetting interface. The constructor
 * sets enabled to true, checked to false, spaceAfter to 0, and
 * identWidth to 30.
 */
public abstract class EnvSettingBase extends DataType implements EnvSetting {
	private String			_name;
	private boolean			_enabled;
	private boolean			_checked;
	private int				_spaceAfter;
	private int				_identWidth;

	protected EnvSettingBase () {
		_enabled		= true;
		_checked		= false;
		_spaceAfter		= 0;
		_identWidth		= 30;
	}

	@Override
	public boolean getChecked() {
		return _checked;
	}

	@Override
	public boolean getEnabled() {
		return _enabled;
	}

	@Override
	public String getName() {
		return _name;
	}

	@Override
	public boolean isChecked() {
		return _checked;
	}

	@Override
	public boolean isEnabled() {
		return _enabled;
	}

	@Override
	public void setChecked(boolean f) {
		_checked = f;
	}

	@Override
	public void setEnabled(boolean f) {
		_enabled = f;
	}

	@Override
	public void setName (String name) {
		if (name == null)
			throw new NullPointerException ("name is null");
		name = name.trim ();
		if (name.isEmpty ())
			throw new BuildException ("enivornment setting name must not be empty");
	}

	/**
	 * The width of the ident field to use for displaying the setting name
	 * in name/value lines
	 * @return the width (&gt;= 20)
	 */
	protected int getIdentWidth () {
		return _identWidth;
	}
	/**
	 * Set the ident display width
	 * @param width the width in characters (&gt;= 20)
	 */
	protected void setIdentWidth (int width) {
		if (width < 20) 
			throw new IllegalArgumentException ("width < 20");
		_identWidth = width;
	}

	@Override
	public int getSpaceAfter() {
		return _spaceAfter;
	}

	@Override
	public void setSpaceAfter(int count) {
		if (count < 0)
			throw new BuildException ("the spaceafter value must be >= 0");
		_spaceAfter = count;
	}

	/**
	 * Return the ident as space padded string. The string will 
	 * always end with a space even if the ident is longer
	 * than the ident width.
	 * @param ident the identification (not null) 
	 * @return the space padded string
	 */
	protected String identString (String ident) {
		if (ident == null)
			throw new NullPointerException ("ident is null");
		if (ident.length () >= _identWidth)
			return ident + " ";

		StringBuilder sb = new StringBuilder (_identWidth);
		sb.append (ident);
		while (sb.length () < _identWidth)
			sb.append (' ');
		
		return sb.toString ();
	}
	
	protected void logNameValue (Project project, String value) {
		project.log (
			identString (_name) + "= " + 
				(value == null ? "<NULL>" : value)
		);
	}
}
