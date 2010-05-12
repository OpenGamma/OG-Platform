/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.monitor;

import org.slf4j.Logger;

import com.opengamma.util.ArgumentChecker;

/**
 * Wraps a time-critical operation, allowing for timing to be maintained for development
 * and monitoring purposes.
 * {@code OperationTimer} requires a timer back-end to actually report on statistics.
 *
 * @author kirk
 */
public class OperationTimer {
  private final long _startTime;
  private final Logger _logger;
  private final String _format;
  private final Object[] _arguments;
  
  public OperationTimer(Logger logger, String format, Object... arguments) {
    ArgumentChecker.notNull(logger, "Logger");
    ArgumentChecker.notNull(format, "Reporting format");
    
    _startTime = System.nanoTime();
    _logger = logger;
    _format = format;
    _arguments = arguments;
  }
  
  public long finished() {
    long stopTime = System.nanoTime();
    long duration = stopTime - _startTime;
    long durationInMilliseconds = duration / 1000;
    getReporter().report(durationInMilliseconds, _logger, _format, _arguments);
    return durationInMilliseconds;
  }
  
  private static final OperationTimeReporter REPORTER = new LoggingOperationTimeReporter();
  public static OperationTimeReporter getReporter() {
    // TODO kirk 2010-04-13 -- Make a proper factory pattern.
    return REPORTER;
  }

}
