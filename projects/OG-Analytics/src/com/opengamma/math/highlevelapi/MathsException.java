/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.highlevelapi;

/**
 * Provides a manner in which maths exceptions can be thrown.
 */
public class MathsException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MathsException() {
    super();
  }

  public MathsException(final String s) {
    super(s);
  }

  public MathsException(final String s, final Throwable cause) {
    super(s, cause);
  }

  public MathsException(final Throwable cause) {
    super(cause);
  }
}
