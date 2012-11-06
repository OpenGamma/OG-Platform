/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.log;

/**
 * Implementation of {@link LogEventListener} which forwards messages to a thread-local listener.
 */
public class ThreadLocalLogEventListener implements LogEventListener {

  private final ThreadLocal<LogEventListener> _listener = new ThreadLocal<LogEventListener>();
  
  //-------------------------------------------------------------------------
  /**
   * Sets the listener for the calling thread.
   * 
   * @param listener  the listener, not null
   */
  public void setThreadLocalListener(LogEventListener listener) {
    _listener.set(listener);
  }
  
  /**
   * Removes the listener for the calling thread.
   * 
   * @throws IllegalStateException  if there is no listener to remove for the calling thread
   */
  public void removeThreadLocalListener() {
    // No synchronisation necessary as all thread-local
    if (_listener.get() == null) {
      throw new IllegalStateException("No listener to remove for the calling thread");
    }
    _listener.remove();
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void log(LogEvent event) {
    LogEventListener listener = _listener.get();
    if (listener == null) {
      return;
    }
    listener.log(event);
  }

}
