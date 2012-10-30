/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial;

/**
 * 
 */
public class ExpiredException extends RuntimeException {

  private static final long serialVersionUID = 3262904185820120177L;

  public ExpiredException() {
    super();
  }

  public ExpiredException(final String s) {
    super(s);
  }

  public ExpiredException(final String s, final Throwable cause) {
    super(s, cause);
  }

  public ExpiredException(final Throwable cause) {
    super(cause);
  }
}
