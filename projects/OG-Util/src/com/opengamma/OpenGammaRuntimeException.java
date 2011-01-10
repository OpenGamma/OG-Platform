/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma;

/**
 * Runtime exception used by OpenGamma outside the standard defined runtime exceptions.
 */
public class OpenGammaRuntimeException extends RuntimeException {

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
