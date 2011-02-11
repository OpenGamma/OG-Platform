/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * Ensures that the Ivy configuration is sufficient to launch an in-memory ActiveMQ connection.
 */
public class ActiveMQTestUtil {

  /**
   * Creates an MQ connection factory for testing.
   * 
   * @return the connection factory
   */
  public static ActiveMQConnectionFactory createTestConnectionFactory() {
    ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
    return cf;
  }

}
