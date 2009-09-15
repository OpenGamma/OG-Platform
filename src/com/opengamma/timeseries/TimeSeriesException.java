package com.opengamma.timeseries;

/**
 * 
 * @author emcleod
 * 
 */

public class TimeSeriesException extends Exception {

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
