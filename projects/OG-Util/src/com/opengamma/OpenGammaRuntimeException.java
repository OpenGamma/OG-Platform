/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma;

/**
 * The base class for all runtime exceptions thrown by any OpenGamma code,
 * outside the base JRE-defined runtime exceptions.
 */
public class OpenGammaRuntimeException extends RuntimeException {

  /**
   * Creates an exception with a message.
   * @param message  the message, may be null
   */
  public OpenGammaRuntimeException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a message.
   * @param message  the message, may be null
   * @param cause  the underlying cause, may be null
   */
  public OpenGammaRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

}
