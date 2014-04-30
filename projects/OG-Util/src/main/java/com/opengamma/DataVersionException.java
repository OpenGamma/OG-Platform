/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma;

/**
 * Runtime exception used to indicate that the action is being attempted on
 * an old version of the entity.
 * <p>
 * A typical use case is when updating data and the object to be updated
 * has an old version. This avoids two users overwriting changes.
 */
public class DataVersionException extends OpenGammaRuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an exception with a message.
   * 
   * @param message  the message, may be null
   */
  public DataVersionException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a message.
   * 
   * @param message  the message, may be null
   * @param cause  the underlying cause, may be null
   */
  public DataVersionException(String message, Throwable cause) {
    super(message, cause);
  }

}
