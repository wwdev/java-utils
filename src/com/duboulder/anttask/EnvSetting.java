package com.duboulder.anttask;

import org.apache.tools.ant.*;

/**
 * An environment setting to display or to check. Used as nested
 * elements of EnvSettings. The default values for all implementations
 * must be: enable=true, checked=false.
 */
public interface EnvSetting {
	/**
	 * The name of the setting (eg a property name)
	 * @return the name (never null, never empty)
	 */
	String getName ();
	/**
	 * Set the setting name
	 * @param name the new name (not null, not empty).
	 */
	public void setName (String name);
	/**
	 * Indicator of whether the setting is enabled
	 * @return true if the setting is enabled
	 */
	boolean isEnabled ();
	/**
	 * Indicator of whether the setting is enabled
	 * @return true if the setting is enabled
	 */
	boolean getEnabled ();
	/**
	 * Set enabled attribute
	 * @param f the enabled value
	 */
	void setEnabled (boolean f);
	/**
	 * The number spacing lines that should follow the display
	 * of this setting.
	 * @return the number of lines (&gt;= 0)
	 */
	int getSpaceAfter ();
	/**
	 * Set the number of spacing lines after this setting&apos;s output
	 * @param f the number of lines (&gt;= 0)
	 */
	void setSpaceAfter (int count);
	/**
	 * Indicator of whether environment checking should
	 * really done by the check method. 
	 * @return true if checking is enabled
	 */
	boolean isChecked ();
	/**
	 * Indicator of whether environment checking should
	 * really done by the check method. 
	 * @return true if checking is enabled
	 */
	boolean getChecked ();
	/**
	 * Set the checking enable value.
	 * @param f the new check enable value
	 */
	void setChecked (boolean f);
	/**
	 * Display the environment setting using the log methods of
	 * the supplied project.
	 * @param project the project to use as the environment and to
	 * 		use for logging
	 */
	void show (Project project);
	/**
	 * Perform an environment check for the specified project
	 * if the enabled  property and checked property are true. 
	 * When checking is not enabled, always return true.
	 * @param project the project to use for as the environment
	 * @return null if the check was successfull, otherwise an
	 *    error message which may have embedded new lines.
	 */
	String check (Project project);
}
