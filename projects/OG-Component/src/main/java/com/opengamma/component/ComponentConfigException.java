/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Runtime exception used to indicate a problem in the component-based configuration.
 */
public class ComponentConfigException extends OpenGammaRuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an exception with a message.
   * 
   * @param message  the message, may be null
   */
  public ComponentConfigException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a message.
   * 
   * @param message  the message, may be null
   * @param cause  the underlying cause, may be null
   */
  public ComponentConfigException(String message, Throwable cause) {
    super(message, cause);
  }

}
