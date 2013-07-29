package com.duboulder.reflection;

/**
 * Convenience class for passing a place to store throwables (e.g.
 * ReflectionUtils.CreateInstance)
 */
public class ThrowableHolder {
	/**
	 * The changeable Throwable reference.
	 */
	public Throwable	throwable;
	
	/**
	 * Initialize throwable to null.
	 */
	public ThrowableHolder () {}
	/**
	 * Initialize throwable to t.
	 * @param t the initial value for throwable.
	 */
	public ThrowableHolder (Throwable t) {
		this.throwable = t;
	}
	
	/**
	 * The throwable that has been set (defaults to null)
	 * @return the throwable or null
	 */
	public Throwable getThrowable () { return throwable; }
	public void setThrowable (Throwable t) { throwable = t; }
	
	/**
	 * Get the throwable&apos;s message or return an empty string when there is no
	 * throwable or its message is null
	 * @param prefix optional prefix appended to a non-null, not empty message
	 * @return the message text or the empty string
	 */
	public String getNotNullMessage (String prefix) {
		if (throwable == null) return "";

		String msg = throwable.getMessage ();
		if (msg == null || msg.isEmpty ()) return "";
		
		return prefix == null ? msg : prefix + msg;
	}
}
