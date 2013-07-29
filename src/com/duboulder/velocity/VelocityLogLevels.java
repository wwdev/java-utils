package com.duboulder.velocity;

/**
 * An enumeration version of the velocity LogChute
 * log level info.
 */
public enum VelocityLogLevels {
	TRACE_ID (-1, "[TRACE]"),
	DEBUG_ID (0,  "[DEBUG]"),
	INFO_ID  (1,  " [INFO]"),
	WARN_ID  (2,  " [WARN]"),
	ERROR_ID (3,  "[ERROR]");

	private final int			_id;		// velocity id
	private final String		_prefix;	// message prefix

	private VelocityLogLevels (int id, String prefix) {
		_id = id;
		_prefix = prefix;
	}
	
	/**
	 * The log level number value
	 * @return the log level number
	 */
	public int getId () { return _id; }
	
	/**
	 * A fixed width prefix identifying the log level.
	 * @return the prefix (never null, never empty)
	 */
	public String getPrefix () { return _prefix; }

	/**
	 * Map an int log level to an enum. If the level is not an exact match,
	 * the enum with the closest id value is returned.
	 * @param level the log level to match
	 * @return a log level enum
	 */
	public static VelocityLogLevels mapInt (int level) {
		for (VelocityLogLevels ll : values ()) {
			if (ll.getId () == level) return ll;
		}
		
		return level < 0 ? TRACE_ID : ERROR_ID;
	}
}
