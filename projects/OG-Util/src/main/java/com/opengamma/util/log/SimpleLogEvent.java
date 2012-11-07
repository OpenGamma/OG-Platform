/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.log;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.util.ArgumentChecker;

/**
 * A simple, immutable implementation of {@link LogEvent} which holds the fields directly.
 */
public class SimpleLogEvent implements LogEvent {

  private final LogLevel _level;
  private final String _message;
  
  /**
   * Constructs an instance.
   * 
   * @param level  the log level, not null
   * @param message  the log message
   */
  public SimpleLogEvent(LogLevel level, String message) {
    ArgumentChecker.notNull(level, "level");
    _level = level;
    _message = message;
  }
  
  /**
   * Constructs an instance.
   * 
   * @param level  the log level, not null
   * @param message  the log message
   * @return the instance, not null
   */
  public static LogEvent of(LogLevel level, String message) {
    return new SimpleLogEvent(level, message);
  }
  
  @Override
  public LogLevel getLevel() {
    return _level;
  }

  @Override
  public String getMessage() {
    return _message;
  }

  //-------------------------------------------------------------------------
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_level == null) ? 0 : _level.hashCode());
    result = prime * result + ((_message == null) ? 0 : _message.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof SimpleLogEvent)) {
      return false;
    }
    SimpleLogEvent other = (SimpleLogEvent) obj;
    return ObjectUtils.equals(_level, other._level)
        && ObjectUtils.equals(_message, other._message);
  }
  
}
