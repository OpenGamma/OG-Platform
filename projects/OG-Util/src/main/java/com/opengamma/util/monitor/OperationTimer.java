/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.monitor;

import org.slf4j.Logger;

import com.opengamma.util.ArgumentChecker;

/**
 * Timer that can be wrapped around a time-critical operation to monitor its performance.
 * <p>
 * Timing output is sent to an {@code OperationTimeReporter}.
 */
public class OperationTimer {

  /**
   * The underlying reporter to use.
   */
  private static final OperationTimeReporter REPORTER = new LoggingOperationTimeReporter();

  /**
   * The start instant in nanoseconds.
   */
  private final long _startTime;
  /**
   * The logger for reporting.
   */
  private final Logger _logger;
  /**
   * The reporting format.
   */
  private final String _format;
  /**
   * The reporting arguments.
   */
  private final Object[] _arguments;

  /**
   * Gets the underlying reporter.
   * 
   * @return the reporter, not null
   */
  public static OperationTimeReporter getReporter() {
    return REPORTER;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a new instance and starts the timer.
   * 
   * @param logger  the logger for reporting, not null
   * @param format  the format for reporting, not null
   * @param arguments  the arguments for reporting, not null
   */
  public OperationTimer(Logger logger, String format, Object... arguments) {
    ArgumentChecker.notNull(logger, "logger");
    ArgumentChecker.notNull(format, "format");
    _startTime = System.nanoTime();
    _logger = logger;
    _format = format;
    _arguments = arguments;
  }

  /**
   * Stops the timer and write the report.
   * 
   * @return the time in milliseconds
   */
  public long finished() {
    long stopTime = System.nanoTime();
    long duration = stopTime - _startTime;
    long durationInMilliseconds = duration / 1000000;
    getReporter().report(durationInMilliseconds, _logger, _format, _arguments);
    return durationInMilliseconds;
  }

}
