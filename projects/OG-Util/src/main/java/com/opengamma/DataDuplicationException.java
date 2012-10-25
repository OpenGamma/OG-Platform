/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma;

/**
 * Runtime exception used to indicate that the action would create a duplicate.
 * <p>
 * A typical use case is when adding data and a similar item already exists.
 */
public class DataDuplicationException extends OpenGammaRuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an exception with a message.
   * 
   * @param message  the message, may be null
   */
  public DataDuplicationException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a message.
   * 
   * @param message  the message, may be null
   * @param cause  the underlying cause, may be null
   */
  public DataDuplicationException(String message, Throwable cause) {
    super(message, cause);
  }

}
