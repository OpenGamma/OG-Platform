/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * 
 *
 * @author kirk
 */
public class QueueDestination extends AbstractDestination {

  /**
   * @param name
   */
  public QueueDestination(String name) {
    super(name);
  }

  @Override
  public Destination constructDestination(Session jmsSession) {
    try {
      return jmsSession.createQueue(getName());
    } catch (JMSException e) {
      throw new JmsRuntimeException("Creating queue " + getName(), e);
    }
  }

}
