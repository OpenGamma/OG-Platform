package com.opengamma.math;

/**
 * 
 * @author emcleod
 * 
 */

public class ConvergenceException extends RuntimeException {

  public ConvergenceException() {
    super();
  }

  public ConvergenceException(String s) {
    super(s);
  }

  public ConvergenceException(String s, Throwable cause) {
    super(s, cause);
  }

  public ConvergenceException(Throwable cause) {
    super(cause);
  }
}
