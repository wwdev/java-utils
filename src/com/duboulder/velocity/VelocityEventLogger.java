/**
 * 
 */
package com.duboulder.velocity;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.*;
import com.duboulder.log.*;

/**
 * This class implements the org.apache.velocity.runtime.RuntimeServices.LogChute
 * interface. Log requests are converted to LogEvents which are placed on the 
 * specified LogEventQueue.
 */
public class VelocityEventLogger implements LogChute {
	private LogEventQueue			_eventQueue;

	public VelocityEventLogger (LogEventQueue eventQueue) {
		setEventQueue (eventQueue);
	}

	/**
	 * @return the event queue this logger sends events to (never null)
	 */
	public LogEventQueue getEventQueue () { return _eventQueue; }
	public void setEventQueue (LogEventQueue eventQueue) {
		if (eventQueue == null)
			throw new NullPointerException ("eventQueue is null");
		_eventQueue = eventQueue;
	}

	@Override
	public void init (RuntimeServices ri) throws Exception {
		// NOOP
	}

	@Override
	public boolean isLevelEnabled (int level) {
		// Do event filtering in the queue processors
		return true;
	}

	@Override
	public void log (int level, String msg) {
		_eventQueue.add (new LogEventImpl (
			convertLogLevel (level), new LogSrcException (1), msg
		));
	}

	@Override
	public void log (int level, String msg, Throwable exception) {
		_eventQueue.add (new LogEventImpl (
			convertLogLevel (level), new LogSrcException (1), msg, exception
		));
	}

	public static LogLevel convertLogLevel (int velocityLevel) {
		switch (velocityLevel) {
			case WARN_ID: 	return LogLevel.WARN;
			case ERROR_ID: 	return LogLevel.ERROR;
			case INFO_ID: 	return LogLevel.INFO;
			case TRACE_ID: 	return LogLevel.TRACE;
			case DEBUG_ID: 	return LogLevel.DEBUG;
		}
		return LogLevel.ERROR;
	}
}
