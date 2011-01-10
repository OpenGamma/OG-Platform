/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import org.springframework.jms.core.JmsTemplate;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public abstract class AbstractJmsByteArraySender {
  private final String _destinationName;
  private final JmsTemplate _jmsTemplate;
  
  public AbstractJmsByteArraySender(String destinationName, JmsTemplate jmsTemplate) {
    ArgumentChecker.notNull(destinationName, "destinationName");
    ArgumentChecker.notNull(jmsTemplate, "jmsTemplate");
    _destinationName = destinationName;
    _jmsTemplate = jmsTemplate;
  }

  /**
   * @return the destinationName
   */
  public String getDestinationName() {
    return _destinationName;
  }

  /**
   * @return the jmsTemplate
   */
  public JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }


}
