/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TemporaryTopic;

/**
 * 
 *
 * @author kirk
 */
public class TemporaryTopicDestination extends AbstractDestination {
  private String _temporaryTopicName = null;

  /**
   * @param name
   */
  public TemporaryTopicDestination() {
    super("");
  }

  
  /**
   * @return the temporaryTopicName
   */
  public String getTemporaryTopicName() {
    return _temporaryTopicName;
  }


  /**
   * @param temporaryTopicName the temporaryTopicName to set
   */
  public void setTemporaryTopicName(String temporaryTopicName) {
    _temporaryTopicName = temporaryTopicName;
  }


  @Override
  public String getName() {
    if(getTemporaryTopicName() == null) {
      return super.getName();
    }
    return getTemporaryTopicName();
  }


  @Override
  public Destination constructDestination(Session jmsSession) {
    try {
      TemporaryTopic tempTopic = jmsSession.createTemporaryTopic();
      setTemporaryTopicName(tempTopic.getTopicName());
      return tempTopic;
    } catch (JMSException e) {
      throw new JmsRuntimeException("Creating temporary topic", e);
    }
  }

}
