/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

/**
 * 
 */
public class IntegrationException extends RuntimeException {
  public IntegrationException() {
    super();
  }

  public IntegrationException(final String s) {
    super(s);
  }

  public IntegrationException(final String s, final Throwable cause) {
    super(s, cause);
  }

  public IntegrationException(final Throwable cause) {
    super(cause);
  }
}
