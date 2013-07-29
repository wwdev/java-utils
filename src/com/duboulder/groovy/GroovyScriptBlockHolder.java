package com.duboulder.groovy;

/**
 * An interface for classes that have a Groovy script aspect.
 */
public interface GroovyScriptBlockHolder {
	/**
	 * The text for script
	 * @return not null
	 */
	String getScriptText ();
	/**
	 * Set the script text
	 * @param text the script text (not null)
	 */
	void setScriptText (String text);

	/**
	 * The script block instance for the class wrapping
	 * the script fragment.
	 * @return the wrapper instance (may be null)
	 */
	GroovyScriptBlock getScriptBlock ();
	/**
	 * Set the script block instance.
	 * @param scriptBlock the script wrapper (may be null)
	 */
	void setScriptBlock (GroovyScriptBlock scriptBlock);
}
