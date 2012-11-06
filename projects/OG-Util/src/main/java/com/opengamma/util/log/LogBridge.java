/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.log;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.opengamma.util.ArgumentChecker;

/**
 * A bridge designed to receive log messages from a compatible appender and forward them to any registered listeners
 * through a standard interface. This allows application code to receive log messages regardless of the logging
 * framework in use, and without a dependency on any such logging framework; a standard appender would be
 * framework-specific.
 */
public final class LogBridge {

  private static final LogBridge s_instance = new LogBridge();
  
  private final Set<LogEventListener> _listeners = new CopyOnWriteArraySet<LogEventListener>();

  //-------------------------------------------------------------------------
  /**
   * Hidden constructor.
   */
  private LogBridge() {
  }
  
  /**
   * Gets the bridge instance.
   * 
   * @return the bridge instance, not null
   */
  public static LogBridge getInstance() {
    return s_instance;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Adds a listener.
   * 
   * @param listener  the listener to add, not null
   */
  public void addListener(LogEventListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }
  
  /**
   * Removes a listener.
   * 
   * @param listener  the listener to remove, not null
   */
  public void removeListener(LogEventListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.remove(listener);
  }
  
  /**
   * Indicates whether the bridge has any listeners.
   * 
   * @return true if the bridge has any listeners, false otherwise.
   */
  public boolean hasListeners() {
    return !_listeners.isEmpty();
  }
  
  //-------------------------------------------------------------------------
  /**
   * Passes a log event to all registered listeners.
   * 
   * @param event  the logging event, not null
   */
  public void log(LogEvent event) {
    for (LogEventListener listener : getListeners()) {
      listener.log(event);
    }
  }
  
  //-------------------------------------------------------------------------
  private Set<LogEventListener> getListeners() {
    return _listeners;
  }
  
}
