/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.log;

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
  
  @Override
  public LogLevel getLevel() {
    return _level;
  }

  @Override
  public String getMessage() {
    return _message;
  }

}
