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
 * 
 */
public abstract class AbstractSpringContainerFactory {
  /**
   * The JMS connection factory.
   */
  private final ConnectionFactory _connectionFactory;
  
  protected AbstractSpringContainerFactory(ConnectionFactory connectionFactory) {
    ArgumentChecker.notNull(connectionFactory, "connectionFactory");
    _connectionFactory = connectionFactory;
  }

  /**
   * Gets the connection factory.
   * 
   * @return the connection factory, not null
   */
  public ConnectionFactory getConnectionFactory() {
    return _connectionFactory;
  }

  /**
   * Creates a container to receive JMS messages.
   * 
   * @param connectionFactory  the JMS connection factory, not null
   * @param destinationName  the queue or topic name, not null
   * @param isPubSub true if this is for a topic
   * @param listener  the listener, not null
   * @return the container, not null
   */
  protected DefaultMessageListenerContainer doCreate(ConnectionFactory connectionFactory, String destinationName, boolean isPubSub, Object listener) {
    DefaultMessageListenerContainer jmsContainer = new DefaultMessageListenerContainer();
    jmsContainer.setConnectionFactory(connectionFactory);
    jmsContainer.setDestinationName(destinationName);
    jmsContainer.setPubSubDomain(isPubSub);
    jmsContainer.setMessageListener(listener);
    return jmsContainer;
  }

  /**
   * Container used to receive JMS messages.
   */
  static class OpenGammaSpringJmsContainer implements JmsTopicContainer, JmsQueueContainer {

    /**
     * The JMS container.
     */
    private final AbstractMessageListenerContainer _jmsContainer;

    /**
     * Creates an instance.
     * 
     * @param jmsContainer  the container, not null
     */
    OpenGammaSpringJmsContainer(AbstractMessageListenerContainer jmsContainer) {
      ArgumentChecker.notNull(jmsContainer, "jmsContainer");
      _jmsContainer = jmsContainer;
      _jmsContainer.afterPropertiesSet();
    }

    @Override
    public void start() {
      _jmsContainer.start();
    }

    @Override
    public void stop() {
      _jmsContainer.stop();
    }

    @Override
    public boolean isRunning() {
      return _jmsContainer.isRunning();
    }
  }
}
