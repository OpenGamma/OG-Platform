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
public class TemporaryTopicDestination extends AbstractDestination {

  /**
   * @param name
   */
  public TemporaryTopicDestination() {
    super("");
  }

  @Override
  public Destination constructDestination(Session jmsSession) {
    try {
      return jmsSession.createTemporaryTopic();
    } catch (JMSException e) {
      throw new JMSRuntimeException("Creating temporary topic", e);
    }
  }

}
