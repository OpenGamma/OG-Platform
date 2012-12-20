/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.monitor;

import org.slf4j.Logger;

/**
 * Reporter handling timing events.
 * <p>
 * The {@link OperationTimer} class calls the class whenever a timing session is complete.
 */
public interface OperationTimeReporter {

  /**
   * Handles a timer reporting event.
   * 
   * @param durationMillis  the duration in milliseconds
   * @param logger  the logger for reporting, not null
   * @param format  the format for reporting, not null
   * @param arguments  the arguments for reporting, not null
   */
  void report(long durationMillis, Logger logger, String format, Object[] arguments);

}
