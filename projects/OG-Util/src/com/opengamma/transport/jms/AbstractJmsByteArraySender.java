/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import org.springframework.jms.core.JmsTemplate;

import com.opengamma.util.ArgumentChecker;

/**
 * Base class used to implement JMS senders.
 */
public abstract class AbstractJmsByteArraySender {

  /**
   * The JMS destination name.
   */
  private final String _destinationName;
  /**
   * The JMS template.
   */
  private final JmsTemplate _jmsTemplate;

  /**
   * Creates an instance associated with a destination and template.
   * 
   * @param destinationName  the destination name, not null
   * @param jmsTemplate  the template, not null
   */
  public AbstractJmsByteArraySender(final String destinationName, final JmsTemplate jmsTemplate) {
    ArgumentChecker.notNull(destinationName, "destinationName");
    ArgumentChecker.notNull(jmsTemplate, "jmsTemplate");
    _destinationName = destinationName;
    _jmsTemplate = jmsTemplate;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the JMS destination.
   * 
   * @return the destination name, not null
   */
  public String getDestinationName() {
    return _destinationName;
  }

  /**
   * Gets the JMS template.
   * 
   * @return the template, not null
   */
  public JmsTemplate getJmsTemplate() {
    return _jmsTemplate;
  }

}
