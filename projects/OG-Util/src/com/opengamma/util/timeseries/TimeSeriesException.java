/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.timeseries;

/**
 * 
 * @author emcleod
 * 
 */

public class TimeSeriesException extends RuntimeException {

  public TimeSeriesException() {
    super();
  }

  public TimeSeriesException(String s) {
    super(s);
  }

  public TimeSeriesException(String s, Throwable cause) {
    super(s, cause);
  }

  public TimeSeriesException(Throwable cause) {
    super(cause);
  }
}
