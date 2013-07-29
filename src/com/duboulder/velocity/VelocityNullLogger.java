package com.duboulder.velocity;

import org.apache.velocity.runtime.*;
import org.apache.velocity.runtime.log.*;

/**
 * Null logger that suppresses all velocity logging output 
 */
public class VelocityNullLogger implements LogChute {
	public VelocityNullLogger () {}

	@Override
	public void init (RuntimeServices rs) throws Exception {
	}

	@Override
	public boolean isLevelEnabled (int level) {
		return false;
	}

	@Override
	public void log (int level, String message, Throwable t) {
	}

	@Override
	public void log (int level, String message) {
	}
}
