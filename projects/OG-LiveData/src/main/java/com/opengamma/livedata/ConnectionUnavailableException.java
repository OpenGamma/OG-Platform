/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Indicates that no connection is available to the underlying data provider.
 */
public class ConnectionUnavailableException extends OpenGammaRuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = -1122607839250772942L;

  /**
   * @param message The error message
   */
  public ConnectionUnavailableException(String message) {
    super(message);
  }

  /**
   * @param message The error message
   * @param cause The underlying cause
   */
  public ConnectionUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }

}
