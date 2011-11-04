/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;

/**
 * Ensures that the Ivy configuration is sufficient to launch an in-memory ActiveMQ connection.
 */
public class ActiveMQTestUtils {

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
    ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
    return cf;
  }

  /**
   * Creates a JMS connector for testing.
   * 
   * @return the JMS connector, not null
   */
  public static JmsConnector createJmsConnector() {
    return createJmsConnector(null);
  }

  /**
   * Creates a JMS connector for testing.
   * 
   * @param topicName  the topic name, null if no topic name required
   * @return the JMS connector, not null
   */
  public static JmsConnector createJmsConnector(String topicName) {
    ConnectionFactory cf = createTestConnectionFactory();
    JmsConnectorFactoryBean factory = new JmsConnectorFactoryBean();
    factory.setName("ActiveMQTestUtils");
    factory.setConnectionFactory(cf);
    factory.setTopicName(topicName);
    return factory.createObject();
  }

}
