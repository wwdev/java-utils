package com.duboulder.velocity;

import java.io.*;
import org.apache.velocity.runtime.*;
import org.apache.velocity.runtime.log.*;

/**
 * Logger that sends messages of all levels to the provided print stream.
 */
public class VelocityPSLogger implements LogChute {
	private PrintStream		_os;
	
    /**
     * Initialize useing the specified print stream as the destination
     * @param os the destination stream (not null)
     */
	public VelocityPSLogger (PrintStream os) {
		if (os == null)
			throw new NullPointerException ("os is null");
		_os = os;
	}

	@Override
	public void init(RuntimeServices rs) throws Exception {
	}

	@Override
	public boolean isLevelEnabled(int level) {
		return true;
	}

	@Override
	public void log (int level, String message, Throwable t) {
		log (level, message);
		if (t == null) return;
		t.printStackTrace (_os);
	}

	@Override
	public void log (int level, String message) {
		VelocityLogLevels ll = VelocityLogLevels.mapInt (level);
		_os.println (
			ll.getPrefix() + " " +
			(message == null ? "" : message)
		);
	}
}
