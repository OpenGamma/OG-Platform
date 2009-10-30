/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
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
public class JMSRuntimeException extends RuntimeException {
  
  public JMSRuntimeException(JMSException cause) {
    super(cause);
  }
  
  public JMSRuntimeException(String message, JMSException cause) {
    super(message, cause);
  }

}
