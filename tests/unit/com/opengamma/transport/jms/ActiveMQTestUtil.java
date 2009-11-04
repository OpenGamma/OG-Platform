/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * This test is only here to ensure that the Ivy configuration is sufficient
 * to launch an in-memory ActiveMQ connection.
 *
 * @author kirk
 */
public class ActiveMQTestUtil {
  
  public static ConnectionFactory createTestConnectionFactory() {
    ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
    return cf;
  }
}
