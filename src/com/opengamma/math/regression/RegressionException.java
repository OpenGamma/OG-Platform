/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.regression;

public class RegressionException extends RuntimeException {
  public RegressionException() {
    super();
  }

  public RegressionException(final String s) {
    super(s);
  }

  public RegressionException(final String s, final Throwable cause) {
    super(s, cause);
  }

  public RegressionException(final Throwable cause) {
    super(cause);
  }
}
