/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.maths.commonapi.exceptions;

/**
 * Provides a manner in which maths exceptions relating to null pointer access can be thrown
 */
public class MathsExceptionNullPointer extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public MathsExceptionNullPointer() {
    super();
  }

  public MathsExceptionNullPointer(final String s) {
    super(s + " points to null.");
  }

  public MathsExceptionNullPointer(final String s, final Throwable cause) {
    super(s + " points to null.", cause);
  }

  public MathsExceptionNullPointer(final Throwable cause) {
    super(cause);
  }
}
