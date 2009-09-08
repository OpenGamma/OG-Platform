package com.opengamma.math;

/**
 * 
 * @author emcleod
 * 
 */
public class MathException extends Exception {
  private static final long serialVersionUID = -3826040789998627634L;

  public MathException() {
    super();
  }

  public MathException(String s) {
    super(s);
  }

  public MathException(String s, Throwable cause) {
    super(s, cause);
  }

  public MathException(Throwable cause) {
    super(cause);
  }
}
