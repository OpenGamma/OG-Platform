/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma;

/**
 * Runtime exception used when loading data, and the data is not found.
 * <p>
 * A typical use case is when loading data by unique identifier, and the identifier is not found.
 */
public class DataNotFoundException extends OpenGammaRuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an exception with a message.
   * 
   * @param message  the message, may be null
   */
  public DataNotFoundException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a message.
   * 
   * @param message  the message, may be null
   * @param cause  the underlying cause, may be null
   */
  public DataNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

}
