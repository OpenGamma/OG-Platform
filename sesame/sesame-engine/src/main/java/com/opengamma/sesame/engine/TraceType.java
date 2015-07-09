/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.engine;

/**
 * The type of information to be captured by a trace.
 */
public enum TraceType {
  /**
   * Do not capture any trace information.
   */
  NONE,
  /**
   * Capture method timings for all calls.
   */
  TIMINGS_ONLY,
  /**
   * Capture timings, arguments and return values but
   * convert to strings before returning. This means
   * trace information can be serialized and sent to
   * remote processes.
   */
  FULL_AS_STRING,
  /**
   * Capture timings, arguments and return values. This means
   * trace information cannot necessarily be serialized and
   * sent to remote processes.
   */
  FULL
}
