/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math;

/**
 * 
 */
public class MathException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MathException() {
    super();
  }

  public MathException(final String s) {
    super(s);
  }

  public MathException(final String s, final Throwable cause) {
    super(s, cause);
  }

  public MathException(final Throwable cause) {
    super(cause);
  }
}
