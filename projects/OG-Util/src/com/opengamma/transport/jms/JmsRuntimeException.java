/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.JMSException;

/**
 * A runtime exception wrapping the checked {@code JMSException}.
 */
public class JmsRuntimeException extends RuntimeException {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Creates an instance based on a {@code JMSException}.
   * 
   * @param cause  the cause, may be null
   */
  public JmsRuntimeException(JMSException cause) {
    super(cause);
  }

  /**
   * Creates an instance based on a {@code JMSException}.
   * 
   * @param message  the textual message to use, may be null
   * @param cause  the cause, may be null
   */
  public JmsRuntimeException(String message, JMSException cause) {
    super(message, cause);
  }

}
