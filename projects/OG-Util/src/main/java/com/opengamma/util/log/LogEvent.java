/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.log;

/**
 * Represents a log event.
 * <p>
 * Implementations should query any underlying event lazily for a minimal performance impact in the case that no
 * listener is interested.
 */
public interface LogEvent {

  /**
   * Gets the log level.
   * 
   * @return  the log level, not null
   */
  LogLevel getLevel();
  
  /**
   * Gets the log message.
   * 
   * @return  the log message.
   */
  String getMessage();
  
}
