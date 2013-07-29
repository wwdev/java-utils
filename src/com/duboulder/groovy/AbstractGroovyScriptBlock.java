package com.duboulder.groovy;

/**
 * Implementation helper for the GroovyScriptBlock interface
 */
public abstract class AbstractGroovyScriptBlock implements GroovyScriptBlock {
	private String					_packageName;
	private String					_name;

	protected AbstractGroovyScriptBlock (String packageName, String name) {
		zzCheck ("packageName", packageName);
		zzCheck ("name", name);
		_packageName = packageName;
		_name = name;
	}

	@Override
	public String getName () { return _name; }

	@Override
	public String getPackage () { return _packageName; }

	protected static void zzCheck (String ident, String name) {
		if (name == null)
			throw new NullPointerException (ident + " is null");
		if (name.isEmpty ())
			throw new IllegalArgumentException (ident + " is empty");
	}
}
