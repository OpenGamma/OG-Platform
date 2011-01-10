/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.JMSException;

/**
 * 
 *
 * @author kirk
 */
public class JmsRuntimeException extends RuntimeException {
  
  public JmsRuntimeException(JMSException cause) {
    super(cause);
  }
  
  public JmsRuntimeException(String message, JMSException cause) {
    super(message, cause);
  }

}
