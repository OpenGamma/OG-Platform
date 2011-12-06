/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

/**
 * An exception used to indicate a problem with a time-series.
 */
public class TimeSeriesException extends RuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates a new instance.
   */
  public TimeSeriesException() {
    super();
  }

  /**
   * Creates a new instance.
   * 
   * @param message  the message, may be null
   */
  public TimeSeriesException(String message) {
    super(message);
  }

  /**
   * Creates a new instance.
   * 
   * @param cause  the underlying cause, may be null
   */
  public TimeSeriesException(Throwable cause) {
    super(cause);
  }

  /**
   * Creates a new instance.
   * 
   * @param message  the message, may be null
   * @param cause  the underlying cause, may be null
   */
  public TimeSeriesException(String message, Throwable cause) {
    super(message, cause);
  }

}
