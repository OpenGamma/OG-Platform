/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.jms;

import javax.jms.ConnectionFactory;

import org.springframework.jms.listener.AbstractMessageListenerContainer;

/**
 * Container used to receive JMS messages.
 */
public class SpringJmsTopicContainerFactory extends AbstractSpringContainerFactory implements JmsTopicContainerFactory {

  /**
   * Creates an instance.
   * 
   * @param connectionFactory  the JMS connection factory, not null
   */
  public SpringJmsTopicContainerFactory(ConnectionFactory connectionFactory) {
    super(connectionFactory);
  }

  //-------------------------------------------------------------------------
  @Override
  public JmsTopicContainer create(String topicName, Object listener) {
    AbstractMessageListenerContainer jmsContainer = doCreate(getConnectionFactory(), topicName, true, listener);
    return new OpenGammaSpringJmsContainer(jmsContainer);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return SpringJmsTopicContainerFactory.class.getSimpleName();
  }

}
