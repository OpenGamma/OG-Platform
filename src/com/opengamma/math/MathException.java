/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math;

/**
 * 
 * @author emcleod
 */
public class MathException extends RuntimeException {

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
