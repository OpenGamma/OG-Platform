/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.jms;

import javax.jms.ConnectionFactory;

import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Container used to receive JMS messages.
 */
public class SpringJmsTopicContainerFactory implements JmsTopicContainerFactory {

  /**
   * The JMS connection factory.
   */
  private final ConnectionFactory _connectionFactory;

  /**
   * Creates an instance.
   * 
   * @param connectionFactory  the JMS connection factory, not null
   */
  public SpringJmsTopicContainerFactory(ConnectionFactory connectionFactory) {
    ArgumentChecker.notNull(connectionFactory, "connectionFactory");
    _connectionFactory = connectionFactory;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the connection factory.
   * 
   * @return the connection factory, not null
   */
  public ConnectionFactory getConnectionFactory() {
    return _connectionFactory;
  }

  //-------------------------------------------------------------------------
  @Override
  public JmsTopicContainer create(String topicName, Object listener) {
    AbstractMessageListenerContainer jmsContainer = doCreate(_connectionFactory, topicName, listener);
    return new SpringJmsTopicContainer(jmsContainer);
  }

  /**
   * Creates a container to receive JMS messages.
   * 
   * @param connectionFactory  the JMS connection factory, not null
   * @param topicName  the topic name, not null
   * @param listener  the listener, not null
   * @return the container, not null
   */
  protected AbstractMessageListenerContainer doCreate(ConnectionFactory connectionFactory, String topicName, Object listener) {
    DefaultMessageListenerContainer jmsContainer = new DefaultMessageListenerContainer();
    jmsContainer.setConnectionFactory(connectionFactory);
    jmsContainer.setDestinationName(topicName);
    jmsContainer.setPubSubDomain(true);
    jmsContainer.setMessageListener(listener);
    return jmsContainer;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return SpringJmsTopicContainerFactory.class.getSimpleName();
  }

  //-------------------------------------------------------------------------
  /**
   * Container used to receive JMS messages.
   */
  class SpringJmsTopicContainer implements JmsTopicContainer {

    /**
     * The JMS container.
     */
    private final AbstractMessageListenerContainer _jmsContainer;

    /**
     * Creates an instance.
     * 
     * @param jmsContainer  the container, not null
     */
    SpringJmsTopicContainer(AbstractMessageListenerContainer jmsContainer) {
      ArgumentChecker.notNull(jmsContainer, "jmsContainer");
      if (jmsContainer.isPubSubDomain() == false) {
        throw new OpenGammaRuntimeException("Underlying Spring container must be topic based");
      }
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
