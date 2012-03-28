/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.maths.commonapi.exceptions;

/**
 * Provides a manner in which maths exceptions can be thrown.
 */
public class MathsExceptionGeneric extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MathsExceptionGeneric() {
    super();
  }

  public MathsExceptionGeneric(final String s) {
    super(s);
  }

  public MathsExceptionGeneric(final String s, final Throwable cause) {
    super(s, cause);
  }

  public MathsExceptionGeneric(final Throwable cause) {
    super(cause);
  }
}
