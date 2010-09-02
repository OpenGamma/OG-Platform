/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma;

/**
 * Exception used when loading data, typically by unique identifier, and the data is not found.
 */
public class DataNotFoundException extends OpenGammaRuntimeException {

  /**
   * Creates an exception with a message.
   * @param message  the message, may be null
   */
  public DataNotFoundException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a message.
   * @param message  the message, may be null
   * @param cause  the underlying cause, may be null
   */
  public DataNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
