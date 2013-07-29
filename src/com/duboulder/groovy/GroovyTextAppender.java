package com.duboulder.groovy;

import java.io.*;

/**
 * A GroovyCodeFragment that appends a fixed text in a specified scope.
 */
public class GroovyTextAppender extends AbstractGroovyCodeFragment {
	private String			_text;

	/**
	 * Initialize to add the text in the specified scope
	 * @param scope the generation scope (not null, not empty)
	 * @param text the text to add (not null, not empty)
	 */
	public GroovyTextAppender (String scope, String text) {
		super (scope);
		zzCheck ("text", text);
		_text = text;
	}

	/**
	 * The text to add
	 * @return the text (not null, not empty)
	 */
	public String getText () { return _text; }

	@Override
	public void addText(Object context, String scope, PrintWriter output) {
		if (zScopeIndex (scope) >= -1)
			output.println (_text);
	}
}
