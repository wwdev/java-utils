package com.duboulder.groovy;

public abstract class AbstractGroovyCodeFragment implements GroovyCodeFragment {
	private String[]			_scopes;

	protected AbstractGroovyCodeFragment () {}

	protected AbstractGroovyCodeFragment (String... scopes) {
		if (scopes != null && scopes.length > 0)
			_scopes = scopes.clone ();
	}

	@Override
	public boolean handlesScope (String scope) {
		zzCheck ("scope", scope);
		if (_scopes == null) return true;
		for (String testScope : _scopes) {
			if (testScope.equals (scope)) return true;
		}
		return false;
	}

	/**
	 * Helper that searches the list of scopes and returns the matching index
	 * from the order the scopes were defined in constructor.
	 * @param scope the scope to test (not null, not empty)
	 * @return -1 if no scopes have been defined, -2 if there are scopes
	 * 		but the supplied scope does not match any of them, 0 to n-1
	 * 		if the scope matches one of the defined scopes
	 */
	protected int zScopeIndex (String scope) {
		if (_scopes == null) return -1;
		for (int i=0; i<_scopes.length; i++) {
			if (_scopes[i].equals (scope))
				return i;
		}
		return -2;
	}

	protected static void zzCheck (String ident, String value) {
		if (value == null)
			throw new NullPointerException (ident + " is null");
		if (value.isEmpty ())
			throw new IllegalArgumentException (ident + " is empty");
	}
	
	protected static void zzCheck (String ident, Object value) {
		if (value == null)
			throw new NullPointerException (ident + " is null");
	}

	// Trim leading/trailing white space - convert empty strings to nulls
	protected static String zzTrim (String value) {
		if (value == null) return null;
		value = value.trim ();
		return value.isEmpty () ? null : value;
	}
}
