/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.net.URI;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;

/**
 * Ensures that the Ivy configuration is sufficient to launch an in-memory ActiveMQ connection.
 */
public class ActiveMQTestUtils {

  /**
   * The broker URI.
   */
  private static final URI BROKER_URI = URI.create("vm://localhost?broker.persistent=false");

  /**
   * Restricted constructor.
   */
  protected ActiveMQTestUtils() {
    super();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an MQ connection factory for testing.
   * 
   * @return the connection factory, not null
   */
  public static ActiveMQConnectionFactory createTestConnectionFactory() {
    return new ActiveMQConnectionFactory(BROKER_URI);
  }

  /**
   * Creates a JMS connector for testing.
   * 
   * @return the JMS connector, not null
   */
  public static JmsConnector createTestJmsConnector() {
    return createTestJmsConnector(null);
  }

  /**
   * Creates a JMS connector for testing.
   * 
   * @param topicName  the topic name, null if no topic name required
   * @return the JMS connector, not null
   */
  public static JmsConnector createTestJmsConnector(String topicName) {
    ActiveMQConnectionFactory cf = createTestConnectionFactory();
    JmsConnectorFactoryBean factory = new JmsConnectorFactoryBean();
    factory.setName("ActiveMQTestUtils");
    factory.setConnectionFactory(new PooledConnectionFactory(cf));
    factory.setClientBrokerUri(BROKER_URI);
    factory.setTopicName(topicName);
    return factory.getObjectCreating();
  }

}
