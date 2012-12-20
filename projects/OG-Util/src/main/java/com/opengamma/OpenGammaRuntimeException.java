/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma;

/**
 * Runtime exception used by OpenGamma outside the standard defined runtime exceptions.
 */
public class OpenGammaRuntimeException extends RuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an exception with a message.
   * 
   * @param message  the message, may be null
   */
  public OpenGammaRuntimeException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a message.
   * 
   * @param message  the message, may be null
   * @param cause  the underlying cause, may be null
   */
  public OpenGammaRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

}
