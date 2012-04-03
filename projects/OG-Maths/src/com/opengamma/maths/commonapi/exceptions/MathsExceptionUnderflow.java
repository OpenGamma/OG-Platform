/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.commonapi.exceptions;

/**
 * Provides a manner in which maths exceptions can be thrown.
 */
public class MathsExceptionUnderflow extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MathsExceptionUnderflow() {
    super();
  }

  public MathsExceptionUnderflow(final String s) {
    super(s);
  }

  public MathsExceptionUnderflow(final String s, final Throwable cause) {
    super(s, cause);
  }

  public MathsExceptionUnderflow(final Throwable cause) {
    super(cause);
  }
}
