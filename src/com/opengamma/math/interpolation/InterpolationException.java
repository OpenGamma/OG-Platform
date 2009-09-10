package com.opengamma.math.interpolation;

/**
 * 
 * @author emcleod
 * 
 */

public class InterpolationException extends Exception {

  public InterpolationException() {
    super();
  }

  public InterpolationException(String s) {
    super(s);
  }

  public InterpolationException(String s, Throwable cause) {
    super(s, cause);
  }

  public InterpolationException(Throwable cause) {
    super(cause);
  }
}
