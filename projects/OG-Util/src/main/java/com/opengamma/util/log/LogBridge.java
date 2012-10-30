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
  
  private final Set<LogBridgeListener> _listeners = new CopyOnWriteArraySet<LogBridgeListener>();

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
  public void addListener(LogBridgeListener listener) {
    ArgumentChecker.notNull(listener, "listener");
    _listeners.add(listener);
  }
  
  /**
   * Removes a listener.
   * 
   * @param listener  the listener to remove, not null
   */
  public void removeListener(LogBridgeListener listener) {
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
   * Passes a log message to all registered listeners.
   * 
   * @param level  the logging level, not null
   * @param message  the log message, not null
   */
  public void log(LogLevel level, String message) {
    for (LogBridgeListener listener : getListeners()) {
      listener.logReceived(level, message);
    }
  }
  
  //-------------------------------------------------------------------------
  private Set<LogBridgeListener> getListeners() {
    return _listeners;
  }
  
}
