/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.jms;

import javax.jms.ConnectionFactory;

import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * Container used to receive JMS messages.
 */
public class SpringJmsQueueContainerFactory extends AbstractSpringContainerFactory implements JmsQueueContainerFactory {
  
  /**
   * Creates an instance.
   * 
   * @param connectionFactory  the JMS connection factory, not null
   */
  public SpringJmsQueueContainerFactory(ConnectionFactory connectionFactory) {
    super(connectionFactory);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public JmsQueueContainer create(String queueName, Object listener) {
    AbstractMessageListenerContainer jmsContainer = doCreate(getConnectionFactory(), queueName, false, listener);
    return new OpenGammaSpringJmsContainer(jmsContainer);
  }

  @Override
  public JmsQueueContainer create(String queueName, Object listener, int concurrentConsumers, int maxConcurrentConsumers) {

    ArgumentChecker.notNegativeOrZero(concurrentConsumers, "concurrentConsumers");
    ArgumentChecker.notNegativeOrZero(maxConcurrentConsumers, "maxConcurrentConsumers");

    DefaultMessageListenerContainer jmsContainer = doCreate(getConnectionFactory(), queueName, false, listener);
    jmsContainer.setConcurrentConsumers(concurrentConsumers);
    jmsContainer.setMaxConcurrentConsumers(maxConcurrentConsumers);
    return new OpenGammaSpringJmsContainer(jmsContainer);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return SpringJmsQueueContainerFactory.class.getSimpleName();
  }

}
